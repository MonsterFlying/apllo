package com.gofobao.framework.borrow.service.impl;

import com.gofobao.framework.borrow.contants.BorrowContants;
import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.repository.BorrowRepository;
import com.gofobao.framework.borrow.service.BorrowService;
import com.gofobao.framework.borrow.vo.request.VoBorrowByIdReq;
import com.gofobao.framework.borrow.vo.request.VoBorrowListReq;
import com.gofobao.framework.borrow.vo.response.VoBorrowByIdRes;
import com.gofobao.framework.borrow.vo.response.VoViewBorrowListRes;
import com.gofobao.framework.common.constans.MoneyConstans;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.NumberHelper;
import com.gofobao.framework.helper.StringHelper;
import com.gofobao.framework.helper.project.BorrowCalculatorHelper;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.*;


/**
 * Created by admin on 2017/5/17.
 */
@Service
@Slf4j
public class BorrowServiceImpl implements BorrowService {

    @Autowired
    private BorrowRepository borrowRepository;


    /**
     * 首页标列表
     *
     * @param voBorrowListReq
     * @return
     */
    @Override
    public List<VoViewBorrowListRes> findAll(VoBorrowListReq voBorrowListReq) {

        /**
         * 排序
         */
        Sort sort = null;
        if (!StringUtils.isEmpty(voBorrowListReq.getType())&&voBorrowListReq.getType() != BorrowContants.INDEX_TYPE_CE_DAI) {
            sort = new Sort(
                    new Sort.Order(Sort.Direction.DESC, "status,successAt,id"));
        }
        Pageable pageable = new PageRequest(voBorrowListReq.getPageNumber()
                , voBorrowListReq.getPageSize()
                , sort);

        //过滤掉 发标待审 初审不通过；复审不通过 已取消
        List borrowsStatusArray = Lists.newArrayList(
                new Integer(BorrowContants.CANCEL),
                new Integer(BorrowContants.NO_PASS),
                new Integer(BorrowContants.RECHECK_NO_PASS));
        Page<Borrow> borrows;
        if (!ObjectUtils.isEmpty(voBorrowListReq.getType())) {
            borrows = borrowRepository.findByTypeAndStatusNotIn(voBorrowListReq.getType(),
                    borrowsStatusArray
                    , pageable);
        } else {  //全部
            borrows = borrowRepository.findByAndStatusNotIn(borrowsStatusArray, pageable);
        }

        List<Borrow> borrowLists = new ArrayList(borrows.getContent());
        if (StringUtils.isEmpty(voBorrowListReq.getType())) {//全部
            borrowLists.sort((o1, o2) -> {
                if (o1.getMoneyYes() / o1.getMoney() == o2.getMoneyYes() / o2.getMoney()) {
                    return  o2.getId().intValue()-o1.getId().intValue();
                } else {
                    return  o2.getMoneyYes() / o2.getMoney()-o1.getMoneyYes() / o1.getMoney() ;
                }
            });
        }else if(voBorrowListReq.getType()==BorrowContants.INDEX_TYPE_CE_DAI){  //车贷
            Comparator<Borrow> c= Comparator.comparing(p->p.getStatus());
            c.thenComparing(b->b.getMoney()/b.getMoneyYes()).reversed().thenComparing(w->w.getSuccessAt()).thenComparing(w->w.getId());
            borrowLists.sort(c);
        }
        Optional<List<Borrow>> objBorrow = Optional.ofNullable(borrowLists);
        List<VoViewBorrowListRes> listResList = new ArrayList<>();
        objBorrow.ifPresent(p -> p.forEach(
                m -> {
                    VoViewBorrowListRes item = new VoViewBorrowListRes();
                    item.setId(m.getId());
                    item.setMoney(NumberHelper.to2DigitString(new Double(m.getMoney() / 100d)) + MoneyConstans.RMB);
                    item.setIsContinued(m.getIsContinued());
                    item.setLockStatus(m.getIsLock());
                    item.setIsImpawn(m.getIsImpawn());
                    item.setApr((m.getApr() / 100d) + MoneyConstans.PERCENT);
                    item.setName(m.getName());
                    item.setMoneyYes(NumberHelper.to2DigitString(new Double(m.getMoneyYes() / 100d)) + MoneyConstans.RMB);
                    item.setIsNovice(m.getIsNovice());
                    item.setIsMortgage(m.getIsMortgage());
                    if (m.getType() == BorrowContants.REPAY_FASHION_ONCE) {
                        item.setTimeLimit(m.getTimeLimit() + BorrowContants.DAY);
                    } else {
                        item.setTimeLimit(m.getTimeLimit() + BorrowContants.MONTH);
                    }
                    //1.待发布 2.还款中 3.招标中 4.已完成 5.其它
                    Integer status = m.getStatus();
                    if (!ObjectUtils.isEmpty(m.getSuccessAt()) && !ObjectUtils.isEmpty(m.getCloseAt())) {   //满标时间 结清
                        status = 4; //已完成
                    }
                    if (status == BorrowContants.BIDDING) {//发标中
                       Integer validDay = m.getValidDay();
                       Date endAt = DateHelper.addDays(DateHelper.beginOfDate(m.getReleaseAt()), (validDay + 1));
                        if (new Date().getTime() >endAt.getTime()) {  //当前时间大于满标时间
                            status = 5; //已过期
                        } else {
                            status = 3; //招标中
                        }
                    }
                    if (status == BorrowContants.PASS && ObjectUtils.isEmpty(m.getCloseAt())) {
                        status = 2; //还款中
                    }
                    if (status == BorrowContants.BIDDING) {
                        status = 1;//待发布
                    }
                    item.setStatus(status);
                    item.setRepayFashion(m.getRepayFashion());
                    item.setIsContinued(m.getIsContinued());

                    item.setIsConversion(m.getIsConversion());
                    item.setIsVouch(m.getIsVouch());
                    item.setTenderCount(m.getTenderCount());
                    item.setType(m.getType());
                    listResList.add(item);
                }
                )
        );
        Optional<List<VoViewBorrowListRes>> result = Optional.empty();
        return result.ofNullable(listResList).orElse(Collections.emptyList());
    }

    /**
     * 标详情
     *
     * @param req
     * @return
     */
    @Override
    public VoBorrowByIdRes findByBorrowId(VoBorrowByIdReq req) {

        VoBorrowByIdRes voBorrowByIdRes = new VoBorrowByIdRes();
        try {
            Borrow borrow = borrowRepository.findOne(new Long(req.getBorrowId()));
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
            voBorrowByIdRes.setSpend(NumberHelper.to2DigitString(borrow.getMoneyYes() / borrow.getMoney()) + MoneyConstans.PERCENT);
            Date endAt = DateHelper.addDays(DateHelper.beginOfDate(borrow.getReleaseAt()), (borrow.getValidDay() + 1));//结束时间
            voBorrowByIdRes.setEndAt(DateHelper.dateToString(endAt, DateHelper.DATE_FORMAT_YMDHMS));
            voBorrowByIdRes.setSuccessAt(DateHelper.dateToString(borrow.getSuccessAt(), DateHelper.DATE_FORMAT_YMDHMS));
        } catch (Exception e) {
            return null;
        }
        return voBorrowByIdRes;
    }


    public long countByUserIdAndStatusIn(Long userId,List<Integer> statusList){
        return borrowRepository.countByUserIdAndStatusIn(userId,statusList);
    }

}
