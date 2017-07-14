package com.gofobao.framework.lend.service.impl;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.asset.entity.Asset;
import com.gofobao.framework.asset.service.AssetService;
import com.gofobao.framework.borrow.contants.BorrowContants;
import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.repository.BorrowRepository;
import com.gofobao.framework.common.page.Page;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.StringHelper;
import com.gofobao.framework.helper.project.UserHelper;
import com.gofobao.framework.lend.contants.LendContants;
import com.gofobao.framework.lend.entity.Lend;
import com.gofobao.framework.lend.entity.LendBlacklist;
import com.gofobao.framework.lend.repository.LendBlacklistRepository;
import com.gofobao.framework.lend.repository.LendRepository;
import com.gofobao.framework.lend.service.LendService;
import com.gofobao.framework.lend.vo.request.VoUserLendReq;
import com.gofobao.framework.lend.vo.response.LendInfo;
import com.gofobao.framework.lend.vo.response.LendInfoList;
import com.gofobao.framework.lend.vo.response.UserLendInfo;
import com.gofobao.framework.lend.vo.response.VoViewLend;
import com.gofobao.framework.member.entity.UserCache;
import com.gofobao.framework.member.entity.Users;
import com.gofobao.framework.member.repository.UsersRepository;
import com.gofobao.framework.member.service.UserCacheService;
import com.gofobao.framework.repayment.entity.BorrowRepayment;
import com.gofobao.framework.repayment.repository.BorrowRepaymentRepository;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
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
    private BorrowRepository borrowRepository;

    @Autowired
    private BorrowRepaymentRepository borrowRepaymentRepository;


    @Autowired
    private LendBlacklistRepository blacklistRepository;

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
    public Map<String, Object> list(Page page) {
        Map<String, Object> resultMaps = Maps.newHashMap();

        org.springframework.data.domain.Page<Lend> lends = lendRepository.findAll(
                new PageRequest(
                        page.getPageIndex(),
                        page.getPageSize(),
                        new Sort(Sort.Direction.DESC, "createdAt")));
        List<Lend> lendList = lends.getContent();
        Long totalCount = lends.getTotalElements();

        resultMaps.put("totalCount", totalCount);
        if (CollectionUtils.isEmpty(lendList)) {
            resultMaps.put("lends", lendList);
            return resultMaps;
        }
        Set<Long> userIds = lendList.stream()
                .map(w -> w.getUserId())
                .collect(Collectors.toSet());
        List<Users> usersList = usersRepository.findByIdIn(new ArrayList(userIds));

        Map<Long, Users> usersMap = usersList.stream()
                .collect(Collectors.toMap(Users::getId, Function.identity()));
        List<VoViewLend> lendListRes = Lists.newArrayList();
        lendList.stream().forEach(p -> {
            VoViewLend lend = new VoViewLend();
            lend.setLendId(p.getId());
            lend.setApr(StringHelper.formatMon(p.getApr() / 100D) + BorrowContants.PERCENT);
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
            lend.setReleaseAt(DateHelper.dateToString(p.getCreatedAt()));
            lend.setCollectionAt(DateHelper.dateToString(p.getRepayAt()));
            lend.setSpend(Double.parseDouble(StringHelper.formatMon(p.getMoneyYes() / new Double(p.getMoney()))));
            lend.setLimit(p.getTimeLimit());
            lend.setStatus(p.getStatus());
            lendListRes.add(lend);
        });
        resultMaps.put("lends", lendListRes);
        return resultMaps;
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
        lendInfo.setApr(StringHelper.formatMon(lend.getApr() / 100D));
        lendInfo.setId(lend.getId());
        //起投金额
        lendInfo.setStartMoneyHide(lend.getLowest());
        lendInfo.setStartMoney(StringHelper.formatMon(lend.getLowest() / 100D));

        if (lend.getStatus() == LendContants.STATUS_NO) {
            lendInfo.setSurplusMoney(StringHelper.formatMon((lend.getMoney() - lend.getMoneyYes()) / 100D));
            lendInfo.setSurplusMoneyHide(lend.getMoney() - lend.getMoneyYes());
        } else {
            lendInfo.setSurplusMoney(StringHelper.formatMon(lend.getMoney() / 100D));
            lendInfo.setSurplusMoneyHide(lend.getMoney());
        }
        if (lend.getTimeLimit() == BorrowContants.REPAY_FASHION_ONCE) {
            lendInfo.setTimeLimit(lend.getTimeLimit() + BorrowContants.DAY);
        } else {
            lendInfo.setTimeLimit(lend.getTimeLimit() + BorrowContants.MONTH);
        }
        lendInfo.setCollectionAt(DateHelper.dateToString(lend.getRepayAt()));

        Users users = usersRepository.findOne(userId);
        lendInfo.setUserName(StringUtils.isEmpty(users.getUsername()) ? UserHelper.hideChar(users.getPhone(), UserHelper.PHONE_NUM) : UserHelper.hideChar(users.getUsername(), UserHelper.USERNAME_NUM));
        Asset asset = assetService.findByUserId(userId); //查询会员资产信息
        if (ObjectUtils.isEmpty(asset)) {
            return null;
        }

        UserCache userCache = userCacheService.findById(userId);  //查询会员缓存信息
        if (ObjectUtils.isEmpty(userCache)) {
            return null;
        }
        lendInfo.setRepayAtYes(StringUtils.isEmpty(lend.getRepayAt()) ? "----" : DateHelper.dateToString(lend.getRepayAt()));
        lendInfo.setStatus(lend.getStatus());
        Long useMoney = asset.getUseMoney();
        Long waitCollectionPrincipal = userCache.getWaitCollectionPrincipal();
        Long payment = asset.getPayment();
        int netWorthQuota = new Double((useMoney + waitCollectionPrincipal) * 0.8 - payment).intValue();//计算净值额度
        lendInfo.setEquityLimit(StringHelper.formatMon(netWorthQuota / 100D));
        lendInfo.setEquityLimitHide(netWorthQuota);
        return lendInfo;
    }


    @Override
    public List<LendInfoList> infoList(Long userId, Long lendId) {
        Specification lendSpecification=Specifications.<Lend>and()
                .eq("id",lendId)
                .eq("userId",userId)
                .build();
        Lend lend=lendRepository.findOne(lendSpecification);
        if(ObjectUtils.isEmpty(lend)){
            return Collections.EMPTY_LIST;
        }
        Specification borrowSpecification = Specifications.<Borrow>and()
                .eq("lendId", lendId)
                .build();
        Borrow borrow = borrowRepository.findOne(borrowSpecification);

        if (ObjectUtils.isEmpty(borrow)) {
            return Collections.EMPTY_LIST;
        }
        List<LendBlacklist>blacklists=blacklistRepository.findByUserId(userId);
        Map<Long,LendBlacklist> blacklistMap=blacklists.stream().collect(Collectors.toMap(LendBlacklist::getBlackUserId,Function.identity()));
        List<BorrowRepayment> borrowRepayments = borrowRepaymentRepository.findByBorrowId(borrow.getId());
        List<Long> userIds = borrowRepayments.stream().map(p -> p.getUserId()).collect(Collectors.toList());
        List<Users> users = usersRepository.findByIdIn(new ArrayList(userIds));
        Map<Long, Users> usersMap = users.stream().collect(Collectors.toMap(Users::getId, Function.identity()));
        List<LendInfoList> lendInfos = Lists.newArrayList();
        borrowRepayments.stream().forEach(p -> {
            LendInfoList lendInfo = new LendInfoList();
            Users tempUser = usersMap.get(p.getUserId());
            lendInfo.setToUserBackList(ObjectUtils.isEmpty(blacklistMap.get(p.getUserId()))?false:true);
            lendInfo.setUserName(StringUtils.isEmpty(tempUser.getUsername()) ? tempUser.getPhone() : tempUser.getUsername());
            lendInfo.setUserId(tempUser.getId());
            lendInfo.setApr(StringHelper.formatMon(borrow.getApr()/100D));
            lendInfo.setMoney(StringHelper.formatMon(p.getRepayMoney()/100D));
            lendInfo.setRepaymentId(p.getId());
            lendInfo.setRepayAtYes(ObjectUtils.isEmpty(p.getRepayAtYes())?"":DateHelper.dateToString(p.getRepayAtYes()));
            lendInfo.setRepayAt(DateHelper.dateToString(p.getRepayAt()));
            lendInfo.setTimeLimit(lend.getTimeLimit());
            lendInfos.add(lendInfo);
        });
        return lendInfos;
    }

    @Override
    public Map<String, Object> queryUser(VoUserLendReq voUserLendReq) {
        Map<String, Object> resultMaps=Maps.newHashMap();
        Specification specification = Specifications.<Lend>and()
                .eq("userId", voUserLendReq.getUserId())
                .build();
        org.springframework.data.domain.Page lendPage = lendRepository.findAll(specification,
                new PageRequest(
                        voUserLendReq.getPageIndex(),
                        voUserLendReq.getPageSize(),
                        new Sort(Sort.Direction.DESC, "id")));
        List<Lend> lendList = lendPage.getContent();
        Long totalCount=lendPage.getTotalElements();
        resultMaps.put("totalCount",totalCount);
        if (CollectionUtils.isEmpty(lendList)) {
            resultMaps.put("lendList",new ArrayList<>(0));
            return resultMaps;
        }
        List<UserLendInfo> userLendInfos = Lists.newArrayList();
        lendList.stream().forEach(p -> {
            UserLendInfo userLendInfo = new UserLendInfo();
            userLendInfo.setLendId(p.getId());
            userLendInfo.setApr(StringHelper.formatMon(p.getApr() / 100D));
            userLendInfo.setRepayAt(DateHelper.dateToString(p.getRepayAt()));
            userLendInfo.setLendMoney(StringHelper.formatMon(p.getMoney() / 100D));
            userLendInfo.setTitle(p.getTimeLimit() + BorrowContants.DAY + "," + DateHelper.dateToString(p.getCreatedAt()));
            userLendInfo.setSurplusMoney(StringHelper.formatMon(p.getMoney() - p.getMoneyYes() / 100D));
            String statusStr = p.getStatus() == LendContants.STATUS_NO ? LendContants.STATUS_NO_STR : LendContants.STATUS_YES_STR;
            userLendInfo.setStatusStr(statusStr);
            userLendInfo.setStatus(p.getStatus());
            userLendInfo.setTimeLimit(p.getTimeLimit());
            userLendInfo.setCollectionAt(DateHelper.dateToString(p.getRepayAt()));
            userLendInfos.add(userLendInfo);
        });

        resultMaps.put("lendList",userLendInfos);
        return resultMaps;
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

    public Lend save(Lend lend) {
        return lendRepository.save(lend);
    }

    public List<Lend> save(List<Lend> lendList) {
        return lendRepository.save(lendList);
    }
}
