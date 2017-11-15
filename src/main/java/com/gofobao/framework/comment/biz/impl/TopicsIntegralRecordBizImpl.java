package com.gofobao.framework.comment.biz.impl;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.comment.biz.TopicsIntegralRecordBiz;
import com.gofobao.framework.comment.entity.TopicsIntegralRecord;
import com.gofobao.framework.comment.entity.TopicsUsers;
import com.gofobao.framework.comment.service.TopicsIntegralRecordService;
import com.gofobao.framework.comment.service.TopicsUsersService;
import com.gofobao.framework.comment.vo.response.VoTopicIntegralListResp;
import com.gofobao.framework.comment.vo.response.VoTopicMemberIntegralResp;
import com.gofobao.framework.comment.vo.response.VoTopicsIntegralRecord;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.google.common.base.Preconditions;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TopicsIntegralRecordBizImpl implements TopicsIntegralRecordBiz {

    @Autowired
    TopicsIntegralRecordService topicsIntegralRecordService;

    @Autowired
    TopicsUsersService topicsUsersService;

    @Override
    public ResponseEntity<VoTopicMemberIntegralResp> memberIntegral(@NonNull Long userId) {
        TopicsUsers topicsUsers = null;
        try {
            topicsUsers = topicsUsersService.findByUserId(userId);
        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, e.getMessage(), VoTopicMemberIntegralResp.class));
        }
        Preconditions.checkNotNull(topicsUsers, "TopicsUsers record is empty");
        VoTopicMemberIntegralResp voTopicMemberIntegralResp = VoBaseResp.ok("操作成功", VoTopicMemberIntegralResp.class);
        // 已用资金
        voTopicMemberIntegralResp.setNoUseIntegral(topicsUsers.getNoUseIntegral());
        // 累计总积分
        voTopicMemberIntegralResp.setTotalIntegral(topicsUsers.getNoUseIntegral() + topicsUsers.getUseIntegral());
        // 可用总积分
        voTopicMemberIntegralResp.setUseIntegral(topicsUsers.getUseIntegral());

        return ResponseEntity.ok(VoBaseResp.ok("操作成功", VoTopicMemberIntegralResp.class));


    }

    @Override
    public ResponseEntity<VoTopicIntegralListResp> list(@NonNull Integer pageInde, @NonNull Long userId) {
        pageInde = pageInde - 1;
        Sort sort = new Sort(new Sort.Order(Sort.Direction.DESC, "id"));
        Pageable pageable = new PageRequest(pageInde, 10, sort);
        Specification<TopicsIntegralRecord> topicsIntegralRecordSpecification = Specifications
                .<TopicsIntegralRecord>and()
                .eq("userId", userId)
                .eq("del", 0)
                .build();
        List<TopicsIntegralRecord> topicsIntegralRecordList
                = topicsIntegralRecordService.findAll(topicsIntegralRecordSpecification, pageable);
        VoTopicIntegralListResp voTopicIntegralListResp = VoBaseResp.ok("成功", VoTopicIntegralListResp.class);
        List<VoTopicsIntegralRecord> voTopicCommentItemList = voTopicIntegralListResp.getVoTopicCommentItemList();
        Preconditions.checkNotNull(voTopicCommentItemList, "voTopicCommentItemList collect is empty");
        for (TopicsIntegralRecord item : topicsIntegralRecordList) {
            VoTopicsIntegralRecord record = new VoTopicsIntegralRecord();
            record.setCreateDate(item.getCreateDate());
            record.setOpFlag(item.getOpFlag());
            record.setOpName(item.getOpName());
            record.setOpValue(item.getOpMoney());
            record.setTotalUseIntegral(item.getUseIntegral() + item.getNoUseIntegral());
            record.setUseIntegral(item.getUseIntegral());
            voTopicCommentItemList.add(record);
        }
        return ResponseEntity.ok(voTopicIntegralListResp);
    }
}
