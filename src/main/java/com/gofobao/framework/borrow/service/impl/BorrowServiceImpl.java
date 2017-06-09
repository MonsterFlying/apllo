package com.gofobao.framework.borrow.service.impl;

import com.github.wenhao.jpa.Sorts;
import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.borrow.contants.BorrowContants;
import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.repository.BorrowRepository;
import com.gofobao.framework.borrow.service.BorrowService;
import com.gofobao.framework.borrow.vo.request.VoBorrowByIdReq;
import com.gofobao.framework.borrow.vo.request.VoBorrowListReq;
import com.gofobao.framework.borrow.vo.response.VoBorrowByIdRes;
import com.gofobao.framework.borrow.vo.response.VoViewBorrowList;
import com.gofobao.framework.common.constans.MoneyConstans;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.NumberHelper;
import com.gofobao.framework.helper.StringHelper;
import com.gofobao.framework.helper.project.BorrowCalculatorHelper;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.groovy.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.*;


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

    /**
     * 首页标列表
     *
     * @param voBorrowListReq
     * @return
     */
    @Override
    public List<VoViewBorrowList> findAll(VoBorrowListReq voBorrowListReq) {
        Integer type = voBorrowListReq.getType();
        if (type == -1) {
            type = null;
        }
        //过滤掉 发标待审 初审不通过；复审不通过 已取消
        List statusArray = Lists.newArrayList(
                new Integer(BorrowContants.CANCEL),
                new Integer(BorrowContants.NO_PASS),
                new Integer(BorrowContants.RECHECK_NO_PASS));

        StringBuilder sb=new StringBuilder(" SELECT b FROM Borrow b WHERE 1=1 ");
        /**
         *条件
         */
        if(!StringUtils.isEmpty(type)){  // 全部
            if (type==2) {
                sb.append(" AND b.tenderId is not null ");
            }else {
                sb.append(" AND b.type=" + type);
            }
        }
        sb.append(" AND b.status NOT IN(:statusArray)");

        /**
         * 排序
         */
        if(StringUtils.isEmpty(type)){
            sb.append(" ORDER BY FIELD(b.type,0, 4, 1, 2),(b.moneyYes / b.money) DESC, b.id DESC");
        }else {
            if (type == BorrowContants.INDEX_TYPE_CE_DAI) {
                sb.append(" ORDER BY b.status ASC,(b.moneyYes / b.money) DESC, b.successAt DESC,b.id DESC");
            } else {
                sb.append(" ORDER BY b.status, b.successAt DESC, b.id DESC");
            }
        }
        List<Borrow>borrowLists=entityManager.createQuery(sb.toString(),Borrow.class)
                .setParameter("statusArray",statusArray)
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
                    item.setMoney(NumberHelper.to2DigitString(m.getMoney() / 100d) + MoneyConstans.RMB);
                    item.setIsContinued(m.getIsContinued());
                    item.setLockStatus(m.getIsLock());
                    item.setIsImpawn(m.getIsImpawn());
                    item.setApr(NumberHelper.to2DigitString(m.getApr() / 100d) + MoneyConstans.PERCENT);
                    item.setName(m.getName());
                    item.setMoneyYes(NumberHelper.to2DigitString(m.getMoneyYes() / 100d) + MoneyConstans.RMB);
                    item.setIsNovice(m.getIsNovice());
                    item.setIsMortgage(m.getIsMortgage());
                    if (m.getType() == BorrowContants.REPAY_FASHION_ONCE) {
                        item.setTimeLimit(m.getTimeLimit() + BorrowContants.DAY);
                    } else {
                        item.setTimeLimit(m.getTimeLimit() + BorrowContants.MONTH);
                    }
                    if(!StringUtils.isEmpty(m.getTenderId())&&m.getTenderId()>0){
                        item.setType(2);
                    }
                    //1.待发布 2.还款中 3.招标中 4.已完成 5.其它
                    Integer status = m.getStatus();
                    if(status==0){ //待发布
                        status=1;
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
                    if(status==3){
                        item.setSpend(NumberHelper.to2DigitString(m.getMoneyYes()/m.getMoney()));
                    }else {
                        item.setSpend("0");
                    }
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
    public VoBorrowByIdRes findByBorrowId(Long borrowId) {

        VoBorrowByIdRes voBorrowByIdRes = new VoBorrowByIdRes();
        try {
            Borrow borrow = borrowRepository.findOne(new Long(borrowId));
            if (ObjectUtils.isEmpty(borrow)) {
                return voBorrowByIdRes;
            }
            voBorrowByIdRes.setApr(NumberHelper.to2DigitString(borrow.getApr() / 100d));
            voBorrowByIdRes.setLowest(borrow.getLowest() / 100d + "");
            voBorrowByIdRes.setMoneyYes(NumberHelper.to2DigitString(borrow.getMoneyYes() / 100d));
            if (borrow.getType() == BorrowContants.REPAY_FASHION_ONCE) {
                voBorrowByIdRes.setTimeLimit(borrow.getTimeLimit() + BorrowContants.DAY);
            } else {
                voBorrowByIdRes.setTimeLimit(borrow.getTimeLimit() + BorrowContants.MONTH);
            }
            double principal = (double) 10000 * 100;
            double apr = NumberHelper.toDouble(StringHelper.toString(borrow.getApr()));
            BorrowCalculatorHelper borrowCalculatorHelper = new BorrowCalculatorHelper(principal, apr, borrow.getTimeLimit(), borrow.getSuccessAt());
            Map<String, Object> calculatorMap = borrowCalculatorHelper.simpleCount(borrow.getRepayFashion());
            Integer earnings = NumberHelper.toInt(StringHelper.toString(calculatorMap.get("earnings")));
            voBorrowByIdRes.setEarnings(earnings + MoneyConstans.RMB);
            voBorrowByIdRes.setTenderCount(borrow.getTenderCount() + BorrowContants.TIME);
            voBorrowByIdRes.setMoney(NumberHelper.to2DigitString(borrow.getMoney() / 100d));
            voBorrowByIdRes.setRepayFashion(borrow.getRepayFashion());
            voBorrowByIdRes.setSpend(borrow.getMoneyYes() / borrow.getMoney() + MoneyConstans.PERCENT);
            Date endAt = DateHelper.addDays(DateHelper.beginOfDate(borrow.getReleaseAt()), (borrow.getValidDay() + 1));//结束时间
            voBorrowByIdRes.setEndAt(DateHelper.dateToString(endAt, DateHelper.DATE_FORMAT_YMDHMS));
            voBorrowByIdRes.setSuccessAt(DateHelper.dateToString(borrow.getSuccessAt(), DateHelper.DATE_FORMAT_YMDHMS));
        } catch (Exception e) {
            return voBorrowByIdRes;
        }
        return voBorrowByIdRes;
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
