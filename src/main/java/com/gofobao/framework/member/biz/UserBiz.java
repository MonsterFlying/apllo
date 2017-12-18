package com.gofobao.framework.member.biz;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.member.entity.Users;
import com.gofobao.framework.member.entity.Vip;
import com.gofobao.framework.member.vo.request.*;
import com.gofobao.framework.member.vo.response.VoBasicUserInfoResp;
import com.gofobao.framework.member.vo.response.VoOpenAccountInfo;
import com.gofobao.framework.member.vo.response.VoThirdLoginRes;
import com.gofobao.framework.member.vo.response.pc.BalanceOfPaymentRes;
import com.gofobao.framework.member.vo.response.pc.UserInfoExt;
import com.gofobao.framework.member.vo.response.pc.VipInfoRes;
import com.gofobao.framework.member.vo.response.pc.VoViewServiceUserListWarpRes;
import com.gofobao.framework.security.vo.VoLoginReq;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * Created by Zeke on 2017/5/19.
 */
public interface UserBiz {

    /**
     * 第三方登录
     *
     * @param voThirdLoginReq
     * @return
     * @throws Exception
     */
    ResponseEntity<VoThirdLoginRes> pcThirdLogin(VoThirdLoginReq voThirdLoginReq) throws Exception;

    /**
     * 用户注册
     *
     * @param request       请求
     * @param voRegisterReq 注册实体
     * @return
     */
    ResponseEntity<VoBaseResp> register(HttpServletRequest request, VoRegisterReq voRegisterReq) throws Exception;

    /**
     * 获取用户详情
     *
     * @param user
     * @return
     */
    ResponseEntity<VoBasicUserInfoResp> getUserInfoResp(Users user);

    /**
     * 获取用户配置详情
     *
     * @param userId
     * @return
     */
    ResponseEntity<VoBasicUserInfoResp> userInfo(Long userId);

    /**
     * 用户信息
     *
     * @param userId
     * @return
     */
    ResponseEntity<UserInfoExt> pcUserInfo(Long userId);

    /**
     * 用户资料更新
     *
     * @param infoUpdateReq
     * @return
     */
    ResponseEntity<VoBaseResp> pcUserInfoUpdate(VoUserInfoUpdateReq infoUpdateReq);

    /**
     * 登录
     *
     * @param httpServletRequest
     * @param response
     * @param voLoginReq
     * @param financeState       理财用户为true
     * @return
     */
    ResponseEntity<VoBasicUserInfoResp> login(HttpServletRequest httpServletRequest, HttpServletResponse response, VoLoginReq voLoginReq, boolean financeState);

    /**
     * 开启vip
     *
     * @param vip
     * @return
     */
    ResponseEntity<VoBaseResp> saveVip(Vip vip);


    ResponseEntity<VoViewServiceUserListWarpRes> serviceUserList();

    /**
     * @param userId
     * @return
     */
    ResponseEntity<VipInfoRes> vipInfo(Long userId);


    /**
     * 设置交易密码
     *
     * @param tranPassWord
     * @return
     */
    ResponseEntity<VoBaseResp> saveUserTranPassWord(VoSettingTranPassWord tranPassWord);


    /**
     * 重置交易密码
     *
     * @param restPayPassWord
     * @return
     */
    ResponseEntity<VoBaseResp> restPayPassWord(VoRestPayPassWord restPayPassWord);

    /**
     * 注册后续操作
     *
     * @param userId
     * @return
     */
    boolean registerExtend(Long userId) throws Exception;

    /**
     * 获取存管信息
     *
     * @param userId
     * @return
     */
    ResponseEntity<VoOpenAccountInfo> openAccountInfo(Long userId);


    Map<String, Object> uploadAvatar(byte[] fileBty, String filePath, Users users) throws Exception;

    /**
     * 用户今日收支
     *
     * @param userId
     * @return
     */
    ResponseEntity<BalanceOfPaymentRes> userBalanceOfPayment(Long userId);

}

