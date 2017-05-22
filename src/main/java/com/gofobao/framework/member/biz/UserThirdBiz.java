package com.gofobao.framework.member.biz;

import com.gofobao.framework.member.vo.response.VoPreOpenAccountResp;
import org.springframework.http.ResponseEntity;

/**
 * 银行存管账户
 * Created by Max on 17/5/22.
 */
public interface UserThirdBiz {


    /**
     * 会员存管开户前置请求
     * @param userId
     * @return
     */
    ResponseEntity<VoPreOpenAccountResp> preOpenAccount(Long userId);

}
