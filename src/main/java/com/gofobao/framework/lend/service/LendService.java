package com.gofobao.framework.lend.service;

import com.gofobao.framework.lend.entity.Lend;

/**
 * Created by Zeke on 2017/6/1.
 */
public interface LendService {
    Lend insert(Lend lend);

    boolean updateById(Lend lend);

    Lend findByIdLock(Long id);

    Lend findById(Long id);
}
