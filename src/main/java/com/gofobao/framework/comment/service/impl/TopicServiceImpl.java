package com.gofobao.framework.comment.service.impl;

import alex.zhrenjie04.wordfilter.WordFilterUtil;
import alex.zhrenjie04.wordfilter.result.FilteredResult;
import com.gofobao.framework.comment.entity.Topic;
import com.gofobao.framework.comment.entity.TopicType;
import com.gofobao.framework.comment.repository.TopicRepository;
import com.gofobao.framework.comment.repository.TopicTypeRepository;
import com.gofobao.framework.comment.service.TopicService;
import com.gofobao.framework.comment.vo.request.VoTopicReq;
import com.gofobao.framework.comment.vo.response.VoTopicListResp;
import com.gofobao.framework.comment.vo.response.VoTopicResp;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.member.entity.Users;
import com.gofobao.framework.member.repository.UsersRepository;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by xin on 2017/11/8.
 */
@Service
public class TopicServiceImpl implements TopicService {
    @Autowired
    private TopicRepository topicRepository;

    private TopicTypeRepository topicTypeRepository;

    @Autowired
    private UsersRepository usersRepository;

    @Override
    public ResponseEntity<VoTopicResp> publishTopic(VoTopicReq voTopicReq, @NonNull Long userId, HttpServletRequest httpServletRequest) {
        Date nowDate = new Date() ;
        Preconditions.checkNotNull(voTopicReq) ;
        // 判断板块id存在否？
        TopicType topicType = topicTypeRepository.findById(voTopicReq.getTopicTypeId()) ;
        Preconditions.checkNotNull(topicType,"topicType is not exist") ;

        Users user = usersRepository.findById(userId);
        Preconditions.checkNotNull(user, "user record is empty") ;



        // 用户内容铭感词过滤
        FilteredResult result = WordFilterUtil.filterText(voTopicReq.getContent(),'*');

        // 图片有可能有间隙
        List<String> imgs= Lists.newArrayList() ;
        imgs.add(voTopicReq.getImg1()) ;
        imgs.add(voTopicReq.getImg2()) ;
        imgs.add(voTopicReq.getImg3()) ;
        imgs.add(voTopicReq.getImg4()) ;
        imgs.add(voTopicReq.getImg5()) ;
        imgs.add(voTopicReq.getImg6()) ;
        imgs.add(voTopicReq.getImg7()) ;
        imgs.add(voTopicReq.getImg8()) ;
        imgs.add(voTopicReq.getImg9()) ;
        imgs=imgs.stream().filter(s->s==null).collect(Collectors.toList());

        // 保存数据
        Topic topic = new Topic();
        topic.setUserId(userId);
        topic.setContent(voTopicReq.getContent()) ;
        topic.setUserName(user.getUsername()) ;

        topic.setCreateDate(nowDate) ;
        topic.setUpdateDate(nowDate) ;
        topic.setContent(voTopicReq.getContent()) ;
        topic = topicRepository.save(topic) ;
        return ResponseEntity.ok(VoBaseResp.ok("发布主题成功",VoTopicResp.class)) ;
    }

    @Override
    public ResponseEntity<VoTopicResp> delTopic(long id,long userId) {
        Topic topic = topicRepository.findOne(id);
        if(ObjectUtils.isEmpty(topic)){
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR,"无权删除",VoTopicResp.class));
        }
        if (topic.getUserId() != userId){
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR,"无权删除",VoTopicResp.class));
        }
        try {
            topicRepository.delete(id);
        }catch (Exception e){
             return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR,"删除主题失败",VoTopicResp.class));
        }
        return ResponseEntity.ok(VoBaseResp.ok("删除主题成功",VoTopicResp.class));

    }

    @Override
    public ResponseEntity<VoTopicListResp> listTopic(Integer topicTypeId) {
        List<Topic> list=topicRepository.findByTopicTypeIdOrderByCreateDateDesc(topicTypeId);

        return null;
    }
}
