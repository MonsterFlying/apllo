package com.gofobao.framework.member.service;

import com.gofobao.framework.member.entity.Users;

import java.util.List;

/**
 * Created by Max on 17/5/17.
 */
public interface UserService {

    List<Users> listUser(Users users) ;

    /**
     * 根据账号查找用户信息
     * @param account
     * @return
     */
    Users findByAccount(String account) ;

    /**
     * 根据唯一标示查找用户
     * @param id
     * @return
     */
    Users findById(Long id) ;

    /**
     * 判断手机是否唯一
     * @param phone 手机唯一
     * @return
     */
    boolean notExistsByPhone(String phone);

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


    /**
     * 判断用户名是否存在
     * @param userName
     * @return
     */
    boolean notExistsByUserName(String userName);


    /**
     * 根据推荐吗查找用户
     * @param inviteCode 推荐码
     * @return
     */
    Users findByInviteCode(String inviteCode);

    /**
     * 保存/更新用户
     * @param users
     * @return
     */
    Users save(Users users);

    /**
     * 检测邮箱不存在
     * @param email
     * @return
     */
    boolean notExistsByEmail(String email);

}
