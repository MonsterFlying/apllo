package com.gofobao.framework.windmill.borrow.service;

import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.tender.entity.Tender;

import java.util.List;

/**
 * Created by admin on 2017/8/1.
 */
public interface WindmillBorrowService {
    /**
     * 。标列列表
     *
     * @param borrowId
     * @return
     */
    List<Borrow> list(Long borrowId);

    /**
     * @param borrowId
     * @param Date
     * @return
     */
    List<Tender> tenderList(Long borrowId, String Date);
}