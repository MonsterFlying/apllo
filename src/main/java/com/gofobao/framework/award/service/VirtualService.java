package com.gofobao.framework.award.service;

import com.gofobao.framework.award.vo.request.VoVirtualReq;
import com.gofobao.framework.award.vo.response.AwardStatistics;
import com.gofobao.framework.award.vo.response.VirtualBorrowRes;
import com.gofobao.framework.award.vo.response.VirtualStatistics;
import com.gofobao.framework.award.vo.response.VirtualTenderRes;
import com.gofobao.framework.collection.entity.VirtualCollection;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

/**
 * Created by admin on 2017/6/8.
 */


public interface VirtualService {
    /**
     * 体验金统计
     * @param userId
     * @return
     */
    VirtualStatistics statistics(Long userId);
    /**
     * 用户投标列表
     * @param userId
     * @return
     */
    List<VirtualTenderRes> userTenderList(Long userId);


    /**
     * 体验标列表
     * @return
     */
    List<VirtualBorrowRes>list();


    Boolean tenderCreate(VoVirtualReq voVirtualReq) throws Exception;

    /**
     * 奖励统计
     * @param userId
     * @return
     */
    AwardStatistics query(Long userId);

    VirtualCollection save(VirtualCollection virtualCollection);

    List<VirtualCollection> save(List<VirtualCollection> virtualCollectionList);

    List<VirtualCollection> findList(Specification<VirtualCollection> specification);

    List<VirtualCollection> findList(Specification<VirtualCollection> specification, Sort sort);

    List<VirtualCollection> findList(Specification<VirtualCollection> specification, Pageable pageable);

    long count(Specification<VirtualCollection> specification);
}
