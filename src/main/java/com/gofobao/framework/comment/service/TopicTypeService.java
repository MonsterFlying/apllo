package com.gofobao.framework.comment.service;

import com.gofobao.framework.borrow.vo.request.VoDoAgainVerifyReq;
import com.gofobao.framework.comment.entity.TopicType;
import com.gofobao.framework.comment.vo.request.VoTopicTypeReq;
import com.gofobao.framework.comment.vo.response.VoTopicResp;
import com.gofobao.framework.comment.vo.response.VoTopicTypeListResp;
import com.gofobao.framework.comment.vo.response.VoTopicTypeResp;
import com.gofobao.framework.core.vo.VoBaseResp;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by xin on 2017/11/8.
 */

public interface TopicTypeService {
    /**
     * 发布主题类型
     * @param voDoAgainVerifyReq
     * @return
     */
    ResponseEntity<VoBaseResp> publishTopicType( VoDoAgainVerifyReq voDoAgainVerifyReq);

    /**
     * 删除主题类型
     * @param id
     * @return
     */
    ResponseEntity<VoBaseResp> delTopicType(long id, long userId);

    /**
     * 查询主题板块列表
     * @return
     */
    ResponseEntity<VoTopicTypeListResp> listTopicType();

}
