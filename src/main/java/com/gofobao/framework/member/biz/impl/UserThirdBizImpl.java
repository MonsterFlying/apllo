package com.gofobao.framework.member.biz.impl;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.member.biz.UserThirdBiz;
import com.gofobao.framework.member.entity.Users;
import com.gofobao.framework.member.service.BankAccountService;
import com.gofobao.framework.member.service.UserService;
import com.gofobao.framework.member.vo.response.VoPreOpenAccountResp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * Created by Max on 17/5/22.
 */
@Service
public class UserThirdBizImpl implements UserThirdBiz {

    @Autowired
    UserService userService ;

    @Autowired
    BankAccountService bankAccountService ;

    @Override
    public ResponseEntity<VoPreOpenAccountResp> preOpenAccount(Long userId) {
        // 验证用户是否存在
        Users user = userService.findById(userId);
        if(ObjectUtils.isEmpty(user)){
            return ResponseEntity.badRequest().body(new VoPreOpenAccountResp(VoBaseResp.ERROR, "你访问的账户不存在")) ;
        }

        // 判断用户是否已经开过存管账户
        if(!StringUtils.isEmpty(user.getThirdAccount())){
            return ResponseEntity.badRequest().body(new VoPreOpenAccountResp(VoBaseResp.ERROR, "你的账户已经开户！")) ;
        }


        // 判断用户是否已经开过存管账户
        if(!StringUtils.isEmpty(user.getPhone())){
            return ResponseEntity.badRequest().body(new VoPreOpenAccountResp(VoBaseResp.ERROR, "你的账户没有绑定手机，请先绑定手机！")) ;
        }

        VoPreOpenAccountResp voPreOpenAccountResp = new VoPreOpenAccountResp( ) ;
        voPreOpenAccountResp.setState(VoBaseResp.OK);
        voPreOpenAccountResp.setMsg("查询成功");
        voPreOpenAccountResp.setMobile(user.getPhone());
        voPreOpenAccountResp.setIdNo(user.getCardId());
        voPreOpenAccountResp.setName(user.getRealname());
        return null ;

    }
}
