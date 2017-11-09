package com.gofobao.framework.member.service.impl;

import com.gofobao.framework.member.repository.UserAddressRepository;
import com.gofobao.framework.member.entity.UserAddress;
import com.gofobao.framework.member.service.UserAddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by Zeke on 2017/11/8.
 */
@Service
public class UserAddressServiceImpl implements UserAddressService {
    @Autowired
    UserAddressRepository userAddressRepository;

    @Override
    public UserAddress findById(long id) {
        return userAddressRepository.findOne(id);
    }
    @Override
    public List<UserAddress> findList(Specification<UserAddress> specification) {
        return userAddressRepository.findAll(specification);
    }
    @Override
    public List<UserAddress> findList(Specification<UserAddress> specification, Sort sort) {
        return userAddressRepository.findAll(specification, sort);
    }
    @Override
    public List<UserAddress> findList(Specification<UserAddress> specification, Pageable pageable) {
        return userAddressRepository.findAll(specification, pageable).getContent();
    }
    @Override
    public long count(Specification<UserAddress> specification) {
        return userAddressRepository.count(specification);
    }
    @Override
    public UserAddress save(UserAddress UserAddress) {
        return userAddressRepository.save(UserAddress);
    }
    @Override
    public List<UserAddress> save(List<UserAddress> UserAddressList) {
        return userAddressRepository.save(UserAddressList);
    }
}
