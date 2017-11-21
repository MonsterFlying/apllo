package com.gofobao.framework.comment.service.impl;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.borrow.vo.request.VoDoAgainVerifyReq;
import com.gofobao.framework.comment.entity.TopicType;
import com.gofobao.framework.comment.repository.TopicTypeRepository;
import com.gofobao.framework.comment.service.TopicTypeService;
import com.gofobao.framework.comment.vo.request.VoTopicTypeReq;
import com.gofobao.framework.comment.vo.response.VoTopicListResp;
import com.gofobao.framework.comment.vo.response.VoTopicTypeListResp;
import com.gofobao.framework.comment.vo.response.VoTopicTypeResp;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.project.SecurityHelper;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
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
    public ResponseEntity<VoBaseResp> publishTopicType(VoDoAgainVerifyReq voDoAgainVerifyReq) {
        //只有管理员才能发布主题类型
        if (ObjectUtils.isEmpty(voDoAgainVerifyReq)
                || StringUtils.isEmpty(voDoAgainVerifyReq.getParamStr())
                || StringUtils.isEmpty(voDoAgainVerifyReq.getSign())
                || !SecurityHelper.checkSign(voDoAgainVerifyReq.getSign(), voDoAgainVerifyReq.getParamStr())) {
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR,"非法访问",VoBaseResp.class));

        }
        Map<String, VoTopicTypeReq> paramMap = new Gson().fromJson(voDoAgainVerifyReq.getParamStr(), new TypeToken<Map<String, String>>() {
        }.getType()) ;

        VoTopicTypeReq voTopicTypeReq = paramMap.get("voTopicTypeReq") ;
        TopicType topicType = new TopicType() ;
        topicType.setAdminId(voTopicTypeReq.getAdminId()) ;
        topicType.setIconUrl(voTopicTypeReq.getIconUrl()) ;
        topicType.setTopicTypeName(voTopicTypeReq.getTopicTypeName()) ;
        topicType = topicTypeRepository.save(topicType);
        Preconditions.checkNotNull(topicType,"保存用户失败");
        return ResponseEntity.ok(VoBaseResp.ok("发布主题类型成功", VoBaseResp.class)) ;
    }

    @Override
    @Transactional
    public ResponseEntity<VoBaseResp> delTopicType(long id, long userId) {
        TopicType topicType = topicTypeRepository.findOne(id);
        if (ObjectUtils.isEmpty(topicType)) {
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "删除主题类型失败", VoBaseResp.class)) ;
        }
        //只有发帖人才能删除主题类型板块
        if (topicType.getAdminId() != userId) {
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "无权删除", VoBaseResp.class)) ;
        }
        try {
            Integer count = topicTypeRepository.updateDel(id);
            Preconditions.checkNotNull(count,"删除主题类型失败");
           // topicTypeRepository.delete(id);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "删除主题类型失败", VoBaseResp.class)) ;
        }
        return ResponseEntity.ok(VoBaseResp.ok("删除主题类型成功", VoBaseResp.class)) ;
    }

    @Override
    public ResponseEntity<VoTopicTypeListResp> listTopicType() {
        Specification<TopicType> specification = Specifications.
                <TopicType>and()
                .eq("del",0)
                .build();

        List<TopicType> topicTypes = topicTypeRepository.findAll(specification) ;
        VoTopicTypeListResp voTopicTypeListResp = VoBaseResp.ok("查询主题模块成功", VoTopicTypeListResp.class) ;
        for (TopicType topicType : topicTypes) {
            VoTopicTypeResp voTopicTypeResp = new VoTopicTypeResp() ;
            voTopicTypeResp.setTopicTypeName(topicType.getTopicTypeName()) ;
            voTopicTypeResp.setAdminId(topicType.getAdminId()) ;
            voTopicTypeResp.setHotState(topicType.getHotState()) ;
            voTopicTypeResp.setNewState(topicType.getNewState()) ;
            voTopicTypeResp.setDel(topicType.getDel()) ;
            voTopicTypeResp.setIconUrl(topicType.getIconUrl());
            voTopicTypeResp.setTopicTotalNum(topicType.getTopicTotalNum());
            voTopicTypeResp.setCreateDate(topicType.getCreateDate());
            voTopicTypeResp.setUpdateDate(topicType.getUpdateDate());
            voTopicTypeListResp.getVoTopicTypeRespList().add(voTopicTypeResp);
        }

        return  ResponseEntity.ok(voTopicTypeListResp) ;
    }
}
