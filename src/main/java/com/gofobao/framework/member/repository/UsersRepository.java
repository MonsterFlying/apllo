package com.gofobao.framework.member.repository;

import com.gofobao.framework.member.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;
import java.util.List;

/**
 * Created by Max on 17/5/16.
 */
@Repository
public interface UsersRepository extends JpaRepository<Users,Long>{

    List<Users> findByUsernameOrPhoneOrEmail(String username, String phone, String email);

    /** 通过手机号码查找会员*/
    List<Users> findByPhone(String phone);

    /** 通过邮箱查找会员*/
    List<Users> findByEmail(String email);

    /** 带锁查询会员 **/
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Users findById(Long userId);

    /** 通过用户名查找会员*/
    List<Users> findByUsername(String userName);

    /** 根据邀请码获取用户信息*/
    List<Users> findByInviteCode(String inviteCode);
}
