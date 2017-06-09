package com.gofobao.framework.member.biz;

import com.gofobao.framework.asset.vo.request.VoJudgmentAvailableReq;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.member.vo.request.VoBindSwitchPhoneReq;
import com.gofobao.framework.member.vo.request.VoCheckSwitchPhoneReq;
import org.springframework.http.ResponseEntity;

/**
 * Created by Zeke on 2017/5/19.
 */
public interface UserPhoneBiz {
    /**
     * 更改手机
     * @param voCheckSwitchPhoneReq
     * @return
     */
    ResponseEntity<VoBaseResp> checkSwitchPhone(VoCheckSwitchPhoneReq voCheckSwitchPhoneReq);

    /**
     * 更改手机
     * @param voBindSwitchPhoneReq
     * @return
     */
    ResponseEntity<VoBaseResp> bindSwitchPhone(VoBindSwitchPhoneReq voBindSwitchPhoneReq);


    /**
     * 检测用户手机/邮箱/用户名是否唯一
     * @param voJudgmentAvailableReq
     * @return
     */
    ResponseEntity<VoBaseResp> checkOnlyForUserInfo(VoJudgmentAvailableReq voJudgmentAvailableReq);
}
