package com.gofobao.framework.tender.service.impl;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.repository.BorrowRepository;
import com.gofobao.framework.borrow.vo.response.VoBorrowTenderUserRes;
import com.gofobao.framework.common.constans.MoneyConstans;
import com.gofobao.framework.common.data.DataObject;
import com.gofobao.framework.common.data.GeSpecification;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.NumberHelper;
import com.gofobao.framework.helper.project.UserHelper;
import com.gofobao.framework.member.entity.Users;
import com.gofobao.framework.member.repository.UsersRepository;
import com.gofobao.framework.tender.contants.TenderConstans;
import com.gofobao.framework.tender.entity.Tender;
import com.gofobao.framework.tender.repository.TenderRepository;
import com.gofobao.framework.tender.service.TenderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * Created by admin on 2017/5/19.
 */
@Service
@Slf4j
public class TenderServiceImpl implements TenderService {

    @Autowired
    private TenderRepository tenderRepository;

    @Autowired
    private BorrowRepository borrowRepository;


    @Autowired
    private UsersRepository usersRepository;

    /**
     * 投标用户列表
     *
     * @param borrowId
     * @return
     */
    @Override
    public List<VoBorrowTenderUserRes> findBorrowTenderUser(Long borrowId) {
        Borrow borrow = borrowRepository.findById(borrowId);
        if (ObjectUtils.isEmpty(borrow)) {
            return Collections.EMPTY_LIST;
        }
        List<VoBorrowTenderUserRes> tenderUserResList = new ArrayList<>();
        Tender tender = new Tender();
        tender.setBorrowId(borrowId);
        tender.setStatus(TenderConstans.SUCCESS);

        ExampleMatcher matcher = ExampleMatcher.matching().withIgnorePaths("isAuto");
        Example<Tender> ex = Example.of(tender, matcher);
        List<Tender> tenderList = tenderRepository.findAll(ex);
        Optional<List<Tender>> listOptional = Optional.ofNullable(tenderList);

        listOptional.ifPresent(items -> items.forEach(item -> {
            VoBorrowTenderUserRes tenderUserRes = new VoBorrowTenderUserRes();
            tenderUserRes.setMoney(NumberHelper.to2DigitString(item.getValidMoney() / 100d) + MoneyConstans.RMB);
            tenderUserRes.setDate(DateHelper.dateToString(item.getCreatedAt(), DateHelper.DATE_FORMAT_YMDHMS));
            tenderUserRes.setType(item.getIsAuto() ? TenderConstans.AUTO : TenderConstans.MANUAL);
            Users user = usersRepository.findOne(new Long(item.getUserId()));
            String userName = StringUtils.isEmpty(user.getUsername()) ?
                    UserHelper.hideChar(user.getPhone(), UserHelper.PHONE_NUM) :
                    UserHelper.hideChar(user.getUsername(), UserHelper.USERNAME_NUM);
            tenderUserRes.setUserName(userName);
            tenderUserResList.add(tenderUserRes);
        }));
        return Optional.empty().ofNullable(tenderUserResList).orElse(Collections.emptyList());
    }

    public Tender insert(Tender tender) {
        if (ObjectUtils.isEmpty(tender)) {
            return null;
        }
        tender.setId(null);
        return tenderRepository.save(tender);
    }

    public boolean updateById(Tender tender) {
        if (ObjectUtils.isEmpty(tender) || ObjectUtils.isEmpty(tender.getId())) {
            return false;
        }
        return !ObjectUtils.isEmpty(tenderRepository.save(tender));
    }

    public List<Tender> findList(Specification<Tender> specification) {
        if (ObjectUtils.isEmpty(specification)) {
            return null;
        }
        return tenderRepository.findAll(specification);
    }

    public List<Tender> findList(Specification<Tender> specification, Pageable pageable) {
        if (ObjectUtils.isEmpty(specification) || ObjectUtils.isEmpty(pageable)) {
            return null;
        }
        return tenderRepository.findAll(specification, pageable).getContent() ;
    }

    public List<Tender> findList(Specification<Tender> specification, Sort sort) {
        if (ObjectUtils.isEmpty(specification) || ObjectUtils.isEmpty(sort)) {
            return null;
        }
        return tenderRepository.findAll(specification, sort);
    }

    public long count(Specification<Tender> specification) {
        if (ObjectUtils.isEmpty(specification)) {
            return 0;
        }
        return tenderRepository.count(specification);
    }
    /**
     * 检查投标是否太频繁
     *
     * @param borrowId
     * @param userId
     * @return
     */
    public boolean checkTenderNimiety(Long borrowId, Long userId) {

        Specification<Tender> specification = Specifications.<Tender>and()
                .eq("userId", userId)
                .eq("borrowId", borrowId)
                .eq("status", 1)
                .predicate(new GeSpecification<Tender>("updatedAt", new DataObject(DateHelper.subMinutes(new Date(), 1))))
                .build();
        List<Tender> tenderList = tenderRepository.findAll(specification);
        return !CollectionUtils.isEmpty(tenderList);
    }

    public Tender findById(Long tenderId) {
        return tenderRepository.findOne(tenderId);
    }
}
