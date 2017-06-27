package com.gofobao.framework.lend.biz.impl;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.asset.entity.Asset;
import com.gofobao.framework.asset.service.AssetService;
import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.service.BorrowService;
import com.gofobao.framework.common.page.Page;
import com.gofobao.framework.common.rabbitmq.MqConfig;
import com.gofobao.framework.common.rabbitmq.MqHelper;
import com.gofobao.framework.common.rabbitmq.MqQueueEnum;
import com.gofobao.framework.common.rabbitmq.MqTagEnum;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.StringHelper;
import com.gofobao.framework.lend.biz.LendBiz;
import com.gofobao.framework.lend.entity.Lend;
import com.gofobao.framework.lend.entity.LendBlacklist;
import com.gofobao.framework.lend.service.LendBlackListService;
import com.gofobao.framework.lend.service.LendService;
import com.gofobao.framework.lend.vo.request.*;
import com.gofobao.framework.lend.vo.response.*;
import com.gofobao.framework.member.entity.UserCache;
import com.gofobao.framework.member.entity.Users;
import com.gofobao.framework.member.service.UserCacheService;
import com.gofobao.framework.member.service.UserService;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by admin on 2017/6/6.
 */
@Slf4j
@Service
public class LendBizImpl implements LendBiz {

    final Gson GSON = new Gson();

    @Autowired
    private LendService lendService;
    @Autowired
    private AssetService assetService;
    @Autowired
    private UserService userService;
    @Autowired
    private UserCacheService userCacheService;
    @Autowired
    private LendBlackListService lendBlackListService;
    @Autowired
    private BorrowService borrowService;
    @Autowired
    private MqHelper mqHelper;


    /**
     * 出借列表
     *
     * @param page
     * @return
     */
    @Override
    public ResponseEntity<VoViewLendListWarpRes> list(Page page) {
        try {
            VoViewLendListWarpRes warpRes = VoBaseResp.ok("查询成功", VoViewLendListWarpRes.class);
            List<VoViewLend> lends = lendService.list(page);
            warpRes.setVoViewLends(lends);
            return ResponseEntity.ok(warpRes);
        } catch (Exception e) {
            log.info("LendBizImpl list query fail", e);
            return ResponseEntity.badRequest().body(VoBaseResp.ok("查询失败", VoViewLendListWarpRes.class));
        }
    }

    /**
     * @param userId
     * @param lendId
     * @return
     */
    @Override
    public ResponseEntity<VoViewLendInfoWarpRes> info(Long userId, Long lendId) {
        try {
            VoViewLendInfoWarpRes warpRes = VoBaseResp.ok("查询成功", VoViewLendInfoWarpRes.class);
            LendInfo lends = lendService.info(userId, lendId);
            if (ObjectUtils.isEmpty(lends)) {
                return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "非法请求", VoViewLendInfoWarpRes.class));
            }
            warpRes.setLendInfo(lends);
            return ResponseEntity.ok(warpRes);
        } catch (Exception e) {
            log.info("LendBizImpl detail query fail", e);
            return ResponseEntity.badRequest().body(VoBaseResp.ok("查询失败", VoViewLendInfoWarpRes.class));
        }
    }

    /**
     * @param voUserLendReq
     * @return
     */
    @Override
    public ResponseEntity<VoViewUserLendInfoWarpRes> byUserId(VoUserLendReq voUserLendReq) {
        try {
            VoViewUserLendInfoWarpRes warpRes = VoBaseResp.ok("查询成功", VoViewUserLendInfoWarpRes.class);
            List<UserLendInfo> lends = lendService.queryUser(voUserLendReq);
            warpRes.setLendInfos(lends);
            return ResponseEntity.ok(warpRes);
        } catch (Exception e) {
            log.info("LendBizImpl detail query fail", e);
            return ResponseEntity.badRequest().body(VoBaseResp.ok("查询失败", VoViewUserLendInfoWarpRes.class));
        }
    }

    /**
     * 发布有草出借
     *
     * @param voCreateLend
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<VoBaseResp> create(VoCreateLend voCreateLend) {
        Double money = voCreateLend.getMoney();//借款金额（分）
        Double lowest = voCreateLend.getLowest();
        Integer timeLimit = voCreateLend.getTimeLimit();
        Date repayAt = DateHelper.stringToDate(voCreateLend.getRepayAt());
        Date nowDate = new Date();
        Long userId = voCreateLend.getUserId();
        Integer apr = voCreateLend.getApr(); //年利率 2450

        if (lowest > money) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "借款金额小于起借金额"));
        }

        Date countRepayDate = DateHelper.addDays(new Date(), timeLimit);//计算截止时间
        if (!DateHelper.isSameDay(repayAt, countRepayDate)) {//比较截止日期与还款期限是否是同一天
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "借款还款时间不能是今天或今天以前的时间"));
        }

        Asset asset = assetService.findByUserIdLock(userId);
        Preconditions.checkNotNull(asset, "用户资产为空!");

        if (money > asset.getUseMoney()) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "借款金额不能大于可用金额"));
        }

        //查询当前会员的借款记录
        Specification<Lend> ls = Specifications
                .<Lend>and()
                .eq("userId", userId)
                .eq("status", 0)
                .build();

        if (lendService.count(ls) > 0) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "您已经有一个借款信息"));
        }

        //进行借款的新增
        Lend lend = new Lend();
        lend.setUserId(userId);
        lend.setMoney(money.intValue());
        lend.setLowest(lowest.intValue());
        lend.setApr(apr);
        lend.setTimeLimit(timeLimit);
        lend.setMoneyYes(0);
        lend.setCreatedAt(nowDate);
        lend.setRepayFashion(1);
        lend.setCreatedAt(nowDate);
        lend.setRepayAt(repayAt);
        lend.setUpdatedAt(nowDate);

        lendService.insert(lend);
        return ResponseEntity.ok(VoBaseResp.ok("有草出借创建成功!"));
    }

    /**
     * 结束有草出借
     *
     * @param voEndLend
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<VoBaseResp> end(VoEndLend voEndLend) {
        long userId = voEndLend.getUserId();
        long lendId = voEndLend.getLendId();

        Specification<Lend> ls = Specifications
                .<Lend>and()
                .eq("userId", userId)
                .eq("id", lendId)
                .build();

        long count = lendService.count(ls);
        if (count <= 0) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "出借不存在或者，您无权限操作！"));
        }

        Lend tempLend = lendService.findByIdLock(lendId);
        tempLend.setStatus(1);
        tempLend.setId(voEndLend.getLendId());
        lendService.updateById(tempLend);
        return ResponseEntity.ok(VoBaseResp.ok("取消借款成功!"));
    }

    /**
     * 有草出借摘草
     *
     * @param voLend
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<VoBaseResp> lend(VoLend voLend) {
        int rs = 1;
        long userId = voLend.getUserId();
        Double money = voLend.getMoney();
        long lendId = voLend.getLendId();

        Lend lend = lendService.findByIdLock(lendId);
        Preconditions.checkNotNull(lend, "您看到的有草出借消失了!");

        long lendUserId = lend.getUserId();
        Asset asset = assetService.findByUserIdLock(lendUserId);
        Preconditions.checkNotNull(lend, "用户资产记录获取失败!");

        Users user = userService.findById(userId);
        Preconditions.checkNotNull(lend, "用户记录获取失败!");

        if (user.getIsLock()) { //判断用户是否锁定
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "账户已被锁定，请联系客服人员!"));
        }

        UserCache userCache = userCacheService.findById(userId);
        Preconditions.checkNotNull(lend, "用户缓存记录获取失败!");
        Asset userAsset = assetService.findByUserIdLock(userId);
        Preconditions.checkNotNull(lend, "用户资产记录获取失败!");
        double totalMoney = (userAsset.getUseMoney() + userCache.getWaitCollectionPrincipal()) * 0.8 - userAsset.getPayment();  // 用户净值金额
        if (money > totalMoney) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "摘草金额大于净值额度"));
        }

        if (lend.getStatus() != 0) { //该出借信息已结束
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "出借已结束"));
        }

        if (StringHelper.toString(lend.getUserId()).equals(StringHelper.toString(userId))) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "该出借是本人发布的"));
        }

        Specification<LendBlacklist> lbs = Specifications
                .<LendBlacklist>and()
                .eq("userId", lend.getUserId())
                .eq("blackUserId", userId)
                .build();

        long count = lendBlackListService.count(lbs);
        if (count > 0) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "当前会员被拉黑"));
        }

        if (money > (lend.getMoney() - lend.getMoneyYes())) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "借入金额不能大于剩余金额"));
        }

        if (money < lend.getLowest()) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "借入金额不能小于起借金额"));
        }

        Date nowDate = new Date();
        Date endDate = DateHelper.endOfDate(lend.getCreatedAt());
        if (((lend.getMoney() - lend.getMoneyYes()) < lend.getLowest())
                || asset.getUseMoney() < money
                || nowDate.getTime() > endDate.getTime()
                ) {
            Lend lend1 = lendService.findById(lendId);
            lend1.setId(lendId);
            lend1.setStatus(1);
            lendService.updateById(lend1);

            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "出借已结束"));
        }

        Borrow tempBorrow = new Borrow();
        tempBorrow.setType(1);
        tempBorrow.setRepayFashion(1);
        tempBorrow.setTimeLimit(lend.getTimeLimit());
        tempBorrow.setMoney(money.intValue());
        tempBorrow.setApr(lend.getApr());
        tempBorrow.setLowest(lend.getLowest());
        tempBorrow.setValidDay(1);
        tempBorrow.setName((StringUtils.isEmpty(user.getUsername()) ? user.getPhone() : user.getUsername()) + "-" + lend.getTimeLimit() + "天-" + DateHelper.dateToString(lend.getRepayAt(), "HH:mm"));
        tempBorrow.setUserId(userId);
        tempBorrow.setLendId(lend.getId());
        tempBorrow.setCreatedAt(new Date());
        tempBorrow.setMost(0);
        tempBorrow.setMostAuto(0);
        tempBorrow.setAwardType(0);
        tempBorrow.setAward(0);
        tempBorrow.setDescription("");
        tempBorrow.setPassword("");
        tempBorrow.setMoneyYes(0);
        tempBorrow.setTenderCount(0);

        borrowService.insert(tempBorrow);
        lend.setMoneyYes(lend.getMoneyYes() + money.intValue());
        if ((lend.getMoney() - lend.getMoneyYes()) < lend.getLowest()) {
            lend.setStatus(1);
        }

        lendService.updateById(lend);

        //初审
        MqConfig mqConfig = new MqConfig();
        mqConfig.setQueue(MqQueueEnum.RABBITMQ_BORROW);
        mqConfig.setTag(MqTagEnum.FIRST_VERIFY);
        ImmutableMap<String, String> body = ImmutableMap
                .of(MqConfig.MSG_BORROW_ID, StringHelper.toString(tempBorrow.getId()), MqConfig.MSG_TIME, DateHelper.dateToString(new Date()));
        mqConfig.setMsg(body);
        try {
            log.info(String.format("borrowBizImpl firstVerify send mq %s", GSON.toJson(body)));
            mqHelper.convertAndSend(mqConfig);
        } catch (Exception e) {
            log.error("borrowBizImpl firstVerify send mq exception", e);
        }

        return ResponseEntity.ok(VoBaseResp.ok("摘草成功!"));
    }

    /**
     * 获取当前用户黑名单列表
     *
     * @param voGetLendBlacklists
     * @return
     * @throws Exception
     */
    public ResponseEntity<VoViewLendBlacklists> getLendBlacklists(VoGetLendBlacklists voGetLendBlacklists) {
        VoViewLendBlacklists voViewLendBlacklists = new VoViewLendBlacklists();
        List<VoLendBlacklist> voLendBlacklists = new ArrayList<>();
        int pageIndex = voGetLendBlacklists.getPageIndex();
        int pageSize = voGetLendBlacklists.getPageSize();
        do {

            Specification<LendBlacklist> lbs = Specifications
                    .<LendBlacklist>and()
                    .eq("userId", voGetLendBlacklists.getUserId())
                    .build();

            Pageable pageable = new PageRequest(pageIndex, pageSize, new Sort(Sort.Direction.ASC));

            List<LendBlacklist> lendBlacklists = lendBlackListService.findList(lbs, pageable);
            if (CollectionUtils.isEmpty(lendBlacklists)) {
                break;
            }

            voLendBlacklists = GSON.fromJson(GSON.toJson(lendBlacklists), new TypeToken<LendBlacklist>() {
            }.getType());

        } while (false);
        voViewLendBlacklists.setBlacklists(voLendBlacklists);
        voViewLendBlacklists.setPageIndex(pageIndex);
        voViewLendBlacklists.setPageSize(pageSize);
        return ResponseEntity.ok(voViewLendBlacklists);
    }

    /**
     * 添加有草出借黑名单
     *
     * @param voAddLendBlacklist
     * @return
     * @throws Exception
     */
    public ResponseEntity<VoBaseResp> addLendBlacklist(VoAddLendBlacklist voAddLendBlacklist) {
        LendBlacklist lendBlacklist = new LendBlacklist();
        lendBlacklist.setBlackUserId(voAddLendBlacklist.getBlackUserId());
        lendBlacklist.setUserId(voAddLendBlacklist.getUserId());
        lendBlacklist.setCreatedAt(new Date());
        lendBlackListService.save(lendBlacklist);
        return ResponseEntity.ok(VoBaseResp.ok("有草出借黑名单添加成功!"));
    }

    /**
     * 移除有草出借黑名单
     *
     * @param voDelLendBlacklist
     * @return
     * @throws Exception
     */
    public ResponseEntity<VoBaseResp> delLendBlacklist(VoDelLendBlacklist voDelLendBlacklist) {
        Specification<LendBlacklist> lbs = Specifications
                .<LendBlacklist>and()
                .eq("userId", voDelLendBlacklist.getUserId())
                .eq("blackUserId", voDelLendBlacklist.getBlackUserId())
                .build();
        List<LendBlacklist> lendBlacklistList = lendBlackListService.findList(lbs);
        if (CollectionUtils.isEmpty(lendBlacklistList)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "有草出借黑名单查询失败!"));
        }

        if (lendBlacklistList.size() == 1) {
            lendBlackListService.delete(lendBlacklistList.get(0));
        }
        return ResponseEntity.ok(VoBaseResp.ok("有草出借黑名单删除成功!"));
    }

    /**
     * 获取有草出借借款列表
     *
     * @param voGetPickLendList
     * @return
     * @throws Exception
     */
    public ResponseEntity<VoViewPickLendList> getPickLendList(VoGetPickLendList voGetPickLendList) {
        List<VoPickLend> voPickLendList = new ArrayList<>();
        long lendId = voGetPickLendList.getLendId();
        int pageIndex = voGetPickLendList.getPageIndex();
        int pageSize = voGetPickLendList.getPageSize();
        do {
            Specification<Lend> ls = Specifications
                    .<Lend>and()
                    .eq("userId", voGetPickLendList.getUserId())
                    .eq("id", lendId)
                    .build();

            List<Lend> lendList = lendService.findList(ls);
            if (CollectionUtils.isEmpty(lendList)) {
                return ResponseEntity
                        .badRequest()
                        .body(VoViewPickLendList.error(VoBaseResp.ERROR, "有草出借不存在!", VoViewPickLendList.class));
            }

            Specification<Borrow> bs = Specifications
                    .<Borrow>and()
                    .eq("lendId", lendId)
                    .build();

            List<Borrow> borrowList = borrowService.findList(bs);
            if (CollectionUtils.isEmpty(borrowList)) {
                return ResponseEntity
                        .badRequest()
                        .body(VoViewPickLendList.error(VoBaseResp.ERROR, "借款不存在!", VoViewPickLendList.class));
            }

            List<Long> borrowUserIds = new ArrayList<>();
            List<Long> userIds = new ArrayList<>();
            for (Borrow borrow : borrowList) {
                borrowUserIds.add(borrow.getUserId());
            }

            Specification<Users> us = Specifications
                    .<Users>and()
                    .in("id", userIds.toArray())
                    .build();

            List<Users> userList = userService.findList(us);//查询会员
            if (CollectionUtils.isEmpty(userList)) {
                return ResponseEntity
                        .badRequest()
                        .body(VoViewPickLendList.error(VoBaseResp.ERROR, "会员记录查询失败!", VoViewPickLendList.class));
            }

            //查询黑名单
            Specification<LendBlacklist> lbs = Specifications
                    .<LendBlacklist>and()
                    .eq("userId", lendList.get(0).getUserId())
                    .in("blackUserId", borrowUserIds.toArray())
                    .build();

            Pageable pageable = new PageRequest(pageIndex, pageSize, new Sort(Sort.Direction.ASC));
            List<LendBlacklist> lendBlacklistList = lendBlackListService.findList(lbs, pageable);

            voPickLendList = GSON.fromJson(GSON.toJson(borrowList), new TypeToken<List<VoPickLend>>() {
            }.getType());

            for (VoPickLend voPickLend : voPickLendList) {
                for (LendBlacklist lendBlacklist : lendBlacklistList) {
                    if (StringHelper.toString(voPickLend.getUserId()).equals(StringHelper.toString(lendBlacklist.getUserId()))) {
                        voPickLend.setIsBlacklist(true);
                        break;
                    } else {
                        voPickLend.setIsBlacklist(false);
                    }
                }

                for (Users user : userList) {
                    if (StringHelper.toString(voPickLend.getUserId()).equals(StringHelper.toString(user.getId()))) {
                        voPickLend.setUsername(user.getUsername());
                        break;
                    }
                }
            }
        } while (false);

        VoViewPickLendList voViewPickLendList = VoViewPickLendList.ok("查询成功!", VoViewPickLendList.class);
        voViewPickLendList.setPickList(voPickLendList);
        voViewPickLendList.setPageIndex(pageIndex);
        voViewPickLendList.setPageSize(pageSize);
        return ResponseEntity.ok(voViewPickLendList);
    }
}
