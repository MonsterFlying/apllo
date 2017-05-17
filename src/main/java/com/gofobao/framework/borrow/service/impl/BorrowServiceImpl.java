package com.gofobao.framework.borrow.service.impl;

import com.gofobao.framework.borrow.contants.BorrowContants;
import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.repository.BorrowRepository;
import com.gofobao.framework.borrow.service.BorrowService;
import com.gofobao.framework.borrow.vo.VoBorrowListReq;
import com.gofobao.framework.borrow.vo.VoViewBorrowListRes;
import com.gofobao.framework.helper.JacksonHelper;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by admin on 2017/5/17.
 */
public class BorrowServiceImpl implements BorrowService {

    @Autowired
    private BorrowRepository borrowRepository;

    @Override
    public List<VoViewBorrowListRes> findAll(VoBorrowListReq voBorrowListReq) {

        Sort sort = new Sort(Sort.Direction.DESC, "id");
        Pageable pageable = new PageRequest(voBorrowListReq.getPageIndex(), voBorrowListReq.getPageSize(), sort);
        List<VoViewBorrowListRes> listResList = new ArrayList<>();

        Page<Borrow> borrows = borrowRepository.findByTypeAndStatusNotIn(voBorrowListReq.getType(), Lists.newArrayList(
                new Integer(BorrowContants.PENDING),
                new Integer(BorrowContants.CANCEL),
                new Integer(BorrowContants.NO_PASS),
                new Integer(BorrowContants.RECHECK_NO_PASS)), pageable);
        try {
            String jsonStr = JacksonHelper.obj2json(borrows);
            listResList = JacksonHelper.json2list(jsonStr, VoViewBorrowListRes.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return listResList;
    }

}
