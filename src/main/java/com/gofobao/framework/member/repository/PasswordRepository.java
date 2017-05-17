package com.gofobao.framework.member.repository;

import com.gofobao.framework.member.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by admin on 2017/5/17.
 */
public interface PasswordRepository extends JpaRepository<Users,Long> {
    Users findByName(String name);


}
