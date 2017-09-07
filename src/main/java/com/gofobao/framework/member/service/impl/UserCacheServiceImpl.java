package com.gofobao.framework.member.service.impl;

import com.gofobao.framework.asset.entity.Asset;
import com.gofobao.framework.asset.repository.AssetRepository;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.StringHelper;
import com.gofobao.framework.member.entity.UserCache;
import com.gofobao.framework.member.entity.Users;
import com.gofobao.framework.member.repository.UserCacheRepository;
import com.gofobao.framework.member.service.UserCacheService;
import com.gofobao.framework.member.vo.response.pc.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by Zeke on 2017/5/19.
 */
@Service
public class UserCacheServiceImpl implements UserCacheService {

    @Autowired
    private UserCacheRepository userCacheRepository;

    @Autowired
    private AssetRepository assetRepository;

    /**
     * 根据id查询UserCache
     *
     * @param id
     * @return
     */
    public UserCache findById(Long id) {
        return userCacheRepository.findOne(id);
    }

    public UserCache findByUserIdLock(Long userId) {
        return userCacheRepository.findByUserId(userId);
    }

    public UserCache save(UserCache userCache) {
        return userCacheRepository.save(userCache);
    }

    public UserCache updateById(UserCache userCache) {
        return userCacheRepository.save(userCache);
    }

    public List<UserCache> findList(Specification<UserCache> specification) {
        return userCacheRepository.findAll(specification);
    }

    public List<UserCache> findList(Specification<UserCache> specification, Sort sort) {
        return userCacheRepository.findAll(specification, sort);
    }

    public List<UserCache> findList(Specification<UserCache> specification, Pageable pageable) {
        return userCacheRepository.findAll(specification, pageable).getContent();
    }

    public long count(Specification<UserCache> specification) {
        return userCacheRepository.count(specification);
    }


    @Override
    public AssetStatistic assetStatistic(Long userId) {
        AssetStatistic statistic = new AssetStatistic();

        Asset asset = assetRepository.findOne(userId);
        UserCache userCache = userCacheRepository.findOne(userId);
        //可用额度
        Long userMoney = asset.getUseMoney();
        statistic.setUseMoney(StringHelper.formatMon(userMoney / 100D));
        //冻结金额
        Long noUseMoney = asset.getNoUseMoney();
        statistic.setNoUseMoney(StringHelper.formatMon(noUseMoney / 100D));
        //待收
        Long collection = asset.getCollection();
        statistic.setCollection(StringHelper.formatMon(collection / 100D));
        //待还
        Long payment = asset.getPayment();
        statistic.setPayment(StringHelper.formatMon(payment / 100D));

        NetProceedsDetails netProceedsDetails = new NetProceedsDetails();

        statistic.setJingZhiDetails(netProceedsDetails);

        PaymentDetails paymentDetails = new PaymentDetails();
        //车贷利息
        Long tjWaitCollectionInterest = userCache.getTjWaitCollectionInterest();
        //车贷本金
        Long tjWaitCollectionPrincipal = userCache.getTjWaitCollectionPrincipal();
        //渠道利息
        Long qdWaitCollectionInterest = userCache.getQdWaitCollectionInterest();
        // 渠道本金
        Long qdWaitCollectionPrincipal = userCache.getQdWaitCollectionPrincipal();
        //总待收利息
        Long waitCollectionInterest = userCache.getWaitCollectionInterest();
        //总待收本金
        Long waitCollectionPrincipal = userCache.getWaitCollectionPrincipal();

        //总待收
        //  Long sumWaitCollectionMoney = waitCollectionInterest + waitCollectionPrincipal;
        //statistic.setPayment(StringHelper.formatMon(sumWaitCollectionMoney/100D));

        //待收明细
        paymentDetails.setChedaiWaitCollectionInterest(StringHelper.formatMon(tjWaitCollectionInterest / 100D));
        paymentDetails.setChedaiWaitCollectionPrincipal(StringHelper.formatMon(tjWaitCollectionPrincipal / 100D));
        paymentDetails.setQudaoWaitCollectionInterest(StringHelper.formatMon(qdWaitCollectionInterest / 100D));
        paymentDetails.setQudaoWaitCollectionPrincipal(StringHelper.formatMon(qdWaitCollectionPrincipal / 100D));
        paymentDetails.setJingzhiWaitCollectionInterest(StringHelper.formatMon((waitCollectionInterest - tjWaitCollectionInterest - qdWaitCollectionInterest) / 100D));
        paymentDetails.setJingzhiWaitCollectionPrincipal(StringHelper.formatMon((waitCollectionPrincipal - tjWaitCollectionPrincipal - qdWaitCollectionPrincipal) / 100D));
        statistic.setPaymentDetails(paymentDetails);

        //费用支出
        Long expenditureFee = userCache.getExpenditureFee();
        //利息支出
        Long expenditureInterest = userCache.getExpenditureInterest();
        //利息管理费支出
        Long expenditureInterestManage = userCache.getExpenditureInterestManage();
        //账户管理费支出
        Long expenditureManage = userCache.getExpenditureManage();
        //逾期支出
        Long expenditureOverdue = userCache.getExpenditureOverdue();
        //其他支出
        Long expenditureOther = userCache.getExpenditureOther();
        //待还利息
        Long waitRepayInterest = userCache.getWaitRepayInterest();
        //待还本金
        Long waitRepayPrincipal = userCache.getWaitRepayPrincipal();

        Long waitExpenditureInterestManageFee = new Long(userCache.getWaitExpenditureInterestManageFee());
        Long netAsset = new Double((asset.getCollection() + asset.getNoUseMoney() + asset.getUseMoney()) - asset.getPayment()).longValue();
        netProceedsDetails.setWaitInterest(StringHelper.formatMon(waitRepayInterest / 100D));
        netProceedsDetails.setWaitPrincipal(StringHelper.formatMon(waitRepayPrincipal / 100D));

        //总支出
        Long sumExpend = waitExpenditureInterestManageFee + waitRepayInterest + expenditureFee + expenditureInterest + expenditureInterestManage + expenditureManage + expenditureOverdue + expenditureOther;

        /**
         * 已实现净收益总额 = 已实现收入总额 - 已支出总额
         * @return array
         */
        Long netIncomeTotal = userCache.getIncomeTotal() - sumExpend;
        statistic.setNetProceeds(StringHelper.formatMon(netIncomeTotal / 100D));

        /**
         * 待付支出总额
         */
        Double sumWaitExpend = (tjWaitCollectionInterest + qdWaitCollectionInterest) * 0.1;

        /**
         * 未实现收入总额
         * @return float
         */
        Long collectionInterest = userCache.getWaitCollectionInterest();

        /**
         * 未实现净收益总额 = 未实现收入总额 - 待付支出总额
         * @return array
         */
        Double noNetProceeds = new Double(collectionInterest) - sumWaitExpend;
        statistic.setNoNetProceeds(StringHelper.formatMon(noNetProceeds / 100D));


        /**
         * 总净收益 = 已实现净收益总额 + 未实现净收益总额
         * @return float
         */
        Double sumJingshou = netIncomeTotal + noNetProceeds;
        statistic.setSumNetProceeds(StringHelper.formatMon(sumJingshou / 100D));

        //净值额度
        Double netWorthQuota = new Double((asset.getUseMoney() + waitCollectionPrincipal) * 0.8 - collection);//计算净值额度
        statistic.setNetWorthLimit(StringHelper.formatMon(netWorthQuota / 100D));

        //净资产
        statistic.setAssetTotal(StringHelper.formatDouble((asset.getUseMoney() + asset.getNoUseMoney() + asset.getCollection() - asset.getPayment()) / 100D, true));

        //总支出
        statistic.setSumExpend(StringHelper.formatMon(sumExpend / 100D));
        //总收益
        statistic.setSumEarnings(StringHelper.formatMon((userCache.getIncomeTotal() + waitCollectionInterest) / 100D));

        return statistic;
    }

    /**
     * 总收益统计
     *
     * @param userId
     * @return
     */
    @Override
    public ResponseEntity<IncomeEarnedDetail> incomeEarned(Long userId) {

        IncomeEarnedDetail incomeEarnedDetail = VoBaseResp.ok("查詢成功", IncomeEarnedDetail.class);

        UserCache userCache = userCacheRepository.findOne(userId);
        Long incomeTotal = userCache.getIncomeTotal();  //总
        Long incomeOverdue = userCache.getIncomeOverdue();
        Long incomeOther = userCache.getIncomeOther();
        Long incomeInterest = userCache.getIncomeInterest();
        Long incomeAward = userCache.getIncomeAward();
        Long incomeBonus = userCache.getIncomeBonus();
        Long incomeIntegralCash = userCache.getIncomeIntegralCash();
        Long waitCollectionInterest = userCache.getWaitCollectionInterest();

        incomeEarnedDetail.setIncomeAward(StringHelper.formatMon(incomeAward / 100D));
        incomeEarnedDetail.setIncomeBonus(StringHelper.formatMon(incomeBonus / 100D));
        incomeEarnedDetail.setIncomeIntegralCash(StringHelper.formatMon(incomeIntegralCash / 100D));
        incomeEarnedDetail.setIncomeInterest(StringHelper.formatMon(incomeInterest / 100D));
        incomeEarnedDetail.setIncomeOther(StringHelper.formatMon(incomeOther / 100D));
        incomeEarnedDetail.setIncomeOverdue(StringHelper.formatMon(incomeOverdue / 100D));
        incomeEarnedDetail.setIncomeEarned(StringHelper.formatMon((waitCollectionInterest + incomeTotal) / 100D));
        incomeEarnedDetail.setWaitCollectionInterest(StringHelper.formatMon(waitCollectionInterest / 100D));
        incomeEarnedDetail.setWaitIncomeInterest(StringHelper.formatMon(waitCollectionInterest / 100D));

        return ResponseEntity.ok(incomeEarnedDetail);
    }

    /**
     * 支出统计
     *
     * @param userId
     * @return
     */
    @Override
    public ResponseEntity<ExpenditureDetail> expenditureDetail(Long userId) {
        ExpenditureDetail expenditureDetail = VoBaseResp.ok("查詢成功", ExpenditureDetail.class);

        UserCache userCache = userCacheRepository.findOne(userId);
        Long expenditureInterestManage = userCache.getExpenditureInterestManage();
        Long expenditureInterest = userCache.getExpenditureInterest();
        Long expenditureFee = userCache.getExpenditureFee();
        Long expenditureManage = userCache.getExpenditureManage();
        Long expenditureOther = userCache.getExpenditureOther();
        Long expenditureOverdue = userCache.getExpenditureOverdue();
        Long waitRepayInterest = userCache.getWaitRepayInterest();
        Double waitExpenditureInterestManage = (userCache.getTjWaitCollectionInterest() + userCache.getQdWaitCollectionInterest()) * 0.1;
        //已付利息管理费
        expenditureDetail.setInterestManageFee(StringHelper.formatMon(expenditureInterestManage / 100D));
        //其他支出
        expenditureDetail.setOtherFee(StringHelper.formatMon(expenditureOther / 100D));
        //账户管理费
        expenditureDetail.setAccountMangeFee(StringHelper.formatMon(expenditureManage / 100D));
        //已还利息
        expenditureDetail.setPaymentInterest(StringHelper.formatMon(expenditureInterest / 100D));
        //费用
        expenditureDetail.setFee(StringHelper.formatMon(expenditureFee / 100D));
        //逾期罚息
        expenditureDetail.setOverdueFee(StringHelper.formatMon(expenditureOverdue / 100D));
        //待付利息
        expenditureDetail.setWaitExpendInterest(StringHelper.formatMon(waitRepayInterest / 100D));
        //待付利息管理费
        expenditureDetail.setWaitExpendInterestManageFee(StringHelper.formatMon(userCache.getWaitExpenditureInterestManageFee() / 100D));
        //待付支出
        expenditureDetail.setWaitExpendTotal(StringHelper.formatMon((waitExpenditureInterestManage + waitRepayInterest) / 100D));
        //已支出总额
        expenditureDetail.setExpenditureTotal(StringHelper.formatMon(userCache.getIncomeTotal() / 100D));

        return ResponseEntity.ok(expenditureDetail);

    }

    @Override
    public boolean isNew(Users user) {
        UserCache userCache = userCacheRepository.findByUserId(user.getId());
        return !userCache.getTenderQudao() && !userCache.getTenderTuijian();
    }
}
