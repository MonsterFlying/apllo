package com.gofobao.framework.listener.providers;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.asset.entity.Asset;
import com.gofobao.framework.asset.service.AssetService;
import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.service.BorrowService;
import com.gofobao.framework.common.data.DataObject;
import com.gofobao.framework.common.data.GeSpecification;
import com.gofobao.framework.common.data.LeSpecification;
import com.gofobao.framework.common.rabbitmq.MqConfig;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.MathHelper;
import com.gofobao.framework.helper.NumberHelper;
import com.gofobao.framework.helper.StringHelper;
import com.gofobao.framework.helper.ThirdAccountHelper;
import com.gofobao.framework.helper.project.BorrowHelper;
import com.gofobao.framework.member.entity.UserThirdAccount;
import com.gofobao.framework.member.service.UserThirdAccountService;
import com.gofobao.framework.tender.biz.TransferBiz;
import com.gofobao.framework.tender.entity.AutoTender;
import com.gofobao.framework.tender.entity.Transfer;
import com.gofobao.framework.tender.service.AutoTenderService;
import com.gofobao.framework.tender.service.TransferBuyLogService;
import com.gofobao.framework.tender.service.TransferService;
import com.gofobao.framework.tender.vo.request.VoBuyTransfer;
import com.gofobao.framework.tender.vo.request.VoCreateTenderReq;
import com.gofobao.framework.tender.vo.request.VoThirdBatchCreditInvest;
import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by Zeke on 2017/8/1.
 */
@Component
@Slf4j
public class TransferProvider {

    @Autowired
    private TransferService transferService;
    @Autowired
    private AutoTenderService autoTenderService;
    @Autowired
    private BorrowService borrowService;
    @Autowired
    private AssetService assetService;
    @Autowired
    private UserThirdAccountService userThirdAccountService;
    @Autowired
    private TransferBiz transferBiz;

    final Gson GSON = new GsonBuilder().create();

    /**
     * 自动债权转让
     *
     * @param msg
     * @throws Exception
     */
    @Transactional(rollbackFor = Exception.class)
    public void autoTransfer(Map<String, String> msg) throws Exception {
        /*
         * 1.批次自动投标规则
         * 2.筛选合适的自动投标规则进行购买债权
         * 3.更新自动投标规则
         */
        Date nowDate = new Date();
        long transferId = NumberHelper.toLong(msg.get(MqConfig.MSG_TRANSFER_ID));/* 债权转让id */
        Transfer transfer = transferService.findByIdLock(transferId);/* 债权转让记录 */
        Preconditions.checkNotNull(transfer, "债权转让记录不存在!");
        Borrow borrow = borrowService.findById(transfer.getBorrowId());/* 债权转让 借款记录 */
        Preconditions.checkNotNull(borrow, "借款记录不存在!");
        String[] RepayFashions = BorrowHelper.countRepayFashions(borrow.getRepayFashion()).split(",");/* 还款方式 */

        int maxSize = 50;
        int pageNum = 0;
        List<AutoTender> autoTenderList = null;
        // 0、不限定，1、按月，2、按天
        int notTimeLimitType = borrow.getRepayFashion() == 1 ? 1 : 2;/* 如果债权转让 借款是按月分期  则notTimelimitType为2,借款是按天 则notTimelimitType为1*/
        List<Long> userIds = null;/* 匹配上的自动投标规则 */
        Specification<Asset> as = null;/* 匹配资产规则 */
        Specification<UserThirdAccount> utas = null;/* 匹配存管账户记录规则 */
        long principalYes = transfer.getPrincipalYes();/* 债权转让已购金额 */
        long principal = transfer.getPrincipal();/* 债权转让金额 */
        List<Long> tenderUserIds = new ArrayList<>();/* 已经购买债权的用户id */
        List<Long> autoTenderIds = new ArrayList<>();/* 已经触发的自动投标id */

        //变量定义
        Map<Long, Asset> assetMaps = null;
        Asset asset = null;
        Map<Long, UserThirdAccount> userThirdAccountMaps = null;
        UserThirdAccount userThirdAccount = null;
        boolean flag = false;
        int autoTenderCount = 0;

        Specification<AutoTender> ats = Specifications/* 匹配自动投标规则 */
                .<AutoTender>and()
                .eq("tender3", 1)
                .eq("status", 1)
                .notIn("userId", transfer.getUserId())/* 排除转让人自动投标规则 */
                .in("repayFashions", RepayFashions)
                .notIn("timelimitType", notTimeLimitType)
                .predicate(new GeSpecification("timelimitLast", new DataObject(transfer.getTimeLimit())))
                .predicate(new LeSpecification("timelimitFirst", new DataObject(transfer.getTimeLimit())))
                .predicate(new GeSpecification("aprFirst", new DataObject(transfer.getApr())))
                .predicate(new LeSpecification("aprLast", new DataObject(transfer.getApr())))
                .build();

        do {
            autoTenderList = autoTenderService.findList(ats, new PageRequest(pageNum++, maxSize, new Sort(Sort.Direction.ASC, "order")));
            if (CollectionUtils.isEmpty(autoTenderList)) {
                log.info("自动购买债权转让MQ：第" + (pageNum + 1) + "页,没有匹配到自动投标规则！");
                break;
            }

            userIds = autoTenderList.stream().map(autoTender -> autoTender.getUserId()).collect(Collectors.toList());
            //查询自动投标投资人的资产记录
            as = Specifications
                    .<Asset>and()
                    .in("userId", userIds)
                    .build();
            assetMaps = assetService.findList(as).stream().collect(Collectors.toMap(Asset::getUserId, Function.identity()));
            //查询自动投标投资人的存管账户记录
            utas = Specifications
                    .<UserThirdAccount>and()
                    .in("userId", userIds)
                    .build();
            userThirdAccountMaps = userThirdAccountService.findList(utas).stream().collect(Collectors.toMap(UserThirdAccount::getUserId, Function.identity()));

            for (AutoTender autoTender : autoTenderList) {
                asset = assetMaps.get(autoTender.getUserId());/* 自动投标投资人资产记录 */
                userThirdAccount = userThirdAccountMaps.get(autoTender.getUserId());/* 自动投标投资人存管信息记录 */

                // 保证每个用户 和 每个自动投标规则只能使用一次
                if (tenderUserIds.contains(NumberHelper.toLong(autoTender.getUserId()))
                        || autoTenderIds.contains(NumberHelper.toLong(autoTender.getId()))) {
                    continue;
                }

                //判断自动投标投资人是否开户
                if (ThirdAccountHelper.allConditionCheck(userThirdAccount).getBody().getState().getCode() != VoBaseResp.OK) {
                    continue;
                }

                if ((principalYes >= principal)) {  // 判断是否满标或者 达到自动投标最大额度
                    flag = true;
                    break;
                }

                long useMoney = asset.getUseMoney();  // 用户可用金额
                long buyMoney = autoTender.getMode() == 1 ? autoTender.getTenderMoney() : useMoney;/* 有效购买金额 */
                buyMoney = Math.min(useMoney - autoTender.getSaveMoney(), buyMoney);
                long lowest = autoTender.getLowest(); // 最小投标金额
                if ((buyMoney < lowest)) {
                    continue;
                }

                // 标的金额小于 最小投标金额
                if (principal - principalYes < lowest) {
                    continue;
                }

                if ((!tenderUserIds.contains(autoTender.getUserId()))
                        && (!autoTenderIds.contains(autoTender.getId()))) {  // 保证自动不能重复
                    //购买债权转让
                    VoBuyTransfer voBuyTransfer = new VoBuyTransfer();
                    voBuyTransfer.setUserId(autoTender.getUserId());
                    voBuyTransfer.setAuto(true);
                    voBuyTransfer.setBuyMoney(MathHelper.myRound(buyMoney / 100.0, 2));
                    voBuyTransfer.setAutoOrder(autoTender.getOrder());
                    ResponseEntity<VoBaseResp> response = transferBiz.buyTransfer(voBuyTransfer);
                    if (response.getStatusCode().equals(HttpStatus.OK)) { //购买债权转让成功后更新自动投标规则
                        principalYes += lowest;
                        autoTenderIds.add(autoTender.getId());
                        tenderUserIds.add(autoTender.getUserId());
                        autoTender.setAutoAt(nowDate);
                        autoTenderService.updateById(autoTender);
                        autoTenderCount++;
                    } else {
                        continue;
                    }
                }

            }
        } while (autoTenderList.size() >= maxSize && !flag);

        if (autoTenderCount >= 1) {//如果自动投标被触发则更新自动投标规则
            autoTenderService.updateAutoTenderOrder();
        }

        // 解除锁定
        if (principalYes != principal) { // 在自动投标中, 标的未满.马上将其解除.
            transfer.setUpdatedAt(nowDate);
            transfer.setLock(false);
            transferService.save(transfer);
        }
    }

    /**
     * 债权转让复审
     *
     * @param msg
     * @throws Exception
     */
    public boolean againVerifyTransfer(Map<String, String> msg) throws Exception {
        long transferId = NumberHelper.toLong(msg.get(MqConfig.MSG_TRANSFER_ID));/* 债权转让id */
        Transfer transfer = transferService.findByIdLock(transferId);
        Preconditions.checkNotNull(transfer, "债权转让记录不存在!");

        if (transfer.getState() != 1) {
            log.error("复审：债权转让状态已发生改变！transferId:" + transferId);
            return false;
        }

        log.info(String.format("复审: 批量债权转让申请开始: %s", GSON.toJson(msg)));
        /**
         * @// TODO: 2017/8/1
         * 1.通知即信处理债权转让
         * 2.记录资产变更
         * 3.债权转让债权转让记录状态变成
         */
        if (1!=1) {
            log.info(String.format("复审: 批量债权转让申请成功: %s", GSON.toJson(msg)));
            return true;
        } else {
            log.error(String.format("复审: 批量债权转让申请失败: %s", null));
            return false;
        }
    }
}
