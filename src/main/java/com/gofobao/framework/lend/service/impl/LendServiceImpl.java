package com.gofobao.framework.lend.service.impl;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.asset.entity.Asset;
import com.gofobao.framework.asset.service.AssetService;
import com.gofobao.framework.borrow.contants.BorrowContants;
import com.gofobao.framework.common.page.Page;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.StringHelper;
import com.gofobao.framework.helper.project.UserHelper;
import com.gofobao.framework.lend.contants.LendContants;
import com.gofobao.framework.lend.entity.Lend;
import com.gofobao.framework.lend.repository.LendRepository;
import com.gofobao.framework.lend.service.LendService;
import com.gofobao.framework.lend.vo.request.VoUserLendReq;
import com.gofobao.framework.lend.vo.response.LendInfo;
import com.gofobao.framework.lend.vo.response.UserLendInfo;
import com.gofobao.framework.lend.vo.response.VoViewLend;
import com.gofobao.framework.member.entity.UserCache;
import com.gofobao.framework.member.entity.Users;
import com.gofobao.framework.member.repository.UsersRepository;
import com.gofobao.framework.member.service.UserCacheService;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by Zeke on 2017/6/1.
 */
@Component
public class LendServiceImpl implements LendService {
    @Autowired
    private LendRepository lendRepository;

    @Autowired
    private AssetService assetService;

    @Autowired
    private UserCacheService userCacheService;

    @Autowired
    private UsersRepository usersRepository;

    public Lend insert(Lend lend) {
        if (ObjectUtils.isEmpty(lend)) {
            return null;
        }
        lend.setId(null);
        return lendRepository.save(lend);
    }

    public boolean updateById(Lend lend) {
        if (ObjectUtils.isEmpty(lend) || ObjectUtils.isEmpty(lend.getId())) {
            return false;
        }
        return !ObjectUtils.isEmpty(lendRepository.save(lend));
    }

    public Lend findByIdLock(Long id) {
        return lendRepository.findById(id);
    }

    public Lend findById(Long id) {
        return lendRepository.findOne(id);
    }

    @Override
    public List<VoViewLend> list(Page page) {
        org.springframework.data.domain.Page<Lend> lends = lendRepository.findAll(
                new PageRequest(
                        page.getPageIndex(),
                        page.getPageSize(),
                        new Sort(Sort.Direction.DESC, "createdAt")));

        List<Lend> lendList = lends.getContent();
        if (CollectionUtils.isEmpty(lendList)) {
            return Collections.EMPTY_LIST;
        }
        Set<Long> userIds = lendList.stream().map(w -> w.getUserId()).collect(Collectors.toSet());
        List<Users> usersList = usersRepository.findByIdIn(new ArrayList(userIds));

        Map<Long, Users> usersMap = usersList.stream().collect(Collectors
                .toMap(Users::getId, Function.identity()));
        List<VoViewLend> lendListRes = Lists.newArrayList();
        lendList.stream().forEach(p -> {
            VoViewLend lend = new VoViewLend();
            lend.setLendId(p.getId());
            lend.setApr(StringHelper.formatMon(p.getApr() / 100D)+ BorrowContants.PERCENT);
            Users user = usersMap.get(p.getUserId());
            String userName = StringUtils.isEmpty(user.getUsername()) ?
                    UserHelper.hideChar(user.getPhone(), UserHelper.PHONE_NUM) :
                    UserHelper.hideChar(user.getUsername(), UserHelper.USERNAME_NUM);
            lend.setUserName(userName);
            lend.setMoney(StringHelper.formatMon(p.getMoney() / 100D));
            if (p.getStatus() == LendContants.STATUS_NO) {
                lend.setStatusStr(LendContants.STATUS_NO_STR);
            } else {
                lend.setSpend(1d);
                lend.setStatusStr(LendContants.STATUS_YES_STR);
            }
            lend.setSpend(Double.parseDouble(StringHelper.formatMon(p.getMoneyYes()/new Double(p.getMoney()))));
            lend.setLimit(p.getTimeLimit());
            lend.setStatus(p.getStatus());
            lendListRes.add(lend);
        });
        return Optional.ofNullable(lendListRes).orElse(Collections.EMPTY_LIST);
    }

    @Override
    public LendInfo info(Long userId, Long lendId) {
        Specification specification = Specifications.<Lend>and()
                .eq("id", lendId)
                .build();
        Lend lend = lendRepository.findOne(specification);
        if (ObjectUtils.isEmpty(lend)) {
            return null;
        }
        LendInfo lendInfo = new LendInfo();
        lendInfo.setApr(StringHelper.formatMon(lend.getApr() / 100d));
        lendInfo.setId(lend.getId());
        lendInfo.setStartMoney(StringHelper.formatMon(lend.getLowest() / 100d));

        if (lend.getStatus() == LendContants.STATUS_NO) {
            lendInfo.setSurplusMoney(StringHelper.formatMon(lend.getMoney() - lend.getMoneyYes()));
        } else {
            lendInfo.setSurplusMoney(StringHelper.formatMon(lend.getMoney()));
        }
        if (lend.getTimeLimit() == BorrowContants.REPAY_FASHION_ONCE) {
            lendInfo.setTimeLimit(lend.getTimeLimit() + BorrowContants.DAY);
        } else {
            lendInfo.setTimeLimit(lend.getTimeLimit() + BorrowContants.MONTH);
        }
        lendInfo.setCollectionAt(DateHelper.dateToString(lend.getRepayAt()));

        Users users = usersRepository.findOne(userId);
        lendInfo.setUserName(StringUtils.isEmpty(users.getUsername()) ? users.getPhone() : users.getUsername());
        Asset asset = assetService.findByUserId(userId); //查询会员资产信息
        if (ObjectUtils.isEmpty(asset)) {
            return null;
        }

        UserCache userCache = userCacheService.findById(userId);  //查询会员缓存信息
        if (ObjectUtils.isEmpty(userCache)) {
            return null;
        }

        lendInfo.setStatus(lend.getStatus());
        Integer useMoney = asset.getUseMoney();
        Integer waitCollectionPrincipal = userCache.getWaitCollectionPrincipal();
        Integer payment = asset.getPayment();
        int netWorthQuota = new Double((useMoney + waitCollectionPrincipal) * 0.8 - payment).intValue();//计算净值额度
        lendInfo.setEquityLimit(StringHelper.formatMon(netWorthQuota / 100d));
        return lendInfo;
    }


    @Override
    public List<UserLendInfo> queryUser(VoUserLendReq voUserLendReq) {
        Specification specification = Specifications.<Lend>and()
                .eq("userId", voUserLendReq.getUserId())
                .build();

        org.springframework.data.domain.Page lendPage = lendRepository.findAll(specification,
                new PageRequest(
                        voUserLendReq.getPageIndex(),
                        voUserLendReq.getPageSize(),
                        new Sort(Sort.Direction.DESC, "id")));
        List<Lend> lendList = lendPage.getContent();
        if (CollectionUtils.isEmpty(lendList)) {
            return Collections.EMPTY_LIST;
        }
        List<UserLendInfo> userLendInfos = Lists.newArrayList();
        lendList.stream().forEach(p -> {
            UserLendInfo userLendInfo = new UserLendInfo();
            userLendInfo.setLendId(p.getId());
            userLendInfo.setApr(StringHelper.formatMon(p.getApr() / 100d));
            userLendInfo.setRepayAt(DateHelper.dateToString(p.getRepayAt()));
            userLendInfo.setLendMoney(StringHelper.formatMon(p.getMoney() / 100d));
            userLendInfo.setTitle(p.getTimeLimit() + BorrowContants.DAY + "," + DateHelper.dateToString(p.getCreatedAt()));
            userLendInfo.setSurplusMoney(StringHelper.formatMon(p.getMoney() - p.getMoneyYes() / 100d));
            String statusStr = p.getStatus() == LendContants.STATUS_NO ? LendContants.STATUS_NO_STR : LendContants.STATUS_YES_STR;
            userLendInfo.setStatusStr(statusStr);
            userLendInfo.setStatus(p.getStatus());
            userLendInfos.add(userLendInfo);
        });
        return Optional.ofNullable(userLendInfos).orElse(Collections.EMPTY_LIST);
    }

    /**
     * 查询列表
     *
     * @param specification
     * @return
     */
    public List<Lend> findList(Specification<Lend> specification) {
        return lendRepository.findAll(specification);
    }

    /**
     * 查询列表
     *
     * @param specification
     * @return
     */
    public List<Lend> findList(Specification<Lend> specification, Sort sort) {
        return lendRepository.findAll(specification, sort);
    }

    /**
     * 查询列表
     *
     * @param specification
     * @return
     */
    public List<Lend> findList(Specification<Lend> specification, Pageable pageable) {
        return lendRepository.findAll(specification, pageable).getContent();
    }

    public long count(Specification<Lend> specification) {
        return lendRepository.count(specification);
    }
}
