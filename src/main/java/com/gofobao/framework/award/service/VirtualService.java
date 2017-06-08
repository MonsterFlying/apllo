package com.gofobao.framework.award.service;

import com.gofobao.framework.award.vo.response.VirtualStatistics;

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
}
