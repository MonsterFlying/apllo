package com.gofobao.framework.asset.service.impl;

import com.gofobao.framework.asset.entity.BankAccount;
import com.gofobao.framework.asset.repository.BankAccountRepository;
import com.gofobao.framework.asset.service.BankAccountService;
import com.gofobao.framework.member.entity.Users;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

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

    @Override
    public List<BankAccount> listBankByUserId(Long userId) {
        List<BankAccount> bankAccountList =  bankAccountRepository.findByUserIdAndDeletedAtIsNull(userId) ;
        return Optional.fromNullable(bankAccountList).or(Lists.newArrayList());
    }

    @Override
    public BankAccount findByUserIdAndId(Long userId, Long bankId) {
        List<BankAccount> bankAccounts = bankAccountRepository.findByIdAndUserIdAndDeletedAtIsNull(bankId, userId) ;

        if(CollectionUtils.isEmpty(bankAccounts)){
            return null ;
        }else{
            return bankAccounts.get(0) ;
        }
    }
}
