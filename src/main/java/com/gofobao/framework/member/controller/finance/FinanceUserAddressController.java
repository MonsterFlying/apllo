package com.gofobao.framework.member.controller.finance;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.member.biz.UserAddressBiz;
import com.gofobao.framework.member.vo.request.*;
import com.gofobao.framework.member.vo.response.VoViewFindUserAddressDetail;
import com.gofobao.framework.member.vo.response.VoViewFindUserAddressList;
import com.gofobao.framework.security.contants.SecurityContants;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;

/**
 * Created by Zeke on 2017/11/10.
 */
@RestController
@RequestMapping
@Slf4j
@Api(description = "用户收货地址控制器")
public class FinanceUserAddressController {

    @Autowired
    private UserAddressBiz userAddressBiz;

    /**
     * 保存收货地址
     *
     * @param voAddUserAddress
     * @return
     */
    @ApiOperation("保存收货地址")
    @PostMapping("/finance/user/address/add")
    public ResponseEntity<VoBaseResp> addUserAddress(@Valid @ModelAttribute VoAddUserAddress voAddUserAddress,
                                                     @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        voAddUserAddress.setUserId(userId);
        return userAddressBiz.addUserAddress(voAddUserAddress);
    }


    /**
     * 更新收货地址
     *
     * @param voUpdateUserAddress
     * @return
     */
    @ApiOperation("更新收货地址")
    @PostMapping("/finance/user/address/upd")
    public ResponseEntity<VoBaseResp> updateUserAddress(@Valid @ModelAttribute VoUpdateUserAddress voUpdateUserAddress
            , @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        voUpdateUserAddress.setUserId(userId);
        return userAddressBiz.updateUserAddress(voUpdateUserAddress);
    }

    /**
     * 设置默认地址
     *
     * @param voSetDefaultUserAddress
     * @return
     */
    @ApiOperation("设置默认地址")
    @PostMapping("/finance/user/address/set/default")
    public ResponseEntity<VoBaseResp> setDefaultUserAddress(@Valid @ModelAttribute VoSetDefaultUserAddress voSetDefaultUserAddress
            , @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        voSetDefaultUserAddress.setUserId(userId);
        return userAddressBiz.setDefaultUserAddress(voSetDefaultUserAddress);
    }


    /**
     * 删除收货地址
     *
     * @param voDeleteUserAddress
     * @return
     */
    @ApiOperation("删除收货地址")
    @PostMapping("/finance/user/address/del")
    public ResponseEntity<VoBaseResp> deleteUserAddress(@Valid @ModelAttribute VoDeleteUserAddress voDeleteUserAddress
            , @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        voDeleteUserAddress.setUserId(userId);
        return userAddressBiz.deleteUserAddress(voDeleteUserAddress);
    }

    /**
     * 查询收货地址详情
     *
     * @param voFindUserAddress
     * @return
     */
    @ApiOperation("查询收货地址详情")
    @PostMapping("/finance/user/address/detail")
    public ResponseEntity<VoViewFindUserAddressDetail> findUserAddressDetail(@Valid @ModelAttribute VoFindUserAddress voFindUserAddress
            , @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        voFindUserAddress.setUserId(userId);
        return userAddressBiz.findUserAddressDetail(voFindUserAddress);
    }


    /**
     * 查询收货地址详列表
     *
     * @param voFindUserAddressList
     * @return
     */
    @ApiOperation("查询收货地址详列表")
    @PostMapping("/finance/user/address/list")
    public ResponseEntity<VoViewFindUserAddressList> findUserAddressList(@Valid @ModelAttribute VoFindUserAddressList voFindUserAddressList
            , @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        voFindUserAddressList.setUserId(userId);
        return userAddressBiz.findUserAddressList(voFindUserAddressList);
    }
}
