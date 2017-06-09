package com.gofobao.framework.award.biz;

import com.gofobao.framework.award.vo.response.VoViewVirtualBorrowResWarpRes;
import com.gofobao.framework.award.vo.response.VoViewVirtualStatisticsWarpRes;
import com.gofobao.framework.award.vo.response.VoViewVirtualTenderResWarpRes;
import org.springframework.http.ResponseEntity;

/**
 * Created by admin on 2017/6/8.
 */
public interface VirtualBiz {
    /**
     *
     * @param userId
     * @return
     */
    ResponseEntity<VoViewVirtualStatisticsWarpRes>query(Long userId);


    /**
     * 用户投资体验金列表
     * @param userId
     * @return
     */
    ResponseEntity<VoViewVirtualTenderResWarpRes> userTenderList(Long userId);


    /**
     * 投标金列表
     * @return
     */
    ResponseEntity<VoViewVirtualBorrowResWarpRes> list();

}
