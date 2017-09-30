package com.gofobao.framework.asset.biz.impl;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.api.model.account_details_query.AccountDetailsQueryItem;
import com.gofobao.framework.asset.biz.OfflineRechargeSynBiz;
import com.gofobao.framework.asset.entity.RechargeDetailLog;
import com.gofobao.framework.asset.service.RechargeDetailLogService;
import com.gofobao.framework.collection.vo.response.web.Collection;
import com.gofobao.framework.common.assets.AssetChange;
import com.gofobao.framework.common.assets.AssetChangeProvider;
import com.gofobao.framework.common.assets.AssetChangeTypeEnum;
import com.gofobao.framework.common.rabbitmq.MqConfig;
import com.gofobao.framework.common.rabbitmq.MqHelper;
import com.gofobao.framework.common.rabbitmq.MqQueueEnum;
import com.gofobao.framework.common.rabbitmq.MqTagEnum;
import com.gofobao.framework.financial.entity.NewEve;
import com.gofobao.framework.financial.service.NewEveService;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.ExceptionEmailHelper;
import com.gofobao.framework.helper.MoneyHelper;
import com.gofobao.framework.helper.StringHelper;
import com.gofobao.framework.member.entity.UserThirdAccount;
import com.gofobao.framework.member.service.UserThirdAccountService;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Range;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

@Service
@Slf4j
public class OfflineRechargeSynBizImpl implements OfflineRechargeSynBiz {

    private final static Gson GSON = new Gson();

    @Autowired
    ExceptionEmailHelper exceptionEmailHelper;

    @Autowired
    RechargeDetailLogService rechargeDetailLogService;

    @Autowired
    NewEveService newEveService;

    @Autowired
    UserThirdAccountService userThirdAccountService;

    @Autowired
    AssetChangeProvider assetChangeProvider;

    @Autowired
    MqHelper mqHelper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean process(String date) throws Exception {
        log.info("================================");
        log.info(String.format("线下充值确认, 时间: %s", date));
        log.info("================================");
        String transtype = "7820";  // 线下转账类型
        Specification<NewEve> eveSpecification = Specifications.<NewEve>and()
                .eq("transtype", transtype)
                .eq("queryTime", date)
                .build();
        Long count = newEveService.countByTranstypeAndQueryTime(transtype, date);
        if (count <= 0) {
            log.info("查询当天线下转账数据为0");
            return true;
        }

        int pageSize = 20, pageIndex = 0, pageIndexTotal = 0;
        pageIndexTotal = count.intValue() / pageSize;
        pageIndexTotal = count.intValue() % pageSize == 0 ? pageIndexTotal : pageIndexTotal + 1;
        for (; pageIndex < pageIndexTotal; pageIndex++) {
            Pageable pageable = new PageRequest(pageIndex, pageSize, new Sort(new Sort.Order(Sort.Direction.DESC, "id")));
            List<NewEve> newEveList = newEveService.findByTranstypeAndQueryTime(transtype, date, pageable);
            for (NewEve item : newEveList) {   // 查询用户
                String accountId = item.getCardnbr();
                UserThirdAccount userThirdAccount = userThirdAccountService.findByAccountId(accountId);
                if (ObjectUtils.isEmpty(userThirdAccount)) {
                    log.error(String.format("检测线下充值遗漏程序运行异常, %s 当前用户未开户", accountId));
                    continue;
                }
                // 开始查看同步
                doRechargeAssetSynchronize(userThirdAccount, date);
            }
        }

        // 查询所有资金变动记录
        return true;
    }

    /**
     * 资金同步
     *
     * @param userThirdAccount
     * @param date
     */
    private void doRechargeAssetSynchronize(UserThirdAccount userThirdAccount, String date) throws Exception {
        String transtype = "7820";  // 线下转账类型
        //获取线下充值资金流水
        Specification<NewEve> specification = Specifications
                .<NewEve>and()
                .eq("cardnbr", userThirdAccount.getAccountId()) // 账号
                .eq("queryTime", date)// 时间
                .eq("transtype", transtype) // 线下充值类型
                .build();

        List<NewEve> newEveList = newEveService.findAll(specification);
        if (CollectionUtils.isEmpty(newEveList)) {
            log.warn("进入资金同步中, 查询到该账户的线下充值记录为空");
            return;
        }

        Date synchronizzeDate = DateHelper.stringToDate(date, DateHelper.DATE_FORMAT_YMD_NUM); // 同步时间
        Date beginDate = DateHelper.endOfDate(DateHelper.subDays(synchronizzeDate, 1)); // 开始时间
        Date endDate = DateHelper.beginOfDate(DateHelper.addDays(synchronizzeDate, 1)); // 结束时间
        // 查询当天线下充值流水
        Specification<RechargeDetailLog> rechargeDetailLogSpecification = Specifications
                .<RechargeDetailLog>and()
                .between("createTime", new Range<>(beginDate, endDate))
                .eq("userId", userThirdAccount.getUserId())  // 用户ID
                .eq("rechargeChannel", 1) // 线下充值
                .eq("state", 1)  // 充值成功
                .build();

        List<RechargeDetailLog> rechargeDetailLogList = rechargeDetailLogService.findAll(rechargeDetailLogSpecification);
        // ======================================
        // 剔除已经同步的充值记录
        // ======================================
        if (!CollectionUtils.isEmpty(rechargeDetailLogList)) {
            Iterator<NewEve> eveIterator = newEveList.iterator(); // 线下记录
            Iterator<RechargeDetailLog> recharegeIterator = rechargeDetailLogList.iterator(); // 充值记录
            while (recharegeIterator.hasNext()) {
                RechargeDetailLog rechargeDeatail = recharegeIterator.next();  // 充值记录
                boolean matchState = false;
                while (eveIterator.hasNext()) {
                    NewEve newEve = eveIterator.next();  // newEve
                    long money = MoneyHelper.yuanToFen(newEve.getAmount());
                    // 匹配到金额一直的
                    if (money == rechargeDeatail.getMoney()) {
                        matchState = true;
                        recharegeIterator.remove();
                        eveIterator.remove();
                        break;
                    }
                }

                if (!matchState) {
                    log.error("当前本地充值记录, 未匹配到线上充值记录");
                    exceptionEmailHelper.sendErrorMessage("线下充值同步",
                            String.format("当前充值记录未找到, 线上充值记录, 数据[%s]", GSON.toJson(rechargeDeatail)));
                }
            }
        }

        if (CollectionUtils.isEmpty(newEveList)) {
            log.info("线下充值本地记录和线上记录一致, 无需同步");
            return;
        }

        //==========================================
        // 线下记录同步
        //==========================================
        Date nowDate = new Date();
        for (NewEve item : newEveList) {
            String msg = GSON.toJson(item);
            log.info(String.format("新版线下充值同步进行中, 数据[%s]", msg));
            exceptionEmailHelper.sendErrorMessage("每日检查线下充值同步", msg);

            long money = MoneyHelper.yuanToFen(item.getAmount()); // 带同步金额
            String seqNo = item.getOrderno(); // 流水号
            if (ObjectUtils.isEmpty(seqNo)) {  // 防止即信返回流水号为空
                seqNo = String.format("%s%s%s", item.getCendt(), item.getTranno());
            }

            RechargeDetailLog rechargeDetailLog = new RechargeDetailLog();
            rechargeDetailLog.setUserId(userThirdAccount.getUserId()); // 用户Id
            rechargeDetailLog.setBankName(userThirdAccount.getBankName());  // 银行卡
            rechargeDetailLog.setCallbackTime(nowDate); // 充值时间
            rechargeDetailLog.setCreateTime(synchronizzeDate);  // 创建时间
            rechargeDetailLog.setUpdateTime(nowDate);  // 更改时间
            rechargeDetailLog.setCardNo(userThirdAccount.getCardNo()); // 银行卡号
            rechargeDetailLog.setDel(0);
            rechargeDetailLog.setState(1); // 充值成功
            rechargeDetailLog.setMoney(money);
            rechargeDetailLog.setRechargeChannel(1);  // 其他渠道
            rechargeDetailLog.setRechargeType(1); // 线下充值
            rechargeDetailLog.setSeqNo(seqNo);  // 流水
            rechargeDetailLog = rechargeDetailLogService.save(rechargeDetailLog);

            AssetChange assetChange = new AssetChange();
            assetChange.setType(AssetChangeTypeEnum.offlineRecharge);  // 线下充值
            assetChange.setUserId(userThirdAccount.getUserId());
            assetChange.setMoney(money);
            assetChange.setRemark(String.format("成功线下充值%s元", StringHelper.formatDouble(money / 100D, true)));
            assetChange.setSourceId(rechargeDetailLog.getId());
            assetChange.setSeqNo(seqNo);
            assetChange.setSourceId(rechargeDetailLog.getId());
            assetChange.setGroupSeqNo(assetChange.getGroupSeqNo());
            assetChangeProvider.commonAssetChange(assetChange);

            // 触发用户充值
            MqConfig mqConfig = new MqConfig();
            mqConfig.setTag(MqTagEnum.RECHARGE);
            mqConfig.setQueue(MqQueueEnum.RABBITMQ_USER_ACTIVE);
            mqConfig.setSendTime(DateHelper.addSeconds(nowDate, 30));
            ImmutableMap<String, String> body = ImmutableMap.of(MqConfig.MSG_ID, rechargeDetailLog.getId().toString());
            mqConfig.setMsg(body);
            mqHelper.convertAndSend(mqConfig);
            log.info("同步线下充值记录成功");
        }
    }
}
