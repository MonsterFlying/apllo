package com.gofobao.framework.starfire.user.biz.impl;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.asset.entity.Asset;
import com.gofobao.framework.asset.service.AssetService;
import com.gofobao.framework.collection.contants.BorrowCollectionContants;
import com.gofobao.framework.collection.entity.BorrowCollection;
import com.gofobao.framework.collection.service.BorrowCollectionService;
import com.gofobao.framework.common.rabbitmq.MqConfig;
import com.gofobao.framework.common.rabbitmq.MqHelper;
import com.gofobao.framework.common.rabbitmq.MqQueueEnum;
import com.gofobao.framework.common.rabbitmq.MqTagEnum;
import com.gofobao.framework.core.helper.PasswordHelper;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.*;
import com.gofobao.framework.member.biz.UserBiz;
import com.gofobao.framework.member.entity.UserCache;
import com.gofobao.framework.member.entity.Users;
import com.gofobao.framework.member.service.UserCacheService;
import com.gofobao.framework.member.service.UserService;
import com.gofobao.framework.member.vo.response.VoBasicUserInfoResp;
import com.gofobao.framework.security.helper.JwtTokenHelper;
import com.gofobao.framework.security.vo.VoLoginReq;
import com.gofobao.framework.starfire.common.request.BaseRequest;
import com.gofobao.framework.starfire.common.response.CodeTypeConstant;
import com.gofobao.framework.starfire.common.response.ResultCodeEnum;
import com.gofobao.framework.starfire.common.response.ResultCodeMsgEnum;
import com.gofobao.framework.starfire.user.biz.StarFireUserBiz;
import com.gofobao.framework.starfire.user.vo.request.*;
import com.gofobao.framework.starfire.user.vo.response.*;
import com.gofobao.framework.starfire.util.AES;
import com.gofobao.framework.starfire.util.SignUtil;
import com.gofobao.framework.tender.contants.TenderConstans;
import com.gofobao.framework.tender.entity.Tender;
import com.gofobao.framework.tender.service.TenderService;
import com.gofobao.framework.windmill.user.vo.request.BindLoginReq;
import com.gofobao.framework.windmill.util.PassWordCreate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Range;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static io.netty.handler.codec.http.multipart.DiskFileUpload.prefix;

/**
 * Created by master on 2017/9/26.
 */
@Slf4j
@Service
public class StarFireUserBizImpl implements StarFireUserBiz {

    @Value("${starfire.key}")
    private String key;

    @Value("${starfire.initVector}")
    private String initVector;

    @Value("${starfire.notify_url}")
    private String notifyUrl;

    @Autowired
    private BaseRequest baseRequest;

    @Autowired
    private UserService userService;

    @Autowired
    private UserBiz userBiz;

    @Autowired
    private JwtTokenHelper jwtTokenHelper;

    @Value("${jwt.header}")
    private String tokenHeader;

    private static Gson GSON = new Gson();

    @Value("${gofobao.h5Domain}")
    private String h5Domain;

    @Value("${gofobao.pcDomain}")
    private String pcDomain;

    @Autowired
    private MqHelper mqHelper;

    @Autowired
    private AssetService assetService;

    @Autowired
    private UserCacheService userCacheService;

    @Autowired
    private TenderService tenderService;

    @Autowired
    private RedisHelper redisHelper;


    /**
     * 1.注册绑定查询
     *
     * @param registerQuery
     * @return
     */
    @Override
    public RegisterQueryRes registerQuery(RegisterQuery registerQuery) {

        log.info("============进入注册绑定查询接口===========");
        log.info("打印星火请求参数:" + GSON.toJson(registerQuery));
        //封装参数
        baseRequest.setSerial_num(registerQuery.getSerial_num());
        baseRequest.setC_code(registerQuery.getC_code());
        baseRequest.setSign(registerQuery.getSign());
        baseRequest.setT_code(registerQuery.getT_code());
        //封装返回参数
        RegisterQueryRes registerQueryRes = new RegisterQueryRes();
        registerQueryRes.setSerial_num(registerQuery.getSerial_num());
        registerQueryRes.setMobile(registerQuery.getMobile());
        try {
            //检查对象和 验证签名
            do {
                if (ObjectUtils.isEmpty(registerQuery)
                        || !SignUtil.checkSign(baseRequest, key, initVector)
                        || StringUtils.isEmpty(registerQuery.getMobile())) {
                    //未通过安全校验
                    String code = ResultCodeEnum.getCode(CodeTypeConstant.CHECK_SIGN_NO_PASS);
                    registerQueryRes.setErr_msg(ResultCodeMsgEnum.getResultMsg(code));
                    registerQueryRes.setResult(code);
                    log.info("未通过安全校验");
                    break;
                }
                String mobile = AES.decrypt(key, initVector, registerQuery.getMobile());
                Users user = userService.findByAccount(mobile);
                if (ObjectUtils.isEmpty(user)) {
                    //用户未注册
                    String code = ResultCodeEnum.getCode(CodeTypeConstant.NOT_EXIST_REGISTER);
                    registerQueryRes.setResult(code);
                    log.info("用户未注册");
                    break;
                }
                //已注册，未绑定任何渠道（含星火智)
                if (StringUtils.isEmpty(user.getWindmillId()) && StringUtils.isEmpty(user.getStarFireRegisterToken())) {
                    registerQueryRes.setResult(ResultCodeEnum.getCode(CodeTypeConstant.EXIST_NO_BIND));
                    log.info("已注册，未绑定任何渠道 (含星火智投)");
                    break;
                }
                //已注册，已绑定星火智投的其他渠道用户
                if (!StringUtils.isEmpty(user.getWindmillId()) && !StringUtils.isEmpty(user.getStarFireRegisterToken())) {
                    registerQueryRes.setResult(ResultCodeEnum.getCode(CodeTypeConstant.OTHER_CHANNEL_FIRE));
                    registerQueryRes.setRegister_token(user.getStarFireRegisterToken());
                    log.info("已注册，已绑定星火智投的其他渠道用户");
                    break;
                }
                //已注册，已绑定星火智投的引流用户(通过星火智投新注册平台的用户)
                if (!StringUtils.isEmpty(user.getStarFireRegisterToken())) {
                    registerQueryRes.setRegister_token(user.getStarFireRegisterToken());
                    registerQueryRes.setResult(ResultCodeEnum.getCode(CodeTypeConstant.EXIST_AND_BIND_STAR_FIRE));
                    log.info("已注册，已绑定星火智投的引流用户(通过星火智投新注册平台的用户)");
                    break;
                }
                //已注册，其他渠道用户
                if (StringUtils.isEmpty(user.getStarFireRegisterToken()) && !StringUtils.isEmpty(user.getWindmillId())) {
                    registerQueryRes.setResult(ResultCodeEnum.getCode(CodeTypeConstant.OTHER_CHANNEL));
                    log.info("已注册，其他渠道用户");
                    break;
                }
            } while (false);
        } catch (Exception e) {
            //其他错误
            log.info("其他错误", e);
            registerQueryRes.setResult(ResultCodeEnum.getCode(CodeTypeConstant.OTHER_ERROR));
        }
        return registerQueryRes;
    }

    /**
     * 2.新用户注册接口
     *
     * @param registerModel
     * @return
     */
    @Override
    public RegisterRes register(RegisterModel registerModel, HttpServletRequest request) {
        log.info("============进入新用户注册接口===========");
        log.info("打印星火请求参数:" + GSON.toJson(registerModel));
        //封装验签参数
        baseRequest.setT_code(registerModel.getT_code());
        baseRequest.setSign(registerModel.getSign());
        baseRequest.setC_code(registerModel.getC_code());
        baseRequest.setSerial_num(registerModel.getSerial_num());

        //封装返回参数
        RegisterRes resultMsg = new RegisterRes();
        resultMsg.setUser_id(registerModel.getUser_id());
        resultMsg.setMobile(registerModel.getMobile());
        resultMsg.setSerial_num(registerModel.getSerial_num());
        if (ObjectUtils.isEmpty(registerModel) || !SignUtil.checkSign(baseRequest, key, initVector)) {
            //未通过安全校验
            String code = ResultCodeEnum.getCode(CodeTypeConstant.CHECK_SIGN_NO_PASS);
            resultMsg.setErr_msg(ResultCodeMsgEnum.getResultMsg(code));
            resultMsg.setResult(code);
            log.info("未通过安全校验");
            return resultMsg;
        }
        //解密手机号 身份证
        String mobile = AES.decrypt(key, initVector, registerModel.getMobile());
        String identity = AES.decrypt(key, initVector, registerModel.getUser_identity());
        try {
            //判断用户是否存在
            if (!ObjectUtils.isEmpty(userService.findByAccount(mobile))
                    || !userService.notExistsByIdCard(identity)) {
                resultMsg.setRealNameAuthenticResult(!userService.notExistsByIdCard(identity) ? "1" : "");
                String code = ResultCodeEnum.getCode(CodeTypeConstant.FAIL_USER_EXIST);
                resultMsg.setResult(code);
                resultMsg.setErr_msg(ResultCodeMsgEnum.getResultMsg(code));
                log.info("注册失败,该用户已存在");
                return resultMsg;
            }
            //解密名字 星火用户id
            String trueName = AES.decrypt(key, initVector, registerModel.getUser_name());
            String starFireUserId = AES.decrypt(key, initVector, registerModel.getUser_id());
            // 插入数据
            Users starFireUser = new Users();
            starFireUser.setStarFireUserId(starFireUserId);
            starFireUser.setEmail(null);
            starFireUser.setPhone(mobile);
            starFireUser.setCardId(identity);
            PassWordCreate pwc = new PassWordCreate();
            String password = pwc.createPassWord(8);
            starFireUser.setPassword(PasswordHelper.encodingPassword(password)); // 设置密码
            starFireUser.setPayPassword("");
            starFireUser.setRealname(trueName);
            starFireUser.setType("");
            starFireUser.setBranch(0);
            starFireUser.setSource(13);
            starFireUser.setInviteCode(GenerateInviteCodeHelper.getRandomCode()); // 生成用户邀请码
            starFireUser.setParentId(0L);
            starFireUser.setParentAward(0);
            Date nowDate = new Date();
            //
            String registerToken = pwc.createPassWord(30);
            starFireUser.setStarFireRegisterToken(registerToken);
            starFireUser.setStarFireBindAt(nowDate);
            starFireUser.setCreatedAt(nowDate);
            starFireUser.setUpdatedAt(nowDate);
            starFireUser = userService.save(starFireUser);
            //保存是否成功
            if ((ObjectUtils.isEmpty(starFireUser)) ||
                    (ObjectUtils.isEmpty(starFireUser.getId())) ||
                    !userBiz.registerExtend(starFireUser.getId())) {
                resultMsg.setResult(ResultCodeEnum.getCode(CodeTypeConstant.FAIL_OTHER));
                resultMsg.setErr_msg("平台添加用户信息异常");
                log.info("星火智投用户 注册到平台失败");
                return resultMsg;
            }
            // 4.触发注册事件
            MqConfig mqConfig = new MqConfig();
            mqConfig.setQueue(MqQueueEnum.RABBITMQ_USER_ACTIVE);
            mqConfig.setTag(MqTagEnum.USER_ACTIVE_REGISTER);
            ImmutableMap<String, String> body = ImmutableMap
                    .of(MqConfig.MSG_USER_ID, starFireUser.getId().toString(),
                            MqConfig.MSG_TIME, DateHelper.dateToString(nowDate));
            mqConfig.setMsg(body);
            mqConfig.setSendTime(DateHelper.addSeconds(new Date(), 30));
            boolean mqState;
            try {
                log.info(String.format("星火智投用户注册 发送到mq body消息：%s", GSON.toJson(body)));
                mqState = mqHelper.convertAndSend(mqConfig);
            } catch (Throwable e) {
                log.error("星火智投用户注册:发送到mq失败", e);
                mqState = false;
            }
            if (!mqState) {
                resultMsg.setResult(ResultCodeEnum.getCode(CodeTypeConstant.OTHER_ERROR));
                resultMsg.setErr_msg("星火智投用户注册:平台注册发送MQ失败");
                return resultMsg;
            }
            //发送星火理财用户：注册成功的短信
            MqConfig smsConfig = new MqConfig();
            smsConfig.setQueue(MqQueueEnum.RABBITMQ_SMS);
            smsConfig.setTag(MqTagEnum.SMS_WINDMILL_USER_REGISTER);
            ImmutableMap<String, String> smsBody = ImmutableMap
                    .of(MqConfig.PHONE, mobile, MqConfig.IP, request.getRemoteAddr(), MqConfig.PASSWORD, password);
            smsConfig.setMsg(smsBody);
            mqHelper.convertAndSend(smsConfig);
            //注册成功返回
            resultMsg.setRealNameAuthenticResult("1");
            resultMsg.setResult(ResultCodeEnum.getCode(CodeTypeConstant.SUCCESS));
            resultMsg.setRegister_token(AES.encrypt(key, initVector, registerToken));
            resultMsg.setPlatform_uid(AES.encrypt(key, initVector, String.valueOf(starFireUser.getId())));
            log.info("星火智投用户注册成功");
            return resultMsg;
        } catch (Exception e) {
            resultMsg.setResult(ResultCodeEnum.getCode(CodeTypeConstant.OTHER_ERROR));
            resultMsg.setErr_msg("星火智投用户注册:失败");
            log.info("星火智投用户注册", e);
            return resultMsg;
        }
    }

    public String bindHtml(BindUserModel bindUserModel) {
        //封装验证参数
        baseRequest.setSign(bindUserModel.getSign());
        baseRequest.setC_code(bindUserModel.getC_code());
        baseRequest.setSerial_num(bindUserModel.getSerial_num());
        baseRequest.setT_code(bindUserModel.getT_code());
        if (!SignUtil.checkSign(baseRequest, key, initVector) || StringUtils.isEmpty(bindUserModel.getSource())) {
            return null;
        }
        String params = new Gson().toJson(bindUserModel);
        if (bindUserModel.getSource().equals("1")) {
            return pcDomain + "/third/xhzlogin?params=" + params;
        } else {
            //TODO 暂默认是pc登录地址
            return h5Domain + "/third/xhzlogin?params=" + params;
        }
    }

    /**
     * 用户登录绑定
     *
     * @param request
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<VoBasicUserInfoResp> bindLogin(HttpServletRequest request,
                                                         HttpServletResponse response,
                                                         BindLoginReq bindLoginReq) {
        log.info("===============进入星火智投绑定用户登录===============");
        log.info("用户名：" + bindLoginReq.getUserName() + ",密文：" + bindLoginReq.getParams());
        log.info("==================================================");

        String param = bindLoginReq.getParams();
        BindUserModel bindUserModel = GSON.fromJson(param, new TypeToken<BindUserModel>() {
        }.getType());
        //封装返回参数
        UserLoginBind userLoginBind = new UserLoginBind();
        userLoginBind.setSign(bindUserModel.getSign());
        userLoginBind.setMobile(bindUserModel.getMobile());
        userLoginBind.setSerial_num(bindUserModel.getSerial_num());
        userLoginBind.setUser_id(bindUserModel.getUser_id());
        userLoginBind.setT_code(bindUserModel.getT_code());
        userLoginBind.setC_code(bindUserModel.getC_code());
        //封装验证参数
        baseRequest.setSign(bindUserModel.getSign());
        baseRequest.setC_code(bindUserModel.getC_code());
        baseRequest.setSerial_num(bindUserModel.getSerial_num());
        baseRequest.setT_code(bindUserModel.getT_code());
        try {
            //验证sign
            if (!SignUtil.checkSign(baseRequest, key, initVector)) {
                String code = ResultCodeEnum.getCode(CodeTypeConstant.CHECK_SIGN_NO_PASS);
                userLoginBind.setResult(code);
                userLoginBind.setErr_msg(ResultCodeMsgEnum.getResultMsg(code));
                bindNotify(userLoginBind); //通知火星
                //验签失败
                return ResponseEntity.badRequest()
                        .body(VoBaseResp.error(
                                VoBaseResp.ERROR,
                                "验签失败",
                                VoBasicUserInfoResp.class));
            }
            VoLoginReq voLoginReq = new VoLoginReq();
            voLoginReq.setAccount(bindLoginReq.getUserName());
            voLoginReq.setPassword(bindLoginReq.getPassword());
            ResponseEntity entity = userBiz.login(request, response, voLoginReq, false);
            //登陸成功
            if (entity.getStatusCode() == HttpStatus.OK) {
                log.info("================用户登录成功===============");
                Users user = userService.findByAccount(bindLoginReq.getUserName());
                //如果本地不存在用户名 身份证 手机号 持久到本地
                user.setRealname(StringUtils.isEmpty(user.getRealname())
                        ? bindUserModel.getUser_name()
                        : user.getRealname());
                user.setCardId(StringUtils.isEmpty(user.getCardId())
                        ? bindUserModel.getUser_identity()
                        : user.getCardId());
                user.setPhone(StringUtils.isEmpty(user.getPhone())
                        ? bindUserModel.getMobile()
                        : user.getPhone());
                user.setStarFireUserId(AES.decrypt(key, initVector, bindUserModel.getUser_id()));
                PassWordCreate pwc = new PassWordCreate();
                String registerToken = pwc.createPassWord(30);
                user.setStarFireRegisterToken(registerToken);
                user.setStarFireBindAt(new Date());
                userService.save(user);
                try {
                    log.info("登录成功进入生成token");
                    VoBasicUserInfoResp voBasicUserInfoResp = VoBaseResp.ok("操作成功", VoBasicUserInfoResp.class);
                    String targetUrl = "";
                    String tokenStr = redisHelper.get("JWT_TOKEN_" + user.getId(), null);
                    if (StringUtils.isEmpty(tokenStr)) {
                        tokenStr = jwtTokenHelper.generateToken(user, Integer.valueOf(bindUserModel.getSource()));
                        response.addHeader(tokenHeader, String.format("%s %s", prefix, tokenStr));
                        // 触发登录队列
                        MqConfig mqConfig = new MqConfig();
                        mqConfig.setTag(MqTagEnum.LOGIN);
                        mqConfig.setQueue(MqQueueEnum.RABBITMQ_USER_ACTIVE);
                        mqConfig.setSendTime(DateHelper.addSeconds(new Date(), 10));
                        ImmutableMap<String, String> body = ImmutableMap.of(MqConfig.MSG_USER_ID, user.getId().toString());
                        mqConfig.setMsg(body);
                        mqHelper.convertAndSend(mqConfig);
                    }
                    //跳转target_url
                    String bidUrl = bindUserModel.getBid_url();
                    //if (voLoginReq.getSource().equals("1")) {  //pc端
                    targetUrl += StringUtils.isEmpty(bidUrl) ? pcDomain : pcDomain + bidUrl;
                    // } else {
                    //  targetUrl = StringUtils.isEmpty(bidUrl) ? h5Domain : h5Domain + "/" + bidUrl;
                    //}
                    voBasicUserInfoResp.setTarget_url(targetUrl + "?token=" + tokenStr);
                    userLoginBind.setRealNameAuthenticResult("true");
                    userLoginBind.setIsXeenhoChanne(StringUtils.isEmpty(user.getWindmillId()) ? "false" : "true");
                    UserCache userCache = userCacheService.findById(user.getId());
                    if (!StringUtils.isEmpty(userCache.getTenderQudao())
                            || !StringUtils.isEmpty(userCache.getTenderJingzhi())
                            || !StringUtils.isEmpty(userCache.getTenderMiao())
                            || StringUtils.isEmpty(userCache.getTenderTuijian())) {
                        userLoginBind.setIsInvested("true");
                    } else {
                        userLoginBind.setIsInvested("false");
                    }
                    userLoginBind.setPlatform_uid(AES.encrypt(key, initVector, user.getId().toString()));
                    userLoginBind.setResult(ResultCodeEnum.getCode(CodeTypeConstant.SUCCESS));
                    userLoginBind.setRegister_token(AES.encrypt(key, initVector, user.getStarFireRegisterToken()));
                    bindNotify(userLoginBind); //通知火星
                    return ResponseEntity.ok(voBasicUserInfoResp);
                } catch (Exception e) {
                    log.error("系统异常", e);
                    String code = ResultCodeEnum.getCode(CodeTypeConstant.OTHER_ERROR);
                    userLoginBind.setResult(code);
                    userLoginBind.setErr_msg(ResultCodeMsgEnum.getResultMsg(code));
                    bindNotify(userLoginBind);
                    return ResponseEntity.badRequest()
                            .body(VoBaseResp.error(
                                    VoBaseResp.ERROR,
                                    "系统异常",
                                    VoBasicUserInfoResp.class));
                }
            } else {
                log.info("账号或密码错误");
                userLoginBind.setResult(ResultCodeEnum.getCode(CodeTypeConstant.BIND_FAIL));
                userLoginBind.setErr_msg("账号或密码错误");
                bindNotify(userLoginBind);
                //登陆失败
                return ResponseEntity.badRequest()
                        .body(VoBaseResp.error(
                                VoBaseResp.ERROR,
                                "账号或密码错误",
                                VoBasicUserInfoResp.class));
            }
        } catch (Exception e) {
            log.error("星火智投登录绑定失败", e);
            String code = ResultCodeEnum.getCode(CodeTypeConstant.OTHER_ERROR);
            userLoginBind.setResult(code);
            userLoginBind.setErr_msg(ResultCodeMsgEnum.getResultMsg(code));
            bindNotify(userLoginBind);
            //登陆失败
            return ResponseEntity.badRequest()
                    .body(VoBaseResp.error(
                            VoBaseResp.ERROR,
                            "系统异常",
                            VoBasicUserInfoResp.class));
        }
    }

    /**
     * 获取login_token
     *
     * @param fetchLoginToken
     * @return
     */
    @Override
    @Transactional
    public FetchLoginTokenRes fetchLoginToken(FetchLoginToken fetchLoginToken) {
        log.info("===============进入星火获取绑定用户token==============");
        log.info("打印请求参数:" + GSON.toJson(fetchLoginToken));
        baseRequest.setT_code(fetchLoginToken.getT_code());
        baseRequest.setSerial_num(fetchLoginToken.getSerial_num());
        baseRequest.setSign(fetchLoginToken.getSign());
        baseRequest.setC_code(fetchLoginToken.getC_code());
        //封装返回参数
        FetchLoginTokenRes loginTokenRes = new FetchLoginTokenRes();
        loginTokenRes.setSerial_num(fetchLoginToken.getSerial_num());
        //验证签名
        try {
            if (!SignUtil.checkSign(baseRequest, key, initVector)) {
                String code = ResultCodeEnum.getCode(CodeTypeConstant.CHECK_SIGN_NO_PASS);
                loginTokenRes.setResult(code);
                loginTokenRes.setErr_msg(ResultCodeMsgEnum.getResultMsg(code));
                return loginTokenRes;
            }
            //解密参数
            String userId = AES.decrypt(key, initVector, fetchLoginToken.getPlatform_uid());
            String starFireUserId = AES.decrypt(key, initVector, fetchLoginToken.getUser_id());
            String registerToken = AES.decrypt(key, initVector, fetchLoginToken.getRegister_token());
            Users users = userService.findById(Long.valueOf(userId));
            //验证用户是否绑定
            if (ObjectUtils.isEmpty(users)
                    || ObjectUtils.isEmpty(users)
                    || StringUtils.isEmpty(users.getStarFireUserId())
                    || StringUtils.isEmpty(users.getStarFireRegisterToken())
                    || !users.getStarFireUserId().equals(starFireUserId)
                    || !users.getStarFireRegisterToken().equals(registerToken)) {
                String code = ResultCodeEnum.getCode(CodeTypeConstant.REGISTER_SUCCESS_BIND_FIRE_FAIL);
                log.info("当前用户未绑定星火,打印用户信息:" + GSON.toJson(users));
                loginTokenRes.setResult(code);
                loginTokenRes.setErr_msg(ResultCodeMsgEnum.getResultMsg(code));
                return loginTokenRes;
            }
            log.info("打印用户信息:" + GSON.toJson(users));
            try {
                //返回token
                String code = ResultCodeEnum.getCode(CodeTypeConstant.SUCCESS);
                loginTokenRes.setLogin_token(fetchLoginToken.getRegister_token());
                loginTokenRes.setResult(code);
                return loginTokenRes;
            } catch (Exception e) {
                log.info("获取登陆授权失败，系统异常", e);
                String code = ResultCodeEnum.getCode(CodeTypeConstant.OTHER_ERROR);
                loginTokenRes.setResult(code);
                loginTokenRes.setErr_msg(ResultCodeMsgEnum.getResultMsg(code));
                return loginTokenRes;
            }
        } catch (Exception e) {
            log.error("获取登陆授权失败，系统异常", e);
            String code = ResultCodeEnum.getCode(CodeTypeConstant.OTHER_ERROR);
            loginTokenRes.setResult(code);
            loginTokenRes.setErr_msg(ResultCodeMsgEnum.getResultMsg(code));
            return loginTokenRes;
        }
    }

    /**
     * 授权登录接口
     *
     * @param loginModel
     * @return
     */
    @Override
    public String requestUrl(LoginModel loginModel, HttpServletRequest request, HttpServletResponse response) {

        //封装验证参数
        baseRequest.setSign(loginModel.getSign());
        baseRequest.setSerial_num(loginModel.getSerial_num());
        baseRequest.setC_code(loginModel.getC_code());
        baseRequest.setT_code(loginModel.getT_code());
        String source = loginModel.getSource();
        String targetUrl = loginModel.getBid_url();
        boolean flag = true;
        Users users = null;
        do {
            //验签
            if (!SignUtil.checkSign(baseRequest, key, initVector)) {
                flag = false;
                break;
            }
            //解密参数
            String mobile = AES.decrypt(key, initVector, loginModel.getMobile());
            String userId = AES.decrypt(key, initVector, loginModel.getPlatform_uid());
            String loginToken = AES.decrypt(key, initVector, loginModel.getLogin_token());
            String starFireUserId = AES.decrypt(key, initVector, loginModel.getUser_id());
            //查询用户
            users = userService.findById(Long.valueOf(userId));
            //检查用户
            if (ObjectUtils.isEmpty(users)
                    || StringUtils.isEmpty(users.getStarFireRegisterToken())
                    || StringUtils.isEmpty(users.getStarFireUserId())
                    || !users.getStarFireRegisterToken().equals(loginToken)
                    || !users.getStarFireUserId().equals(starFireUserId)) {
                flag = false;
                break;
            }
            log.info("授权登录成功,打印用户信息:" + GSON.toJson(users));
            break;
        } while (false);
        //验证成功
        if (flag) {
            try {
                String redisTokenStr = redisHelper.get("JWT_TOKEN_" + users.getId(), null);
                String bidUrl = loginModel.getBid_url();
                if (StringUtils.isEmpty(redisTokenStr)) {
                    redisTokenStr = jwtTokenHelper.generateToken(users, Integer.valueOf(source));
                    response.addHeader(tokenHeader, String.format("%s %s", prefix, redisTokenStr));
                    users.setPlatform(3);
                    if (StringUtils.isEmpty(users.getPushId())) {   // 产生一次永久保存
                        users.setPushId(UUID.randomUUID().toString().replace("-", ""));  // 设置唯一标识
                    }
                    users.setIp(IpHelper.getIpAddress(request)); // 设置ip
                    userService.save(users);   // 记录登录信息
                    // 触发登录队列
                    MqConfig mqConfig = new MqConfig();
                    mqConfig.setTag(MqTagEnum.LOGIN);
                    mqConfig.setQueue(MqQueueEnum.RABBITMQ_USER_ACTIVE);
                    mqConfig.setSendTime(DateHelper.addSeconds(new Date(), 10));
                    ImmutableMap<String, String> body = ImmutableMap.of(MqConfig.MSG_USER_ID, users.getId().toString());
                    mqConfig.setMsg(body);
                    mqHelper.convertAndSend(mqConfig);
                }
                String address = source.equals("1") ? pcDomain : h5Domain;
                targetUrl = address + bidUrl + "?token=" + redisTokenStr;
            } catch (Exception e) {
                log.info("授权登录 获取token失败", e);
            }
        }
        return targetUrl;
    }

    /**
     * 用户登陆绑定结果通知星火
     *
     * @param userLoginBind
     */
    private void bindNotify(UserLoginBind userLoginBind) {
        try {
            log.info("===========进入用户绑定通知星火=============");
            log.info("打印用户登录结果通知星火请求参数:", GSON.toJson(userLoginBind));
            Map<String, String> paramMap = GSON.fromJson(GSON.toJson(userLoginBind),
                    new TypeToken<Map<String, String>>() {
                    }.getType());
            String resultStr = OKHttpHelper.postForm(notifyUrl, paramMap, null);
            log.info("打印通知火星返回结果:" + resultStr);
        } catch (Exception e) {
            log.info("用户登录结果通知星火失败", e);
        }
    }


    @Autowired
    private BorrowCollectionService borrowCollectionService;

    /**
     * 账户信息查询
     *
     * @param userAccount
     * @return
     */
    @Override
    public UserAccountRes userAccount(UserAccount userAccount) {
        //封装验签参数
        baseRequest.setSign(userAccount.getSign());
        baseRequest.setSerial_num(userAccount.getSerial_num());
        baseRequest.setC_code(userAccount.getC_code());
        baseRequest.setT_code(userAccount.getT_code());
        //封装返回参数
        UserAccountRes userAccountRes = new UserAccountRes();
        userAccountRes.setSerial_num(userAccount.getSerial_num());
        //验签
        if (!SignUtil.checkSign(baseRequest, key, initVector)) {
            String code = ResultCodeEnum.getCode(CodeTypeConstant.CHECK_SIGN_NO_PASS);
            userAccountRes.setErr_msg(ResultCodeMsgEnum.getResultMsg(code));
            return userAccountRes;
        }
        try {

            String userIdStr = userAccount.getPlatform_uid();
            List<Long> userIds;
            if (StringUtils.isEmpty(userIdStr)) {
                Specification<Users> usersSpecification = Specifications.<Users>and()
                        .ne("starFireUserId", null)
                        .ne("starFireRegisterToken",null)
                        .eq("isLock", false)
                        .build();
                List<Users> usersList = userService.findList(usersSpecification);
                userIds = usersList.stream()
                        .map(p -> p.getId())
                        .collect(Collectors.toList());
            } else {
                String platFormUid = AES.decrypt(key, initVector, userIdStr);
                userIds = Lists.newArrayList(platFormUid.split(",")).stream()
                        .map(p -> Long.parseLong(p.trim()))
                        .collect(Collectors.toList());
            }
            //用户集合
            List<Users> usersList = userService.findByIdIn(userIds);
            Map<Long, Users> usersMap = usersList.stream()
                    .collect(Collectors.toMap(Users::getId,
                            Function.identity()));

            //userCache
            List<UserCache> userCaches = userCacheService.findByUserIds(userIds);
            Map<Long, UserCache> userCacheMap = userCaches.stream()
                    .collect(Collectors.toMap(UserCache::getUserId,
                            Function.identity()));
            //用户资金集合
            List<Asset> assets = assetService.findByUserIds(userIds);
            Integer size = assets.size();
            List<UserAccountRes.Records> records = new ArrayList<>(size);
            Date nowDate = new Date();
            assets.forEach(asset -> {
                UserAccountRes.Records record = userAccountRes.new Records();
                Long userId = asset.getUserId();
                Users users = usersMap.get(userId);
                UserCache userCache = userCacheMap.get(userId);
                record.setMobile(users.getPhone());
                record.setPlatform_uid(userId);
                List<UserAccountRes.AccountRecords> accountRecords = new ArrayList<>(1);
                UserAccountRes.AccountRecords accountRecord = userAccountRes.new AccountRecords();
                Long noUseMoney = asset.getNoUseMoney();
                Long useMoney = asset.getUseMoney();
                Long collection = asset.getCollection();
                Long payment = asset.getPayment();
                //账户总额
                accountRecord.setAssetsAmount(StringHelper.formatDouble((noUseMoney + useMoney + collection - payment) / 100d, false));
                //账户余额
                accountRecord.setBalanceAmount(StringHelper.formatDouble((useMoney + noUseMoney) / 100D, false));
                //冻结金额
                accountRecord.setFrozenCapital(StringHelper.formatDouble(noUseMoney / 100D, false));
                //可用余额
                accountRecord.setAvailableBanlance(StringHelper.formatDouble(useMoney / 100d, false));
                //待收收益
                accountRecord.setUncollectedInterest(StringHelper.formatDouble(collection / 100D, false));
                //已收收益
                accountRecord.setProfitAmount(StringHelper.formatDouble(userCache.getIncomeTotal() / 100D, false));
                //今日收益
                Specification<BorrowCollection> specification = Specifications.<BorrowCollection>and()
                        .eq("userId", userId)
                        .eq("status", BorrowCollectionContants.STATUS_YES)
                        .between("collectionAtYes",
                                new Range<>(DateHelper.beginOfDate(nowDate),
                                        DateHelper.endOfDate(nowDate)))
                        .build();
                List<BorrowCollection> borrowCollections = borrowCollectionService.findList(specification);
                if (!CollectionUtils.isEmpty(borrowCollections)) {
                    long sum = borrowCollections.stream()
                            .mapToLong(b -> b.getCollectionMoneyYes())
                            .sum();
                    accountRecord.setTodayProfitAmount(StringHelper.formatDouble(sum, 100, false));
                }
                //投资总额
                Specification<Tender> tenderSpecification = Specifications.<Tender>and()
                        .eq("userId", userId)
                        .eq("status", TenderConstans.SUCCESS)
                        .build();
                List<Tender> tenders = tenderService.findList(tenderSpecification);
                if (!CollectionUtils.isEmpty(tenders)) {
                    //投资总额
                    Long sumValidMoney = tenders.stream().mapToLong(p -> p.getValidMoney()).sum();
                    accountRecord.setBidAmount(StringHelper.formatMon(sumValidMoney / 100d));
                }
                accountRecord.setDate(DateHelper.dateToString(asset.getUpdatedAt(), DateHelper.DATE_FORMAT_YMD));
                accountRecords.add(accountRecord);
                record.setAccountRecords(accountRecords);
                records.add(record);
            });
            userAccountRes.setRecords(records);
            userAccountRes.setTotalCount(size);
            userAccountRes.setResult(ResultCodeEnum.getCode(CodeTypeConstant.SUCCESS));
            return userAccountRes;
        } catch (Exception e) {
            String code = ResultCodeEnum.getCode(CodeTypeConstant.OTHER_ERROR);
            userAccountRes.setErr_msg(ResultCodeMsgEnum.getResultMsg(code));
            return userAccountRes;
        }
    }
}
