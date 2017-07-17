package com.gofobao.framework.lend.service;

import com.gofobao.framework.lend.entity.LendBlacklist;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Map;

/**
 * Created by Zeke on 2017/6/9.
 */
public interface LendBlackListService {
    LendBlacklist save(LendBlacklist LendBlacklist);

    LendBlacklist findById(Long id);

    /**
     * 查询列表
     *
     * @param specification
     * @return
     */
    List<LendBlacklist> findList(Specification<LendBlacklist> specification);

    /**
     * 查询列表
     *
     * @param specification
     * @return
     */
    List<LendBlacklist> findList(Specification<LendBlacklist> specification, Sort sort);

    /**
     * 查询列表
     *
     * @param specification
     * @return
     */
    Map<String, Object> findList(Specification<LendBlacklist> specification, Pageable pageable);

    long count(Specification<LendBlacklist> specification);

    void delete(LendBlacklist lendBlacklist);
}
