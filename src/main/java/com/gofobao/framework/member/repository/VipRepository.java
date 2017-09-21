package com.gofobao.framework.member.repository;

import com.gofobao.framework.member.entity.Vip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;

/**
 * Created by Administrator on 2017/6/16 0016.
 */
@Repository
public interface VipRepository extends JpaRepository<Vip, Long> {
    /**
     * vip信息
     * @param userId
     * @param status
     * @param date
     * @return
     */
    Vip findTopByUserIdAndStatusIsAndExpireAtGreaterThan(Long userId, int status, Date date);
}
