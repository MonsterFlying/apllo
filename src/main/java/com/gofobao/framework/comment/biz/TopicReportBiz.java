package com.gofobao.framework.comment.biz;

import com.gofobao.framework.comment.vo.request.VoTopicReportReq;
import com.gofobao.framework.core.vo.VoBaseResp;
import org.springframework.http.ResponseEntity;

public interface TopicReportBiz {

    /**
     * 举报帖子, 评论, 回复
     * @param userId
     * @param voTopicReportReq
     * @return
     */
    ResponseEntity<VoBaseResp> report(Long userId, VoTopicReportReq voTopicReportReq) ;
}
