package com.gofobao.framework.comment.biz;

import com.gofobao.framework.comment.entity.TopicComment;
import com.gofobao.framework.comment.entity.TopicReply;

public interface TopicsNoticesBiz {
    /**
     * 获取评论总数
     *
     * @param userId
     * @return
     */
    Long count(Long userId);

    /**
     * 根据条件查询
     *
     * @param userId
     * @param sourceType
     * @param veiwState
     * @return
     */
    Long count(Long userId, Integer sourceType, boolean veiwState);

    /**
     * 根据评论添加推送信息
     * @param topicComment
     * @return
     */
    boolean noticesByComment(TopicComment topicComment) ;


    /**
     *  根据回复添加推送信息
     * @param ropicReply
     * @return
     */
    boolean noticesByReplay(TopicReply ropicReply) ;

}
