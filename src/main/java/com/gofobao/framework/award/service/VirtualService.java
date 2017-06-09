package com.gofobao.framework.award.service;

import com.gofobao.framework.award.vo.response.VirtualBorrowRes;
import com.gofobao.framework.award.vo.response.VirtualStatistics;
import com.gofobao.framework.award.vo.response.VirtualTenderRes;

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
}
