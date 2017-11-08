package com.gofobao.framework.member.biz;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.member.vo.request.*;
import com.gofobao.framework.member.vo.response.VoViewFindUserAddressDetail;
import com.gofobao.framework.member.vo.response.VoViewFindUserAddressList;
import org.springframework.http.ResponseEntity;

/**
 * Created by Zeke on 2017/11/8.
 */
public interface UserAddressBiz {
    /**
     * 新增收货地址
     *
     * @param voAddUserAddress
     * @return
     */
    ResponseEntity<VoBaseResp> addUserAddress(VoAddUserAddress voAddUserAddress);

    /**
     * 更新收货地址
     *
     * @param voUpdateUserAddress
     * @return
     */
    ResponseEntity<VoBaseResp> updateUserAddress(VoUpdateUserAddress voUpdateUserAddress);

    /**
     * 设置默认地址
     *
     * @param voSetDefaultUserAddress
     * @return
     */
    ResponseEntity<VoBaseResp> setDefaultUserAddress(VoSetDefaultUserAddress voSetDefaultUserAddress);

    /**
     * 删除地址
     *
     * @param voDeleteUserAddress
     * @return
     */
    ResponseEntity<VoBaseResp> deleteUserAddress(VoDeleteUserAddress voDeleteUserAddress);

    /**
     * 查询收货地址详情
     *
     * @param voFindUserAddress
     * @return
     */
    ResponseEntity<VoViewFindUserAddressDetail> findUserAddressDetail(VoFindUserAddress voFindUserAddress);


    /**
     * 查询收货地址详情
     *
     * @param voFindUserAddressList
     * @return
     */
    ResponseEntity<VoViewFindUserAddressList> findUserAddressList(VoFindUserAddressList voFindUserAddressList);
}
