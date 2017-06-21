package com.gofobao.framework.tender.service;

import com.gofobao.framework.borrow.vo.response.VoBorrowTenderUserRes;
import com.gofobao.framework.tender.entity.Tender;
import com.gofobao.framework.tender.vo.request.TenderUserReq;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

/**
 * Created by admin on 2017/5/19.
 */
public interface TenderService {


    Tender insert(Tender tender);

    boolean updateById(Tender tender);

    List<VoBorrowTenderUserRes> findBorrowTenderUser(TenderUserReq tenderUserReq);

    List<Tender> findList(Specification<Tender> specification);

    List<Tender> findList(Specification<Tender> specification, Pageable pageable);

    List<Tender> findList(Specification<Tender> specification, Sort sort);

    long count(Specification<Tender> specification);

    /**
     * 检查投标是否太频繁
     *
     * @param borrowId
     * @param userId
     * @return
     */
    boolean checkTenderNimiety(Long borrowId, Long userId);

    Tender findById(Long tenderId);

    Tender findByAuthCode(String authCode);

}
