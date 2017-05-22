package com.gofobao.framework.asset.service.impl;

import com.gofobao.framework.asset.entity.BankAccount;
import com.gofobao.framework.asset.repository.BankAccountRepository;
import com.gofobao.framework.asset.service.BankAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by Zeke on 2017/5/22.
 */
@Service
public class BankAccountServiceImpl implements BankAccountService {

    @Autowired
    private BankAccountRepository bankAccountRepository;

    /**
     * 查询用户银行列表
     *
     * @param userId
     * @return
     */
    public List<BankAccount> findByDeletedAtIsNullAndUserIdAndIsVerify(Long userId, Integer isVerify) {
        return bankAccountRepository.findByDeletedAtIsNullAndUserIdAndIsVerify(userId, isVerify);
    }
}
