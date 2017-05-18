package com.gofobao.framework.borrow.service.impl;

import com.gofobao.framework.borrow.contants.BorrowContants;
import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.repository.BorrowRepository;
import com.gofobao.framework.borrow.service.BorrowService;
import com.gofobao.framework.borrow.vo.VoBorrowListReq;
import com.gofobao.framework.borrow.vo.VoViewBorrowListRes;
import com.gofobao.framework.common.constans.MoneyConstans;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.NumberHelper;
import com.gofobao.framework.helper.StringHelper;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.*;

/**
 * Created by admin on 2017/5/17.
 */
@Service
@Slf4j
public class BorrowServiceImpl implements BorrowService {

    @Autowired
    private BorrowRepository borrowRepository;

    @Override
    public List<VoViewBorrowListRes> findAll(VoBorrowListReq voBorrowListReq) {

        Sort sort = new Sort(Sort.Direction.DESC, "id");
        Pageable pageable = new PageRequest(voBorrowListReq.getPageIndex()
                , voBorrowListReq.getPageSize()
                , sort);
        //过滤掉 发标待审 初审不通过；复审不通过 已取消
        List borrowsStatusArray=Lists.newArrayList(
             //   new Integer(BorrowContants.PENDING),
                new Integer(BorrowContants.CANCEL),
                new Integer(BorrowContants.NO_PASS),
                new Integer(BorrowContants.RECHECK_NO_PASS));

        Page<Borrow> borrows;
        if(!ObjectUtils.isEmpty(voBorrowListReq.getType())) {
            borrows = borrowRepository.findByTypeAndStatusNotIn(voBorrowListReq.getType(),
                    borrowsStatusArray
                    , pageable);
        }else {  //全部
            borrows = borrowRepository.findByAndStatusNotIn(borrowsStatusArray, pageable);
        }
        List<Borrow> borrowList = borrows.getContent();

        Optional<List<Borrow>> objBorrow = Optional.ofNullable(borrowList);
        List<VoViewBorrowListRes> listResList = new ArrayList<>();
        objBorrow.ifPresent(p -> p.forEach(
                m -> {
                    VoViewBorrowListRes item = new VoViewBorrowListRes();
                    item.setId(m.getId());
                    item.setMoney(NumberHelper.to2DigitString(new Double(m.getMoney() / 100d))+MoneyConstans.YUAN);
                    item.setIsContinued(m.getIsContinued());
                    item.setLockStatus(m.getIsLock());
                    item.setIsImpawn(m.getIsImpawn());
                    item.setApr((m.getApr() / 100d)+ MoneyConstans.PERCENT);
                    item.setName(m.getName());
                    item.setMoneyYes(NumberHelper.to2DigitString(new Double(m.getMoneyYes()/100d))+MoneyConstans.YUAN);
                    item.setIsNovice(m.getIsNovice());
                    item.setIsMortgage(m.getIsMortgage());
                    if (m.getType() == BorrowContants.REPAY_FASHION_ONCE) {
                        item.setTimeLimit(m.getTimeLimit() + BorrowContants.DAY);
                    } else {
                        item.setTimeLimit(m.getTimeLimit() + BorrowContants.MONTH);
                    }

                    //1.待发布 2.还款中 3.招标中 4.已完成 5.其它
                    Integer status=m.getStatus();
                    if(!ObjectUtils.isEmpty(m.getSuccessAt())&&!ObjectUtils.isEmpty(m.getCloseAt())){   //满标时间 结清
                        status=4; //已完成
                    }
                    if(status==BorrowContants.BIDDING){//发标中
                        if(new Date().getTime()>m.getSuccessAt().getTime()){  //当前时间大于满标时间
                            status=5; //已过期
                        }else{
                            status=3; //招标中
                        }
                    }
                    if(status==BorrowContants.PASS&&ObjectUtils.isEmpty(m.getCloseAt())){
                        status=2; //还款中
                    }if(status==BorrowContants.BIDDING){
                        status=1;//待发布
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

}
