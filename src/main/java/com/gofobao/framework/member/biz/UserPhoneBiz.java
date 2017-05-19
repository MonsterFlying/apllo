package com.gofobao.framework.member.biz;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.member.vo.request.VoBindSwitchPhone;
import com.gofobao.framework.member.vo.request.VoCheckSwitchPhone;
import org.springframework.http.ResponseEntity;

/**
 * Created by Zeke on 2017/5/19.
 */
public interface UserPhoneBiz {
    /**
     * 更改手机
     * @param voCheckSwitchPhone
     * @return
     */
    ResponseEntity<VoBaseResp> checkSwitchPhone(VoCheckSwitchPhone voCheckSwitchPhone);

    /**
     * 更改手机
     * @param voBindSwitchPhone
     * @return
     */
    ResponseEntity<VoBaseResp> bindSwitchPhone(VoBindSwitchPhone voBindSwitchPhone);
}
