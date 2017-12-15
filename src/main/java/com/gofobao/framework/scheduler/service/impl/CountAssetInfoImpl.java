package com.gofobao.framework.scheduler.service.impl;

import com.gofobao.framework.as.repository.RealtimeAssetRepository;
import com.gofobao.framework.asset.entity.Asset;
import com.gofobao.framework.asset.repository.AssetRepository;
import com.gofobao.framework.borrow.repository.BorrowRepository;
import com.gofobao.framework.comment.vo.response.VoCommonDataStatistic;
import com.gofobao.framework.financial.repository.AleveRepository;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.member.repository.UserCacheRepository;
import com.gofobao.framework.member.service.UserCacheService;
import com.gofobao.framework.scheduler.entity.Count;
import com.gofobao.framework.scheduler.repository.CountRepository;
import com.gofobao.framework.scheduler.service.CountAssetInfo;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by xin on 2017/12/7.
 */
@Service
public class CountAssetInfoImpl implements CountAssetInfo {
    public static final long ZFH = 22002;
    public static final long GFB = 22;
    public static final long ADMIN = 1;
    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private AleveRepository aleveRepository;

    @Autowired
    private BorrowRepository borrowRepository;

    @Autowired
    private UserCacheRepository userCacheRepository;

    @Autowired
    private RealtimeAssetRepository realtimeAssetRepository;

    @Autowired
    private UserCacheService userCacheService;

    @Autowired
    private CountRepository countRepository;


    @Override
    public void dayStatistic(Date date) {
        /**
         * 统计前一月
         */
        Date startDate = DateHelper.beginOfMonth(DateHelper.subMonths(date, 1));
        Date endDate = DateHelper.endOfMonth(DateHelper.subMonths(date, 1));
        List<Long> ids = Lists.newArrayList();
        ids.add(ZFH);
        ids.add(GFB);
        ids.add(ADMIN);
        /**
         * 按截止时间点统计
         */
        /**
         * 网站余额zfh
         */
        //可用余额
        long zfhUseMoney = 0L;
        //冻结金额
        long zfhNoUseMoney = 0L;
        //理财计划金额
        long zfhFinacePlanMoney = 0L;
        Asset zfh = assetRepository.findUserIdAndUpDated(ZFH, endDate);
//        Preconditions.checkNotNull(zfh, "用户不存在");
        if (!ObjectUtils.isEmpty(zfh)) {
            //可用余额
            zfhUseMoney = zfh.getUseMoney();
            //冻结金额
            zfhNoUseMoney = zfh.getNoUseMoney();
            //理财计划金额
            zfhFinacePlanMoney = zfh.getFinancePlanMoney();
        }


        /**
         * 网站余额other
         */
        /**
         * 可用余额
         */
        Integer otherUserMoney = assetRepository.findOtherUserMoney(ids, endDate);
        /**
         * 冻结金额
         */
        Integer otherNoUserMoney = assetRepository.findOtherNoUseMoney(ids, endDate);
        /**
         * 理财计划金额
         */
        Integer otherFinacePlanMoney = assetRepository.findOtherFinancePlan(ids, endDate);

        /**
         * 存管账户余额
         */

        //存管账户zfh,广富宝,admin余额
        List<Integer> account_zfh = realtimeAssetRepository.findUserIdAndUpdated(ids, endDate);

        //存管账户其他用户
        List<Integer> account_others = realtimeAssetRepository.findOtherUserIdAndUpdated(ids, endDate);


        //净值标待收本金
        Integer waitCollectionPrincipal = userCacheRepository.findWaitCollectionPrincipal(1, endDate);
        //净值标待收利息
        Integer waitCollectionInterest = userCacheRepository.findWaitCollectionInterest(1, endDate);
        //净值标待还本金
        Integer waitRepaymentPrincipal = userCacheRepository.findWaitRepaymentPrincipal(1, endDate);
        //净值标待还利息
        Integer waitRepaymentInterest = userCacheRepository.findWaitRepaymentInterest(1, endDate);
        //净值标垫付未收回本金
        Integer advanceNoPrincipal = userCacheRepository.findAdvancePrincipal(1, endDate);
        //净值标垫付未收回利息
        Integer advanceNoInterest = userCacheRepository.findAdvanceInterest(1, endDate);


        //车贷标待收
        List<VoCommonDataStatistic> carWaitCollection = userCacheService.findWaitCollection(0, endDate);


        //车贷标待还
        List<VoCommonDataStatistic> carWaitRepayment = userCacheService.findWaitRepayment(0, endDate);


        //渠道标待收
        List<VoCommonDataStatistic> channelWaitCollection = userCacheService.findWaitCollection(4, endDate);


        //渠道标待还
        List<VoCommonDataStatistic> channelWaitRepayment = userCacheService.findWaitRepayment(4, endDate);
        /**
         * 按月统计时间段
         */
        //净值标借款本金
        Integer borrowPrincipal = borrowRepository.findBorrowPrincipal(1, startDate, endDate);
        //净值标还款本金
        Integer repaymentPrincipal = borrowRepository.findRepaymentPrincipal(1, startDate, endDate);
        //净值标垫付本金
        Integer advancePrincipal = borrowRepository.findAdvancePrincipal(1, startDate, endDate);
        //净值标垫付后收回本金
        Integer advanceYesPrincipal = borrowRepository.findAdvanceYesPrincipal(1, startDate, endDate);
        //净垫付本金 = 垫付本金 - 垫付后收回本金
        Integer netAdvancePrincipal = advancePrincipal - advanceYesPrincipal;
        //净新增本金 = 借款本金-还款本金
        Integer netAddPrincipal = borrowPrincipal - repaymentPrincipal;
        //净值标待收本金(时间段)
        Integer netWaitCollectionPrincipal = borrowRepository.findNetWaitCollectionPrincipal(1, startDate, endDate);

        //车贷标借款本金
        List<Object[]> carBorrowPrincipal = borrowRepository.findCarBorrowPrincipal(0, startDate, endDate);
        Map<Integer, Integer> carBorrowMap = Maps.newHashMap();
        for (Object[] o : carBorrowPrincipal) {
            carBorrowMap.put((Integer) o[1], Integer.valueOf(o[0].toString()));
        }

        //车贷标还款本金
        List<Object[]> carRepaymentPrincipal = borrowRepository.findCarRepaymentPrincipal(0, startDate, endDate);
        Map<Integer, Integer> carRepaymentMap = Maps.newHashMap();
        for (Object[] o : carRepaymentPrincipal) {
            carRepaymentMap.put((Integer) o[1], Integer.valueOf(o[0].toString()));
        }
        //车贷标净新增本金 = 还款本金 - 借款本金
        //Integer carNetAddPrincipal = carBorrowPrincipal - carRepaymentPrincipal;
        Map<Integer, Integer> carIncreaseMap = Maps.newHashMap();

        for (Map.Entry<Integer, Integer> map : carBorrowMap.entrySet()) {
            Integer temp = 0;
            if (carRepaymentMap.containsKey(map.getKey())) {
                temp = carRepaymentMap.get(map.getKey());
            }
            carIncreaseMap.put(map.getKey(), temp - map.getValue());
        }

        //车贷标待收本金
        List<Object[]> carWaitCollectionPrincipal = borrowRepository.findCarWaitCollectionPrincipal(0, startDate, endDate);
        Map<Integer, Integer> carWaitCollectionPrincipalMap = Maps.newHashMap();
        for (Object[] o : carWaitCollectionPrincipal) {
            carWaitCollectionPrincipalMap.put((Integer) o[1], Integer.valueOf(o[0].toString()));
        }


        //渠道标借款本金
        List<Object[]> channelBorrowPrincipal = borrowRepository.findCarBorrowPrincipal(4, startDate, endDate);
        Map<Integer, Integer> channelBorrowMap = Maps.newHashMap();
        for (Object[] o : channelBorrowPrincipal) {
            channelBorrowMap.put((Integer) o[1], Integer.valueOf(o[0].toString()));
        }
        //渠道标还款本金
        List<Object[]> channelRepaymentPrincipal = borrowRepository.findCarRepaymentPrincipal(4, startDate, endDate);
        Map<Integer, Integer> channelRepaymentMap = Maps.newHashMap();
        for (Object[] o : channelRepaymentPrincipal) {
            channelRepaymentMap.put((Integer) o[1], Integer.valueOf(o[0].toString()));
        }
        //渠道标净新增本金
        //Integer channelNetAddPrincipal = channelBorrowPrincipal - channelRepaymentPrincipal;
        Map<Integer, Integer> channelIncreaseMap = Maps.newHashMap();

        for (Map.Entry<Integer, Integer> map : channelBorrowMap.entrySet()) {
            Integer temp = 0;
            if (channelRepaymentMap.containsKey(map.getKey())) {
                temp = channelRepaymentMap.get(map.getKey());
            }
            channelIncreaseMap.put(map.getKey(), temp - map.getValue());
        }

        //渠道标待收本金
        List<Object[]> channelWaitCollectionPrincipal = borrowRepository.findCarWaitCollectionPrincipal(4, startDate, endDate);
        Map<Integer, Integer> channelWaitCollectionPrincipalMap = Maps.newHashMap();
        for (Object[] o : channelWaitCollectionPrincipal) {
            channelWaitCollectionPrincipalMap.put((Integer) o[1], Integer.valueOf(o[0].toString()));
        }

        /**
         * 数据入库
         */
        /**
         * 网站余额
         */
        Count count = new Count();
        Gson gson = new Gson();
        Map<String, Long> zfhBalance = Maps.newHashMap();
        zfhBalance.put("zfhUseMoney", zfhUseMoney);
        zfhBalance.put("zfhNoUseMoney", zfhNoUseMoney);
        zfhBalance.put("zfhFinacePlanMoney", zfhFinacePlanMoney);
        Map<String, Long> adminBalance = Maps.newHashMap();
        Map<String, Long> gfbBalance = Maps.newHashMap();
        Map<Object, Object> siteBalance = Maps.newHashMap();

        /**
         * 网站余额gfb
         */
        Asset gfb = assetRepository.findUserIdAndUpDated(GFB, endDate);
        if (!ObjectUtils.isEmpty(gfb)) {
            //可用余额
            gfbBalance.put("gfbUseMoney", gfb.getUseMoney());
            //冻结金额
            gfbBalance.put("gfbNoUseMoney", gfb.getNoUseMoney());
            //理财计划金额
            gfbBalance.put("gfbFinacePlanMoney", gfb.getFinancePlanMoney());
            siteBalance.put("gfbBalance", gfbBalance);
        } else if (ObjectUtils.isEmpty(gfb)) {
            //可用余额
            gfbBalance.put("gfbUseMoney", 0L);
            //冻结金额
            gfbBalance.put("gfbNoUseMoney", 0L);
            //理财计划金额
            gfbBalance.put("gfbFinacePlanMoney", 0L);
            siteBalance.put("gfbBalance", gfbBalance);
        }


        /**
         * 网站余额admin
         */
        Asset admin = assetRepository.findUserIdAndUpDated(ADMIN, endDate);

        if (!ObjectUtils.isEmpty(admin)) {
            //可用余额
            adminBalance.put("adminUseMoney", admin.getUseMoney());
            //冻结金额
            adminBalance.put("adminNoUseMoney", admin.getNoUseMoney());
            //理财计划金额
            adminBalance.put("adminFinacePlanMoney", admin.getFinancePlanMoney());
            siteBalance.put("zfhBalance", zfhBalance);
        } else if (ObjectUtils.isEmpty(admin)) {
            //可用余额
            adminBalance.put("adminUseMoney", 0L);
            //冻结金额
            adminBalance.put("adminNoUseMoney", 0L);
            //理财计划金额
            adminBalance.put("adminFinacePlanMoney", 0L);
            siteBalance.put("zfhBalance", zfhBalance);
        }
        Map<String, Long> othersBalance = Maps.newHashMap();
        othersBalance.put("otherUserMoney", Long.valueOf(otherUserMoney));
        othersBalance.put("otherNoUserMoney", Long.valueOf(otherNoUserMoney));
        othersBalance.put("otherFinacePlanMoney", Long.valueOf(otherFinacePlanMoney));
        siteBalance.put("adminBalance", adminBalance);
        siteBalance.put("othersBalance", othersBalance);
        Object obj = siteBalance;
        Map<Object, Object> info = Maps.newHashMap();
        info.put("siteBalance", obj);
        String site = gson.toJson(info);
        count.setSiteBalance(site);

        /**
         * 存管账户余额
         */
        Map<String, Long> accountBalance = Maps.newHashMap();
        if (!CollectionUtils.isEmpty(account_zfh)) {
            accountBalance.put("zfh", Long.valueOf(account_zfh.get(0)));
            accountBalance.put("gfb", Long.valueOf(account_zfh.get(1)));
            accountBalance.put("admin", Long.valueOf(account_zfh.get(2)));
        } else {
            accountBalance.put("zfh", 0L);
            accountBalance.put("gfb", 0L);
            accountBalance.put("admin", 0L);
        }
        if (ObjectUtils.isEmpty(account_others)) {
            accountBalance.put("others", 0L);
        } else {
            accountBalance.put("others", Long.valueOf(account_others.get(0)));
        }

        Map<Object, Object> account = Maps.newHashMap();
        account.put("account", accountBalance);
        String accoun = gson.toJson(account);

        count.setAccountBalance(accoun);
        count.setNetWaitCollectionPrincipal(Long.valueOf(waitCollectionPrincipal));
        count.setNetWaitCollectionInterest(Long.valueOf(waitCollectionInterest));
        count.setNetWaitRepaymentPrincipal(Long.valueOf(waitRepaymentPrincipal));
        count.setNetWaitRepaymentInterest(Long.valueOf(waitRepaymentInterest));
        count.setNetAdvanceNoPrincipal(Long.valueOf(advanceNoPrincipal));
        count.setNetAdvanceNoInterest(Long.valueOf(advanceNoInterest));
        /**
         * 车贷标
         */
        count.setCarWaitRepayment(gson.toJson(carWaitRepayment));
        count.setChannelWaitRepayment(gson.toJson(channelWaitRepayment));
        count.setCarWaitCollection(gson.toJson(carWaitCollection));
        count.setChannelWaitCollection(gson.toJson(channelWaitCollection));

        count.setNetBorrowPrincipal(Long.valueOf(borrowPrincipal));
        count.setNetRepaymentPrincipal(Long.valueOf(repaymentPrincipal));
        count.setNetAdvancePrincipal(Long.valueOf(advancePrincipal));
        count.setNetAdvanceYesPrincipal(Long.valueOf(advanceYesPrincipal));
        count.setNetNetAdvancePrincipal(Long.valueOf(netAdvancePrincipal));
        count.setNetNetIncreasePrincipal(Long.valueOf(netAddPrincipal));
        count.setWaitCollectionPrincipal(Long.valueOf(netWaitCollectionPrincipal));
        count.setCarBorrowPrincipal(gson.toJson(carBorrowMap));
        count.setCarRepaymentPrincipal(gson.toJson(carRepaymentMap));
        count.setCarIncreasePrincipal(gson.toJson(carIncreaseMap));
        count.setCarWaitCollectionPrincipal(gson.toJson(carWaitCollectionPrincipalMap));
        count.setChannelBorrowPrincipal(gson.toJson(channelBorrowMap));
        count.setChannelRepaymentPrincipal(gson.toJson(channelRepaymentMap));
        count.setChannelIncreasePrincipal(gson.toJson(channelIncreaseMap));
        count.setChannelWaitCollectionPrincipal(gson.toJson(channelWaitCollectionPrincipalMap));
        count.setCreateTime(new Date());
        count.setCountDate(DateHelper.beginOfMonth(DateHelper.subMonths(date, 1)));
        //如果统计的年月份已经存在,则更新统计的数据
        Count data = countRepository.findByCountDate(DateHelper.beginOfMonth(DateHelper.subMonths(new Date(), 1)));
        if (!ObjectUtils.isEmpty(data)) {
            count.setId(data.getId());
            count.setCreateTime(data.getCreateTime());
            count.setUpdateTime(new Date());
            countRepository.save(count);
        } else {
            //如果统计的年月份不存在,则直接插入统计的数据
            count.setUpdateTime(new Date());
            countRepository.save(count);
        }


    }
}
