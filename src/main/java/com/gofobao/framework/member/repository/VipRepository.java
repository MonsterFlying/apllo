package com.gofobao.framework.member.repository;

import com.gofobao.framework.member.entity.Vip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by Administrator on 2017/6/16 0016.
 */
@Repository
public interface VipRepository extends JpaRepository<Vip, Long> {
    Vip findTopByUserIdAndStatus(Long userId, int status);
}
