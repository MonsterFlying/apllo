package com.gofobao.framework.member.service;

import com.gofobao.framework.member.entity.UserThirdAccount;

/**
 * Created by Max on 17/5/22.
 */
public interface UserThirdAccountService {

    /**
     * 根据用户Id查询用户存管信息
     * @param id
     * @return
     */
    UserThirdAccount findByUserId(Long id);

    /**
     * 保存存管账户
     * @param entity
     * @return
     */
    Long save(UserThirdAccount entity);

    UserThirdAccount findTopByCardNo(String account);

    UserThirdAccount findByMobile(String phone);
}
