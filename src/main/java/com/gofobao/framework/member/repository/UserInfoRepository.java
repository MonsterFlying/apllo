package com.gofobao.framework.member.repository;

import com.gofobao.framework.member.entity.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by Max on 17/6/1.
 */
@Repository
public interface UserInfoRepository extends JpaRepository<UserInfo, Long> {
}
