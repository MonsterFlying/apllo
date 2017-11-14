package com.gofobao.framework.comment.service.impl;

import alex.zhrenjie04.wordfilter.WordFilterUtil;
import alex.zhrenjie04.wordfilter.result.FilteredResult;
import com.gofobao.framework.comment.biz.TopicTopRecordBiz;
import com.gofobao.framework.comment.entity.Topic;
import com.gofobao.framework.comment.entity.TopicTopRecord;
import com.gofobao.framework.comment.entity.TopicType;
import com.gofobao.framework.comment.entity.TopicsUsers;
import com.gofobao.framework.comment.repository.TopicRepository;
import com.gofobao.framework.comment.repository.TopicTypeRepository;
import com.gofobao.framework.comment.repository.TopicsUsersRepository;
import com.gofobao.framework.comment.service.TopicService;
import com.gofobao.framework.comment.vo.request.VoTopicReq;
import com.gofobao.framework.comment.vo.response.VoTopicListResp;
import com.gofobao.framework.comment.vo.response.VoTopicResp;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.member.entity.Users;
import com.gofobao.framework.member.repository.UsersRepository;
import com.gofobao.framework.security.helper.JwtTokenHelper;
import com.gofobao.framework.system.biz.FileManagerBiz;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by xin on 2017/11/8.
 */
@Service
@Slf4j
public class TopicServiceImpl implements TopicService {
    @Autowired
    private TopicRepository topicRepository;

    @Autowired
    private TopicTypeRepository topicTypeRepository;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private TopicsUsersRepository topicsUsersRepository;

    @Autowired
    private TopicTopRecordBiz topicTopRecordBiz;

    @Autowired
    FileManagerBiz fileManagerBiz;

    @Value("${qiniu.domain}")
    private String imgPrefix;

    @Autowired
    JwtTokenHelper jwtTokenHelper;

    public static final Integer CONTENT_COMMENT_LIMIT = 999;

    public static final int CONTENT_SHOW_LIMIT = 100;

    public static final Integer CONTENT_TOP_LIMIT = 999;


    @Override
    @Transactional
    public ResponseEntity<VoBaseResp> publishTopic(@NonNull VoTopicReq voTopicReq,
                                                   @NonNull Long userId,
                                                   HttpServletRequest httpServletRequest) {

        //判断用户是否被禁止发言
        TopicsUsers topicsUsers = topicsUsersRepository.findByUserId(userId);
        Preconditions.checkNotNull(topicsUsers, "用户不存在");
        if (topicsUsers.getForceState() != 0) {
            return ResponseEntity.ok(VoBaseResp.ok("用户已被禁言", VoBaseResp.class));
        }

        Date nowDate = new Date();
        // 判断板块id存在否？
        TopicType topicType = topicTypeRepository.findById(voTopicReq.getTopicTypeId());
        Preconditions.checkNotNull(topicType, "topicType is not exist");

        // 图片获取
        List<String> files = null;

        try {
            files = fileManagerBiz.multiUpload(userId, httpServletRequest, "files");
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity
                    .badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "文件保存失败", VoBaseResp.class));
        }


        // 用户内容铭感词过滤
        FilteredResult filteredResult = WordFilterUtil.filterText(voTopicReq.getContent(), '*');
        String filteredContent = filteredResult.getFilteredContent();

        // 保存数据
        Topic topic = new Topic();
        topic.setUserId(userId);
        topic.setContent(filteredContent);
        topic.setUserName(topicsUsers.getUsername());
        topic.setUserIconUrl(topicsUsers.getAvatar());
        // 设置图片
        for (int i = 1, len = files.size(); i <= len; i++) {
            try {
                PropertyUtils.setProperty(topic, "img" + i, files.get(i - 1));
            } catch (Exception e) {
                return ResponseEntity
                        .badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "系统异常， 请稍后再试！", VoBaseResp.class));
            }
        }
        topic.setTopicTypeId(voTopicReq.getTopicTypeId());
        topic.setCreateDate(nowDate);
        topic.setUpdateDate(nowDate);
        topic.setContent(filteredResult.getFilteredContent());
        Topic saveTopic = topicRepository.save(topic);
        Preconditions.checkNotNull(saveTopic, "topic record is empty");

        //发帖后相应版块下数量改变
        topicTypeRepository.updateTopicTotalNum(topic.getTopicTypeId(), nowDate);
        return ResponseEntity.ok(VoBaseResp.ok("发布主题成功", VoBaseResp.class));
    }

    @Override
    @Transactional
    public ResponseEntity<VoBaseResp> delTopic(long id, long userId) {
        Topic topic = topicRepository.findOne(id);
        if (ObjectUtils.isEmpty(topic)) {
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "无权删除", VoBaseResp.class));
        }
        if (topic.getUserId() != userId) {
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "无权删除", VoBaseResp.class));
        }
        try {
            Integer count = topicRepository.updateDel(id);
            Preconditions.checkNotNull(count, "删除帖子失败");
            //topicRepository.delete(id);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "删除帖子失败", VoBaseResp.class));
        }
        return ResponseEntity.ok(VoBaseResp.ok("删除帖子成功", VoBaseResp.class));

    }

    @Override
    public ResponseEntity<VoTopicListResp> listTopic(HttpServletRequest httpServletRequest, long topicTypeId, Integer pageable) {
        Topic topic1 = new Topic();
        topic1.setTopicTypeId(topicTypeId);
        topic1.setDel(0);
        Example<Topic> example = Example.of(topic1);
        if (pageable <= 0) {
            pageable = 1;
        }
        PageRequest pageRequest = new PageRequest(pageable - 1, 10,
                new Sort(new Sort.Order(Sort.Direction.DESC, "id")));

        Page<Topic> page = topicRepository.findAll(example, pageRequest);
        List<Topic> topics = page.getContent();
        //List<Topic> topics = topicRepository.findByTopicTypeIdAndDelOrderByCreateDateDesc(topicTypeId,0, new PageRequest(pageable - 1, 10));
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
            if(StringUtils.isEmpty(voTopicResp.getUserIconUrl())){
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
                Long userId = jwtTokenHelper.getUserIdFromToken(token);
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
    public ResponseEntity<VoTopicResp> findTopic(long topicId, HttpServletRequest httpServletRequest) {
        Topic topic = topicRepository.findByIdAndDel(topicId, 0);
        VoTopicResp voTopicResp = VoBaseResp.ok("查询主题成功", VoTopicResp.class);
        voTopicResp.setTitle(topic.getTitle());
        voTopicResp.setContent(topic.getContent());
        voTopicResp.setUserName(topic.getUserName());
        voTopicResp.setUserIconUrl(topic.getUserIconUrl());
        //帖子发布时间
        long publishTime = topic.getCreateDate().getTime();
        voTopicResp.setTime(DateHelper.getPastTime(publishTime));

        //点赞数显示设置
        voTopicResp.setTopTotalNum(topic.getTopTotalNum() > CONTENT_TOP_LIMIT ? CONTENT_TOP_LIMIT : topic.getTopTotalNum());

        //判断用户是否已经点赞过
        List<Long> ids = Lists.newArrayList();
        ids.add(topicId);
        try {
            String token = jwtTokenHelper.getToken(httpServletRequest);
            if (!StringUtils.isEmpty(token)
                    && !CollectionUtils.isEmpty(ids)) {
                Long userId = jwtTokenHelper.getUserIdFromToken(token);
                Map<Long, TopicTopRecord> map = topicTopRecordBiz.findTopState(0, userId, ids);
                TopicTopRecord topicTopRecord = map.get(0);
                if (ObjectUtils.isEmpty(topicTopRecord)) {
                    voTopicResp.setTopState(false);
                } else {
                    voTopicResp.setTopState(true);
                }
            }
        } catch (Exception e) {
            return ResponseEntity
                    .badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "系统异常， 请稍后再试！", VoTopicResp.class));
        }

        //内容评论数显示
        voTopicResp.setContentTotalNum(topic.getContentTotalNum() > CONTENT_COMMENT_LIMIT ? CONTENT_COMMENT_LIMIT : topic.getContentTotalNum());

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
                        .badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "系统异常， 请稍后再试！", VoTopicResp.class));
            }
        }
        return ResponseEntity.ok(voTopicResp);
    }

    @Override
    public Topic findById(Long soucreId) {
        return topicRepository.findOne(soucreId);
    }

    @Override
    public void batchUpdateRedundancy(@NonNull Long userId, String username, String avatar) throws Exception {
        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(avatar)) {
            throw new Exception("参数错误!");
        }

        if (!StringUtils.isEmpty(username)) {
            log.info("=========批量修改用户名==========");
            topicRepository.batchUpateUsernameByUserId(userId, username);
        } else {
            log.info("=========批量修改头像==========");
            topicRepository.batchUpateAvatarByUserId(userId, avatar);
        }
    }
}
