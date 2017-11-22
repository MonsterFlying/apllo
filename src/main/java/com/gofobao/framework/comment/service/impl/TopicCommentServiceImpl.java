package com.gofobao.framework.comment.service.impl;

import alex.zhrenjie04.wordfilter.WordFilterUtil;
import alex.zhrenjie04.wordfilter.result.FilteredResult;
import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.comment.biz.TopicTopRecordBiz;
import com.gofobao.framework.comment.biz.TopicsNoticesBiz;
import com.gofobao.framework.comment.biz.TopisIntegralBiz;
import com.gofobao.framework.comment.entity.*;
import com.gofobao.framework.comment.repository.TopicCommentRepository;
import com.gofobao.framework.comment.repository.TopicReplyRepository;
import com.gofobao.framework.comment.repository.TopicRepository;
import com.gofobao.framework.comment.repository.TopicsUsersRepository;
import com.gofobao.framework.comment.service.TopicCommentService;
import com.gofobao.framework.comment.service.TopicsUsersService;
import com.gofobao.framework.comment.vo.request.VoTopicCommentReq;
import com.gofobao.framework.comment.vo.response.VoReplyDetailItem;
import com.gofobao.framework.comment.vo.response.VoTopicCommentItem;
import com.gofobao.framework.comment.vo.response.VoTopicCommentListResp;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.security.helper.JwtTokenHelper;
import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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

    @Autowired
    private TopicsUsersRepository topicsUsersRepository;

    @Autowired
    TopisIntegralBiz topisIntegralBiz;

    @Value("${qiniu.domain}")
    private String imgDomain;

    @Autowired
    TopicTopRecordBiz topicTopRecordBiz;

    @Autowired
    TopicReplyRepository topicReplyRepository;

    LoadingCache<Long, TopicsUsers> userCache = CacheBuilder
            .newBuilder()
            .expireAfterAccess(5, TimeUnit.MINUTES)
            .maximumSize(1024)
            .build(new CacheLoader<Long, TopicsUsers>() {
                @Override
                public TopicsUsers load(Long userId) throws Exception {
                    //查询当前登录用户
                    TopicsUsers topicsUsers = topicsUsersService.findByUserId(userId);
                    return topicsUsers;
                }
            });

    LoadingCache<Long, Topic> topicCache = CacheBuilder
            .newBuilder()
            .expireAfterAccess(5, TimeUnit.MINUTES)
            .maximumSize(1024)
            .build(new CacheLoader<Long, Topic>() {
                @Override
                public Topic load(Long topicId) throws Exception {
                    Topic topic = topicRepository.findByIdAndDel(topicId, 0);
                    return topic;
                }
            });

    LoadingCache<String, List<TopicComment>> topicCommentCache = CacheBuilder
            .newBuilder()
            .expireAfterAccess(5, TimeUnit.MINUTES)
            .maximumSize(1024)
            .build(new CacheLoader<String, List<TopicComment>>() {
                @Override
                public List<TopicComment> load(String s) throws Exception {
                    String comments[] = s.split(":");
                    PageRequest pageable = new PageRequest(Integer.valueOf(comments[1]), 10);
                    List<TopicComment> commentList = topicCommentRepository.findByTopicIdAndDelOrderByIdAsc(Long.valueOf(comments[0]),
                            0, pageable);
                    return commentList;
                }
            });

    LoadingCache<Long, TopicComment> lastCommentCache = CacheBuilder
            .newBuilder()
            .expireAfterAccess(5, TimeUnit.MINUTES)
            .maximumSize(1024)
            .build(new CacheLoader<Long, TopicComment>() {
                @Override
                public TopicComment load(Long userId) throws Exception {
                    TopicComment topicComment = topicCommentRepository.findTopByUserIdOrderByIdDesc(userId);
                    return topicComment;
                }
            });

    @Override
    @Transactional
    public ResponseEntity<VoBaseResp> publishComment(@NonNull VoTopicCommentReq voTopicCommentReq,
                                                     @NonNull Long userId) {
        // 判断用户
        TopicsUsers topicsUsers;
        try {
            topicsUsers = userCache.get(userId);
            //topicsUsers = topicsUsersService.findByUserId(userId);
        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, e.getMessage()));
        }
        Preconditions.checkNotNull(topicsUsers, "用户不存在");
        if (topicsUsers.getForceState() != 0) {
            return ResponseEntity.ok(VoBaseResp.ok("用户已被禁言", VoBaseResp.class));
        }

        //判断话题id是否存在?
        Topic topic = null;
        try {
            topic = topicCache.get(voTopicCommentReq.getTopicId());
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        //Topic topic = topicRepository.findByIdAndDel(voTopicCommentReq.getTopicId(), 0);
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
        TopicComment lastComment = null;
        try {
            lastComment = lastCommentCache.get(userId);
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        //TopicComment lastComment = topicCommentRepository.findTopByUserIdOrderByIdDesc(userId);
        if (!ObjectUtils.isEmpty(lastComment)) {
            Date createDate = lastComment.getCreateDate();
            createDate = ObjectUtils.isArray(createDate) ? nowDate : createDate;
            if (nowDate.getTime() - (createDate.getTime() + DateHelper.MILLIS_PER_MINUTE) < 0) {
                return ResponseEntity
                        .badRequest()
                        .body(VoBaseResp.error(VoBaseResp.ERROR, "评论过于频繁, 请1分钟后再试!"));
            }
        }

        TopicComment commentResult = topicCommentRepository.save(topicComment);
        Preconditions.checkNotNull(commentResult, "comment is fail");
        //发布成功修改评论总数
        topicRepository.updateToTalComment(topicComment.getTopicId());
        // 发送通知
        topicsNoticesBiz.noticesByComment(topicComment);
        // 发送积分
        topisIntegralBiz.publishComment(topicComment);
        //发布评论之后清除缓存
        topicCache.invalidateAll();
        topicCommentCache.invalidateAll();
        lastCommentCache.invalidateAll();
        //评论完之后推送消息给发帖人

        return ResponseEntity.ok(VoBaseResp.ok("发布成功", VoBaseResp.class));
    }

    @Override
    public TopicComment findById(Long id) {
        return topicCommentRepository.findOne(id);
    }

    @Override
    @Transactional
    public void batchUpdateRedundancy(Long userId, String username, String avatar) throws Exception {
        if (StringUtils.isEmpty(username) && StringUtils.isEmpty(avatar)) {
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
    public ResponseEntity<VoTopicCommentListResp> listDetail(HttpServletRequest httpServletRequest,
                                                             Long topicId, Integer pageIndex, boolean hot) {
        pageIndex = pageIndex - 1;
        List<TopicComment> topicComments;
        if (hot) {
            //如果是热点评论进行先根据点赞数排序,然后根据评论数排序
            Sort sorts = new Sort(Sort.Direction.ASC, "topTotalNum")
                    .and(new Sort(Sort.Direction.ASC, "contentTotalNum"));
            Pageable pageable = new PageRequest(pageIndex, 10, sorts);
            topicComments = topicCommentRepository.findByTopicIdAndDel(topicId, 0, pageable);
        } else {
            Pageable pageable = new PageRequest(pageIndex, 10);
            topicComments = topicCommentRepository.findByTopicIdAndDelOrderByIdAsc(topicId, 0, pageable);
        }


        VoTopicCommentListResp voTopicCommentListResp = VoBaseResp.ok("操作成功", VoTopicCommentListResp.class);
        List<VoTopicCommentItem> result = voTopicCommentListResp.getVoTopicCommentItemList();

        Set<Long> commonIds = null;
        Map<Long/** commid*/, List<TopicReply>> commentIdsMap = null;
        if (!CollectionUtils.isEmpty(topicComments)) {
            commonIds = topicComments
                    .stream()
                    .map(topicComment -> topicComment.getId()).collect(Collectors.toSet());
            // 取回复
            Specification<TopicReply> topicReplySpecification = Specifications
                    .<TopicReply>and()
                    .eq("topicId", topicId)
                    .eq("del", 0)
                    .in("topicCommentId", commonIds.toArray())
                    .build();
            List<TopicReply> replyLists = topicReplyRepository.findAll(topicReplySpecification);
            if (!CollectionUtils.isEmpty(replyLists)) {
                commentIdsMap = replyLists
                        .stream()
                        .collect(Collectors.groupingBy(TopicReply::getTopicCommentId, Collectors.toList()));
            }
        }

        for (TopicComment topicComment : topicComments) {
            VoTopicCommentItem voTopicCommentItem = new VoTopicCommentItem();
            voTopicCommentItem.setContent(topicComment.getContent());
            voTopicCommentItem.setUserName(topicComment.getUserName());
            voTopicCommentItem.setCommentId(topicComment.getId());
            voTopicCommentItem.setTopTotalNum(topicComment.getTopTotalNum().toString());
            voTopicCommentItem.setUserIconUrl(imgDomain + "/" + topicComment.getUserIconUrl());
            if (!CollectionUtils.isEmpty(commentIdsMap)) {
                List<TopicReply> topicReplies = commentIdsMap.get(topicComment.getId());
                if (!CollectionUtils.isEmpty(topicReplies)) {
                    List<VoReplyDetailItem> replyList = voTopicCommentItem.getReplyList();
                    VoReplyDetailItem voReplyDetailItem = null;
                    for (TopicReply item : topicReplies) {
                        // id 升序
                        voReplyDetailItem = new VoReplyDetailItem();
                        voReplyDetailItem.setCommentId(topicComment.getId());
                        voReplyDetailItem.setReplyId(item.getId());
                        voReplyDetailItem.setContent(item.getContent());
                        voReplyDetailItem.setFromUserName(item.getUserName());
                        voReplyDetailItem.setToUserName(item.getForUserName());
                        replyList.add(voReplyDetailItem);
                    }
                }
            }
            //评论时间分析
            long publishTime = topicComment.getCreateDate().getTime();
            voTopicCommentItem.setTime(DateHelper.getPastTime(publishTime));

            result.add(voTopicCommentItem);
        }

        // 点赞
        try {
            String token = jwtTokenHelper.getToken(httpServletRequest);
            if (!StringUtils.isEmpty(token)
                    && !CollectionUtils.isEmpty(commonIds)) {
                Long visterId = jwtTokenHelper.getUserIdFromToken(token);
                Map<Long, TopicTopRecord> topicTopRecordMap
                        = topicTopRecordBiz.findTopState(1, visterId, new ArrayList<>(commonIds));
                Preconditions.checkNotNull(topicTopRecordMap, "topicTopRecordMap record is empty");
                for (VoTopicCommentItem item : result) {
                    TopicTopRecord topicTopRecord = topicTopRecordMap.get(item.getCommentId());
                    if (!ObjectUtils.isEmpty(topicTopRecord)) {
                        item.setTopState(true);
                        //点赞后清除topic缓存
                        topicCache.invalidateAll();
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

    @Override
    public ResponseEntity<VoBaseResp> delComment(Long topicCommentId, Long userId) {
        if (ObjectUtils.isEmpty(userId)) {
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "无权删除", VoBaseResp.class));
        }
        TopicsUsers topicsUsers = topicsUsersRepository.findByUserId(userId);
        Preconditions.checkNotNull(topicsUsers, "用户不存在");
        if (!topicsUsers.getUserId().equals(userId)) {
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "无权删除", VoBaseResp.class));
        }
        Integer updateCommentCount = topicCommentRepository.updateOneComment(topicCommentId);
        if (ObjectUtils.isEmpty(updateCommentCount)) {
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "删除评论失败", VoBaseResp.class));
        }
        //成功删除评论后删除评论下的所有回复
        Integer updateReplyCount = topicReplyRepository.updateByComment(topicCommentId);
        if (ObjectUtils.isEmpty(updateReplyCount)) {
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "删除评论失败", VoBaseResp.class));
        }
        //删除评论成功后清除topic缓存
        topicCache.invalidateAll();
        //清除comment缓存
        topicCommentCache.invalidateAll();

        return ResponseEntity.ok(VoBaseResp.ok("删除评论成功", VoBaseResp.class));
    }

}
