package com.gofobao.framework.comment.service.impl;

import alex.zhrenjie04.wordfilter.WordFilterUtil;
import alex.zhrenjie04.wordfilter.result.FilteredResult;
import com.gofobao.framework.comment.biz.TopicTopRecordBiz;
import com.gofobao.framework.comment.biz.TopicsNoticesBiz;
import com.gofobao.framework.comment.entity.Topic;
import com.gofobao.framework.comment.entity.TopicComment;
import com.gofobao.framework.comment.entity.TopicTopRecord;
import com.gofobao.framework.comment.entity.TopicsUsers;
import com.gofobao.framework.comment.repository.TopicCommentRepository;
import com.gofobao.framework.comment.repository.TopicRepository;
import com.gofobao.framework.comment.service.TopicCommentService;
import com.gofobao.framework.comment.service.TopicsUsersService;
import com.gofobao.framework.comment.vo.request.VoTopicCommentReq;
import com.gofobao.framework.comment.vo.response.VoTopicCommentItem;
import com.gofobao.framework.comment.vo.response.VoTopicCommentListResp;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.security.helper.JwtTokenHelper;
import com.google.common.base.Preconditions;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

import static com.sun.tools.internal.xjc.reader.Ring.add;

/**
 * Created by xin on 2017/11/10.
 */
@Service
@Slf4j
public class TopicCommentServiceImpl implements TopicCommentService {
    @Autowired
    private TopicCommentRepository topicCommentRepository;

    @Autowired
    private TopicRepository topicRepository;


    @Autowired
    private TopicsNoticesBiz topicsNoticesBiz;

    @Autowired
    JwtTokenHelper jwtTokenHelper;

    @Autowired
    private TopicsUsersService topicsUsersService;

    @Value("${qiniu.domain}")
    private String imgDomain;

    @Autowired
    TopicTopRecordBiz topicTopRecordBiz;


    @Override
    @Transactional
    public ResponseEntity<VoBaseResp> publishComment(@NonNull VoTopicCommentReq voTopicCommentReq,
                                                     @NonNull Long userId) {
        //判断用户
        TopicsUsers topicsUsers = null;
        try {
            topicsUsers = topicsUsersService.findByUserId(userId);
        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, e.getMessage()));
        }
        //TopicsUsers topicsUsers = topicsUsersRepository.findByUserId(userId);
        Preconditions.checkNotNull(topicsUsers, "用户不存在");
        if (topicsUsers.getForceState() != 0) {
            return ResponseEntity.ok(VoBaseResp.ok("用户已被禁言", VoBaseResp.class));
        }

        //判断话题id是否存在?
        Topic topic = topicRepository.findByIdAndDel(voTopicCommentReq.getTopicId(), 0);
        Preconditions.checkNotNull(topic, "topic is not exist");

        Date nowDate = new Date();
        TopicComment topicComment = new TopicComment();
        topicComment.setTopicId(voTopicCommentReq.getTopicId());
        topicComment.setUserId(userId);
        topicComment.setUserName(topicsUsers.getUsername());
        topicComment.setUserIconUrl(topicsUsers.getAvatar());
        topicComment.setCreateDate(nowDate);
        topicComment.setUpdateDate(nowDate);
        topicComment.setTopicTypeId(topic.getTopicTypeId());
        // 用户内容铭感词过滤
        FilteredResult filteredResult = WordFilterUtil.filterText(voTopicCommentReq.getContent(), '*');
        topicComment.setContent(filteredResult.getFilteredContent());
        TopicComment lastComment = topicCommentRepository.findTopByUserIdOrderByIdDesc(userId);
        if (!ObjectUtils.isEmpty(lastComment)) {
            Date createDate = lastComment.getCreateDate();
            createDate = ObjectUtils.isArray(createDate) ? nowDate : createDate;
            if (nowDate.getTime() - (createDate.getTime() + DateHelper.MILLIS_PER_MINUTE) < 0) {
                return ResponseEntity
                        .badRequest()
                        .body(VoBaseResp.error(VoBaseResp.ERROR, "评论过于频繁, 请稍后操作!"));
            }
        }

        TopicComment commentResult = topicCommentRepository.save(topicComment);
        Preconditions.checkNotNull(commentResult, "comment is fail");
        //发布成功修改评论总数
        topicRepository.updateToTalComment(topicComment.getTopicId());
        topicsNoticesBiz.noticesByComment(topicComment);
        return ResponseEntity.ok(VoBaseResp.ok("发布成功", VoBaseResp.class));
    }

    @Override
    public TopicComment findById(Long id) {
        return topicCommentRepository.findOne(id);
    }

    @Override
    public void batchUpdateRedundancy(Long userId, String username, String avatar) throws Exception {
        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(avatar)) {
            throw new Exception("参数错误!");
        }
        if (!StringUtils.isEmpty(username)) {
            topicCommentRepository.batchUpateUsernameByUserId(userId, username);
        } else {
            topicCommentRepository.batchUpateAvatarByUserId(userId, avatar);
        }
    }

    @Override
    public TopicComment save(TopicComment topicComment) {
        return topicCommentRepository.save(topicComment);
    }

    @Override
    public ResponseEntity<VoTopicCommentListResp> listDetail(HttpServletRequest httpServletRequest, Long topicId, Integer pageIndex) {
        pageIndex = pageIndex - 1;
        Pageable pageable = new PageRequest(pageIndex, 10);
        List<TopicComment> topicComments = topicCommentRepository.findByTopicIdAndDelOrderByIdAsc(topicId, 0, pageable);
        VoTopicCommentListResp voTopicCommentListResp = VoBaseResp.ok("操作成功", VoTopicCommentListResp.class);
        List<VoTopicCommentItem> result = voTopicCommentListResp.getVoTopicCommentItemList();

        for (TopicComment topicComment : topicComments) {
            VoTopicCommentItem voTopicCommentItem = new VoTopicCommentItem();
            voTopicCommentItem.setContent(topicComment.getContent());
            voTopicCommentItem.setUserName(topicComment.getUserName());
            voTopicCommentItem.setCommentId(topicComment.getId());
            voTopicCommentItem.setUserIconUrl(imgDomain + "/" + topicComment.getUserIconUrl());
            //评论时间分析
            long publishTime = topicComment.getCreateDate().getTime();
            voTopicCommentItem.setTime(DateHelper.getPastTime(publishTime));
            result.add(voTopicCommentItem);
        }

        // 点赞
        try {

            String token = jwtTokenHelper.getToken(httpServletRequest);
            if (!StringUtils.isEmpty(token)
                    && !CollectionUtils.isEmpty(result)) {
                Long visterId = jwtTokenHelper.getUserIdFromToken(token);
                Set<Long> commonIds = result
                        .stream()
                        .map(voTopicCommentItem -> voTopicCommentItem.getCommentId()).collect(Collectors.toSet());
                Map<Long, TopicTopRecord> topicTopRecordMap
                        = topicTopRecordBiz.findTopState(1, visterId, new ArrayList<>(commonIds));
                Preconditions.checkNotNull(topicTopRecordMap, "topicTopRecordMap record is empty");
                for (VoTopicCommentItem item : result) {
                    TopicTopRecord topicTopRecord = topicTopRecordMap.get(item.getCommentId());
                    if (!ObjectUtils.isEmpty(topicTopRecord)) {
                        item.setTopState(true);
                    }
                }
            }


        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "系统异常", VoTopicCommentListResp.class));
        }


        return ResponseEntity.ok(voTopicCommentListResp);
    }
}
