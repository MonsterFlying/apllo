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
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.member.entity.Users;
import com.gofobao.framework.member.repository.UsersRepository;
import com.gofobao.framework.system.biz.FileManagerBiz;
import com.google.common.base.Preconditions;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

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
    FileManagerBiz fileManagerBiz ;

    @Value("${qiniu.domain}")
    private String imgPrefix;

    public static final Integer CONTENT_COMMENT_LIMIT = 999;

    public static final int CONTENT_SHOW_LIMIT = 54;

    public static final Integer CONTENT_TOP_LIMIT = 999;


    @Override
    @Transactional
    public ResponseEntity<VoBaseResp> publishTopic(VoTopicReq voTopicReq,
                                                    @NonNull Long userId,
                                                    HttpServletRequest httpServletRequest) {
        Date nowDate = new Date() ;
        Preconditions.checkNotNull(voTopicReq) ;
        // 判断板块id存在否？
        TopicType topicType = topicTypeRepository.findById(voTopicReq.getTopicTypeId()) ;
        Preconditions.checkNotNull(topicType,"topicType is not exist") ;

        Users user = usersRepository.findById(userId);
        Preconditions.checkNotNull(user, "user record is empty") ;

        // 图片获取
        List<String> files = null ;

        try{
            files = fileManagerBiz.multiUpload(userId, httpServletRequest, "files");
        }catch (Exception e){
            log.error(e.getMessage());
            return ResponseEntity
                    .badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "文件保存失败", VoBaseResp.class)) ;
        }


        // 用户内容铭感词过滤
        FilteredResult filteredResult = WordFilterUtil.filterText(voTopicReq.getContent(),'*');
        String filteredContent = filteredResult.getFilteredContent();

        // 保存数据
        Topic topic = new Topic();
        topic.setUserId(userId);
        topic.setContent(voTopicReq.getContent()) ;
        topic.setUserName(user.getUsername()) ;
        // 设置图片
        for(int i = 1 ,  len = files.size() ; i <= len; i++){
            try {
                PropertyUtils.setProperty(topic, "img" + i, files.get(i));
            } catch (Exception e) {
                return ResponseEntity
                        .badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "系统异常， 请稍后再试！", VoBaseResp.class)) ;
            }
        }
        topic.setTopicTypeId(voTopicReq.getTopicTypeId()) ;
        topic.setCreateDate(nowDate) ;
        topic.setUpdateDate(nowDate) ;
        topic.setContent(filteredResult.getFilteredContent()) ;
        Topic saveTopic = topicRepository.save(topic);
        Preconditions.checkNotNull(saveTopic, "topic record is empty") ;

        //发帖后相应版块下数量改变
        topicTypeRepository.updateTopicTotalNum(topic.getTopicTypeId(),nowDate);
        return ResponseEntity.ok(VoBaseResp.ok("发布主题成功",VoBaseResp.class)) ;
    }

    @Override
    public ResponseEntity<VoTopicResp> delTopic(long id,long userId) {
        Topic topic = topicRepository.findOne(id) ;
        if(ObjectUtils.isEmpty(topic)){
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR,"无权删除",VoTopicResp.class)) ;
        }
        if (topic.getUserId() != userId){
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR,"无权删除",VoTopicResp.class)) ;
        }
        try {
            topicRepository.delete(id) ;
        }catch (Exception e){
             return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR,"删除主题失败",VoTopicResp.class)) ;
        }
        return ResponseEntity.ok(VoBaseResp.ok("删除主题成功",VoTopicResp.class)) ;

    }

    @Override
    @SuppressWarnings("all")
    public ResponseEntity<VoTopicListResp> listTopic(long topicTypeId, Pageable pageable) {
        List<Topic> topics = topicRepository.findByTopicTypeIdOrderByCreateDateDesc(topicTypeId,pageable) ;
        VoTopicListResp voTopicListResp = VoBaseResp.ok("查询主题成功",VoTopicListResp.class) ;
        for (Topic topic : topics) {
            VoTopicResp voTopicResp=new VoTopicResp() ;
            voTopicResp.setTitle(topic.getTitle()) ;
            //内容评论数量分析,1000以上999+
            voTopicResp.setContentTotalNum(topic.getContentTotalNum()>CONTENT_COMMENT_LIMIT?CONTENT_COMMENT_LIMIT:topic.getContentTotalNum()) ;

            //内容超过两行...代替
            String content=topic.getContent();
            if (content.length()>CONTENT_SHOW_LIMIT){
               content = content.substring(CONTENT_SHOW_LIMIT).concat("...");
            }
            voTopicResp.setContent(content) ;

            //时间分析
            long nowTime=Calendar.getInstance().getTimeInMillis();
            long publishTime = topic.getCreateDate().getTime();
            long between = nowTime-publishTime;
            if (between > DateHelper.MILLIS_PER_DAY*7){
                voTopicResp.setTime("1周前");
            }else if(between >= DateHelper.MILLIS_PER_DAY){
                voTopicResp.setTime(between/DateHelper.MILLIS_PER_DAY+"天前");
            }else if(between >= DateHelper.MILLIS_PER_HOUR ){
                voTopicResp.setTime(between/DateHelper.MILLIS_PER_HOUR+"小时前");
            }else if(between >= DateHelper.MILLIS_PER_MINUTE){
                voTopicResp.setTime(between/DateHelper.MILLIS_PER_MINUTE+"分钟前");
            }

            //点赞总数显示分析，1000以上999+
            voTopicResp.setTopTotalNum(topic.getTopTotalNum()>CONTENT_TOP_LIMIT?CONTENT_TOP_LIMIT:topic.getTopTotalNum());

            //设置图片
            String img=null;
            for(int i = 1 ; i <= 9; i++){

                try {
                    img = (String) PropertyUtils.getProperty(topic, "img" + i);
                    if(StringUtils.isEmpty(img)){
                        break;
                    }
                    PropertyUtils.setProperty(voTopicResp,"img"+i,imgPrefix+"/"+img);
                } catch (Exception e) {
                    return ResponseEntity
                            .badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "系统异常， 请稍后再试！", VoTopicListResp.class)) ;
                }

            }
            voTopicListResp.getVoTopicRespList().add(voTopicResp) ;
        }
        return ResponseEntity.ok(voTopicListResp);
    }

    @Override
    public ResponseEntity<VoTopicResp> findTopic(long topicId) {
        Topic topic = topicRepository.findById(topicId) ;
        VoTopicResp voTopicResp = VoBaseResp.ok("查询主题成功",VoTopicResp.class);
        voTopicResp.setTitle(topic.getTitle()) ;
        voTopicResp.setContent(topic.getContent()) ;
        //点赞数显示设置
        voTopicResp.setTopTotalNum(topic.getTopTotalNum() > CONTENT_TOP_LIMIT ? CONTENT_TOP_LIMIT : topic.getTopTotalNum());
        //内容评论数显示
        voTopicResp.setContentTotalNum(topic.getContentTotalNum() > CONTENT_COMMENT_LIMIT ? CONTENT_COMMENT_LIMIT:topic.getContentTotalNum()) ;

        String img = null;
        for(int i = 1 ; i <= 9; i++){
            try {
                img=(String) PropertyUtils.getProperty(topic, "img" + i);
                if(StringUtils.isEmpty(img)){
                    break;
                }

                PropertyUtils.setProperty(voTopicResp,"img"+i,imgPrefix+"/"+img);
            } catch (Exception e) {
                return ResponseEntity
                        .badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "系统异常， 请稍后再试！", VoTopicResp.class)) ;
            }
        }
        return ResponseEntity.ok(voTopicResp) ;
    }
}
