package com.gofobao.framework.comment.service.impl;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.comment.biz.TopicTopRecordBiz;
import com.gofobao.framework.comment.entity.Topic;
import com.gofobao.framework.comment.entity.TopicTopRecord;
import com.gofobao.framework.comment.entity.TopicsNotices;
import com.gofobao.framework.comment.entity.TopicsUsers;
import com.gofobao.framework.comment.repository.TopicCommentRepository;
import com.gofobao.framework.comment.repository.TopicRepository;
import com.gofobao.framework.comment.repository.TopicsNoticesRepository;
import com.gofobao.framework.comment.repository.TopicsUsersRepository;
import com.gofobao.framework.comment.service.TopicsUsersService;
import com.gofobao.framework.comment.vo.response.*;
import com.gofobao.framework.core.vo.VoBaseReq;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.RandomUtil;
import com.gofobao.framework.member.entity.Users;
import com.gofobao.framework.member.service.UserService;
import com.gofobao.framework.security.helper.JwtTokenHelper;
import com.gofobao.framework.system.biz.FileManagerBiz;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import lombok.NonNull;
import org.apache.commons.beanutils.PropertyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
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

@Service
public class TopicsUsersServiceImpl implements TopicsUsersService {

    @Autowired
    TopicsUsersRepository topicsUsersRepository;

    @Autowired
    UserService userService;

    @Autowired
    TopicRepository topicRepository;

    @Autowired
    FileManagerBiz fileManagerBiz;

    @Value("${qiniu.domain}")
    private String imgPrefix;

    @Autowired
    private TopicTopRecordBiz topicTopRecordBiz;

    @Autowired
    private TopicCommentRepository topicCommentRepository;

    @Autowired
    private TopicsNoticesRepository topicsNoticesRepository;

    @Autowired
    JwtTokenHelper jwtTokenHelper;

    public static final Integer CONTENT_COMMENT_LIMIT = 999;

    public static final int CONTENT_SHOW_LIMIT = 100;

    public static final Integer CONTENT_TOP_LIMIT = 999;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TopicsUsers findByUserId(Long userId) {
        Users users = userService.findByIdLock(userId);
        Preconditions.checkNotNull(users, "find user record is empty");
        Specification<TopicsUsers> specification = Specifications
                .<TopicsUsers>and()
                .eq("userId", userId)
                .build();
        TopicsUsers topicsUsers = topicsUsersRepository.findOne(specification);
        if (ObjectUtils.isEmpty(topicsUsers)) {
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
    @SuppressWarnings("all")
    public ResponseEntity<VoTopicListResp> listUserTopic(Long topicTypeId, Long userId, Integer pageable,
                                                         HttpServletRequest httpServletRequest) {
//        Topic topic1 = new Topic();
//        topic1.setTopicTypeId(topicTypeId);
//        topic1.setDel(0);
//        Example<Topic> example = Example.of(topic1);
//        if (pageable <= 0) {
//            pageable = 1;
//        }
        PageRequest pageRequest = new PageRequest(pageable - 1, 10,
                new Sort(new Sort.Order(Sort.Direction.DESC, "id")));

        //Page<Topic> page = topicRepository.findAll(example, pageRequest);
//        List<Topic> topics = page.getContent();
        List<Topic> topics = topicRepository.findByTopicTypeIdAndDelAndUserIdOrderByCreateDateDesc(topicTypeId, 0, userId,pageRequest);
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
                                                                     Integer pageable, Long userId) {
        VoTopicCommentManagerResp voTopicCommentManagerResp = new VoTopicCommentManagerResp();
        VoTopicCommentManagerListResp voTopicCommentManagerListResp = VoBaseResp.ok("查询成功",VoTopicCommentManagerListResp.class);
        if(sourceType == 0) {
            //我的评论
            List<TopicsNotices> topicsNotices = topicsNoticesRepository.findByUserIdAndSourceType(userId, sourceType);
            for (TopicsNotices notices : topicsNotices) {
                voTopicCommentManagerResp.setTopicId(notices.getSourceId());
                voTopicCommentManagerResp.setContent(notices.getContent());
                //评论时间
                voTopicCommentManagerResp.setTime(DateHelper.getPastTime(notices.getCreateDate().getTime()));
                voTopicCommentManagerListResp.getVoTopicCommentManagerRespList().add(voTopicCommentManagerResp);
            }

        }else {
            //我的回复
            List<TopicsNotices> topicsNotices = topicsNoticesRepository.findByUserIdAndSourceType(userId, sourceType);
            for (TopicsNotices notices : topicsNotices) {
                voTopicCommentManagerResp.setContent(notices.getContent());
                voTopicCommentManagerResp.setCommentId(notices.getSourceId());
                //评论时间
                voTopicCommentManagerResp.setTime(DateHelper.getPastTime(notices.getCreateDate().getTime()));
                voTopicCommentManagerResp.setForUserId(notices.getForUserId());
                voTopicCommentManagerListResp.getVoTopicCommentManagerRespList().add(voTopicCommentManagerResp);
            }
        }

        return ResponseEntity.ok(voTopicCommentManagerListResp);
    }

    @Override
    public ResponseEntity<VoTopicCommentManagerListResp> listByComment(Integer sourceType, HttpServletRequest httpServletRequest, Integer pageable, Long userId) {
        VoTopicCommentManagerResp voTopicCommentManagerResp = new VoTopicCommentManagerResp();
        VoTopicCommentManagerListResp voTopicCommentManagerListResp = VoBaseResp.ok("查询成功", VoTopicCommentManagerListResp.class);
        if (sourceType == 0) {
            //评论我的
            List<TopicsNotices> topicsNotices = topicsNoticesRepository.findByForUserIdAndSourceType(userId, sourceType);
            for (TopicsNotices notices : topicsNotices) {
                voTopicCommentManagerResp.setContent(notices.getContent());
                //评论时间
                voTopicCommentManagerResp.setTime(DateHelper.getPastTime(notices.getCreateDate().getTime()));
                voTopicCommentManagerResp.setTopicId(notices.getSourceId());
                voTopicCommentManagerListResp.getVoTopicCommentManagerRespList().add(voTopicCommentManagerResp);
            }
        } else {
            //回复我的
            List<TopicsNotices> topicsNotices = topicsNoticesRepository.findByForUserIdAndSourceType(userId, sourceType);
            for (TopicsNotices notices : topicsNotices) {
                voTopicCommentManagerResp.setContent(notices.getContent());
                //评论时间
                voTopicCommentManagerResp.setTime(DateHelper.getPastTime(notices.getCreateDate().getTime()));
                TopicsUsers topicsUsers = topicsUsersRepository.findByUserId(notices.getUserId());

                voTopicCommentManagerResp.setUserName(topicsUsers.getUsername());
                voTopicCommentManagerResp.setCommentId(notices.getSourceId());

                voTopicCommentManagerListResp.getVoTopicCommentManagerRespList().add(voTopicCommentManagerResp);
            }
        }
        return ResponseEntity.ok(voTopicCommentManagerListResp);
    }
}
