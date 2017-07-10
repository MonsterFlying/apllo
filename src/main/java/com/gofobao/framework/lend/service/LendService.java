package com.gofobao.framework.lend.service;

import com.gofobao.framework.common.page.Page;
import com.gofobao.framework.lend.entity.Lend;
import com.gofobao.framework.lend.vo.request.VoUserLendReq;
import com.gofobao.framework.lend.vo.response.LendInfo;
import com.gofobao.framework.lend.vo.response.UserLendInfo;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Map;

/**
 * Created by Zeke on 2017/6/1.
 */
public interface LendService {
    Lend insert(Lend lend);

    boolean updateById(Lend lend);

    Lend findByIdLock(Long id);

    Lend findById(Long id);

    Map<String, Object> list(Page page);


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

    List<UserLendInfo>queryUser(VoUserLendReq voUserLendReq);


    long count(Specification<Lend> specification);

    Lend save(Lend lend);

    List<Lend> save(List<Lend> lendList);
}
