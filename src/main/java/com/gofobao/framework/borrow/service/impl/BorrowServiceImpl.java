package com.gofobao.framework.borrow.service.impl;

import com.gofobao.framework.borrow.contants.BorrowContants;
import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.repository.BorrowRepository;
import com.gofobao.framework.borrow.service.BorrowService;
import com.gofobao.framework.borrow.vo.VoBorrowListReq;
import com.gofobao.framework.borrow.vo.VoViewBorrowListRes;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
                new Integer(BorrowContants.PENDING),
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
                    item.setMoney(new Double(m.getMoney() / 100));
                    item.setIsContinued(m.getIsContinued());
                    item.setLockStatus(m.getIsLock());
                    item.setIsImpawn(m.getIsImpawn());
                    item.setApr(new Double(m.getApr() / 100));
                    item.setName(m.getName());
                    item.setMoneyYes(new Double(m.getMoneyYes()/100));
                    item.setIsNovice(m.getIsNovice());
                    item.setIsMortgage(m.getIsMortgage());
                    if (m.getType() == BorrowContants.REPAY_FASHION_ONCE) {
                        item.setTimeLimit(m.getTimeLimit() + BorrowContants.DAY);
                    } else {
                        item.setTimeLimit(m.getTimeLimit() + BorrowContants.MONTH);
                    }
                    item.setIsContinued(m.getIsContinued());
                    item.setStatus(m.getStatus());
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
