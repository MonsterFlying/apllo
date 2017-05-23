package com.gofobao.framework.asset.service;

import com.gofobao.framework.asset.entity.BankAccount;

import java.util.List;

/**
 * Created by Zeke on 2017/5/22.
 */
public interface BankAccountService {

    List<BankAccount> findByDeletedAtIsNullAndUserIdAndIsVerify(Long userId, Integer isVerify);

    List<BankAccount> listBankByUserId(Long userId);

    BankAccount findByUserIdAndId(Long userId, Long bankId);
}
