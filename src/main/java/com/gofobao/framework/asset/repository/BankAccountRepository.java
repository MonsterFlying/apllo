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
}
