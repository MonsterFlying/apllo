package com.gofobao.framework.member.service;

import com.gofobao.framework.member.entity.UserAddress;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

/**
 * Created by Zeke on 2017/11/8.
 */
public interface UserAddressService {
    /**
     * 根据id查询用户地址
     *
     * @param id
     * @return
     */
    UserAddress findById(long id);

    /**
     * 查询列表
     *
     * @param specification
     * @return
     */
    List<UserAddress> findList(Specification<UserAddress> specification);

    /**
     * 查询列表
     *
     * @param specification
     * @param sort
     * @return
     */
    List<UserAddress> findList(Specification<UserAddress> specification, Sort sort);

    /**
     * 查询列表
     *
     * @param specification
     * @param pageable
     * @return
     */
    List<UserAddress> findList(Specification<UserAddress> specification, Pageable pageable);

    /**
     * 计算条数
     *
     * @param specification
     * @return
     */
    long count(Specification<UserAddress> specification);

    /**
     * 保存记录
     *
     * @param UserAddress
     * @return
     */
    UserAddress save(UserAddress UserAddress);

    /**
     * 保存一组记录
     *
     * @param UserAddressList
     * @return
     */
    List<UserAddress> save(List<UserAddress> UserAddressList);
}
