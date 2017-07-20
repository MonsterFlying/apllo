package com.gofobao.framework.member.repository;

import com.gofobao.framework.member.entity.UserThirdAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Created by Max on 17/5/22.
 */
@Repository
public interface UserThirdAccountRepository extends JpaRepository<UserThirdAccount, Long>, JpaSpecificationExecutor<UserThirdAccount> {

    UserThirdAccount findTopByCardNoAndDel(String account, int del);

    UserThirdAccount findTopByMobileAndDel(String phone, int del);
}
