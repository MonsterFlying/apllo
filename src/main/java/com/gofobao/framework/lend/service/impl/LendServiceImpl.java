package com.gofobao.framework.lend.service.impl;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.borrow.contants.BorrowContants;
import com.gofobao.framework.common.page.Page;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.NumberHelper;
import com.gofobao.framework.helper.StringHelper;
import com.gofobao.framework.helper.project.UserHelper;
import com.gofobao.framework.lend.contants.LendContants;
import com.gofobao.framework.lend.entity.Lend;
import com.gofobao.framework.lend.repository.LendRepository;
import com.gofobao.framework.lend.service.LendService;
import com.gofobao.framework.lend.vo.response.LendInfo;
import com.gofobao.framework.lend.vo.response.VoViewLend;
import com.gofobao.framework.member.entity.Users;
import com.gofobao.framework.member.repository.UsersRepository;
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
            lend.setApr(NumberHelper.to2DigitString(p.getApr()/100));
            Users user = usersMap.get(p.getUserId());
            String userName = StringUtils.isEmpty(user.getUsername()) ?
                    UserHelper.hideChar(user.getPhone(), UserHelper.PHONE_NUM) :
                    UserHelper.hideChar(user.getUsername(), UserHelper.USERNAME_NUM);
            lend.setUserName(userName);
            lend.setMoney(NumberHelper.to2DigitString(p.getMoney()/100));
            if (p.getStatus() == LendContants.STATUS_NO) {
                lend.setStatusStr(LendContants.STATUS_NO_STR);
            } else {
                lend.setStatusStr(LendContants.STATUS_YES_STR);
            }
            lend.setLimit(p.getTimeLimit());
            lendListRes.add(lend);
        });
        return Optional.ofNullable(lendListRes).orElse(Collections.EMPTY_LIST);
    }

    @Override
    public LendInfo info(Long userId, Long lendId) {
        Specification specification= Specifications.<Lend>and()
                .eq("userId",userId)
                .eq("id",lendId)
                .build();
        Lend lend=lendRepository.findOne(specification);
        LendInfo lendInfo=new LendInfo();
        lendInfo.setApr(StringHelper.formatMon(lend.getApr()/100d));
        lendInfo.setId(lend.getId());
        lendInfo.setStartMoney(StringHelper.formatMon(lend.getLowest()/100d));

        if(lend.getStatus()==LendContants.STATUS_NO){
            lendInfo.setSurplusMoney(StringHelper.formatMon(lend.getMoney()-lend.getMoneyYes()));
        }else{
            lendInfo.setSurplusMoney(StringHelper.formatMon(lend.getMoney()));
        }
        if(lend.getTimeLimit()== BorrowContants.REPAY_FASHION_ONCE){
            lendInfo.setTimeLimit(lend.getTimeLimit()+BorrowContants.DAY);
        }else{
            lendInfo.setTimeLimit(lend.getTimeLimit()+BorrowContants.MONTH);
        }
        lendInfo.setCollectionAt(DateHelper.dateToString(lend.getRepayAt()));
        Users users=usersRepository.findById(lend.getUserId());
        lendInfo.setUserName(StringUtils.isEmpty(users.getUsername())?users.getPhone():users.getUsername());


        return lendInfo;
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
    public List<Lend> findList(Specification<Lend> specification,Sort sort) {
        return lendRepository.findAll(specification,sort);
    }

    /**
     * 查询列表
     *
     * @param specification
     * @return
     */
    public List<Lend> findList(Specification<Lend> specification,Pageable pageable) {
        return lendRepository.findAll(specification,pageable).getContent();
    }

    public long count(Specification<Lend> specification){
        return lendRepository.count(specification);
    }
}
