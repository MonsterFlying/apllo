package com.gofobao.framework.member.repository;

import com.gofobao.framework.member.entity.UserCache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;

/**
 * Created by Zeke on 2017/5/19.
 */
@Repository
public interface UserCacheRepository extends JpaRepository<UserCache, Long>, JpaSpecificationExecutor<UserCache> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    UserCache findByUserId(Long userId);
    /**
     * 用户总计收益
     *
     * @return
     */
    @Query("SELECT SUM(incomeInterest" +
            "+incomeAward" +
            "+incomeOverdue" +
            "+incomeIntegralCash" +
            "+incomeBonus+incomeOther) FROM UserCache")
    Long userIncomeTotal();
}
