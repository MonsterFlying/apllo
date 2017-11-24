package com.gofobao.framework.comment.service.impl;

import alex.zhrenjie04.wordfilter.WordFilterUtil;
import alex.zhrenjie04.wordfilter.result.FilteredResult;
import com.gofobao.framework.comment.biz.TopicTopRecordBiz;
import com.gofobao.framework.comment.biz.TopisIntegralBiz;
import com.gofobao.framework.comment.entity.*;
import com.gofobao.framework.comment.repository.*;
import com.gofobao.framework.comment.service.TopicService;
import com.gofobao.framework.comment.service.TopicsUsersService;
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
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import io.jsonwebtoken.lang.Strings;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import redis.clients.jedis.Jedis;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

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
    private TopicCommentRepository topicCommentRepository;

    @Autowired
    private TopicTopRecordBiz topicTopRecordBiz;

    @Autowired
    private TopicReplyRepository topicReplyRepository;


    @Autowired
    FileManagerBiz fileManagerBiz;

    @Value("${qiniu.domain}")
    private String imgPrefix;

    @Autowired
    JwtTokenHelper jwtTokenHelper;

    @Autowired
    private TopicsUsersService topicsUsersService;

    public static final Integer CONTENT_COMMENT_LIMIT = 999;

    public static final int CONTENT_SHOW_LIMIT = 100;

    public static final Integer CONTENT_TOP_LIMIT = 999;

    @Autowired
    TopisIntegralBiz topisIntegralBiz;

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

    LoadingCache<Long, TopicType> topicTypeCache = CacheBuilder
            .newBuilder()
            .expireAfterAccess(5, TimeUnit.MINUTES)
            .maximumSize(1024)
            .build(new CacheLoader<Long, TopicType>() {
                @Override
                public TopicType load(Long topicTypeId) throws Exception {
                    //查询话题类型
                    TopicType topicType = topicTypeRepository.findById(topicTypeId);
                    return topicType;
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

    LoadingCache<Long, Topic> lastTopicCache = CacheBuilder
            .newBuilder()
            .expireAfterAccess(5, TimeUnit.MINUTES)
            .maximumSize(1024)
            .build(new CacheLoader<Long, Topic>() {
                @Override
                public Topic load(Long userId) throws Exception {
                    //查询上次发布帖子时间;
                    Topic lastTopic = topicRepository.findTopByUserIdOrderByIdDesc(userId);
                    return lastTopic;
                }
            });

    LoadingCache<String, List<Topic>> topicsCache = CacheBuilder
            .newBuilder()
            .build(new CacheLoader<String, List<Topic>>() {
                @Override
                public List<Topic> load(String s) throws Exception {
                    Topic topic1 = new Topic();
                    //拆分话题类型id和当前页
                    String currenttopicType[] = s.split(":");
                    topic1.setTopicTypeId(Long.valueOf(currenttopicType[0]));
                    Integer pageable = Integer.valueOf(currenttopicType[1]);
                    topic1.setDel(0);
                    Example<Topic> example = Example.of(topic1);
                    PageRequest pageRequest = new PageRequest(pageable - 1, 10,
                            new Sort(new Sort.Order(Sort.Direction.DESC, "id")));
                    Page<Topic> page = topicRepository.findAll(example, pageRequest);
                    return page.getContent();
                }
            });

    @Override
    @Transactional(noRollbackFor = Exception.class)
    public ResponseEntity<VoBaseResp> publishTopic(@NonNull VoTopicReq voTopicReq,
                                                   @NonNull Long userId,
                                                   HttpServletRequest httpServletRequest) {

        //判断用户是否被禁止发言
        TopicsUsers topicsUsers = null;
        try {
            topicsUsers = userCache.get(userId);
        } catch (ExecutionException e) {
            ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "用户不存在", VoBaseResp.class));
        }
        Preconditions.checkNotNull(topicsUsers, "用户不存在");
        if (topicsUsers.getForceState() != 0) {
            return ResponseEntity.ok(VoBaseResp.ok("用户已被禁言", VoBaseResp.class));
        }

        Date nowDate = new Date();
        //查询用户上次发帖时间
        Topic lastTopic = topicRepository.findTopByUserIdOrderByIdDesc(userId);
        if (!ObjectUtils.isEmpty(lastTopic)) {
            Date createDate = lastTopic.getCreateDate();
            createDate = ObjectUtils.isArray(createDate) ? nowDate : createDate;
            if (nowDate.getTime() - (createDate.getTime() + DateHelper.MILLIS_PER_MINUTE*2) < 0) {
                return ResponseEntity
                        .badRequest()
                        .body(VoBaseResp.error(VoBaseResp.ERROR, "发帖过于频繁, 请1小时后再试!"));
            }
        }
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
                    .badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, e.getMessage(), VoBaseResp.class));
        }

        //如果发布的内容何图片都是空,不能发布
        if (StringUtils.isEmpty(voTopicReq.getContent()) && ObjectUtils.isEmpty(files)
                && StringUtils.isEmpty(voTopicReq.getContent().trim())) {
            return ResponseEntity
                    .badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "发布内容不能为空!", VoBaseResp.class));
        }
        //如果只发布图片,则无需敏感词过滤
        // 用户内容铭感词过滤
        FilteredResult filteredResult = null;
        String filteredContent = "";
        if (!StringUtils.isEmpty(Strings.trimAllWhitespace(voTopicReq.getContent()))) {
            filteredResult = WordFilterUtil.filterText(voTopicReq.getContent().trim(), '*');
            filteredContent = filteredResult.getFilteredContent();
        }

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
        if (!ObjectUtils.isEmpty(filteredResult)) {
            topic.setContent(filteredResult.getFilteredContent());
        }

        Topic saveTopic = topicRepository.save(topic);
        Preconditions.checkNotNull(saveTopic, "topic record is empty");
        // 发帖后相应版块下数量改变
        topicTypeRepository.updateTopicTotalNum(topic.getTopicTypeId(), nowDate);
        // 添加积分
        topisIntegralBiz.publishTopic(topic);
        //发布帖子成功,清除lastTopic缓存
        clearLastTopicCache(userId);
        //清除帖子列表详情
        if (!ObjectUtils.isEmpty(topicsCache)) {
            topicsCache.invalidateAll();
        }
        if (!ObjectUtils.isEmpty(topicsCache)) {
            topicCache.invalidateAll();
        }

        return ResponseEntity.ok(VoBaseResp.ok("发布主题成功", VoBaseResp.class));
    }

    @Override
    @Transactional(noRollbackFor = Exception.class)
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
            //删除帖子下面的所有评论
            Integer updateCommentResult = topicCommentRepository.updateComment(id);
            Preconditions.checkNotNull(updateCommentResult, "删除评论失败");
            //删除帖子下所有评论的回复
            List<TopicComment> commentList = topicCommentRepository.findByTopicIdAndDelOrderByIdAsc(id, 0);
            if (!CollectionUtils.isEmpty(commentList)) {
                List<Long> commentIds = Lists.newArrayList();
                Long commentId = null;
                for (TopicComment topicComment : commentList) {
                    commentIds.add(topicComment.getId());
                    //topicReplyRepository.updateReply(topicComment.getId());
                }
                Integer updateReplyResult = topicReplyRepository.updateReply(commentIds);
                Preconditions.checkNotNull(updateReplyResult, "删除回复失败");
            }

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "删除帖子失败", VoBaseResp.class));
        }
        //删除成功清除topicCache topicId缓存
        topicCache.invalidate(id);
        //清除帖子列表缓存
        topicsCache.invalidateAll();
        return ResponseEntity.ok(VoBaseResp.ok("删除帖子成功", VoBaseResp.class));

    }

    @Override
    public ResponseEntity<VoTopicListResp> listTopic(HttpServletRequest httpServletRequest, long topicTypeId, Integer pageable) {
        String token = null;
        Long userId = null;
        try {
            token = jwtTokenHelper.getToken(httpServletRequest);
        } catch (Exception e) {
            return ResponseEntity
                    .badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "系统异常， 请稍后再试！",
                            VoTopicListResp.class));
        }
        userId = jwtTokenHelper.getUserIdFromToken(token);

        if (pageable <= 0) {
            pageable = 1;
        }
        List<Topic> topics = null;
        try {
            topics = topicsCache.get(topicTypeId + ":" + pageable);
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

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
                            .body(VoBaseResp.error(VoBaseResp.ERROR, "系统异常， 请稍后再试！",
                                    VoTopicListResp.class));
                }

            }
            ids.add(topic.getId());
            //判断是否登录或者登录用户是否是发帖用户
            if (topic.getUserId().equals(userId)) {
                voTopicResp.setTopicUser(true);
            }
            voTopicListResp.getVoTopicRespList().add(voTopicResp);
        }


        //判断用户是否已经点赞过
        try {
            if (!StringUtils.isEmpty(token)
                    && !CollectionUtils.isEmpty(ids)) {
                Map<Long, TopicTopRecord> map = topicTopRecordBiz.findTopState(0, userId, ids);
                for (VoTopicResp item : voTopicListResp.getVoTopicRespList()) {
                    TopicTopRecord topicTopRecord = map.get(item.getId());
                    if (ObjectUtils.isEmpty(topicTopRecord)) {
                        item.setTopState(false);
                        //删除成功清除topicCache topicId缓存
                        topicCache.invalidateAll();
                        //清除帖子列表详情
                        topicsCache.invalidateAll();
                    } else {
                        item.setTopState(true);
                        //删除成功清除topicCache topicId缓存
                        topicCache.invalidateAll();
                        //清除帖子列表详情
                        topicsCache.invalidateAll();
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
        String token = null;
        Long userId = null;
        try {
            token = jwtTokenHelper.getToken(httpServletRequest);
        } catch (Exception e) {
            return ResponseEntity
                    .badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "系统异常， 请稍后再试！",
                            VoTopicResp.class));
        }
        userId = jwtTokenHelper.getUserIdFromToken(token);
        Topic topic = topicRepository.findByIdAndDel(topicId, 0);
        VoTopicResp voTopicResp = VoBaseResp.ok("查询主题成功", VoTopicResp.class);
        voTopicResp.setTitle(topic.getTitle());
        voTopicResp.setContent(topic.getContent());
        voTopicResp.setUserName(topic.getUserName());
        voTopicResp.setUserIconUrl(imgPrefix+"/"+topic.getUserIconUrl());
        voTopicResp.setId(topic.getId());
        //判断用户未登录或者是否是发帖用户
        if (topic.getUserId().equals(userId)) {
            voTopicResp.setTopicUser(true);
        }
        //帖子发布时间
        long publishTime = topic.getCreateDate().getTime();
        voTopicResp.setTime(DateHelper.getPastTime(publishTime));

        //点赞数显示设置
        voTopicResp.setTopTotalNum(topic.getTopTotalNum() > CONTENT_TOP_LIMIT ? CONTENT_TOP_LIMIT : topic.getTopTotalNum());

        //判断用户是否已经点赞过
        List<Long> ids = Lists.newArrayList();
        ids.add(topicId);
        if (!StringUtils.isEmpty(token)
                && !CollectionUtils.isEmpty(ids)) {
            Map<Long, TopicTopRecord> map = topicTopRecordBiz.findTopState(0, userId, ids);
            TopicTopRecord topicTopRecord = map.get(topicId);
            if (ObjectUtils.isEmpty(topicTopRecord)) {
                voTopicResp.setTopState(false);
            } else {
                voTopicResp.setTopState(true);
            }

            //删除成功清除topicCache topicId缓存
            if (!ObjectUtils.isEmpty(topicCache)) {
                topicCache.invalidateAll();
            }
            if (!ObjectUtils.isEmpty(topicsCache)) {
                //清除帖子列表详情
                topicsCache.invalidateAll();
            }
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
    @Transactional
    public void batchUpdateRedundancy(@NonNull Long userId, String username, String avatar) throws Exception {
        if (StringUtils.isEmpty(username) && StringUtils.isEmpty(avatar)) {
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

    @Override
    public Topic save(Topic topic) {
        return topicRepository.save(topic);
    }

    /**
     * 清除上次发帖的缓存
     *
     * @param userId
     */
    public void clearLastTopicCache(Long userId) {
        lastTopicCache.invalidate(userId);
    }
}
