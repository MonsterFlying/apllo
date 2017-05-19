package com.gofobao.framework.member.biz;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.member.vo.request.VoCheckFindPasswordReq;
import com.gofobao.framework.member.vo.request.VoFindPasswordReq;
import com.gofobao.framework.member.vo.request.VoModifyPasswordReq;
import org.springframework.http.ResponseEntity;

/**
 * Created by Zeke on 2017/5/18.
 */
public interface UserPasswordBiz {

    /**
     * 用户修改密码
     * @param voModifyPasswordReq
     * @return
     */
    ResponseEntity<VoBaseResp> modifyPassword(VoModifyPasswordReq voModifyPasswordReq);

    /**
     * 用户忘记密码
     * @param voFindPasswordReq
     * @return
     */
    ResponseEntity<VoBaseResp> findPassword(VoFindPasswordReq voFindPasswordReq);

    /**
     * 校验找回密码
     * @param voCheckFindPasswordReq
     * @return
     */
    ResponseEntity<VoBaseResp> checkFindPassword(VoCheckFindPasswordReq voCheckFindPasswordReq);
}
