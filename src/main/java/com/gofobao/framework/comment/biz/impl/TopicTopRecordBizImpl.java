package com.gofobao.framework.comment.biz.impl;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.comment.biz.TopicTopRecordBiz;
import com.gofobao.framework.comment.entity.TopicTopRecord;
import com.gofobao.framework.comment.service.TopicService;
import com.gofobao.framework.comment.service.TopicTopRecordService;
import com.gofobao.framework.comment.vo.request.VoTopicTopReq;
import com.gofobao.framework.comment.vo.response.VoTopicTopRecordResp;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.member.entity.Users;
import com.gofobao.framework.member.service.UserService;
import com.google.common.base.Preconditions;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 用户点赞业务类
 */
@Component
@Slf4j
public class TopicTopRecordBizImpl implements TopicTopRecordBiz {

    @Autowired
    TopicTopRecordService topicTopRecordService;

    @Autowired
    UserService userService;

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
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<VoTopicTopRecordResp> topOrCancelTop(@NonNull Long userId,
                                                               @NonNull VoTopicTopReq voTopicTopReq) throws Exception {

        if (!ArrayUtils.contains(OK_TYPES, voTopicTopReq.getSourceType())) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "来源类型错误", VoTopicTopRecordResp.class));
        }

        Users users = userService.findByIdLock(userId);
        Preconditions.checkNotNull(users, "find user record is empty!");

        List<TopicTopRecord> topicTopRecordList = topicTopRecordService.findByUserIdAndSourceIdAndSourceType(userId,
                voTopicTopReq.getSoucreId(),
                voTopicTopReq.getSourceType());
        VoTopicTopRecordResp voTopicTopRecordResp = VoBaseResp.ok("操作成功", VoTopicTopRecordResp.class);

        if (CollectionUtils.isEmpty(topicTopRecordList)) {
            // 添加点赞
            Date nowDate = new Date();
            TopicTopRecord topicTopRecord = new TopicTopRecord();
            topicTopRecord.setCreateDate(nowDate);
            topicTopRecord.setUpdateDate(nowDate);
            topicTopRecord.setSourceId(voTopicTopReq.getSoucreId());
            topicTopRecord.setSourceType(voTopicTopReq.getSourceType());
            topicTopRecord.setUserId(userId);
            try {
                topicTopRecordService.save(topicTopRecord);
                voTopicTopRecordResp.setTypeState(0);
            } catch (Exception ex) {
                log.error("topic top reocrd save error!", ex);
                throw new Exception(ex);
            }
        } else {
            // 删除点赞
            try {
                topicTopRecordService.delete(topicTopRecordList);
                voTopicTopRecordResp.setTypeState(1);
            } catch (Exception ex) {
                log.error("topic top reocrd delete error!", ex);
                throw new Exception(ex);
            }
        }

        return ResponseEntity.ok(voTopicTopRecordResp);
    }

    @Override
    public Map<Long, TopicTopRecord> findTopState(@NonNull final Integer sourceType,
                                                  @NonNull final Long userId,
                                                  @NonNull final List<Long> sourceIds) {
        Specification<TopicTopRecord> specification = Specifications
                .<TopicTopRecord>and()
                .eq("userId", userId)
                .eq("sourceType", sourceType)
                .in("sourceId", sourceIds.toArray())
                .build();

        List<TopicTopRecord> topicTopRecordList = topicTopRecordService.find(specification);
        Map<Long, TopicTopRecord> tempMap = topicTopRecordList.stream()
                .collect(Collectors.toMap(TopicTopRecord::getSourceId, Function.identity()));

        Map<Long/** 源ID*/, TopicTopRecord> ref = new HashMap<>(10);
        for (Long item : sourceIds) {
            TopicTopRecord topicTopRecord = tempMap.get(item);
            if (ObjectUtils.isEmpty(topicTopRecord)) {
                ref.put(item, null);
            } else {
                ref.put(item, topicTopRecord);
            }
        }

        return ref;
    }
}
