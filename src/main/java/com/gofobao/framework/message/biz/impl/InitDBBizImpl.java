package com.gofobao.framework.message.biz.impl;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.api.contants.ChannelContant;
import com.gofobao.framework.api.contants.DesLineFlagContant;
import com.gofobao.framework.api.contants.JixinResultContants;
import com.gofobao.framework.api.helper.JixinManager;
import com.gofobao.framework.api.helper.JixinTxCodeEnum;
import com.gofobao.framework.api.model.balance_query.BalanceQueryRequest;
import com.gofobao.framework.api.model.balance_query.BalanceQueryResponse;
import com.gofobao.framework.api.model.voucher_pay.VoucherPayRequest;
import com.gofobao.framework.api.model.voucher_pay.VoucherPayResponse;
import com.gofobao.framework.asset.entity.Asset;
import com.gofobao.framework.asset.entity.NewAssetLog;
import com.gofobao.framework.asset.service.AssetService;
import com.gofobao.framework.asset.service.NewAssetLogService;
import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.service.BorrowService;
import com.gofobao.framework.collection.entity.BorrowCollection;
import com.gofobao.framework.collection.service.BorrowCollectionService;
import com.gofobao.framework.common.assets.AssetChange;
import com.gofobao.framework.common.assets.AssetChangeProvider;
import com.gofobao.framework.common.assets.AssetChangeTypeEnum;
import com.gofobao.framework.helper.BeanHelper;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.NumberHelper;
import com.gofobao.framework.helper.StringHelper;
import com.gofobao.framework.helper.project.BorrowCalculatorHelper;
import com.gofobao.framework.member.entity.UserThirdAccount;
import com.gofobao.framework.member.service.UserThirdAccountService;
import com.gofobao.framework.message.biz.InitDBBiz;
import com.gofobao.framework.migrate.FileHelper;
import com.gofobao.framework.repayment.entity.BorrowRepayment;
import com.gofobao.framework.repayment.service.BorrowRepaymentService;
import com.gofobao.framework.tender.entity.Tender;
import com.gofobao.framework.tender.entity.Transfer;
import com.gofobao.framework.tender.entity.TransferBuyLog;
import com.gofobao.framework.tender.service.TenderService;
import com.gofobao.framework.tender.service.TransferBuyLogService;
import com.gofobao.framework.tender.service.TransferService;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

@Service
@Slf4j
public class InitDBBizImpl implements InitDBBiz {

    @Autowired
    BorrowService borrowService;

    @Autowired
    BorrowRepaymentService borrowRepaymentService;

    @Autowired
    TenderService tenderService;

    @Autowired
    BorrowCollectionService borrowCollectionService;

    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private NewAssetLogService newAssetLogService;

    @Autowired
    TransferService transferService;

    @Autowired
    TransferBuyLogService transferBuyLogService;
    @Autowired
    AssetChangeProvider assetChangeProvider;
    @Autowired
    UserThirdAccountService userThirdAccountService;
    @Autowired
    AssetService assetService;
    @Autowired
    private JixinManager jixinManager;

    private static final String DB_PATH = "/root/apollo/";
    private static final String DBFILE = "db";

    @Autowired
    public void initUseAsset() throws Exception {
        String seqNo = assetChangeProvider.getSeqNo(); // 资产记录流水号
        String groupSeqNo = assetChangeProvider.getGroupSeqNo(); // 资产记录分组流水号
        //红包账户
        long redId = assetChangeProvider.getRedpackAccountId();
        UserThirdAccount redpackThirdAccount = userThirdAccountService.findByUserId(redId); //查询红包账户
        //1.查询已开户用户
        Specification<UserThirdAccount> usas = Specifications
                .<UserThirdAccount>and()
                .eq("del", 0)
                .notIn("accountId", String.valueOf("6212462190000000013"))
                .eq("userId", 45219)
                .build();
        int index = 0;
        int pageSize = 50;
        do {
            List<UserThirdAccount> userThirdAccountList = userThirdAccountService.findList(usas, new PageRequest(index, pageSize));
            Set<Long> userIds = userThirdAccountList.stream().map(UserThirdAccount::getUserId).collect(Collectors.toSet());
            //2.查询资产记录
            Specification<Asset> as = Specifications
                    .<Asset>and()
                    .in("userId", userIds.toArray())
                    .build();
            List<Asset> assetList = assetService.findList(as);
            Map<Long, Asset> assetMaps = assetList.stream().collect(Collectors.toMap(Asset::getUserId, Function.identity()));
            for (UserThirdAccount userThirdAccount : userThirdAccountList) {
                //3.查询存管账户是否为空
                BalanceQueryRequest balanceQueryRequest = new BalanceQueryRequest();
                balanceQueryRequest.setChannel(ChannelContant.HTML);
                balanceQueryRequest.setAccountId(userThirdAccount.getAccountId());
                BalanceQueryResponse balanceQueryResponse = jixinManager.send(JixinTxCodeEnum.BALANCE_QUERY, balanceQueryRequest, BalanceQueryResponse.class);
                if ((ObjectUtils.isEmpty(balanceQueryResponse)) || !balanceQueryResponse.getRetCode().equals(JixinResultContants.SUCCESS)) {
                    String msg = ObjectUtils.isEmpty(balanceQueryResponse) ? "当前网络异常, 请稍后尝试!" : balanceQueryResponse.getRetMsg();
                    log.error(String.format("资金同步: %s,userId:%s", msg, userThirdAccount.getUserId()));
                }
                //存管账户总金额
                long money = NumberHelper.toLong(NumberHelper.toDouble(balanceQueryResponse.getCurrBal()) * 100);
                Specification<NewAssetLog> nals = Specifications
                        .<NewAssetLog>and()
                        .eq("userId", userThirdAccount.getUserId())
                        .eq("localType", AssetChangeTypeEnum.initAsset.getLocalType())
                        .build();
                long count = newAssetLogService.count(nals);
                if (count == 0) {
                    Asset asset = assetMaps.get(userThirdAccount.getUserId());

                    AssetChange assetChange = new AssetChange();
                    assetChange.setMoney(0);
                    assetChange.setForUserId(redpackThirdAccount.getUserId());
                    assetChange.setUserId(userThirdAccount.getUserId());
                    assetChange.setRemark(String.format("数据迁移初始化资产账户，金额：%s分，userId：%s", asset.getUseMoney(), asset.getUserId()));
                    assetChange.setSeqNo(seqNo);
                    assetChange.setGroupSeqNo(groupSeqNo);
                    assetChange.setSourceId(asset.getUserId());
                    assetChange.setType(AssetChangeTypeEnum.initAsset);
                    try {
                        assetChangeProvider.commonAssetChange(assetChange);
                    } catch (Exception e) {
                        log.error(String.format("资金变动失败：%s", assetChange));
                        continue;
                    }
                    //3.发送红包
                    VoucherPayRequest voucherPayRequest = new VoucherPayRequest();
                    voucherPayRequest.setAccountId(redpackThirdAccount.getAccountId());
                    voucherPayRequest.setTxAmount(StringHelper.formatDouble(asset.getUseMoney(), 100, false));
                    voucherPayRequest.setForAccountId(userThirdAccount.getAccountId());
                    voucherPayRequest.setDesLineFlag(DesLineFlagContant.TURE);
                    voucherPayRequest.setDesLine("数据迁移账户初始化！");
                    voucherPayRequest.setChannel(ChannelContant.HTML);
                    VoucherPayResponse response = jixinManager.send(JixinTxCodeEnum.SEND_RED_PACKET, voucherPayRequest, VoucherPayResponse.class);
                    if ((ObjectUtils.isEmpty(response)) || (!JixinResultContants.SUCCESS.equals(response.getRetCode()))) {
                        String msg = ObjectUtils.isEmpty(response) ? "当前网络不稳定，请稍候重试" : response.getRetMsg();
                        throw new Exception(msg);
                    }
                }
            }
        } while (false);

    }


    @Override
    public void initDb(long startIndex) {
        int pageSize = 100, pageIndex = 0;
        Date nowDate = new Date();
        ImmutableList<Integer> avableStatus = ImmutableList.of(1, 3); // 保函招标中, 满标复审通过
        Specification<Borrow> borrowSpecification = Specifications.<Borrow>and()
                .in("status", avableStatus.toArray())
                .gt("id", startIndex)
                .build();

        Long count = borrowService.count(borrowSpecification);
        log.info("初始化标量: " + count);
        int pageIndexTotal = count.intValue() / pageSize;
        pageIndexTotal = count.intValue() % pageSize == 0 ? pageIndexTotal : pageIndexTotal + 1;
        log.info("总循环:" + pageIndexTotal);
        BufferedWriter bufferedWriter = null;
        try {
            File tenderFile = FileHelper.createFile(DB_PATH, DBFILE, DateHelper.dateToString(new Date()));
            bufferedWriter = Files.newWriter(tenderFile, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("创建文件失败", e);
            return;
        }
        for (; pageIndex < pageIndexTotal; pageIndex++) {
            Pageable pageable = new PageRequest(pageIndex, pageSize, new Sort(new Sort.Order(Sort.Direction.ASC, "id")));
            List<Borrow> borrowList = borrowService.findList(borrowSpecification, pageable);
            Set<Long> borrowIdSet = borrowList.stream().map(borrow -> borrow.getId()).collect(Collectors.toSet());  // 标的Id集合

            List<BorrowRepayment> borrowRepaymentListByBorrowId = findRepaymentListByBorrowId(borrowIdSet);   // 当前标的所有的还款
            Map<Long, List<BorrowRepayment>> borrowRepaymentAndBorrowIdRefMap =
                    borrowRepaymentListByBorrowId.stream().collect(Collectors.groupingBy(BorrowRepayment::getBorrowId));

            List<Tender> tenderListByBorrowId = findTenderByBorrowIds(borrowIdSet);  // 投标记录
            Map<Long, List<Tender>> tenderAndBorrowIdRefMap = tenderListByBorrowId.stream().collect(Collectors.groupingBy(Tender::getBorrowId));
            Set<Long> tenderIdSet = tenderListByBorrowId.stream().map(tender -> tender.getId()).collect(Collectors.toSet());  // 投标记录ID

            List<BorrowCollection> borrowCollectionListAll = findBorrowCollectionByTenderId(tenderIdSet);
            Map<Long, List<BorrowCollection>> borrowCollectionAndTenderIdRefMap = borrowCollectionListAll.stream().collect(Collectors.groupingBy(BorrowCollection::getTenderId));

            for (Borrow borrow : borrowList) {
                log.info("以迁移标的数量:" + (borrow.getId()));
                long borrowId = borrow.getId();
                int tenderState = caculTenderState(borrow);  // 计算当前标的状态
                Long borrowUserId = borrow.getUserId();
                List<BorrowRepayment> borrowRepaymentList = borrowRepaymentAndBorrowIdRefMap.get(borrowId);
                if (!CollectionUtils.isEmpty(borrowRepaymentList)) {
                    log.info("保顿待还" + (borrow.getId()));
                    for (BorrowRepayment borrowRepayment : borrowRepaymentList) {
                        borrowRepayment.setUserId(borrowUserId);
                        borrowRepayment.setUpdatedAt(nowDate);
                        String insert = "UPDATE `gfb_borrow_repayment`  SET `user_id` = " + borrowUserId + "  WHERE  `id` = " + borrowRepayment.getId() + " ;";
                        try {
                            bufferedWriter.write(insert);
                            bufferedWriter.newLine();
                            bufferedWriter.flush();
                        } catch (IOException e) {
                            log.error("添加待还事变", e);
                        }
                    }
                }

                List<Tender> tenderList = tenderAndBorrowIdRefMap.get(borrowId);
                if (!CollectionUtils.isEmpty(tenderList)) {
                    log.info("保存投标记录" + (borrow.getId()));
                    for (Tender tender : tenderList) {
                        long tenderUserId = tender.getUserId();
                        long tenderId = tender.getId();
                        tender.setState(tenderState);
                        tender.setUpdatedAt(nowDate);

                        List<BorrowCollection> borrowCollectionList = borrowCollectionAndTenderIdRefMap.get(tenderId); // 查询回款记录
                        if (!CollectionUtils.isEmpty(borrowCollectionList)) {
                            log.info("保存待收" + (borrow.getId()));
                            for (BorrowCollection borrowCollection : borrowCollectionList) {
                                borrowCollection.setUserId(tenderUserId);
                                borrowCollection.setBorrowId(borrowId);
                                borrowCollection.setUpdatedAt(nowDate);
                                String insert = "UPDATE `gfb_borrow_collection`  SET `user_id` = " + tenderUserId + " ,  `borrow_id` = " + borrowId + "  WHERE `id`  = " + borrowCollection.getId() + " ;";
                                try {
                                    bufferedWriter.write(insert);
                                    bufferedWriter.newLine();
                                    bufferedWriter.flush();
                                } catch (IOException e) {
                                    log.error("插入待收失败", e);
                                }
                            }
                        }
                        String insert = "UPDATE `gfb_borrow_tender`  SET `state` = " + tenderState + "  WHERE `id` = " + tenderId + " ; ";
                        try {
                            bufferedWriter.write(insert);
                            bufferedWriter.newLine();
                            bufferedWriter.flush();
                        } catch (IOException e) {
                            log.error("插入投标记录失败", e);
                        }
                    }
                }
            }
        }
    }


    @Override
    @Transactional(rollbackOn = Exception.class)
    public void transfer() {
        //1查询债权借款
        String sql = "select id from gfb_borrow b where tender_id > 0 and status not in (4, 5, 2) ";
        List<Long> queryForList = (List<Long>) entityManager.createNativeQuery(sql.toString()).getResultList();
        /* 债权转让借款 */
        Specification<Borrow> bs = Specifications
                .<Borrow>and()
                .in("id", new HashSet(queryForList).toArray())
                .build();
        List<Borrow> transferBorrowList = borrowService.findList(bs);
        Map<Long, Borrow> transferBorrowMaps = transferBorrowList.stream().collect(Collectors.toMap(Borrow::getId, Function.identity()));
        Set<Long> transferBorrowIds = transferBorrowList.stream().map(Borrow::getId).collect(Collectors.toSet());

        /* 转出债权集合 */
        Set<Long> transferTenderIds = transferBorrowList.stream().map(Borrow::getTenderId).collect(Collectors.toSet());
        Specification<Tender> ts = Specifications
                .<Tender>and()
                .in("id", transferTenderIds.toArray())
                .build();
        List<Tender> transferTenderList = tenderService.findList(ts);
        Map<Long, Tender> parentTenderMaps = transferTenderList.stream().collect(Collectors.toMap(Tender::getId, Function.identity()));
        /* 原始标id */
        Set<Long> parentBorrowIds = transferTenderList.stream().map(Tender::getBorrowId).collect(Collectors.toSet());
        /* 原始借款集合 */
        bs = Specifications
                .<Borrow>and()
                .in("id", parentBorrowIds.toArray())
                .build();
        List<Borrow> parentBorrowList = borrowService.findList(bs);
        Map<Long, Borrow> parentBorrowMaps = parentBorrowList.stream().collect(Collectors.toMap(Borrow::getId, Function.identity()));
        /*转出债权回款集合*/
        Specification<BorrowCollection> bcs = Specifications
                .<BorrowCollection>and()
                .in("tenderId", transferTenderIds.toArray())
                .eq("transferFlag", 1)
                .build();
        List<BorrowCollection> transferBorrowCollectionList = borrowCollectionService.findList(bcs, new Sort(Sort.Direction.ASC, "order"));
        Map<Long/* 投标id tender_id */, List<BorrowCollection>> transferBorrowCollectionMaps = transferBorrowCollectionList.stream().collect(groupingBy(BorrowCollection::getTenderId));
        List<Transfer> transferList = new ArrayList<>();
        transferBorrowList.stream().forEach(borrow -> {
            Tender parentTender = parentTenderMaps.get(borrow.getTenderId());
            List<BorrowCollection> transferBorrowCollection = transferBorrowCollectionMaps.get(parentTender.getId());
            boolean flag = !CollectionUtils.isEmpty(transferBorrowCollection);
            Borrow parentBorrow = parentBorrowMaps.get(parentTender.getBorrowId());

            Transfer transfer = new Transfer();
            transfer.setState(borrow.getStatus() == 5 ? 4 : (borrow.getStatus() == 2 ? 3 : 2));
            transfer.setTitle(borrow.getName());
            transfer.setSuccessAt(borrow.getSuccessAt());
            transfer.setUserId(borrow.getUserId());
            transfer.setType(0);
            transfer.setTransferMoney(borrow.getMoney());
            transfer.setTransferMoneyYes(borrow.getMoneyYes());
            transfer.setAlreadyInterest(0l);
            transfer.setApr(borrow.getApr());
            transfer.setTimeLimit(borrow.getTimeLimit());
            transfer.setIsLock(borrow.getIsLock());
            transfer.setIsAll(true);
            transfer.setTenderId(borrow.getTenderId());
            transfer.setBorrowId(parentBorrow.getId());
            if (flag) {
                transfer.setStartOrder(transferBorrowCollection.get(0).getOrder());
                transfer.setEndOrder(transferBorrowCollection.get(transferBorrowCollection.size() - 1).getOrder());
                transfer.setRepayAt(transferBorrowCollection.get(0).getCollectionAt());
            }
            transfer.setReleaseAt(borrow.getReleaseAt());
            transfer.setCreatedAt(borrow.getCreatedAt());
            transfer.setUpdatedAt(borrow.getUpdatedAt());
            transferList.add(transfer);
            //改变债权转让借款标状态为6
            borrow.setStatus(6);
            borrow.setUpdatedAt(new Date());
        });
        transferService.save(transferList);
        Map<Long, Transfer> transferMaps = transferList.stream().filter(transfer -> transfer.getState() == 2).collect(Collectors.toMap(Transfer::getTenderId, Function.identity()));

        /* 查询债权转让borrow的投标记录 */
        for (Long transferBorrowId : transferBorrowIds) {
            ts = Specifications
                    .<Tender>and()
                    .eq("borrowId", transferBorrowId)
                    .eq("status", 1)
                    .build();

            List<Tender> buyTransferTenderList = tenderService.findList(ts);
            List<TransferBuyLog> transferBuyLogList = new ArrayList<>();
            List<TransferBuyLog> transferBuyLogList1 = new ArrayList<>();
            buyTransferTenderList.stream().forEach(buyTransferTender -> {
                Borrow transferBorrow = transferBorrowMaps.get(buyTransferTender.getBorrowId());
                Tender parentTender = parentTenderMaps.get(transferBorrow.getTenderId());
                Transfer transfer = transferMaps.get(parentTender.getId());
                if (ObjectUtils.isEmpty(transfer)) {
                    return;
                }

                TransferBuyLog transferBuyLog = new TransferBuyLog();
                transferBuyLog.setSource(0);
                transferBuyLog.setState(buyTransferTender.getState());
                transferBuyLog.setUserId(buyTransferTender.getUserId());
                transferBuyLog.setUpdatedAt(buyTransferTender.getUpdatedAt());
                transferBuyLog.setType(0);
                transferBuyLog.setValidMoney(buyTransferTender.getValidMoney());
                transferBuyLog.setBuyMoney(buyTransferTender.getValidMoney());
                transferBuyLog.setAlreadyInterest(0l);
                transferBuyLog.setTransferId(transfer.getId());
                transferBuyLog.setPrincipal(buyTransferTender.getValidMoney());
                transferBuyLog.setCreatedAt(transfer.getCreatedAt());
                transferBuyLogList.add(transferBuyLog);
                TransferBuyLog transferBuyLog1 = new TransferBuyLog();
                BeanHelper.copyParamter(transferBuyLog, transferBuyLog1, true);
                transferBuyLog1.setState(buyTransferTender.getState());
                transferBuyLogList1.add(transferBuyLog1);
            });
            transferBuyLogService.save(transferBuyLogList);
            Map<Long, List<TransferBuyLog>> transferBuyMaps = transferBuyLogList.stream().collect(groupingBy(TransferBuyLog::getTransferId));
            Map<Long, List<TransferBuyLog>> transferBuyMaps1 = transferBuyLogList1.stream().collect(groupingBy(TransferBuyLog::getTransferId));
            Set<Long> transferIds = new HashSet<>();
            buyTransferTenderList.stream().forEach(buyTransferTender -> {
                Borrow transferBorrow = transferBorrowMaps.get(buyTransferTender.getBorrowId());
                Tender parentTender = parentTenderMaps.get(transferBorrow.getTenderId());
                Transfer transfer = transferMaps.get(parentTender.getId());
                if (ObjectUtils.isEmpty(transfer) || transfer.getState() != 2 || transferIds.contains(transfer.getId())) {
                    return;
                }
                Borrow prarentBorrow = parentBorrowMaps.get(parentTender.getBorrowId());
                List<TransferBuyLog> transferBuyLogs = transferBuyMaps.get(transfer.getId());
                List<TransferBuyLog> transferBuyLogs1 = transferBuyMaps1.get(transfer.getId());
                List<Tender> childTenderList = addChildTender(transfer.getCreatedAt(), transfer, parentTender, transferBuyLogs, transferBuyLogs1);

                addChildTenderCollection(transfer.getCreatedAt(), transfer, prarentBorrow, childTenderList);
                transferIds.add(transfer.getId());
            });
        }
        borrowService.save(transferBorrowList);
    }

    /**
     * 新增子级标的
     *
     * @param nowDate
     * @param transfer
     * @param parentTender
     * @param transferBuyLogList
     * @return
     */
    public List<Tender> addChildTender(Date nowDate, Transfer transfer, Tender parentTender, List<TransferBuyLog> transferBuyLogList, List<TransferBuyLog> transferBuyLogList1) {
        //生成债权记录与回款记录
        List<Tender> childTenderList = new ArrayList<>();
        Map<Long, TransferBuyLog> transferBuyLogMaps = transferBuyLogList1.stream().collect(Collectors.toMap(TransferBuyLog::getId, Function.identity()));
        transferBuyLogList.stream().forEach(transferBuyLog -> {
            Tender childTender = new Tender();
            /*UserThirdAccount buyUserThirdAccount = userThirdAccountService.findByUserId(transferBuyLog.getUserId());
*/
            TransferBuyLog transferBuyLog1 = transferBuyLogMaps.get(transferBuyLog.getId());
            childTender.setUserId(transferBuyLog.getUserId());
            childTender.setStatus(1);  // 投标成不成功
            childTender.setType(transferBuyLog.getType());
            childTender.setBorrowId(transfer.getBorrowId());
            childTender.setSource(transferBuyLog.getSource());
            childTender.setIsAuto(transferBuyLog.getAuto());
            childTender.setAutoOrder(transferBuyLog.getAutoOrder());
            childTender.setMoney(transferBuyLog.getBuyMoney());
            childTender.setValidMoney(transferBuyLog.getPrincipal());
            childTender.setTransferFlag(0);
            childTender.setTUserId(transferBuyLog.getUserId());
            childTender.setState(transferBuyLog1.getState());
            childTender.setAutoOrder(0);
            childTender.setParentId(parentTender.getId());
            childTender.setTransferBuyId(transferBuyLog.getId());
            childTender.setAlreadyInterest(transferBuyLog.getAlreadyInterest());
            childTender.setThirdTenderOrderId(transferBuyLog.getThirdTransferOrderId());
            childTender.setAuthCode(transferBuyLog.getTransferAuthCode());
            childTender.setCreatedAt(nowDate);
            childTender.setUpdatedAt(nowDate);
            childTenderList.add(childTender);

            //更新购买净值标状态为成功购买
            transferBuyLog.setState(1);
            transferBuyLog.setUpdatedAt(nowDate);
        });
        tenderService.save(childTenderList);
        transferBuyLogService.save(transferBuyLogList);

        //更新老债权为已转让
        parentTender.setTransferFlag(transfer.getIsAll() ? 3 : 2);
        parentTender.setUpdatedAt(nowDate);
        tenderService.save(parentTender);
        //更新债权转让为已转让
        transfer.setState(2);
        transfer.setUpdatedAt(nowDate);
        transferService.save(transfer);
        return childTenderList;
    }

    /**
     * 生成子级债权回款记录，标注老债权回款已经转出
     *
     * @param nowDate
     * @param transfer
     * @param parentBorrow
     * @param childTenderList
     */
    public List<BorrowCollection> addChildTenderCollection(Date nowDate, Transfer transfer, Borrow parentBorrow, List<Tender> childTenderList) {
        List<BorrowCollection> childTenderCollectionList = new ArrayList<>();/* 债权子记录回款记录 */
        String borrowCollectionIds = transfer.getBorrowCollectionIds();

        //生成子级债权回款记录，标注老债权回款已经转出
        Specification<BorrowCollection> bcs = null;

        if (transfer.getIsAll()) {
            bcs = Specifications
                    .<BorrowCollection>and()
                    .eq("tenderId", transfer.getTenderId())
                    .eq("transferFlag", 1)
                    .build();
        } else {
            bcs = Specifications
                    .<BorrowCollection>and()
                    .eq("tenderId", transfer.getTenderId())
                    .eq("id", borrowCollectionIds.split(","))
                    .eq("transferFlag", 1)
                    .build();
        }
        List<BorrowCollection> borrowCollectionList = borrowCollectionService.findList(bcs);/* 债权转让原投资回款记录 */
        Date repayAt = transfer.getRepayAt();/* 原借款下一期还款日期 */
        Date startAt = DateHelper.subMonths(repayAt, 1);/* 计息开始时间 */
        for (int j = 0; j < childTenderList.size(); j++) {
            Tender childTender = childTenderList.get(j);/* 购买债权转让子投资记录 */
            //生成购买债权转让新的回款记录
            BorrowCalculatorHelper borrowCalculatorHelper = new BorrowCalculatorHelper(
                    childTender.getValidMoney().doubleValue(),
                    transfer.getApr().doubleValue(),
                    transfer.getTimeLimit(),
                    startAt);
            Map<String, Object> rsMap = borrowCalculatorHelper.simpleCount(parentBorrow.getRepayFashion());
            List<Map<String, Object>> repayDetailList = (List<Map<String, Object>>) rsMap.get("repayDetailList");
            Preconditions.checkNotNull(repayDetailList, "生成用户回款计划开始: 计划生成为空");
            BorrowCollection borrowCollection;
            long collectionMoney = 0;
            int startOrder = borrowCollectionList.get(0).getOrder();/* 获取开始转让期数,期数下标从0开始 */
            for (int i = 0; i < repayDetailList.size(); i++) {
                borrowCollection = new BorrowCollection();
                Map<String, Object> repayDetailMap = repayDetailList.get(i);
                collectionMoney += new Double(NumberHelper.toDouble(repayDetailMap.get("repayMoney"))).longValue();
                long interest = new Double(NumberHelper.toDouble(repayDetailMap.get("interest"))).longValue();

                borrowCollection.setTenderId(childTender.getId());
                borrowCollection.setStatus(0);
                borrowCollection.setOrder(startOrder++);
                borrowCollection.setUserId(childTender.getUserId());
                borrowCollection.setStartAt(i > 0 ? DateHelper.stringToDate(StringHelper.toString(repayDetailList.get(i - 1).get("repayAt"))) : startAt);
                borrowCollection.setStartAtYes(i > 0 ? DateHelper.stringToDate(StringHelper.toString(repayDetailList.get(i - 1).get("repayAt"))) : nowDate);
                borrowCollection.setCollectionAt(DateHelper.stringToDate(StringHelper.toString(repayDetailMap.get("repayAt"))));
                borrowCollection.setCollectionAtYes(DateHelper.stringToDate(StringHelper.toString(repayDetailMap.get("repayAt"))));
                borrowCollection.setCollectionMoney(NumberHelper.toLong(repayDetailMap.get("repayMoney")));
                borrowCollection.setPrincipal(NumberHelper.toLong(repayDetailMap.get("principal")));
                borrowCollection.setInterest(interest);
                borrowCollection.setCreatedAt(nowDate);
                borrowCollection.setUpdatedAt(nowDate);
                borrowCollection.setCollectionMoneyYes(0l);
                borrowCollection.setLateDays(0);
                borrowCollection.setLateInterest(0l);
                borrowCollection.setBorrowId(parentBorrow.getId());
                childTenderCollectionList.add(borrowCollection);
            }
            borrowCollectionService.save(childTenderCollectionList);
        }

        //更新转出投资记录回款状态
        borrowCollectionList.stream().forEach(bc -> {
            bc.setTransferFlag(1);
        });
        borrowCollectionService.save(borrowCollectionList);

        return childTenderCollectionList;
    }


    private int caculTenderState(Borrow borrow) {
        int tenderState = -1;
        if (borrow.getStatus().intValue() == 1) {  // 招标中
            tenderState = 1;  // 投标中
        } else if (borrow.getStatus().intValue() == 3) {
            if (!ObjectUtils.isEmpty(borrow.getCloseAt())) {
                tenderState = 3;  // 已结清
            } else if (!ObjectUtils.isEmpty(borrow.getSuccessAt())) {
                tenderState = 2;  // 回款中
            } else {
                tenderState = -2;  // 特殊请款
            }
        }
        return tenderState;
    }

    private List<BorrowCollection> findBorrowCollectionByTenderId(Set<Long> tenderIdSet) {
        Specification<BorrowCollection> borrowCollectionSpecification = Specifications.<BorrowCollection>and()
                .in("tenderId", tenderIdSet.toArray())
                .build();

        return borrowCollectionService.findList(borrowCollectionSpecification);
    }

    private List<Tender> findTenderByBorrowIds(Set<Long> borrowIdSet) {
        // 查询投标信息
        Specification<Tender> tenderSpecification = Specifications.<Tender>and()
                .in("borrowId", borrowIdSet.toArray())
                .eq("status", 1) // 成功的
                .build();

        return tenderService.findList(tenderSpecification);
    }

    private List<BorrowRepayment> findRepaymentListByBorrowId(Set<Long> borrowIdSet) {
        Specification<BorrowRepayment> borrowRepaymentSpecification = Specifications.<BorrowRepayment>and()
                .in("borrowId", borrowIdSet.toArray())
                .build();

        return borrowRepaymentService.findList(borrowRepaymentSpecification);
    }
}
