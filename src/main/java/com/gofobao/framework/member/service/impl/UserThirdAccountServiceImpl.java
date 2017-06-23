package com.gofobao.framework.member.service.impl;

import com.gofobao.framework.member.entity.UserThirdAccount;
import com.gofobao.framework.member.repository.UserThirdAccountRepository;
import com.gofobao.framework.member.service.UserThirdAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

/**
 * Created by Max on 17/5/22.
 */
@Service
public class UserThirdAccountServiceImpl implements UserThirdAccountService {

    @Autowired
    private UserThirdAccountRepository userThirdAccountRepository;

    @Override
    public UserThirdAccount findByUserId(Long id) {
        if (ObjectUtils.isEmpty(id)){
            return null;
        }
        UserThirdAccount example = new UserThirdAccount();
        example.setUserId(id);
        example.setDel(0);
        return userThirdAccountRepository.findOne(Example.of(example));
    }

    @Override
    public Long save(UserThirdAccount entity) {
        UserThirdAccount account = userThirdAccountRepository.save(entity);
        if(ObjectUtils.isEmpty(account)){
            return 0L ;
        }
        return account.getId() ;
    }

    @Override
    public UserThirdAccount findTopByCardNo(String account) {
        return userThirdAccountRepository.findTopByCardNo(account) ;
    }

    @Override
    public UserThirdAccount findByMobile(String phone) {
        return userThirdAccountRepository.findTopByMobile(phone) ;
    }
}
