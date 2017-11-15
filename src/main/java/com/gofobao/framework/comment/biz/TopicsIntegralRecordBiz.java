package com.gofobao.framework.comment.biz;

import com.gofobao.framework.comment.vo.response.VoTopicIntegralListResp;
import com.gofobao.framework.comment.vo.response.VoTopicMemberIntegralResp;
import org.springframework.http.ResponseEntity;

public interface TopicsIntegralRecordBiz {
    /**
     * 我的积分
     *
     * @param userId
     * @return
     */
    ResponseEntity<VoTopicMemberIntegralResp> memberIntegral(Long userId);

    /**
     * 查询积分记录
     *
     * @param pageInde
     * @param userId
     * @return
     */
    ResponseEntity<VoTopicIntegralListResp> list(Integer pageInde, Long userId);

    /**
     * 操作积分
     * @param userId 用户ID
     * @param value 金额(可分正负)
     * @param sourceId 积分操作触发来源
     * @param sourceType 触发来源类型
     * @param opTypeId 此次变动类型
     * @return
     */
    boolean operateIntegral(Long userId, Long value, Long sourceId, Integer sourceType, Integer opTypeId) throws Exception;

}
