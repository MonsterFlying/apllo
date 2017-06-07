package com.gofobao.framework.member.service.impl;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.NumberHelper;
import com.gofobao.framework.member.entity.BrokerBouns;
import com.gofobao.framework.member.entity.Users;
import com.gofobao.framework.member.repository.BrokerBounsRepository;
import com.gofobao.framework.member.repository.UsersRepository;
import com.gofobao.framework.member.service.InviteFriendsService;
import com.gofobao.framework.member.vo.request.VoFriendsReq;
import com.gofobao.framework.member.vo.response.FriendsTenderInfo;
import com.gofobao.framework.member.vo.response.InviteAwardStatistics;
import com.gofobao.framework.member.vo.response.InviteFriends;
import com.gofobao.framework.tender.entity.Tender;
import com.gofobao.framework.tender.repository.TenderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by admin on 2017/6/6.
 */
@Component
public class InviteFriendServiceImpl implements InviteFriendsService {

    @Autowired
    private BrokerBounsRepository brokerBounsRepository;

    @Autowired
    private UsersRepository usersRepository;
    @Autowired
    private TenderRepository tenderRepository;

    @Override
    public List<InviteFriends> list(VoFriendsReq voFriendsReq) {
        BrokerBouns brokerBouns = new BrokerBouns();
        brokerBouns.setUserId(voFriendsReq.getUserId());
        Example<BrokerBouns> example = Example.of(brokerBouns);
        Page<BrokerBouns> brokerBounss = brokerBounsRepository.findAll(example,
                new PageRequest(
                        voFriendsReq.getPageIndex(),
                        voFriendsReq.getPageSize(),
                        new Sort(Sort.Direction.DESC, "createdAt")));
        List<BrokerBouns> bounsList = brokerBounss.getContent();
        if (CollectionUtils.isEmpty(bounsList)) {
            return Collections.EMPTY_LIST;
        }
        List<InviteFriends> friendsList = new ArrayList<>();
        bounsList.stream().forEach(p -> {
            InviteFriends friends = new InviteFriends();
            friends.setCreatedAt(DateHelper.dateToString(p.getCreatedAt()));
            friends.setMoney(NumberHelper.to2DigitString(p.getBounsAward() / 100));
            friends.setLeave(p.getLevel());
            friends.setScale(NumberHelper.to2DigitString(p.getAwardApr() / 100));
            friendsList.add(friends);
        });
        return Optional.ofNullable(friendsList).orElse(Collections.EMPTY_LIST);
    }

    @Override
    public InviteAwardStatistics query(Long userId) {

        Specification<BrokerBouns> specification = Specifications.<BrokerBouns>and()
                .eq("userId", userId)
                .build();
        List<BrokerBouns> brokerBounss = brokerBounsRepository.findAll(specification);
        if (CollectionUtils.isEmpty(brokerBounss)) {
            return new InviteAwardStatistics();
        }
        Date yestodayDate = DateHelper.addDays(new Date(), 1);
        Long yestodayBegin = DateHelper.beginOfDate(yestodayDate).getTime();
        Long yestodayEnd = DateHelper.endOfDate(yestodayDate).getTime();


        List<BrokerBouns> yestodayBroker = brokerBounss.stream()
                .filter(p ->
                        p.getCreatedAt().getTime() <= yestodayEnd && p.getCreatedAt().getTime() >= yestodayBegin)
                .collect(Collectors.toList());


        InviteAwardStatistics inviteAwardStatistics = new InviteAwardStatistics();
        //邀请总人数
        inviteAwardStatistics.setCountNum(brokerBounss.size());

        //昨日奖励
        if (CollectionUtils.isEmpty(yestodayBroker)) {
            inviteAwardStatistics.setYesterdayAward("0.00");
        } else {
            Integer yestodayBounsAward = yestodayBroker.stream().mapToInt(w -> w.getBounsAward()).sum();
            inviteAwardStatistics.setYesterdayAward(NumberHelper.to2DigitString(yestodayBounsAward / 100));
        }

        //总奖励
        Integer sumAwad = brokerBounss.stream().mapToInt(w -> w.getBounsAward()).sum();
        inviteAwardStatistics.setSumAward(NumberHelper.to2DigitString(sumAwad / 100));

        return inviteAwardStatistics;
    }

    /**
     *
     * @param voFriendsReq
     * @return
     */
    @Override
    public List<FriendsTenderInfo> inviteUserFirstTender(VoFriendsReq voFriendsReq) {
        Page<Users> usersPage = usersRepository.findByParentId(voFriendsReq.getUserId().intValue(),
                new PageRequest(
                        voFriendsReq.getPageIndex(),
                        voFriendsReq.getPageSize(), new Sort(Sort.Direction.DESC, "createdAt")));
        List<Users> usersList = usersPage.getContent();
        if (CollectionUtils.isEmpty(usersList)) {
            return Collections.EMPTY_LIST;
        }
        Set<Long> userArray = usersList.stream()
                .map(p -> p.getId())
                .collect(Collectors.toSet());

        List<Tender> tenderList = tenderRepository.findUserFirstTender(new ArrayList(userArray));

        Map<Long, Tender> tenderMap = tenderList.stream()
                .collect(Collectors.toMap(Tender::getUserId, a -> a));

        List<FriendsTenderInfo> tenderInfoList=new ArrayList<>();
        usersList.stream().forEach(p->{
            FriendsTenderInfo info=new FriendsTenderInfo();
            info.setUserName(StringUtils.isEmpty(p.getPhone())?p.getUsername():p.getPhone());
            info.setRegisterTime(DateHelper.dateToString(p.getCreatedAt()));
            Tender tender=tenderMap.get(p.getId());
            if(ObjectUtils.isEmpty(tender)){
                info.setFirstTenderTime("---");
            }else{
                info.setFirstTenderTime(DateHelper.dateToString(tender.getCreatedAt()));
            }
            tenderInfoList.add(info);
        });
        return Optional.ofNullable(tenderInfoList).orElse(Collections.EMPTY_LIST);
    }
}
