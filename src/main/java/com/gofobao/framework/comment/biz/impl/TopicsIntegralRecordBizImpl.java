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
import com.gofobao.framework.member.entity.Users;
import com.gofobao.framework.member.service.UserService;
import com.google.common.base.Preconditions;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class TopicsIntegralRecordBizImpl implements TopicsIntegralRecordBiz {

    @Autowired
    TopicsIntegralRecordService topicsIntegralRecordService;

    @Autowired
    TopicsUsersService topicsUsersService;

    @Autowired
    UserService userService;

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


    final static Map<Integer, String> OP_TYPE_ID_REF = new HashMap<>();

    /**
     * 发帖
     */
    public static final  Integer OP_TYPE_ID_TOPIC = 1 ;
    /**
     * 评论
     */
    public static final  Integer OP_TYPE_ID_COMMENT = 2 ;
    /**
     * 回复
     */
    public static final  Integer OP_TYPE_ID_RPELY = 3 ;
    /**
     * 点赞
     */
    public static final  Integer OP_TYPE_ID_TOP = 4 ;
    /**
     * 取消点赞
     */
    public static final  Integer OP_TYPE_ID_CANCEL_TOP = 5 ;
    /**
     * 每日签到
     */
    public static final  Integer OP_TYPE_ID_SIGN_IN = 6 ;
    /**
     * 兑换
     */
    public static final  Integer OP_TYPE_ID_CONVERT = 7 ;

    static {
        OP_TYPE_ID_REF.put(1, "发帖");
        OP_TYPE_ID_REF.put(2, "评论");
        OP_TYPE_ID_REF.put(3, "回复");
        OP_TYPE_ID_REF.put(4, "点赞");
        OP_TYPE_ID_REF.put(5, "取消点赞");
        OP_TYPE_ID_REF.put(6, "每日签到");
        OP_TYPE_ID_REF.put(7, "兑换");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean operateIntegral(@NonNull Long userId, @NonNull Long value, @NonNull Long sourceId,
                                   @NonNull Integer sourceType, @NonNull Integer opTypeId) throws Exception {
        Date nowDate = new Date();
        String opName = OP_TYPE_ID_REF.get(opTypeId);
        Preconditions.checkNotNull(opName, "opTypeId is invalide");
        Preconditions.checkArgument(value != 0, "value isqural zero");

        Users users = userService.findByIdLock(userId);
        Preconditions.checkNotNull(users, "user record is null");
        TopicsUsers topicsUsers = topicsUsersService.findByUserId(userId);
        if (0 != topicsUsers.getForceState()) {
            throw new Exception("抱歉, 你的账户已被拉黑, 如有疑问请联系客服!");
        }

        topicsUsers.setUseIntegral(topicsUsers.getUseIntegral() + value);
        if (topicsUsers.getUseIntegral() < 0) {
            throw new Exception("积分变动后金额为负数");
        }
        if (value < 0) {
            // 负负得正
            topicsUsers.setNoUseIntegral(topicsUsers.getNoUseIntegral() - value);
        }
        topicsUsers.setUpdateDate(nowDate);
        topicsUsers = topicsUsersService.save(topicsUsers);

        // 保存积分变动记录
        TopicsIntegralRecord record = new TopicsIntegralRecord();
        record.setCreateDate(nowDate);
        record.setUpdateDate(nowDate);
        record.setUserId(userId);
        record.setDel(0);
        record.setNoUseIntegral(topicsUsers.getNoUseIntegral());
        record.setUseIntegral(topicsUsers.getUseIntegral());
        record.setOpFlag(value < 0 ? "C" : "D");
        record.setOpName(opName);
        record.setOpMoney(value < 0 ? -value : value);
        record.setSourceId(sourceId);
        record.setSourceType(sourceType);
        topicsIntegralRecordService.save(record) ;
        return true;
    }
}
