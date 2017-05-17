package com.gofobao.framework.member.service;

import com.gofobao.framework.member.entity.Users;

import java.util.List;

/**
 * Created by Max on 17/5/17.
 */
public interface UserService {

    List<Users> listUser(Users users) ;

    Users findByAccount(String account) ;

    /**
     * 注册用户
     * @param voRegisterReq
     * @return
     */
    VoRegisterResp register(VoRegisterReq voRegisterReq);


    /**
     * 注册用户回调
     * @param voRegisterCallReq
     * @return
     */
    VoRegisterCallResp registerCall(VoRegisterCallReq voRegisterCallReq);

    /**
     * 判断手机是否唯一
     * @param phone 手机唯一
     * @return
     */
    boolean phoneIsOnly(String phone);
}
