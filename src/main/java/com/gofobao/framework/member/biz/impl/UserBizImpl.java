package com.gofobao.framework.member.biz.impl;

import com.gofobao.framework.asset.entity.Asset;
import com.gofobao.framework.asset.service.AssetService;
import com.gofobao.framework.common.rabbitmq.MqConfig;
import com.gofobao.framework.common.rabbitmq.MqHelper;
import com.gofobao.framework.common.rabbitmq.MqQueueEnum;
import com.gofobao.framework.common.rabbitmq.MqTagEnum;
import com.gofobao.framework.core.helper.PasswordHelper;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.currency.entity.Currency;
import com.gofobao.framework.currency.service.CurrencyService;
import com.gofobao.framework.helper.*;
import com.gofobao.framework.helper.project.UserHelper;
import com.gofobao.framework.integral.entity.Integral;
import com.gofobao.framework.integral.service.IntegralService;
import com.gofobao.framework.member.biz.UserBiz;
import com.gofobao.framework.member.entity.*;
import com.gofobao.framework.member.enums.RegisterSourceEnum;
import com.gofobao.framework.member.service.*;
import com.gofobao.framework.member.vo.request.VoRegisterReq;
import com.gofobao.framework.member.vo.response.VoBasicUserInfoResp;
import com.gofobao.framework.member.vo.response.pc.UserInfoExt;
import com.gofobao.framework.security.helper.JwtTokenHelper;
import com.gofobao.framework.security.vo.VoLoginReq;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.UUID;

/**
 * Created by Zeke on 2017/5/19.
 */
@Service
@Slf4j
public class UserBizImpl implements UserBiz {
    static final Gson GSON = new Gson();

    @Autowired
    UserService userService;

    @Autowired
    UserInfoService userInfoService;

    @Autowired
    AssetService assetService;

    @Autowired
    UserCacheService userCacheService;

    @Autowired
    IntegralService integralService;

    @Autowired
    CurrencyService currencyService;

    @Autowired
    UserThirdAccountService userThirdAccountService;

    @Autowired
    VipService vipService;


    @Autowired
    RedisHelper redisHelper;

    @Autowired
    MacthHelper macthHelper;


    @Value("${jwt.header}")
    String tokenHeader;

    @Value("${jwt.prefix}")
    String prefix ;

    @Value("${gofobao.imageDomain}")
    String imageDomain;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    JwtTokenHelper jwtTokenHelper;

    @Autowired
    MqHelper mqHelper ;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<VoBaseResp> register(HttpServletRequest request, VoRegisterReq voRegisterReq) throws Exception {
        // 0.短信验证码

        boolean match = macthHelper.match(MqTagEnum.SMS_REGISTER.getValue(), voRegisterReq.getPhone(), voRegisterReq.getSmsCode());
        if (!match) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "短信验证码失效/错误, 请重新发送短信验证码!"));
        }

        // 1.手机处理
        boolean notPhoneState = userService.notExistsByPhone(voRegisterReq.getPhone());
        if (!notPhoneState) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "当前手机已经在平台注册！"));
        }

        // 2.用户名处理
        if (!StringUtils.isEmpty(voRegisterReq.getUserName())) {

            boolean notUserName = userService.notExistsByUserName(voRegisterReq.getUserName());
            if (!notUserName) {
                return ResponseEntity
                        .badRequest()
                        .body(VoBaseResp.error(VoBaseResp.ERROR, "当前用户名已经在平台注册！"));
            }
        }

        Integer parentId = 0;
        if (!StringUtils.isEmpty(voRegisterReq.getInviteCode())) {
            // 3.推荐人处理
            Users invitedUser = userService.findByInviteCode(voRegisterReq.getInviteCode());
            if (ObjectUtils.isEmpty(invitedUser)) {
                return ResponseEntity
                        .badRequest()
                        .body(VoBaseResp.error(VoBaseResp.ERROR, "无效的邀请码！"));
            }

            parentId = invitedUser.getId().intValue();
        }

        // 处理注册来源
        String source = voRegisterReq.getSource();
        Integer channel = RegisterSourceEnum.getIndex(source.toLowerCase());
        Date now = new Date();
        // 插入数据
        Users users = new Users();
        users.setEmail(null);
        users.setUsername(voRegisterReq.getUserName());
        users.setPhone(voRegisterReq.getPhone());
        users.setCardId(null);
        users.setPassword(PasswordHelper.encodingPassword(voRegisterReq.getPassword())); // 设置密码
        users.setPayPassword("");
        users.setRealname("");
        users.setType("");
        users.setBranch(0);
        users.setSource(channel);
        users.setInviteCode(GenerateInviteCodeHelper.getRandomCode()); // 生成用户邀请码
        users.setParentId(parentId);
        users.setParentAward(0);
        users.setCreatedAt(now);
        users.setUpdatedAt(now);

        users = userService.save(users);
        if ((ObjectUtils.isEmpty(users)) || (ObjectUtils.isEmpty(users.getId()))) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "服务器开小差了， 请稍候再试！"));
        }

        if (!registerExtend(users.getId())) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "服务器开小差了， 请稍候再试！"));
        }

        // 4.触发注册事件
        MqConfig mqConfig = new MqConfig();
        mqConfig.setQueue(MqQueueEnum.RABBITMQ_USER_ACTIVE);
        mqConfig.setTag(MqTagEnum.USER_ACTIVE_REGISTER);
        ImmutableMap<String, String> body = ImmutableMap
                .of(MqConfig.MSG_USER_ID, users.getId().toString(), MqConfig.MSG_TIME, DateHelper.dateToString(now));
        mqConfig.setMsg(body);
        mqConfig.setSendTime(DateHelper.addSeconds(new Date(), 30));
        boolean mqState;
        try {
            log.info(String.format("userBizImpl register send mq %s", GSON.toJson(body)));
            mqState = mqHelper.convertAndSend(mqConfig);
        } catch (Throwable e) {
            log.error("userBizImpl register send mq exception", e);
            throw new Exception(e);
        }

        if (!mqState) {
            log.error("userBizImpl register send mq error");
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "服务器开小差了，请稍候再试！"));

        }
        // 5.删除短信验证码
        redisHelper.remove(String.format("%s_%s", MqTagEnum.SMS_REGISTER, voRegisterReq.getPhone()));
        return ResponseEntity.ok(VoBaseResp.ok("注册成功"));
    }

    @Override
    public Users findByAccount(String account) {
        return userService.findByAccount(account);
    }

    @Override
    public ResponseEntity<VoBasicUserInfoResp> getUserInfoResp(Users user) {
        VoBasicUserInfoResp voBasicUserInfoResp = VoBaseResp.ok("操作成功", VoBasicUserInfoResp.class);
        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(user.getId());
        if (ObjectUtils.isEmpty(userThirdAccount)) {
            voBasicUserInfoResp.setThirdAccountState(false);
            voBasicUserInfoResp.setBankPassworState(false);
            voBasicUserInfoResp.setBankState(false);
            voBasicUserInfoResp.setBankAccout(" ");
            voBasicUserInfoResp.setAutoTenderState(false);
            voBasicUserInfoResp.setAutoTranferState(false);
        } else {
            voBasicUserInfoResp.setThirdAccountState(true);
            voBasicUserInfoResp.setBankPassworState(userThirdAccount.getPasswordState() == 1);
            voBasicUserInfoResp.setBankAccout(UserHelper.hideChar(userThirdAccount.getAccountId(), UserHelper.BANK_ACCOUNT_NUM));
            voBasicUserInfoResp.setBankState(!StringUtils.isEmpty(userThirdAccount.getCardNo()));
            voBasicUserInfoResp.setAutoTenderState(userThirdAccount.getAutoTenderState().equals(1));
            voBasicUserInfoResp.setAutoTranferState(userThirdAccount.getAutoTransferState().equals(1));
        }


        // 获取vip状态
        Vip vip = vipService.findTopByUserIdAndStatus(user.getId(), 1);
        voBasicUserInfoResp.setAvatarUrl(String.format("%S/data/images/avatar/$s_avatar_small.jpg", imageDomain, user.getId()));
        voBasicUserInfoResp.setVipState(ObjectUtils.isEmpty(vip) ? false : DateHelper.diffInDays(new Date(), vip.getExpireAt(), false) > 0);
        voBasicUserInfoResp.setEmail(UserHelper.hideChar(StringUtils.isEmpty(user.getEmail()) ? " " : user.getEmail(), UserHelper.EMAIL_NUM));
        voBasicUserInfoResp.setEmailState(!StringUtils.isEmpty(user.getEmail()));
        voBasicUserInfoResp.setPhone(UserHelper.hideChar(StringUtils.isEmpty(user.getPhone()) ? " " : user.getPhone(), UserHelper.PHONE_NUM));
        voBasicUserInfoResp.setPhoneState(!StringUtils.isEmpty(user.getPhone()));
        voBasicUserInfoResp.setRealname(UserHelper.hideChar(StringUtils.isEmpty(user.getRealname()) ? " " : user.getRealname(), UserHelper.REALNAME_NUM));
        voBasicUserInfoResp.setRealnameState(!StringUtils.isEmpty(user.getRealname()));
        voBasicUserInfoResp.setIdNo(UserHelper.hideChar(StringUtils.isEmpty(user.getCardId())? " ": user.getCardId(), UserHelper.CARD_ID_NUM)); ;
        voBasicUserInfoResp.setIdNoState(!StringUtils.isEmpty(user.getCardId())) ;
        voBasicUserInfoResp.setAlias(user.getPushId()) ;
        return ResponseEntity.ok(voBasicUserInfoResp);
    }

    @Override
    public ResponseEntity<VoBasicUserInfoResp> userInfo(Long userId) {
        Users user = userService.findById(userId);
        return getUserInfoResp(user);
    }

    @Override
    public ResponseEntity<VoBasicUserInfoResp> login(HttpServletRequest httpServletRequest, HttpServletResponse response, VoLoginReq voLoginReq) {
        // Perform the security
        final Authentication authentication ;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            voLoginReq.getAccount(),
                            voLoginReq.getPassword()
                    )
            );
        } catch (Throwable e) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "账户/密码错误", VoBasicUserInfoResp.class)) ;
        }
        SecurityContextHolder.getContext().setAuthentication(authentication);
        // Reload password_reset post-security so we can generate captchaToken
        Users user = findByAccount(voLoginReq.getAccount());

        // 保存登录信息
        if(ObjectUtils.isEmpty(user)){
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "账户/密码错误", VoBasicUserInfoResp.class));
        }

        if(user.getIsLock()) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "账户已被系统冻结，如有问题请联系客服！", VoBasicUserInfoResp.class));
        }

        String username = user.getUsername();
        if(StringUtils.isEmpty(username)) username = user.getPhone() ;
        if(StringUtils.isEmpty(username)) username = user.getEmail() ;
        user.setUsername(username);

        final String token = jwtTokenHelper.generateToken(user, voLoginReq.getSource());
        response.addHeader(tokenHeader, String.format("%s %s", prefix, token));
        user.setPlatform( voLoginReq.getSource() );
        user.setPushId(UUID.randomUUID().toString());  // 设置唯一标识
        user.setIp(IpHelper.getIpAddress(httpServletRequest)); // 设置ip
        userService.save(user) ;   // 记录登录信息

        // 触发登录队列
        MqConfig mqConfig = new MqConfig();
        mqConfig.setTag(MqTagEnum.LOGIN);
        mqConfig.setQueue(MqQueueEnum.RABBITMQ_USER_ACTIVE);
        mqConfig.setSendTime(DateHelper.addSeconds(new Date(), 10));
        ImmutableMap<String, String> body = ImmutableMap.of(MqConfig.MSG_USER_ID, user.getId().toString());
        mqConfig.setMsg(body);
        mqHelper.convertAndSend(mqConfig);

        return getUserInfoResp(user) ;
    }

    /**
     * 注册后续操作
     *
     * @param userId
     * @return
     */
    private boolean registerExtend(Long userId) throws Exception {
        Date now = new Date();
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(userId);
        userInfo.setUpdatedAt(now);
        userInfo = userInfoService.save(userInfo);
        if ((ObjectUtils.isEmpty(userInfo))) {
            return false;
        }

        Asset asset = new Asset();
        asset.setCollection(0L);
        asset.setNoUseMoney(0L);
        asset.setPayment(0L);
        asset.setUpdatedAt(now);
        asset.setUseMoney(0L);
        asset.setVirtualMoney(0L);
        asset.setUserId(userId);
        asset = assetService.save(asset);
        if (ObjectUtils.isEmpty(asset)) {
            return false;
        }

        UserCache userCache = new UserCache();
        userCache.setUserId(userId);
        userCache = userCacheService.save(userCache);
        if (ObjectUtils.isEmpty(userCache)) {
            return false;
        }

        Integral integral = new Integral();
        integral.setUserId(userId);
        integral = integralService.save(integral);
        if (ObjectUtils.isEmpty(integral)) {
            return false;
        }

        Currency currency = new Currency();
        currency.setUserId(userId);
        currency = currencyService.save(currency);
        if (ObjectUtils.isEmpty(currency)) {
            return false;
        }

        return true;
    }

    /**
     * 用户扩展信息
     * @param userId
     * @return
     */
    @Override
    public ResponseEntity<UserInfoExt> pcUserInfo(Long userId) {
        UserInfoExt userInfoExt = VoBaseResp.ok("查询成功", UserInfoExt.class);
        UserInfo userInfo = userInfoService.info(userId);
        userInfoExt.setAddress(userInfo.getAddress());
        userInfoExt.setBir(DateHelper.dateToString(userInfo.getBirthday(),DateHelper.DATE_FORMAT_YMD));
        userInfoExt.setQq(userInfo.getQq());
        userInfoExt.setIncome(StringHelper.formatMon(userInfo.getIndustry()/100D));
        userInfoExt.setEducationalHistory(userInfo.getGraduatedSchool());
        userInfoExt.setMaritalStatus(userInfo.getMarital());
        userInfoExt.setSchool(StringUtils.isEmpty(userInfo.getSchool())?"":userInfo.getSchool());
        userInfoExt.setSex(userInfo.getSex());
        return ResponseEntity.ok(userInfoExt);
    }
}
