package com.gofobao.framework.comment.service.impl;

import com.gofobao.framework.borrow.vo.request.VoDoAgainVerifyReq;
import com.gofobao.framework.comment.entity.TopicType;
import com.gofobao.framework.comment.repository.TopicTypeRepository;
import com.gofobao.framework.comment.service.TopicTypeService;
import com.gofobao.framework.comment.vo.request.VoTopicTypeReq;
import com.gofobao.framework.comment.vo.response.VoTopicTypeListResp;
import com.gofobao.framework.comment.vo.response.VoTopicTypeResp;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.project.SecurityHelper;
import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Created by xin on 2017/11/8.
 */
@Service
public class TopicTypeServiceImpl implements TopicTypeService {

    @Autowired
    private TopicTypeRepository topicTypeRepository;

    @Override
    public ResponseEntity<VoTopicTypeResp> publishTopicType(VoDoAgainVerifyReq voDoAgainVerifyReq) {
        //只有管理员才能发布主题类型
        if (ObjectUtils.isEmpty(voDoAgainVerifyReq)
                || StringUtils.isEmpty(voDoAgainVerifyReq.getParamStr())
                || StringUtils.isEmpty(voDoAgainVerifyReq.getSign())
                || !SecurityHelper.checkSign(voDoAgainVerifyReq.getSign(), voDoAgainVerifyReq.getParamStr())) {
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR,"非法访问",VoTopicTypeResp.class));

        }
        Map<String, VoTopicTypeReq> paramMap = new Gson().fromJson(voDoAgainVerifyReq.getParamStr(), new TypeToken<Map<String, String>>() {
        }.getType()) ;

        VoTopicTypeReq voTopicTypeReq = paramMap.get("voTopicTypeReq") ;
        TopicType topicType = new TopicType() ;
        topicType.setAdminId(voTopicTypeReq.getAdminId()) ;
        topicType.setIconUrl(voTopicTypeReq.getIconUrl()) ;
        topicType.setTopicTypeName(voTopicTypeReq.getTopicTypeName()) ;
        topicType = topicTypeRepository.save(topicType) ;
        return ResponseEntity.ok(VoBaseResp.ok("发布主题类型成功", VoTopicTypeResp.class)) ;
    }

    @Override
    public ResponseEntity<VoTopicTypeResp> delTopicType(long id, long userId) {
        TopicType topicType = topicTypeRepository.findOne(id);
        if (ObjectUtils.isEmpty(topicType)) {
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "删除主题类型失败", VoTopicTypeResp.class)) ;
        }
        //只有发帖人才能删除主题类型板块
        if (topicType.getAdminId() != userId) {
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "无权删除", VoTopicTypeResp.class)) ;
        }
        try {
            topicTypeRepository.delete(id);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "删除主题类型失败", VoTopicTypeResp.class)) ;
        }
        return ResponseEntity.ok(VoBaseResp.ok("删除主题类型成功", VoTopicTypeResp.class)) ;
    }

    @Override
    public ResponseEntity<VoTopicTypeListResp> listTopicType() {
        List<TopicType> topicTypes = topicTypeRepository.findAll() ;

        VoTopicTypeListResp voTopicTypeListResp = VoBaseResp.ok("查询主题模块成功", VoTopicTypeListResp.class);
        voTopicTypeListResp.setVoTopicTypeRespList(topicTypes);
        return  ResponseEntity.ok(voTopicTypeListResp);
    }
}
