package com.gofobao.framework.windmill.borrow.service;

import com.gofobao.framework.borrow.entity.Borrow;

import java.util.List;

/**
 * Created by admin on 2017/8/1.
 */
public interface WindmillBorrowService {

    List<Borrow> list(Long id);
}
