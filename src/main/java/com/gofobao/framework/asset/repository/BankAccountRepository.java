package com.gofobao.framework.asset.repository;

import com.gofobao.framework.asset.entity.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by Zeke on 2017/5/22.
 */
@Repository
public interface BankAccountRepository extends JpaRepository<BankAccount,Long>{

    List<BankAccount> findByDeletedAtIsNullAndUserIdAndIsVerify(Long userId,Integer isVerify);

    /**
     * 根据用户ID查询银行卡
     * @param userId 用户ID
     * @return
     */
    List<BankAccount> findByUserIdAndDeletedAtIsNull(Long userId);

    /**
     * 根据用户Id和银行卡Id查询银行卡
     * @param bankId
     * @param userId
     * @return
     */
    List<BankAccount> findByIdAndUserIdAndDeletedAtIsNull(Long bankId, Long userId);
}
