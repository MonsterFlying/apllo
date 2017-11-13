package com.gofobao.framework.member.biz.impl;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.member.biz.UserAddressBiz;
import com.gofobao.framework.member.entity.UserAddress;
import com.gofobao.framework.member.entity.Users;
import com.gofobao.framework.member.service.UserAddressService;
import com.gofobao.framework.member.service.UserService;
import com.gofobao.framework.member.vo.request.*;
import com.gofobao.framework.member.vo.response.VoViewFindUserAddressDetail;
import com.gofobao.framework.member.vo.response.VoViewFindUserAddressList;
import com.gofobao.framework.member.vo.response.VoUserAddress;
import com.google.common.base.Preconditions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Zeke on 2017/11/8.
 */
@Service
public class UserAdressBizImpl implements UserAddressBiz {

    @Autowired
    UserService userService;
    @Autowired
    UserAddressService userAddressService;

    /**
     * 保存收货地址
     *
     * @param voAddUserAddress
     * @return
     */
    @Override
    public ResponseEntity<VoBaseResp> addUserAddress(VoAddUserAddress voAddUserAddress) {
        long userId = voAddUserAddress.getUserId();
        /*用户记录*/
        Users users = userService.findById(userId);
        Preconditions.checkNotNull(users, "用户记录不存在！");
        //判断最大地址不能操作5个
        Specification<UserAddress> uas = com.github.wenhao.jpa.Specifications
                .<UserAddress>and()
                .eq("userId", userId)
                .eq("isDel", false)
                .eq("type", 0)
                .build();
        long count = userAddressService.count(uas);
        /* 最大个数 */
        long maxQuantity = 5;
        //最大不能超过5个
        if (count >= maxQuantity) {
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "收货地址最多设置5个!"));
        }

        Date nowDate = new Date();

        UserAddress userAddress = new UserAddress();
        userAddress.setUserId(userId);
        userAddress.setName(voAddUserAddress.getName());
        userAddress.setPhone(voAddUserAddress.getPhone());
        userAddress.setCountry(voAddUserAddress.getCountry());
        userAddress.setCity(voAddUserAddress.getCity());
        userAddress.setDistrict(voAddUserAddress.getDistrict());
        userAddress.setDetailedAddress(voAddUserAddress.getDetailedAddress());
        userAddress.setCreateAt(nowDate);
        userAddress.setUpdateAt(nowDate);
        //判断是否存在有效的收货地址，没有则设置为默认
        if (count == 0) {
            userAddress.setIsDefault(true);
        }
        userAddressService.save(userAddress);

        return ResponseEntity.ok(VoBaseResp.ok("保存成功!"));
    }


    /**
     * 更新收货地址
     *
     * @param voUpdateUserAddress
     * @return
     */
    @Override
    public ResponseEntity<VoBaseResp> updateUserAddress(VoUpdateUserAddress voUpdateUserAddress) {
        long id = voUpdateUserAddress.getId();
        long userId = voUpdateUserAddress.getUserId();
        /*用户记录*/
        Users users = userService.findById(userId);
        Preconditions.checkNotNull(users, "用户记录不存在！");
        UserAddress userAddress = userAddressService.findById(id);
        Preconditions.checkNotNull(userAddress, "用户地址记录不存在!");
        Preconditions.checkState(userAddress.getUserId().longValue() == userId, "用户地址必须是本人修改!");
        //判断最大地址不能操作5个
        Specification<UserAddress> uas = com.github.wenhao.jpa.Specifications
                .<UserAddress>and()
                .eq("userId", userId)
                .eq("isDel", false)
                .eq("type", 0)
                .build();
        long count = userAddressService.count(uas);
        /* 最大个数 */
        long maxQuantity = 5;
        //最大不能超过5个
        if (count >= maxQuantity) {
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "收货地址最多设置5个!"));
        }

        Date nowDate = new Date();

        userAddress.setName(voUpdateUserAddress.getName());
        userAddress.setPhone(voUpdateUserAddress.getPhone());
        userAddress.setCountry(voUpdateUserAddress.getCountry());
        userAddress.setCity(voUpdateUserAddress.getCity());
        userAddress.setDistrict(voUpdateUserAddress.getDistrict());
        userAddress.setDetailedAddress(voUpdateUserAddress.getDetailedAddress());
        userAddress.setCreateAt(nowDate);
        userAddress.setUpdateAt(nowDate);
        //判断是否存在有效的收货地址，没有则设置为默认
        if (count == 0) {
            userAddress.setIsDefault(true);
        }
        userAddressService.save(userAddress);

        return ResponseEntity.ok(VoBaseResp.ok("保存成功!"));
    }

    /**
     * 设置默认地址
     *
     * @param voSetDefaultUserAddress
     * @return
     */
    @Override
    public ResponseEntity<VoBaseResp> setDefaultUserAddress(VoSetDefaultUserAddress voSetDefaultUserAddress) {
        long id = voSetDefaultUserAddress.getId();
        long userId = voSetDefaultUserAddress.getUserId();
        /*用户记录*/
        Users users = userService.findById(userId);
        Preconditions.checkNotNull(users, "用户记录不存在！");
        UserAddress userAddress = userAddressService.findById(id);
        Preconditions.checkNotNull(userAddress, "用户地址记录不存在!");
        Preconditions.checkState(userAddress.getUserId().longValue() == userId, "用户地址必须是本人修改!");

        Date nowDate = new Date();
        //查询默认地址
        Specification<UserAddress> uas = com.github.wenhao.jpa.Specifications
                .<UserAddress>and()
                .eq("userId", userId)
                .eq("isDel", false)
                .eq("type", 0)
                .eq("isDefault", true)
                .build();
        List<UserAddress> userAddressList = userAddressService.findList(uas);
        userAddressList.stream().forEach(userAddress1 -> {
            userAddress1.setIsDefault(false);
            userAddress.setCreateAt(nowDate);
            userAddress.setUpdateAt(nowDate);
        });
        userAddressService.save(userAddressList);

        //保存记录
        userAddress.setIsDefault(true);
        userAddress.setCreateAt(nowDate);
        userAddress.setUpdateAt(nowDate);
        userAddressService.save(userAddress);
        return ResponseEntity.ok(VoBaseResp.ok("设置成功!"));
    }


    /**
     * 删除地址
     *
     * @param voDeleteUserAddress
     * @return
     */
    @Override
    public ResponseEntity<VoBaseResp> deleteUserAddress(VoDeleteUserAddress voDeleteUserAddress) {
        long id = voDeleteUserAddress.getId();
        long userId = voDeleteUserAddress.getUserId();
        /*用户记录*/
        Users users = userService.findById(userId);
        Preconditions.checkNotNull(users, "用户记录不存在！");
        UserAddress userAddress = userAddressService.findById(id);
        Preconditions.checkNotNull(userAddress, "用户地址记录不存在!");
        Preconditions.checkState(userAddress.getUserId().longValue() == userId, "用户地址必须是本人修改!");

        Date nowDate = new Date();
        //保存记录
        userAddress.setIsDel(true);
        userAddress.setCreateAt(nowDate);
        userAddress.setUpdateAt(nowDate);
        userAddressService.save(userAddress);
        return ResponseEntity.ok(VoBaseResp.ok("删除成功!"));
    }

    /**
     * 查询收货地址详情
     *
     * @param voFindUserAddress
     * @return
     */
    @Override
    public ResponseEntity<VoViewFindUserAddressDetail> findUserAddressDetail(VoFindUserAddress voFindUserAddress) {
        long id = voFindUserAddress.getId();
        long userId = voFindUserAddress.getUserId();
        /*用户记录*/
        Users users = userService.findById(userId);
        Preconditions.checkNotNull(users, "用户记录不存在！");
        UserAddress userAddress = userAddressService.findById(id);
        Preconditions.checkNotNull(userAddress, "用户地址记录不存在!");
        Preconditions.checkState(userAddress.getUserId().longValue() == userId, "用户地址必须是本人修改!");

        VoViewFindUserAddressDetail voViewFindUserAddress = VoBaseResp.ok("查询成功!", VoViewFindUserAddressDetail.class);
        voViewFindUserAddress.setName(userAddress.getName());
        voViewFindUserAddress.setPhone(userAddress.getPhone());
        voViewFindUserAddress.setIsDefault(userAddress.getIsDefault());
        voViewFindUserAddress.setDetailedAddress(userAddress.getDetailedAddress());
        voViewFindUserAddress.setCity(userAddress.getCity());
        voViewFindUserAddress.setProvince(userAddress.getProvince());
        return ResponseEntity.ok(voViewFindUserAddress);
    }


    /**
     * 查询收货地址详情列表
     *
     * @param voFindUserAddressList
     * @return
     */
    @Override
    public ResponseEntity<VoViewFindUserAddressList> findUserAddressList(VoFindUserAddressList voFindUserAddressList) {
        long userId = voFindUserAddressList.getUserId();
        /*用户记录*/
        Users users = userService.findById(userId);
        Preconditions.checkNotNull(users, "用户记录不存在！");

        //查询默认地址
        Specification<UserAddress> uas = com.github.wenhao.jpa.Specifications
                .<UserAddress>and()
                .eq("userId", userId)
                .eq("isDel", false)
                .eq("type", 0)
                .build();
        List<UserAddress> userAddressList = userAddressService.findList(uas,
                new PageRequest(voFindUserAddressList.getPageIndex(), voFindUserAddressList.getPageSize()));
        List<VoUserAddress> showUserAddresses = new ArrayList<>();
        userAddressList.stream().forEach(userAddress -> {
            VoUserAddress voUserAddress = new VoUserAddress();
            voUserAddress.setName(userAddress.getName());
            voUserAddress.setPhone(userAddress.getPhone());
            voUserAddress.setIsDefault(userAddress.getIsDefault());
            voUserAddress.setDetailedAddress(userAddress.getDetailedAddress());
            voUserAddress.setCity(userAddress.getCity());
            voUserAddress.setProvince(userAddress.getProvince());
            showUserAddresses.add(voUserAddress);
        });

        VoViewFindUserAddressList voViewFindUserAddressList = VoBaseResp.ok("查询成功!", VoViewFindUserAddressList.class);
        voViewFindUserAddressList.setUserAddressList(showUserAddresses);
        return ResponseEntity.ok(voViewFindUserAddressList);
    }
}
