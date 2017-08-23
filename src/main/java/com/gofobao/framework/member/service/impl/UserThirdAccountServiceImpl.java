package com.gofobao.framework.member.service.impl;

import com.gofobao.framework.member.entity.UserThirdAccount;
import com.gofobao.framework.member.entity.Users;
import com.gofobao.framework.member.repository.UserThirdAccountRepository;
import com.gofobao.framework.member.service.UserThirdAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.List;

/**
 * Created by Max on 17/5/22.
 */
@Service
public class UserThirdAccountServiceImpl implements UserThirdAccountService {

    @Autowired
    private UserThirdAccountRepository userThirdAccountRepository;

    @Override
    public UserThirdAccount findByUserId(Long id) {
        if (ObjectUtils.isEmpty(id)) {
            return null;
        }
        UserThirdAccount example = new UserThirdAccount();
        example.setUserId(id);
        example.setDel(0);
        return userThirdAccountRepository.findOne(Example.of(example));
    }


    /**
     * 根据存管accountId查询用户存管信息
     *
     * @param accountId
     * @return
     */
    public UserThirdAccount findByAccountId(String accountId) {
        if (ObjectUtils.isEmpty(accountId)) {
            return null;
        }
        UserThirdAccount example = new UserThirdAccount();
        example.setAccountId(accountId);
        return userThirdAccountRepository.findOne(Example.of(example));
    }

    @Override
    public Long save(UserThirdAccount entity) {
        UserThirdAccount account = userThirdAccountRepository.save(entity);
        if (ObjectUtils.isEmpty(account)) {
            return 0L;
        }
        return account.getId();
    }

    @Override
    public UserThirdAccount findTopByCardNo(String account) {
        return userThirdAccountRepository.findTopByCardNoAndDel(account, 0);
    }

    @Override
    public UserThirdAccount findByMobile(String phone) {
        return userThirdAccountRepository.findTopByMobileAndDel(phone, 0);
    }

    @Override
    public void deleteByUserId(Long userId) {
        UserThirdAccount userThirdAccount = findByUserId(userId);
        userThirdAccountRepository.delete(userThirdAccount.getId());
    }

    @Override
    public UserThirdAccount findByDelUseid(Long userId) {
        if (ObjectUtils.isEmpty(userId)) {
            return null;
        }
        UserThirdAccount example = new UserThirdAccount();
        example.setUserId(userId);
        example.setDel(1);
        return userThirdAccountRepository.findOne(Example.of(example));

    }

    @Override
    public List<UserThirdAccount> findByAll() {
        return userThirdAccountRepository.findAll();
    }

    @Override
    public List<UserThirdAccount> findList(Specification<UserThirdAccount> userThirderAccountSpe) {
        return userThirdAccountRepository.findAll(userThirderAccountSpe);
    }

    public List<UserThirdAccount> findList(Specification<UserThirdAccount> userThirderAccountSpe, Pageable pageable) {
        return userThirdAccountRepository.findAll(userThirderAccountSpe, pageable).getContent();
    }

    public List<UserThirdAccount> findList(Specification<UserThirdAccount> userThirderAccountSpe, Sort sort) {
        return userThirdAccountRepository.findAll(userThirderAccountSpe, sort);
    }

    public long count(Specification<UserThirdAccount> userThirderAccountSpe) {
        return userThirdAccountRepository.count(userThirderAccountSpe);
    }

    @Override
    public Page<UserThirdAccount> findAll(Pageable pageable) {
        return userThirdAccountRepository.findAll(pageable);
    }

    @Override
    public void save(List<UserThirdAccount> userThirdAccountList) {
        userThirdAccountRepository.save(userThirdAccountList);
    }
}
