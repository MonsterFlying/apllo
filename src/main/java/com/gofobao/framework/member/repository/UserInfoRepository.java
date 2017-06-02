package com.gofobao.framework.member.repository;

import com.gofobao.framework.member.entity.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by Max on 17/6/1.
 */
public interface UserInfoRepository extends JpaRepository<UserInfo, Long> {
}
