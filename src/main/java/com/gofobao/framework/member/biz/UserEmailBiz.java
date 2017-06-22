package com.gofobao.framework.member.biz;

import com.gofobao.framework.member.vo.request.VoBindEmailReq;
import com.gofobao.framework.member.vo.response.VoBasicUserInfoResp;
import org.springframework.http.ResponseEntity;

/**
 * Created by Administrator on 2017/6/16 0016.
 */
public interface UserEmailBiz {

    /**
     * 绑定邮件
     * @param voBindEmailReq
     * @param userId
     * @return
     */
    ResponseEntity<VoBasicUserInfoResp> bindEmail(VoBindEmailReq voBindEmailReq, Long userId);
}
