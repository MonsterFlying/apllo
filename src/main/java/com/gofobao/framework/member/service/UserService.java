package com.gofobao.framework.member.service;

import com.gofobao.framework.member.entity.Users;
import com.gofobao.framework.member.vo.request.VoRegisterCallReq;
import com.gofobao.framework.member.vo.request.VoRegisterReq;
import com.gofobao.framework.member.vo.response.VoRegisterCallResp;
import com.gofobao.framework.member.vo.response.VoRegisterResp;

import java.util.List;

/**
 * Created by Max on 17/5/17.
 */
public interface UserService {

    List<Users> listUser(Users users) ;

    Users findByAccount(String account) ;

    Users findById(Long id) ;

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
    VoRegisterCallResp registerCallBack(VoRegisterCallReq voRegisterCallReq);

    /**
     * 判断手机是否唯一
     * @param phone 手机唯一
     * @return
     */
    boolean notExistsByPhone(String phone);

    /**
     * 根据id更新用户
     * @param users
     * @return
     */
    boolean updUserById(Users users);

    /**
     * 根据手机号码更新用户
     * @param users
     * @return
     */
    boolean updUserByPhone(Users users);

    /**
     * 带锁查询会员
     * @param userId
     * @return
     */
    Users findByIdLock(Long userId);

    /**
     * 检查是否实名
     * @param users
     * @return
     */
    boolean checkRealname(Users users);

}
