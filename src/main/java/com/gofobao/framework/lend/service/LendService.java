package com.gofobao.framework.lend.service;

import com.gofobao.framework.common.page.Page;
import com.gofobao.framework.lend.entity.Lend;
import com.gofobao.framework.lend.vo.response.VoViewLend;

import java.util.List;

/**
 * Created by Zeke on 2017/6/1.
 */
public interface LendService {
    Lend insert(Lend lend);

    boolean updateById(Lend lend);

    Lend findByIdLock(Long id);

    Lend findById(Long id);

    List<VoViewLend> list(Page page);
}
