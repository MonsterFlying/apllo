package com.gofobao.framework.comment.biz.impl;

import com.gofobao.framework.comment.biz.TopicReportBiz;
import com.gofobao.framework.comment.entity.Topic;
import com.gofobao.framework.comment.entity.TopicComment;
import com.gofobao.framework.comment.entity.TopicReply;
import com.gofobao.framework.comment.entity.TopicReport;
import com.gofobao.framework.comment.service.TopicCommentService;
import com.gofobao.framework.comment.service.TopicReplyService;
import com.gofobao.framework.comment.service.TopicReportService;
import com.gofobao.framework.comment.service.TopicService;
import com.gofobao.framework.comment.vo.request.VoTopicReportReq;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.member.entity.Users;
import com.gofobao.framework.member.service.UserService;
import com.google.common.base.Preconditions;
import lombok.NonNull;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.Date;
import java.util.List;

@Component
public class TopicReportBizImpl implements TopicReportBiz {
    @Autowired
    TopicReportService topicReportService;

    @Autowired
    UserService userService;

    @Autowired
    TopicService topicService;

    @Autowired
    TopicCommentService topicCommentService;

    @Autowired
    TopicReplyService topicReplyService;

    /**
     * 0: 广告
     */
    private final static Integer REPORT_TYPE_0 = 0;
    /**
     * 1: 政治有害类
     */
    private final static Integer REPORT_TYPE_1 = 1;
    /**
     * 2: 暴恐类
     */
    private final static Integer REPORT_TYPE_2 = 2;
    /**
     * 3:淫秽色情类
     */
    private final static Integer REPORT_TYPE_3 = 3;
    /**
     * 4:赌博类
     */
    private final static Integer REPORT_TYPE_4 = 4;
    /**
     * 5:诈骗类
     */
    private final static Integer REPORT_TYPE_5 = 5;
    /**
     * 6:其他有害类
     */
    private final static Integer REPORT_TYPE_6 = 6;

    /**
     * 集合类
     */
    private final static Integer[] REPORT_TYPES = {REPORT_TYPE_0, REPORT_TYPE_1, REPORT_TYPE_2,
            REPORT_TYPE_3, REPORT_TYPE_4, REPORT_TYPE_5,
            REPORT_TYPE_6};
    /**
     * 点赞帖子
     */
    private final static Integer TOP_TYPE_TOPIC = 0;

    /**
     * 点赞评论
     */
    private final static Integer TOP_TYPE_COMMENT = 1;

    /**
     * 点赞回复
     */
    private final static Integer TOP_TYPE_REPLY = 2;

    /**
     * 点赞来源类型
     */
    private final static Integer[] OK_TYPES = {TOP_TYPE_COMMENT, TOP_TYPE_TOPIC, TOP_TYPE_REPLY};

    @Override
    public ResponseEntity<VoBaseResp> report(@NonNull final Long userId,
                                             @NonNull final VoTopicReportReq voTopicReportReq) {
        // 验证来源
        if (!ArrayUtils.contains(OK_TYPES, voTopicReportReq.getSourceType())) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "来源类型错误"));
        }

        if (!ArrayUtils.contains(REPORT_TYPES, voTopicReportReq.getReportType())) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "未知举报类型"));
        }

        Long reportUserId = null;
        if (TOP_TYPE_TOPIC.equals(voTopicReportReq.getSourceType())) {
            Topic topic = topicService.findById(voTopicReportReq.getSoucreId());
            Preconditions.checkNotNull(topic, "find topic record is empty");
            reportUserId = topic.getUserId();
        } else if (TOP_TYPE_COMMENT.equals(voTopicReportReq.getSourceType())) {
            TopicComment topicComment = topicCommentService.findById(voTopicReportReq.getSoucreId());
            Preconditions.checkNotNull(topicComment, "find topicComment record is empty");
            reportUserId = topicComment.getUserId();
        } else if (TOP_TYPE_REPLY.equals(voTopicReportReq.getSourceType())) {
            TopicReply topicReply = topicReplyService.findById(voTopicReportReq.getSoucreId());
            Preconditions.checkNotNull(topicReply, "find topicReply record is empty");
            reportUserId = topicReply.getUserId();
        }

        Date nowDate = new Date();
        // 查询用户
        Users user = userService.findById(userId);
        Preconditions.checkNotNull(user, "find user record is empty");

        // 查找用户最近一条评论, 是否过于频繁
        TopicReport topicReport = topicReportService.findTopByUserIdOrderByIdDesc(userId);
        if (!ObjectUtils.isEmpty(topicReport)) {
            Date createDate = topicReport.getCreateDate();
            createDate = ObjectUtils.isArray(createDate) ? nowDate : createDate;
            if (nowDate.getTime() - (createDate.getTime() + 5 * 60 * 1000) < 0) {
                return ResponseEntity
                        .badRequest()
                        .body(VoBaseResp.error(VoBaseResp.ERROR, "举报过于频繁, 请稍后操作!"));
            }
        }

        // 是否已经评论过
        List<TopicReport> topicReportList = topicReportService.findByUserIdAndSourceIdAndSourceType(userId,
                voTopicReportReq.getSoucreId(),
                voTopicReportReq.getSourceType());

        if (!CollectionUtils.isEmpty(topicReportList)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "你已经举成功举报该用户, 无需重复操作!"));
        }

        TopicReport save = new TopicReport();
        save.setCreateDate(nowDate);
        save.setUpdateDate(nowDate);
        save.setReportType(voTopicReportReq.getReportType());
        save.setSourceId(voTopicReportReq.getSoucreId());
        save.setSourceType(voTopicReportReq.getSourceType());
        save.setUserId(userId);
        save.setReportUserId(reportUserId);
        save.setResponseState(0);
        topicReportService.save(save);
        return ResponseEntity.ok(VoBaseResp.ok("操作成功"));
    }
}
