package com.gofobao.framework.tender.service.impl;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.borrow.contants.BorrowContants;
import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.repository.BorrowRepository;
import com.gofobao.framework.borrow.vo.response.VoBorrowTenderUserRes;
import com.gofobao.framework.common.constans.MoneyConstans;
import com.gofobao.framework.common.data.DataObject;
import com.gofobao.framework.common.data.GeSpecification;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.NumberHelper;
import com.gofobao.framework.helper.StringHelper;
import com.gofobao.framework.helper.project.UserHelper;
import com.gofobao.framework.member.entity.Users;
import com.gofobao.framework.member.repository.UsersRepository;
import com.gofobao.framework.member.service.UserService;
import com.gofobao.framework.tender.contants.TenderConstans;
import com.gofobao.framework.tender.entity.Tender;
import com.gofobao.framework.tender.repository.TenderRepository;
import com.gofobao.framework.tender.service.TenderService;
import com.gofobao.framework.tender.vo.request.TenderUserReq;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.*;
import java.util.stream.Collectors;

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

    @Autowired
    private UserService userService;

    public static final Long SHOW_LIMIT=10000L;


    @PersistenceContext
    private EntityManager entityManager;


    /**
     * 投标用户列表
     *
     * @param tenderUserReq
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @SuppressWarnings("all")
    public List<VoBorrowTenderUserRes> findBorrowTenderUser(TenderUserReq tenderUserReq) {
        final long[] tenderCount = {0};
        Long borrowId = tenderUserReq.getBorrowId();
        Borrow borrow = borrowRepository.findOne(borrowId);
        if (ObjectUtils.isEmpty(borrow)) {
            return Collections.EMPTY_LIST;
        }
        List<VoBorrowTenderUserRes> tenderUserResList = new ArrayList<>();
        PageRequest pageRequest = new PageRequest(tenderUserReq.getPageIndex(),
                tenderUserReq.getPageSize(),
                new Sort(Sort.Direction.DESC, "id"));
        Specification<Tender> tenderSpecification = Specifications.<Tender>and()
                .eq("borrowId", borrowId)
                .eq("status", TenderConstans.SUCCESS)
                .notIn("transferFlag", Lists.newArrayList(TenderConstans.TRANSFER_YES,
                        TenderConstans.TRANSFER_PART_YES).toArray())
                .build();
        Page<Tender> tenderPage = tenderRepository.findAll(tenderSpecification, pageRequest);
        List<Tender> tenderList = tenderPage.getContent();
        if (CollectionUtils.isEmpty(tenderList)) {
            return Collections.EMPTY_LIST;
        }

        Users sendUser = usersRepository.findOne(tenderUserReq.getUserId());
        //获取当前用户类型
        String userType = "";
        if (!ObjectUtils.isEmpty(sendUser)) {
            userType = sendUser.getType();
        }

        if (StringUtils.isEmpty(userType)) {
            userType = "";
        }

        String finalUserType = userType;
        tenderList.stream().forEach(item -> {
            VoBorrowTenderUserRes tenderUserRes = new VoBorrowTenderUserRes();
            tenderUserRes.setValidMoney(StringHelper.formatMon(item.getValidMoney()/100D ) + MoneyConstans.RMB);
            tenderUserRes.setDate(DateHelper.dateToString(item.getCreatedAt(), DateHelper.DATE_FORMAT_YMDHMS));
            // 此处进行优化
            if (item.getIsAuto()) {
                tenderUserRes.setType(TenderConstans.AUTO + "(" + item.getAutoOrder() + ")");
            } else {
                String tenderPlatform;
                Integer source = item.getSource();
                if (ObjectUtils.isEmpty(source)) {
                    source = 0;
                }

                if (source == 0) {
                    tenderPlatform = "电脑端";
                } else if (source == 1) {
                    tenderPlatform = "安卓端";
                } else if (source == 2) {
                    tenderPlatform = "苹果端";
                } else if (source == 3) {
                    tenderPlatform = "触屏端";
                } else {
                    tenderPlatform = "未知设备";
                }

                tenderUserRes.setType(tenderPlatform);
            }


            Users user = usersRepository.findOne(new Long(item.getUserId()));
            //如果当前用户是管理员或者本人 用户名可见
            tenderUserRes.setUserName(finalUserType.equals("manager")
                    ? user.getPhone()
                    : UserHelper.hideChar(user.getPhone(), UserHelper.PHONE_NUM)
            );

            //判断是否是新手标
            if(!borrow.getIsNovice()){
                tenderUserResList.add(tenderUserRes);
                return ;
            }
            if(item.getValidMoney()/100<=SHOW_LIMIT){
                tenderCount[0] = tenderCount[0] +1;
                tenderUserResList.add(tenderUserRes);
                return;
            }
            long count=0;
            long more = item.getValidMoney()/100%SHOW_LIMIT;
            if (item.getValidMoney()/100%SHOW_LIMIT == 0){
                count = item.getValidMoney()/100/SHOW_LIMIT;
                for(int i=0;i<count;i++) {
                    VoBorrowTenderUserRes tenderUserRes1 = new VoBorrowTenderUserRes();
                    tenderUserRes1.setValidMoney(StringHelper.formatMon(SHOW_LIMIT) + MoneyConstans.RMB);
                    tenderUserRes1.setType(tenderUserRes.getType());
                    tenderUserRes1.setUserName(tenderUserRes.getUserName());
                    Calendar calendar=Calendar.getInstance();
                    calendar.setTime(item.getCreatedAt());

                    tenderUserRes1.setDate(DateHelper.dateToString(new Date(calendar.getTimeInMillis()+600*i),DateHelper.DATE_FORMAT_YMDHMS));
                    tenderUserResList.add(tenderUserRes1);
                }
                tenderCount[0] = tenderCount[0] +count;
            } else if (item.getValidMoney()/100<SHOW_LIMIT) {
              tenderUserResList.add(tenderUserRes);
              return;
            } else {
                count = item.getValidMoney() /100/ SHOW_LIMIT + 1;
                for(int i=0;i<count-1;i++) {
                    VoBorrowTenderUserRes tenderUserRes1 = new VoBorrowTenderUserRes();
                    tenderUserRes1.setValidMoney(StringHelper.formatMon(SHOW_LIMIT ) + MoneyConstans.RMB);
                    tenderUserRes1.setType(tenderUserRes.getType());
                    tenderUserRes1.setUserName(tenderUserRes.getUserName());
                    Calendar calendar=Calendar.getInstance();
                    calendar.setTime(item.getCreatedAt());

                    tenderUserRes1.setDate(DateHelper.dateToString(new Date(calendar.getTimeInMillis()+600*i),DateHelper.DATE_FORMAT_YMDHMS));
                    tenderUserResList.add(tenderUserRes1);
                }
                tenderCount[0] = tenderCount[0] +count;

            }
            VoBorrowTenderUserRes tenderUserRes1 = new VoBorrowTenderUserRes();
            tenderUserRes1.setValidMoney(StringHelper.formatMon(more) + MoneyConstans.RMB);
            tenderUserRes1.setType(tenderUserRes.getType());
            tenderUserRes1.setUserName(tenderUserRes.getUserName());
            Calendar calendar=Calendar.getInstance();
            calendar.setTime(item.getCreatedAt());
            tenderUserRes1.setDate(DateHelper.dateToString(new Date(calendar.getTimeInMillis()+count*600),DateHelper.DATE_FORMAT_YMDHMS));
            if (more != 0) {
                tenderUserResList.add(tenderUserRes1);
            }
            //拆分成功后修改表投标数
            borrowRepository.updateTenderCount((int) tenderCount[0],borrowId);
        });
        Collections.sort(tenderUserResList,Comparator.comparing(VoBorrowTenderUserRes::getDate).reversed());
        return Optional.empty().ofNullable(tenderUserResList).orElse(Collections.emptyList());
    }

    /**
     * 查询投标复审金额
     *
     * @param userId
     * @return
     */
    @Override
    public long findTenderAgainVerifyMoney(long userId) {
        StringBuffer sql = new StringBuffer("SELECT sum(t1.`valid_money`)  FROM `gfb_borrow_tender` t1 LEFT JOIN `gfb_borrow`" +
                " t2 on t1.`borrow_id` = t2.`id` WHERE t2.`status` = 1 and t2.`money_yes` = t2.`money` and t2.`success_at` IS NOT NULL  and t1.`user_id` = " + userId + " ;");
        Query query = entityManager.createNativeQuery(sql.toString());
        return NumberHelper.toInt(query.getResultList().get(0));
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
        return Optional.ofNullable(tenderRepository.findAll(specification)).orElse(Collections.EMPTY_LIST);
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

    @Override
    public Map<String, Long> statistic() {
        Date todayAt = new Date();
        Date yesterdayAt = DateHelper.subDays(todayAt, 1);
        Date todayBeginOfDate = DateHelper.beginOfDate(todayAt);
        Specification<Borrow> borrowSpecification = Specifications.<Borrow>and()
                .eq("status", BorrowContants.PASS)
                .between("successAt", new Range<>(DateHelper.beginOfDate(yesterdayAt), todayAt))
                .build();
        List<Borrow> borrows = borrowRepository.findAll(borrowSpecification);
        List<Borrow> yesterdayBorrow = borrows.stream()
                .filter(borrow ->
                        borrow.getSuccessAt().getTime() < todayBeginOfDate.getTime())
                .collect(Collectors.toList());
        List<Borrow> todayBorrow = borrows.stream()
                .filter(borrow ->
                        borrow.getSuccessAt().getTime() > todayBeginOfDate.getTime())
                .collect(Collectors.toList());
        Map<String, Long> statistic = Maps.newHashMap();
        statistic.put("todayTender", CollectionUtils.isEmpty(todayBorrow)
                ? 0
                : todayBorrow.stream()
                .mapToLong(p -> p.getMoneyYes())
                .sum());
        statistic.put("yesterdayTender", CollectionUtils.isEmpty(yesterdayBorrow)
                ? 0
                : yesterdayBorrow.stream()
                .mapToLong(p -> p.getMoneyYes())
                .sum());
        return statistic;
    }
}
