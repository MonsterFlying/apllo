package com.gofobao.framework.comment.biz.impl;

import com.gofobao.framework.comment.biz.TopicsIntegralRecordBiz;
import com.gofobao.framework.comment.entity.TopicsUsers;
import com.gofobao.framework.comment.service.TopicsIntegralRecordService;
import com.gofobao.framework.comment.service.TopicsUsersService;
import com.gofobao.framework.comment.vo.response.VoTopicMemberIntegralResp;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.google.common.base.Preconditions;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

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
                    .body(VoBaseResp.error(VoBaseResp.ERROR, e.getMessage(), VoTopicMemberIntegralResp.class)) ;
        }
        Preconditions.checkNotNull(topicsUsers, "TopicsUsers record is empty");
        VoTopicMemberIntegralResp voTopicMemberIntegralResp = VoBaseResp.ok("操作成功", VoTopicMemberIntegralResp.class);
        // 已用资金
        voTopicMemberIntegralResp.setNoUseIntegral(topicsUsers.getNoUseIntegral()) ;
        //
        voTopicMemberIntegralResp.setTotalIntegral(topicsUsers.getNoUseIntegral() +topicsUsers.getUseIntegral());
        voTopicMemberIntegralResp.setUseIntegral(topicsUsers.getUseIntegral());

        return null;
    }
}
