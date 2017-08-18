package com.gofobao.framework.member.service.impl;

import com.gofobao.framework.member.entity.Users;
import com.gofobao.framework.member.repository.UsersRepository;
import com.gofobao.framework.member.service.UserService;
import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.Query;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * 用户实体类
 * Created by Max on 20.03.16.
 */
@Service
@Slf4j
public class UserServiceImpl implements UserService {
    @Autowired
    private UsersRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    @Override
    public List<Users> listUser(Users users) {
        Example<Users> usersExample = Example.of(users);
        List<Users> usersList = userRepository.findAll(usersExample);
        return Optional.ofNullable(usersList).orElse(Collections.EMPTY_LIST);
    }

    @Override
    public Users findByAccount(String account) {
        List<Users> usersList = userRepository.findByUsernameOrPhoneOrEmail(account, account, account);

        if (!CollectionUtils.isEmpty(usersList)) {
            return usersList.get(0);
        }
        return null;
    }

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    public Users findById(Long id) {
        return userRepository.findOne(id);
    }


    @Override
    public boolean notExistsByPhone(String phone) {
        List<Users> usersList = userRepository.findByPhone(phone);
        return CollectionUtils.isEmpty(usersList);

    }

    /**
     * 带锁查询会员
     *
     * @param userId
     * @return
     */
    public Users findByIdLock(Long userId) {
        return userRepository.findById(userId);
    }

    @Override
    public boolean notExistsByUserName(String userName) {
        List<Users> users = userRepository.findByUsername(userName);
        return CollectionUtils.isEmpty(users);
    }

    @Override
    public Users findByInviteCode(String inviteCode) {
        List<Users> users = userRepository.findByInviteCode(inviteCode);
        if (CollectionUtils.isEmpty(users)) {
            return null;
        } else {
            return users.get(0);
        }

    }

    @Override
    public Users save(Users users) {
        return userRepository.save(users);
    }

    @Override
    public boolean notExistsByEmail(String email) {
        Users users = new Users();
        users.setEmail(email);
        Example<Users> example = Example.of(users);
        return !userRepository.exists(example);
    }

    /**
     * 检查是否实名
     *
     * @param users
     * @return
     */
    public boolean checkRealname(Users users) {
        if (ObjectUtils.isEmpty(users)) {
            return false;
        }
        return !(ObjectUtils.isEmpty(users.getCardId()) || ObjectUtils.isEmpty(users.getUsername()));
    }

    public List<Users> findList(Specification<Users> specification) {
        return userRepository.findAll(specification);
    }

    public List<Users> findList(Specification<Users> specification, Sort sort) {
        return userRepository.findAll(specification, sort);
    }

    public List<Users> findList(Specification<Users> specification, Pageable pageable) {
        return userRepository.findAll(specification, pageable).getContent();
    }

    public long count(Specification<Users> specification) {
        return userRepository.count(specification);
    }


    @Override
    public List<Users> serviceUser() {
        String sql = "SELECT t1.* " +
                "FROM `gfb_users` t1 JOIN gfb_role_user t2 ON t1.id = t2.user_id WHERE t2.role_id in (3, 4)";
        Query query = entityManager.createNativeQuery(sql, Users.class);
        return query.getResultList();
    }

    @Override
    public Users findUserByUserId(Long userId) {
        Users users = userRepository.findById(userId);
        Preconditions.checkNotNull(users, "UserServiceImpl.findUserByUserId: data is null");

        return users;
    }

    @Override
    public Page<Users> findAll(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    /**
     * 验证身份证是否存在
     *
     * @param idCard
     * @return
     */
    @Override
    public boolean notExistsByIdCard(String idCard) {
        Users users = new Users();
        users.setCardId(idCard);
        Example<Users> example = Example.of(users);
        return !userRepository.exists(example);
    }


    @Override
    public List<Users> findByIdIn(List<Long> ids) {


        return userRepository.findByIdIn(ids);
    }
}
