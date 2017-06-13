package com.gofobao.framework.borrow.service.impl;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.borrow.contants.BorrowContants;
import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.repository.BorrowRepository;
import com.gofobao.framework.borrow.service.BorrowService;
import com.gofobao.framework.borrow.vo.request.VoBorrowListReq;
import com.gofobao.framework.borrow.vo.response.BorrowInfoRes;
import com.gofobao.framework.borrow.vo.response.UserAttachmentRes;
import com.gofobao.framework.borrow.vo.response.VoBorrowDescRes;
import com.gofobao.framework.borrow.vo.response.VoViewBorrowList;
import com.gofobao.framework.common.constans.MoneyConstans;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.NumberHelper;
import com.gofobao.framework.helper.StringHelper;
import com.gofobao.framework.helper.ThymeleafHelper;
import com.gofobao.framework.helper.project.BorrowCalculatorHelper;
import com.gofobao.framework.helper.project.UserHelper;
import com.gofobao.framework.member.entity.UserAttachment;
import com.gofobao.framework.member.entity.Users;
import com.gofobao.framework.member.repository.UserAttachmentRepository;
import com.gofobao.framework.member.repository.UsersRepository;
import com.gofobao.framework.tender.entity.Tender;
import com.gofobao.framework.tender.repository.TenderRepository;
import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * Created by admin on 2017/5/17.
 */
@Component
@Slf4j
public class BorrowServiceImpl implements BorrowService {

    @Autowired
    private BorrowRepository borrowRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private UserAttachmentRepository userAttachmentRepository;

    @Autowired
    private UsersRepository usersRepository;


    @Autowired
    private TenderRepository tenderRepository;

    @Autowired
    private ThymeleafHelper thymeleafHelper;

    /**
     * 首页标列表
     *
     * @param voBorrowListReq
     * @return
     */
    @Override
    public List<VoViewBorrowList> findAll(VoBorrowListReq voBorrowListReq) {

        Integer type = voBorrowListReq.getType();
        List<Integer> typeArray = Arrays.asList(-1, 1, 2, 3, 4, 5);
        Boolean flag = typeArray.contains(type);
        if (!flag) {
            return Collections.EMPTY_LIST;
        }
        if (type == -1) {
            type = null;
        }
        //过滤掉 发标待审 初审不通过；复审不通过 已取消
        List statusArray = Lists.newArrayList(
                new Integer(BorrowContants.CANCEL),
                new Integer(BorrowContants.NO_PASS),
                new Integer(BorrowContants.RECHECK_NO_PASS));

        StringBuilder sb = new StringBuilder(" SELECT b FROM Borrow b WHERE 1=1 ");
        /**
         *条件
         */
        if (type != null) {  // 全部
            if (type == 5) {
                sb.append(" AND b.tenderId is not null ");
            } else {
                sb.append(" AND b.type=" + type);
            }
        }
        sb.append(" AND b.status NOT IN(:statusArray)");

        /**
         * 排序
         */
        if (StringUtils.isEmpty(type)) {
            sb.append(" ORDER BY FIELD(b.type,0, 4, 1, 2),(b.moneyYes / b.money) DESC, b.id DESC");
        } else {
            if (type == BorrowContants.INDEX_TYPE_CE_DAI) {
                sb.append(" ORDER BY b.status ASC,(b.moneyYes / b.money) DESC, b.successAt DESC,b.id DESC");
            } else {
                sb.append(" ORDER BY b.status, b.successAt DESC, b.id DESC");
            }
        }
        List<Borrow> borrowLists = entityManager.createQuery(sb.toString(), Borrow.class)
                .setParameter("statusArray", statusArray)
                .setFirstResult(voBorrowListReq.getPageIndex())
                .setMaxResults(voBorrowListReq.getPageSize())
                .getResultList();

        if (CollectionUtils.isEmpty(borrowLists)) {
            return Collections.EMPTY_LIST;
        }
        Optional<List<Borrow>> objBorrow = Optional.ofNullable(borrowLists);
        List<VoViewBorrowList> listResList = new ArrayList<>();
        objBorrow.ifPresent(p -> p.forEach(
                m -> {
                    VoViewBorrowList item = new VoViewBorrowList();
                    item.setId(m.getId());
                    item.setMoney(StringHelper.formatMon(m.getMoney() / 100d) + MoneyConstans.RMB);
                    item.setIsContinued(m.getIsContinued());
                    item.setLockStatus(m.getIsLock());
                    item.setIsImpawn(m.getIsImpawn());
                    item.setApr(StringHelper.formatMon(m.getApr() / 100d) + MoneyConstans.PERCENT);
                    item.setName(m.getName());
                    item.setMoneyYes(StringHelper.formatMon(m.getMoneyYes() / 100d) + MoneyConstans.RMB);
                    item.setIsNovice(m.getIsNovice());
                    item.setIsMortgage(m.getIsMortgage());
                    if (m.getType() == BorrowContants.REPAY_FASHION_ONCE) {
                        item.setTimeLimit(m.getTimeLimit() + BorrowContants.DAY);
                    } else {
                        item.setTimeLimit(m.getTimeLimit() + BorrowContants.MONTH);
                    }

                    //1.待发布 2.还款中 3.招标中 4.已完成 5.其它
                    Integer status = m.getStatus();
                    if (status == 0) { //待发布
                        status = 1;
                    }
                    if (status == BorrowContants.BIDDING) {//招标中
                        Integer validDay = m.getValidDay();
                        Date endAt = DateHelper.addDays(DateHelper.beginOfDate(m.getReleaseAt()), (validDay + 1));
                        if (new Date().getTime() > endAt.getTime()) {  //当前时间大于满标时间
                            status = 5; //已过期
                        } else {
                            status = 3; //招标中
                        }
                    }
                    if (!ObjectUtils.isEmpty(m.getSuccessAt()) && !ObjectUtils.isEmpty(m.getCloseAt())) {   //满标时间 结清
                        status = 4; //已完成
                    }
                    if (status == BorrowContants.PASS && ObjectUtils.isEmpty(m.getCloseAt())) {
                        status = 2; //还款中
                    }
                    //速度
                    if (status == 3) {
                        item.setSpend(Double.parseDouble(StringHelper.formatMon(m.getMoneyYes().doubleValue() / m.getMoney())));
                    } else {
                        item.setSpend(0d);
                    }

                    if (!StringUtils.isEmpty(m.getTenderId()) && m.getTenderId() > 0) {
                        item.setIsFlow(true);
                    } else {
                        item.setIsFlow(false);
                    }
                    item.setType(voBorrowListReq.getType());
                    item.setStatus(status);
                    item.setRepayFashion(m.getRepayFashion());
                    item.setIsContinued(m.getIsContinued());

                    item.setIsConversion(m.getIsConversion());
                    item.setIsVouch(m.getIsVouch());
                    item.setTenderCount(m.getTenderCount());
                    listResList.add(item);
                })
        );
        Optional<List<VoViewBorrowList>> result = Optional.empty();
        return result.ofNullable(listResList).orElse(Collections.emptyList());
    }

    /**
     * 标详情
     *
     * @param borrowId
     * @return
     */
    @Override
    public BorrowInfoRes findByBorrowId(Long borrowId) {

        BorrowInfoRes borrowInfoRes = new BorrowInfoRes();
        Borrow borrow = borrowRepository.findOne(new Long(borrowId));
        if (ObjectUtils.isEmpty(borrow)) {
            return null;

        }
        borrowInfoRes.setApr(NumberHelper.to2DigitString(borrow.getApr() / 100d));
        borrowInfoRes.setLowest(borrow.getLowest() / 100d + "");
        borrowInfoRes.setMoneyYes(NumberHelper.to2DigitString(borrow.getMoneyYes() / 100d));
        if (borrow.getType() == BorrowContants.REPAY_FASHION_ONCE) {
            borrowInfoRes.setTimeLimit(borrow.getTimeLimit() + BorrowContants.DAY);
        } else {
            borrowInfoRes.setTimeLimit(borrow.getTimeLimit() + BorrowContants.MONTH);
        }
        double principal = (double) 10000 * 100;
        double apr = NumberHelper.toDouble(StringHelper.toString(borrow.getApr()));
        BorrowCalculatorHelper borrowCalculatorHelper = new BorrowCalculatorHelper(principal, apr, borrow.getTimeLimit(), borrow.getSuccessAt());
        Map<String, Object> calculatorMap = borrowCalculatorHelper.simpleCount(borrow.getRepayFashion());
        Integer earnings = NumberHelper.toInt(StringHelper.toString(calculatorMap.get("earnings")));
        borrowInfoRes.setEarnings(earnings + MoneyConstans.RMB);
        borrowInfoRes.setTenderCount(borrow.getTenderCount() + BorrowContants.TIME);
        borrowInfoRes.setMoney(NumberHelper.to2DigitString(borrow.getMoney() / 100d));
        borrowInfoRes.setRepayFashion(borrow.getRepayFashion());
        borrowInfoRes.setSpend(borrow.getMoneyYes() / borrow.getMoney() + MoneyConstans.PERCENT);
        Date endAt = DateHelper.addDays(DateHelper.beginOfDate(borrow.getReleaseAt()), (borrow.getValidDay() + 1));//结束时间
        borrowInfoRes.setEndAt(DateHelper.dateToString(endAt, DateHelper.DATE_FORMAT_YMDHMS));
        borrowInfoRes.setSuccessAt(DateHelper.dateToString(borrow.getSuccessAt(), DateHelper.DATE_FORMAT_YMDHMS));
        return borrowInfoRes;

    }

    /**
     * 标简介
     *
     * @param borrowId
     * @return
     */
    @Override
    public VoBorrowDescRes desc(Long borrowId) {
        VoBorrowDescRes voViewBorrowDescRes = new VoBorrowDescRes();
        Borrow borrow = borrowRepository.findOne(borrowId);
        if (ObjectUtils.isEmpty(borrow)) {
            return voViewBorrowDescRes;
        }
        Long userId = borrow.getUserId();
        voViewBorrowDescRes.setBorrowDesc(borrow.getDescription());
        List<UserAttachment> attachmentList = userAttachmentRepository.findByUserId(userId);
        if (CollectionUtils.isEmpty(attachmentList)) {
            Gson gson = new Gson();
            String jsonStr = gson.toJson(attachmentList);
            List<UserAttachmentRes> attachmentRes = gson.fromJson(jsonStr, new TypeToken<List<UserAttachmentRes>>() {
            }.getType());
            voViewBorrowDescRes.setUserAttachmentRes(attachmentRes);
        }
        return voViewBorrowDescRes;
    }

    /**
     * 标合同
     *
     * @param borrowId
     * @param userId
     * @return
     */
    @Override
    public Map<String, Object> contract(Long borrowId, Long userId) {
        Borrow borrow = borrowRepository.findOne(borrowId);
        if (ObjectUtils.isEmpty(borrow)) {
            return null;
        }
        //发标用户
        Long borrowUserId = borrow.getUserId();
        Users users = usersRepository.findOne(borrowUserId);


        Gson gson= new GsonBuilder().create();
        String jsonStr=gson.toJson(borrow);
        Map<String,Object>borrowMap=gson.fromJson(jsonStr,new TypeToken< Map<String,Object>>() {
        }.getType());
        borrowMap.put("username", StringUtils.isEmpty(users.getPhone()) ? users.getUsername() : users.getPhone());
        borrowMap.put("cardId", UserHelper.hideChar(users.getCardId(), UserHelper.CARD_ID_NUM));


        if (!ObjectUtils.isEmpty(borrow.getSuccessAt())) { //判断是否满标
            boolean successAtBool = DateHelper.getMonth(DateHelper.addMonths(borrow.getSuccessAt(), borrow.getTimeLimit())) % 12
                    !=
                    (DateHelper.getMonth(borrow.getSuccessAt()) + borrow.getTimeLimit()) % 12;

            String borrowExpireAtStr = null;
            String monthAsReimbursement = null;//月截止还款日
            if (borrow.getRepayFashion() == 1) {
                borrowExpireAtStr = DateHelper.dateToString(DateHelper.addDays(borrow.getSuccessAt(), borrow.getTimeLimit()), "yyyy-MM-dd");
                monthAsReimbursement = borrowExpireAtStr;
            } else {
                monthAsReimbursement = "每月" + DateHelper.getDay(borrow.getSuccessAt()) + "日";

                if (successAtBool) {
                    borrowExpireAtStr = DateHelper.dateToString(DateHelper.subDays(DateHelper.addDays(DateHelper.setDays(borrow.getSuccessAt(), borrow.getTimeLimit()), 1), 1), "yyyy-MM-dd HH:mm:ss");
                } else {
                    borrowExpireAtStr = DateHelper.dateToString(DateHelper.addMonths(borrow.getSuccessAt(), borrow.getTimeLimit()), "yyyy-MM-dd");
                }
            }
            borrowMap.put("borrowExpireAtStr", borrowExpireAtStr);
            borrowMap.put("monthAsReimbursement", monthAsReimbursement);
        }

        //使用当前借款息信计算利息
        BorrowCalculatorHelper borrowCalculatorHelper = new BorrowCalculatorHelper(new Double(borrow.getMoney()), new Double(borrow.getApr()), borrow.getTimeLimit(), null);
        Map calculatorMap = borrowCalculatorHelper.simpleCount(borrow.getRepayFashion());

        //查询投标信息


        List<Tender> borrowTenderList = new ArrayList<>();
        if (!StringUtils.isEmpty(userId) || userId > 0) {  //当前不是访客
            Specification specification;
            if (!borrowUserId.equals(userId)) {  //当前用户是否 发标用户
                specification = Specifications.<Tender>and()
                        .eq("userId", userId)
                        .eq("borrowId", borrowId)
                        .build();
            } else {
                specification = Specifications.<Tender>and()
                        .eq("borrowId", borrowId)
                        .build();
            }
            borrowTenderList = tenderRepository.findAll(specification);
        }
        List<Map<String, Object>> tenderMapList = Lists.newArrayList();
        if (!CollectionUtils.isEmpty(borrowTenderList)) {
            tenderMapList = gson.fromJson(
                    gson.toJson(borrowTenderList),
                    new TypeToken< List<Object>>() {
                    }.getType());

            List<Long> tenderUserList = borrowTenderList.stream().map(m -> m.getUserId()).collect(Collectors.toList());
            List<Users> usersList = usersRepository.findByIdIn(tenderUserList);
            Map<Long, Users> userMap = usersList.stream().collect(Collectors.toMap(Users::getId, Function.identity()));

            for (Map<String, Object> tempTenderMap : tenderMapList) {
                Long tempUserId = new Double(tempTenderMap.get("userId").toString()).longValue();
                Users usersTemp = userMap.get(tempUserId);
                tempTenderMap.put("username", UserHelper.hideChar(StringUtils.isEmpty(usersTemp.getUsername()) ? usersTemp.getPhone() : usersTemp.getPhone(), UserHelper.USERNAME_NUM));
                borrowCalculatorHelper = new BorrowCalculatorHelper(NumberHelper.toDouble(tempTenderMap.get("validMoney")), new Double(borrow.getApr()), borrow.getTimeLimit(), null);
                calculatorMap = borrowCalculatorHelper.simpleCount(borrow.getRepayFashion());
                tempTenderMap.put("calculatorMap", calculatorMap);
            }
        }

        //使用thymeleaf模版引擎渲染 借款合同html
        Map<String, Object> templateMap = new HashMap<>();
        templateMap.put("borrowMap", borrowMap);
        templateMap.put("tenderMapList", tenderMapList);
        templateMap.put("calculatorMap", calculatorMap);
        return templateMap;
    }

    public long countByUserIdAndStatusIn(Long userId, List<Integer> statusList) {
        return borrowRepository.countByUserIdAndStatusIn(userId, statusList);
    }

    public boolean insert(Borrow borrow) {
        if (ObjectUtils.isEmpty(borrow)) {
            return false;
        }
        borrow.setId(null);
        return !ObjectUtils.isEmpty(borrowRepository.save(borrow));
    }

    public boolean updateById(Borrow borrow) {
        if (ObjectUtils.isEmpty(borrow) || ObjectUtils.isEmpty(borrow.getId())) {
            return false;
        }
        return !ObjectUtils.isEmpty(borrowRepository.save(borrow));
    }

    public Borrow findByIdLock(Long borrowId) {
        return borrowRepository.findById(borrowId);
    }

    /**
     * 检查是否招标中
     *
     * @param borrow
     * @return
     */
    public boolean checkBidding(Borrow borrow) {
        if (ObjectUtils.isEmpty(borrow)) {
            return false;
        }
        return (borrow.getStatus() == 1 && borrow.getMoneyYes() < borrow.getMoney());
    }

    /**
     * 检查是否在发布时间内
     *
     * @param borrow
     * @return
     */
    public boolean checkReleaseAt(Borrow borrow) {
        Date releaseAt = borrow.getReleaseAt();
        if (ObjectUtils.isEmpty(borrow) || ObjectUtils.isEmpty(releaseAt)) {
            return false;
        }
        return new Date().getTime() > releaseAt.getTime();
    }

    /**
     * 检查招标时间是否有效
     *
     * @param borrow
     * @return
     */
    public boolean checkValidDay(Borrow borrow) {
        Date nowDate = new Date();
        Date validDate = DateHelper.beginOfDate(DateHelper.addDays(borrow.getReleaseAt(), borrow.getValidDay() + 1));
        return (nowDate.getTime() < validDate.getTime());
    }

    public Borrow findById(Long borrowId) {
        return borrowRepository.findOne(borrowId);
    }

    /**
     * 查询列表
     *
     * @param specification
     * @return
     */
    public List<Borrow> findList(Specification<Borrow> specification) {
        return borrowRepository.findAll(specification);
    }

    /**
     * 查询列表
     *
     * @param specification
     * @return
     */
    public List<Borrow> findList(Specification<Borrow> specification, Sort sort) {
        return borrowRepository.findAll(specification, sort);
    }

    /**
     * 查询列表
     *
     * @param specification
     * @return
     */
    public List<Borrow> findList(Specification<Borrow> specification, Pageable pageable) {
        return borrowRepository.findAll(specification, pageable).getContent();
    }

    public long count(Specification<Borrow> specification) {
        return borrowRepository.count(specification);
    }
}
