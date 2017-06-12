package com.gofobao.framework.lend.service;

import com.gofobao.framework.common.page.Page;
import com.gofobao.framework.lend.entity.Lend;
import com.gofobao.framework.lend.vo.response.LendInfo;
import com.gofobao.framework.lend.vo.response.VoViewLend;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

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


    /**
     * 查询列表
     * @param specification
     * @return
     */
    List<Lend> findList(Specification<Lend> specification);

    /**
     * 查询列表
     *
     * @param specification
     * @return
     */
    List<Lend> findList(Specification<Lend> specification,Sort sort);

    /**
     * 查询列表
     *
     * @param specification
     * @return
     */
    List<Lend> findList(Specification<Lend> specification,Pageable pageable);

    /**
     * 借款信息
     * @param userId
     * @param lendId
     * @return
     */
    LendInfo info(Long userId,Long lendId);

    long count(Specification<Lend> specification);
}
