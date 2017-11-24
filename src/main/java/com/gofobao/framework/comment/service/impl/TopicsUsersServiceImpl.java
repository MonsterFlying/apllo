package com.gofobao.framework.comment.service.impl;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.comment.biz.TopicTopRecordBiz;
import com.gofobao.framework.comment.entity.*;
import com.gofobao.framework.comment.repository.TopicCommentRepository;
import com.gofobao.framework.comment.repository.TopicReplyRepository;
import com.gofobao.framework.comment.repository.TopicRepository;
import com.gofobao.framework.comment.repository.TopicsUsersRepository;
import com.gofobao.framework.comment.service.TopicsUsersService;
import com.gofobao.framework.comment.vo.response.VoTopicCommentManagerListResp;
import com.gofobao.framework.comment.vo.response.VoTopicCommentManagerResp;
import com.gofobao.framework.comment.vo.response.VoTopicListResp;
import com.gofobao.framework.comment.vo.response.VoTopicResp;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.RandomUtil;
import com.gofobao.framework.member.entity.UserThirdAccount;
import com.gofobao.framework.member.entity.Users;
import com.gofobao.framework.member.service.UserService;
import com.gofobao.framework.member.service.UserThirdAccountService;
import com.gofobao.framework.security.helper.JwtTokenHelper;
import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtils;
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
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TopicsUsersServiceImpl implements TopicsUsersService {

    @Autowired
    TopicsUsersRepository topicsUsersRepository;

    @Autowired
    UserService userService;

    @Autowired
    TopicRepository topicRepository;

    @Value("${qiniu.domain}")
    private String imgPrefix;

    @Autowired
    private TopicTopRecordBiz topicTopRecordBiz;

    @Autowired
    private TopicCommentRepository topicCommentRepository;

    @Autowired
    private TopicsUsersService topicsUsersService;

    @Autowired
    private TopicReplyRepository topicReplyRepository;

    @Autowired
    JwtTokenHelper jwtTokenHelper;

    public static final Integer CONTENT_COMMENT_LIMIT = 999;

    public static final int CONTENT_SHOW_LIMIT = 100;

    public static final Integer CONTENT_TOP_LIMIT = 999;

    @Autowired
    UserThirdAccountService userThirdAccountService;

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


    @Override
    @Transactional(rollbackFor = Exception.class)
    public TopicsUsers findByUserId(Long userId) throws Exception {
        Users users = userService.findByIdLock(userId);
        Preconditions.checkNotNull(users, "find user record is empty");
        Specification<TopicsUsers> specification = Specifications
                .<TopicsUsers>and()
                .eq("userId", userId)
                .build();
        TopicsUsers topicsUsers = topicsUsersRepository.findOne(specification);
        if (ObjectUtils.isEmpty(topicsUsers)) {
            // 判断用户是否开户
            UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(userId);
            if (ObjectUtils.isEmpty(userThirdAccount)) {
                throw new Exception("禁止未开户用户操作社区");
            }
            Date nowDate = new Date();
            TopicsUsers save = new TopicsUsers();
            String username = users.getUsername();
            if (StringUtils.isEmpty(username)) {
                username = "a_z_" + RandomUtil.getRandomString(5) + userId;
            }
            save.setUsername(username);
            save.setAvatar(users.getAvatarPath());
            save.setUserId(userId);
            save.setLevelId(1L);
            save.setCreateDate(nowDate);
            save.setUpdateDate(nowDate);
            save.setForceState(0);
            save.setNoUseIntegral(0L);
            save.setUseIntegral(0L);
            return topicsUsersRepository.save(save);
        } else {
            return topicsUsers;
        }
    }

    @Override
    public TopicsUsers save(@NonNull TopicsUsers topicsUsers) {
        return topicsUsersRepository.save(topicsUsers);
    }

    @Override
    public ResponseEntity<VoTopicListResp> listUserTopic(Long topicTypeId, Long userId, Integer pageable,
                                                         HttpServletRequest httpServletRequest) {
        PageRequest pageRequest = new PageRequest(pageable - 1, 10,
                new Sort(new Sort.Order(Sort.Direction.DESC, "id")));

        List<Topic> topics = topicRepository.findByTopicTypeIdAndDelAndUserIdOrderByCreateDateDesc(topicTypeId, 0, userId, pageRequest);
        VoTopicListResp voTopicListResp = VoBaseResp.ok("查询主题成功", VoTopicListResp.class);
        //点赞集合
        List<Long> ids = Lists.newArrayList();
        for (Topic topic : topics) {
            VoTopicResp voTopicResp = new VoTopicResp();
            voTopicResp.setId(topic.getId());
            voTopicResp.setTitle(topic.getTitle());
            //内容评论数量分析,1000以上999+
            voTopicResp.setContentTotalNum(topic.getContentTotalNum() > CONTENT_COMMENT_LIMIT ? CONTENT_COMMENT_LIMIT : topic.getContentTotalNum());

            //内容超过两行...代替
            String content = topic.getContent();
            if (content.length() > CONTENT_SHOW_LIMIT) {
                content = content.substring(0, CONTENT_SHOW_LIMIT).concat("...");
            }
            voTopicResp.setContent(content);
            voTopicResp.setUserName(topic.getUserName());
            voTopicResp.setTopicUser(true);
            //发帖者图像
            if (StringUtils.isEmpty(voTopicResp.getUserIconUrl())) {
                voTopicResp.setUserIconUrl(imgPrefix + "/" + topic.getUserIconUrl());
            }

            //时间分析
            long publishTime = topic.getCreateDate().getTime();
            voTopicResp.setTime(DateHelper.getPastTime(publishTime));

            //点赞总数显示分析，1000以上999+
            voTopicResp.setTopTotalNum(topic.getTopTotalNum() > CONTENT_TOP_LIMIT ? CONTENT_TOP_LIMIT : topic.getTopTotalNum());

            //设置图片
            String img = null;
            for (int i = 1; i <= 9; i++) {

                try {
                    img = (String) PropertyUtils.getProperty(topic, "img" + i);
                    if (StringUtils.isEmpty(img)) {
                        break;
                    }
                    PropertyUtils.setProperty(voTopicResp, "img" + i, imgPrefix + "/" + img);
                } catch (Exception e) {
                    return ResponseEntity
                            .badRequest()
                            .body(VoBaseResp.error(VoBaseResp.ERROR, "系统异常， 请稍后再试！", VoTopicListResp.class));
                }

            }
            ids.add(topic.getId());
            voTopicListResp.getVoTopicRespList().add(voTopicResp);
        }


        //判断用户是否已经点赞过
        try {
            String token = jwtTokenHelper.getToken(httpServletRequest);
            if (!StringUtils.isEmpty(token)
                    && !CollectionUtils.isEmpty(ids)) {
                //Long userId = jwtTokenHelper.getUserIdFromToken(token);
                Map<Long, TopicTopRecord> map = topicTopRecordBiz.findTopState(0, userId, ids);
                for (VoTopicResp item : voTopicListResp.getVoTopicRespList()) {
                    TopicTopRecord topicTopRecord = map.get(item.getId());
                    if (ObjectUtils.isEmpty(topicTopRecord)) {
                        item.setTopState(false);
                    } else {
                        item.setTopState(true);
                    }
                }
            }
        } catch (Exception e) {
            return ResponseEntity
                    .badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "系统异常， 请稍后再试！", VoTopicListResp.class));
        }

        return ResponseEntity.ok(voTopicListResp);
    }

    @Override
    public ResponseEntity<VoTopicCommentManagerListResp> listComment(Integer sourceType, HttpServletRequest httpServletRequest,
                                                                     Integer pageIndex, Long userId) {

        VoTopicCommentManagerListResp voTopicCommentManagerListResp = VoBaseResp.ok("查询成功",
                VoTopicCommentManagerListResp.class);
        if (pageIndex <= 0) {
            pageIndex = 1;
        }
        if (sourceType == 0) {
            //我的评论
            Pageable pageable = new PageRequest(pageIndex - 1, 10, Sort.Direction.DESC, "id");
            List<TopicComment> topicComments = topicCommentRepository.findByUserIdAndDel(userId, 0,
                    pageable);
            topicComments.stream().forEach((comment) -> {
                VoTopicCommentManagerResp voTopicCommentManagerResp = new VoTopicCommentManagerResp();
                voTopicCommentManagerResp.setContent(comment.getContent());
                //查询发帖用户
                Topic topic = topicRepository.findOne(comment.getTopicId());
                voTopicCommentManagerResp.setTopicId(topic.getId());
                voTopicCommentManagerResp.setCommentId(comment.getId());
                voTopicCommentManagerResp.setForUserId(topic.getUserId());
                voTopicCommentManagerResp.setUserId(userId);
                voTopicCommentManagerResp.setOwn(true);
                TopicsUsers topicsUsers = null;
                try {
                    topicsUsers = userCache.get(userId);
                    Preconditions.checkNotNull(topicsUsers);
                } catch (ExecutionException e) {
                    e.printStackTrace();
                    log.info("用户不存在!!");
                }
                voTopicCommentManagerResp.setUserName(topicsUsers.getUsername());
                voTopicCommentManagerResp.setForUserName(topic.getUserName());
                //我的头像
                voTopicCommentManagerResp.setAvatar(imgPrefix +"/"+ comment.getUserIconUrl());
                //评论时间
                voTopicCommentManagerResp.setTime(DateHelper.getPastTime(comment.getCreateDate().getTime()));
                voTopicCommentManagerListResp.getVoTopicCommentManagerRespList().add(voTopicCommentManagerResp);
            });

        } else {
            //我的回复
            Pageable pageable = new PageRequest(pageIndex - 1, 10, Sort.Direction.DESC, "id");
            List<TopicReply> replys = topicReplyRepository.findByUserIdAndDel(userId, 0,
                    pageable);
            replys.stream().forEach((reply) -> {
                VoTopicCommentManagerResp voTopicCommentManagerResp = new VoTopicCommentManagerResp();

                voTopicCommentManagerResp.setContent(reply.getContent());
                voTopicCommentManagerResp.setCommentId(reply.getTopicCommentId());
                voTopicCommentManagerResp.setForUserId(reply.getForUserId());
                voTopicCommentManagerResp.setUserId(reply.getUserId());
                voTopicCommentManagerResp.setUserName(reply.getUserName());
                //我的头像
                voTopicCommentManagerResp.setAvatar(imgPrefix +"/"+ reply.getUserIconUrl());
                voTopicCommentManagerResp.setForUserName(reply.getForUserName());
                voTopicCommentManagerResp.setTopicReplyId(reply.getId().toString());
                voTopicCommentManagerResp.setOwn(true);
                //评论时间
                voTopicCommentManagerResp.setTime(DateHelper.getPastTime(reply.getCreateDate().getTime()));
                voTopicCommentManagerListResp.getVoTopicCommentManagerRespList().add(voTopicCommentManagerResp);
            });
        }

        return ResponseEntity.ok(voTopicCommentManagerListResp);
    }

    @Override
    public ResponseEntity<VoTopicCommentManagerListResp> listByComment(Integer sourceType,
                                                                       HttpServletRequest httpServletRequest,
                                                                       Integer pageIndex, Long userId) {
        VoTopicCommentManagerListResp voTopicCommentManagerListResp =
                VoBaseResp.ok("查询成功", VoTopicCommentManagerListResp.class);

        if (pageIndex <= 0) {
            pageIndex = 1;
        }
        if (sourceType == 0) {
            //评论我的
            List<Topic> topics = topicRepository.findByUserIdAndDel(userId,0);
            List<Long> topicIds = Lists.newArrayList();
            for (Topic topic : topics) {
                topicIds.add(topic.getId());
            }
            topicIds = topicIds.stream().distinct().collect(Collectors.toList());

            Pageable commentPageable = new PageRequest(pageIndex - 1, 10);
            List<TopicComment> byComments = topicCommentRepository.findByTopicIdInAndDelOrderByIdDesc(topicIds,0, commentPageable);
            TopicsUsers topicsUsers = null;
            try {
                topicsUsers = topicsUsersService.findByUserId(userId);
                Preconditions.checkNotNull(topicsUsers);
            } catch (Exception e) {
                e.printStackTrace();
                log.error("用户不存在!!");
            }

            for (TopicComment comment : byComments) {
                VoTopicCommentManagerResp voTopicCommentManagerResp = new VoTopicCommentManagerResp();
                voTopicCommentManagerResp.setTopicId(comment.getTopicId());
                voTopicCommentManagerResp.setCommentId(comment.getId());
                voTopicCommentManagerResp.setContent(comment.getContent());
                voTopicCommentManagerResp.setUserId(userId);
                voTopicCommentManagerResp.setUserName(comment.getUserName());
                voTopicCommentManagerResp.setForUserName(topicsUsers.getUsername());
                voTopicCommentManagerResp.setForUserId(userId);
                //评论者头像
                voTopicCommentManagerResp.setAvatar(imgPrefix + comment.getUserIconUrl());
                //评论时间
                voTopicCommentManagerResp.setTime(DateHelper.getPastTime(comment.getCreateDate().getTime()));
                voTopicCommentManagerListResp.getVoTopicCommentManagerRespList().add(voTopicCommentManagerResp);
            }
        } else {
            //回复我的
            Pageable pageable = new PageRequest(pageIndex - 1, 10, Sort.Direction.DESC, "id");
            List<TopicReply> topicReplies = topicReplyRepository.findByForUserIdAndDel(userId, 0,
                    pageable);
            topicReplies.stream().forEach((reply) -> {
                VoTopicCommentManagerResp voTopicCommentManagerResp = new VoTopicCommentManagerResp();
                voTopicCommentManagerResp.setContent(reply.getContent());
                //评论时间
                voTopicCommentManagerResp.setTime(DateHelper.getPastTime(reply.getCreateDate().getTime()));
                voTopicCommentManagerResp.setCommentId(reply.getTopicCommentId());
                voTopicCommentManagerResp.setUserId(reply.getUserId());
                voTopicCommentManagerResp.setUserName(reply.getUserName());
                voTopicCommentManagerResp.setForUserId(userId);
                voTopicCommentManagerResp.setForUserName(reply.getForUserName());
                voTopicCommentManagerResp.setTopicReplyId(reply.getId().toString());
                //回复者头像
                voTopicCommentManagerResp.setAvatar(imgPrefix + reply.getUserIconUrl());
                voTopicCommentManagerListResp.getVoTopicCommentManagerRespList().add(voTopicCommentManagerResp);
            });
        }
        return ResponseEntity.ok(voTopicCommentManagerListResp);
    }

    @Override
    public TopicsUsers findTopByUsername(@NonNull String username) {

        return topicsUsersRepository.findTopByUsername(username);
    }

}
