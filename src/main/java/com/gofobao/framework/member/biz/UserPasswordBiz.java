package com.gofobao.framework.member.biz;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.member.vo.request.VoCheckFindPassword;
import com.gofobao.framework.member.vo.request.VoFindPassword;
import com.gofobao.framework.member.vo.request.VoModifyPassword;
import org.springframework.http.ResponseEntity;

/**
 * Created by Zeke on 2017/5/18.
 */
public interface UserPasswordBiz {

    /**
     * 用户修改密码
     * @param voModifyPassword
     * @return
     */
    ResponseEntity<VoBaseResp> modifyPassword(VoModifyPassword voModifyPassword);

    /**
     * 用户忘记密码
     * @param voFindPassword
     * @return
     */
    ResponseEntity<VoBaseResp> findPassword(VoFindPassword voFindPassword);

    /**
     * 校验找回密码
     * @param voCheckFindPassword
     * @return
     */
    ResponseEntity<VoBaseResp> checkFindPassword(VoCheckFindPassword voCheckFindPassword);
}
