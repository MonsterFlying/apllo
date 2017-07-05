package com.gofobao.framework.tender.service.impl;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.repository.BorrowRepository;
import com.gofobao.framework.borrow.vo.response.VoBorrowTenderUserRes;
import com.gofobao.framework.common.constans.MoneyConstans;
import com.gofobao.framework.common.data.DataObject;
import com.gofobao.framework.common.data.GeSpecification;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.StringHelper;
import com.gofobao.framework.helper.project.UserHelper;
import com.gofobao.framework.member.entity.Users;
import com.gofobao.framework.member.repository.UsersRepository;
import com.gofobao.framework.tender.contants.TenderConstans;
import com.gofobao.framework.tender.entity.Tender;
import com.gofobao.framework.tender.repository.TenderRepository;
import com.gofobao.framework.tender.service.TenderService;
import com.gofobao.framework.tender.vo.request.TenderUserReq;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * Created by admin on 2017/5/19.
 */
@Component
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
     * @param tenderUserReq
     * @return
     */
    @Override
    public List<VoBorrowTenderUserRes> findBorrowTenderUser(TenderUserReq tenderUserReq) {
        Long borrowId = tenderUserReq.getBorrowId();
        Borrow borrow = borrowRepository.findOne(borrowId);
        if (ObjectUtils.isEmpty(borrow)) {
            return Collections.EMPTY_LIST;
        }
        List<VoBorrowTenderUserRes> tenderUserResList = new ArrayList<>();
        Tender tender = new Tender();
        tender.setBorrowId(borrowId);
        tender.setStatus(TenderConstans.SUCCESS);

        ExampleMatcher matcher = ExampleMatcher.matching().withIgnorePaths("isAuto");
        Example<Tender> ex = Example.of(tender, matcher);
        Page<Tender> tenderPage = tenderRepository.findAll(ex, new PageRequest(tenderUserReq.getPageIndex(), tenderUserReq.getPageSize(), new Sort(Sort.Direction.DESC, "id")));
        //Optional<List<Tender>> listOptional = Optional.ofNullable(tenderList);
        List<Tender> tenderList = tenderPage.getContent();
        if (CollectionUtils.isEmpty(tenderList)) {
            return Collections.EMPTY_LIST;
        }

        tenderList.stream().forEach(item -> {
            VoBorrowTenderUserRes tenderUserRes = new VoBorrowTenderUserRes();
            tenderUserRes.setValidMoney(StringHelper.formatMon(item.getValidMoney() / 100d) + MoneyConstans.RMB);
            tenderUserRes.setDate(DateHelper.dateToString(item.getCreatedAt(), DateHelper.DATE_FORMAT_YMDHMS));
            tenderUserRes.setType(item.getIsAuto() ? TenderConstans.AUTO : TenderConstans.MANUAL);
            Users user = usersRepository.findOne(new Long(item.getUserId()));

            // TODO 此处没有考虑到 用户名不存在 手机号不存在 只有邮箱的情况 
            String userName = StringUtils.isEmpty(user.getUsername()) ?
                    UserHelper.hideChar(user.getPhone(), UserHelper.PHONE_NUM) :
                    UserHelper.hideChar(user.getUsername(), UserHelper.USERNAME_NUM);
            tenderUserRes.setUserName(userName);
            tenderUserResList.add(tenderUserRes);
        });
        return Optional.empty().ofNullable(tenderUserResList).orElse(Collections.emptyList());
    }

    public Tender save(Tender tender) {
        return tenderRepository.save(tender);
    }

    public List<Tender> save(List<Tender> tender) {
        return tenderRepository.save(tender);
    }

    public boolean updateById(Tender tender) {
        if (ObjectUtils.isEmpty(tender) || ObjectUtils.isEmpty(tender.getId())) {
            return false;
        }
        return !ObjectUtils.isEmpty(tenderRepository.save(tender));
    }

    public List<Tender> findList(Specification<Tender> specification) {
        return  Optional.ofNullable(tenderRepository.findAll(specification)).orElse(Collections.EMPTY_LIST);
    }

    public List<Tender> findList(Specification<Tender> specification, Pageable pageable) {
        if (ObjectUtils.isEmpty(specification) || ObjectUtils.isEmpty(pageable)) {
            return null;
        }
        return tenderRepository.findAll(specification, pageable).getContent();
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

    public Tender findByAuthCode(String authCode) {
        return tenderRepository.findByAuthCode(authCode);
    }
}
