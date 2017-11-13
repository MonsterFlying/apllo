package com.gofobao.framework.comment.biz;

import com.gofobao.framework.comment.entity.TopicTopRecord;
import com.gofobao.framework.comment.vo.request.VoTopicTopReq;
import com.gofobao.framework.comment.vo.response.VoTopicTopRecordResp;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public interface TopicTopRecordBiz {

    /**
     * 点赞或者取消点赞
     *
     * @param userId        用户ID
     * @param voTopicTopReq 用户请求数据
     * @return
     * @throws Exception
     */
    ResponseEntity<VoTopicTopRecordResp> topOrCancelTop(final Long userId, final VoTopicTopReq voTopicTopReq) throws Exception;


    /**
     * 根据条件查询点赞请款
     *
     * @param sourceType
     * @param userId
     * @param sourceIds
     * @return
     */
    Map<Long, TopicTopRecord> findTopState(Integer sourceType, Long userId, List<Long> sourceIds);
}
