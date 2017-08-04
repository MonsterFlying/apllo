package com.gofobao.framework.windmill.user.biz.impl;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.common.rabbitmq.MqConfig;
import com.gofobao.framework.common.rabbitmq.MqHelper;
import com.gofobao.framework.common.rabbitmq.MqQueueEnum;
import com.gofobao.framework.common.rabbitmq.MqTagEnum;
import com.gofobao.framework.core.helper.PasswordHelper;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.*;
import com.gofobao.framework.member.entity.Users;
import com.gofobao.framework.member.enums.RegisterSourceEnum;
import com.gofobao.framework.member.service.UserService;
import com.gofobao.framework.member.vo.response.VoBasicUserInfoResp;
import com.gofobao.framework.security.helper.JwtTokenHelper;
import com.gofobao.framework.security.vo.VoLoginReq;
import com.gofobao.framework.windmill.user.biz.WindmillUserBiz;
import com.gofobao.framework.windmill.user.constant.UserRegisterConstant;
import com.gofobao.framework.windmill.user.vo.request.BindLoginReq;
import com.gofobao.framework.windmill.user.vo.request.UserRegisterReq;
import com.gofobao.framework.windmill.user.vo.respones.UserRegisterRes;
import com.gofobao.framework.windmill.util.PassWordCreate;
import com.gofobao.framework.windmill.util.StrToJsonStrUtil;
import com.gofobao.framework.windmill.util.WrbCoopDESUtil;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import java.net.URLEncoder;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.netty.handler.codec.http.multipart.DiskFileUpload.prefix;
import static java.net.URLDecoder.decode;

/**
 * Created by admin on 2017/7/31.
 */
@Slf4j
@Service
public class WindmillUserBizImpl implements WindmillUserBiz {

    @Autowired
    private UserService userService;

    @Autowired
    private com.gofobao.framework.member.biz.UserBiz userBiz;

    @Autowired
    private MqHelper mqHelper;

    //加密key
    @Value("${windmill.des-key}")
    private static String desKey;

    //风车理财请求地址
    @Value("${windmill.request-address}")
    private String address;
    //平台简介
    @Value("${windmill.short-name}")
    private String shortName;

    @Value("${windmill.from}")
    private String from;

    @Value("${gofobao.h5Domain}")
    private String h5Domain;

    @Value("${jwt.header}")
    private String tokenHeader;

    private static final Gson GSON = new Gson();

    @Autowired
    private JwtTokenHelper jwtTokenHelper;

    @Value("${windmill.local-des-key}")
    private String localDesKey;


    @Transactional
    @Override
    public UserRegisterRes register(HttpServletRequest request) {
        String param = request.getParameter("param");
        UserRegisterRes registerRes = new UserRegisterRes();
        UserRegisterReq registerReq = new UserRegisterReq();
        log.info("============================================");
        log.info("===============风车理财用户注册===============");
        log.info("============================================");
        String decryptStr = "";
        try {
            decryptStr = StrToJsonStrUtil.commonDecryptStr(param);
            log.info("解密参数成功param:" + decryptStr);
        } catch (Exception e) {
            log.info("=====解密参数失败:param:" + param);
        }
        do {
            try {
                registerReq = GSON.fromJson(decryptStr, new TypeToken<UserRegisterReq>() {
                }.getType());
            } catch (Exception e) {
                registerRes.setRetcode(UserRegisterConstant.ORTHER_ERROR);
                registerRes.setRetmsg("json转对象异常");
                break;
            }
            // 1.手机号验证
            boolean notPhoneState = userService.notExistsByPhone(registerReq.getMobile());
            if (!notPhoneState) {//该手机号已存在
                //
                Users users = userService.findByAccount(registerReq.getMobile());
                //当前用户是否有关联风车理财
                if (!StringUtils.isEmpty(users.getWindmillId())) {
                    //该手机号存在的用户的 保存的风车理财id的 和请求的id是否相同
                    if (users.getWindmillId().equals(registerReq.getWrb_user_id())) { //相同
                        registerRes.setRetcode(UserRegisterConstant.SUCCESS); //返回成功
                        registerRes.setRetmsg("注册成功");
                        registerRes.setPf_user_name(StringUtils.isEmpty(users.getUsername()) ? users.getPhone().toString() : users.getUsername());
                        try {
                            registerRes.setPf_user_id(WrbCoopDESUtil.desEncrypt(localDesKey, users.getId().toString()));
                        } catch (Exception e) {

                        }
                        log.info("风车理财用户注册成功---->重入");
                        break;

                    } else {
                        registerRes.setRetcode(UserRegisterConstant.ORTHER_ERROR);
                        registerRes.setRetmsg("平台风车ID和传递的风车ID,不一致！");
                        log.info("风车理财用户注册:注册失败---->平台风车ID和传递的风车ID,不一致！");
                        break;
                    }

                } else { ////该手机号已存在
                    registerRes.setRetcode(UserRegisterConstant.MOBLIE_REP);
                    registerRes.setRetmsg("该手机号已已存在");
                    log.info("风车理财用户注册:注册失败---->该手机号已已存在");
                    break;
                }
            }
            //身份证是否存在
            boolean existsByIdCard = userService.notExistsByIdCard(registerReq.getId_no());
            if (!existsByIdCard) {
                registerRes.setRetcode(UserRegisterConstant.IDCARD_REP);
                registerRes.setRetmsg("身份证已存在");
                log.info("风车理财用户注册:注册失败---->身份证已存在");
                break;
            }
            try {
                // 处理注册来源
                Integer channel = RegisterSourceEnum.getIndex("7");
                Date now = new Date();
                // 插入数据
                Users users = new Users();
                users.setWindmillId(registerReq.getWrb_user_id());
                users.setEmail(StringUtils.isEmpty(registerReq.getEmail()) ? null : registerReq.getEmail());
                users.setUsername(registerReq.getPf_user_name());
                users.setPhone(registerReq.getMobile());
                users.setCardId(registerReq.getId_no());
                PassWordCreate pwc = new PassWordCreate();
                String password = pwc.createPassWord(8);
                users.setPassword(PasswordHelper.encodingPassword(password)); // 设置密码
                users.setPayPassword("");
                users.setRealname("");
                users.setRealname(registerReq.getTrue_name());
                users.setType("");
                users.setBranch(0);
                users.setSource(channel);
                users.setInviteCode(GenerateInviteCodeHelper.getRandomCode()); // 生成用户邀请码
                users.setParentId(1);
                users.setParentAward(0);
                users.setCreatedAt(now);
                users.setUpdatedAt(now);
                users = userService.save(users);
                //保存是否成功
                if ((ObjectUtils.isEmpty(users)) ||
                        (ObjectUtils.isEmpty(users.getId())) ||
                        !userBiz.registerExtend(users.getId())) {
                    registerRes.setRetcode(UserRegisterConstant.ORTHER_ERROR);
                    registerRes.setRetmsg("平台添加用户信息异常");
                    log.info("风车理财用户注册：注册失败---->平台添加用户信息异常");
                    break;
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
                    log.info(String.format("风车理财用户注册 发送到mq body消息：%s", GSON.toJson(body)));
                    mqState = mqHelper.convertAndSend(mqConfig);
                } catch (Throwable e) {
                    log.error("风车理财用户注册:发送到mq失败", e);
                    mqState = false;
                }
                if (!mqState) {
                    registerRes.setRetcode(UserRegisterConstant.ORTHER_ERROR);
                    registerRes.setRetmsg("风车理财用户注册:平台注册发送MQ失败");
                    break;
                }

                //发送风车理财用户：注册成功的短信
                MqConfig smsConfig = new MqConfig();
                smsConfig.setQueue(MqQueueEnum.RABBITMQ_SMS);
                smsConfig.setTag(MqTagEnum.SMS_WINDMILL_USER_REGISTER);
                ImmutableMap<String, String> smsBoby = ImmutableMap
                        .of(MqConfig.PHONE, registerReq.getMobile(), MqConfig.IP, request.getRemoteAddr(), MqConfig.PASSWORD, password);
                smsConfig.setMsg(smsBoby);
                mqHelper.convertAndSend(smsConfig);

                registerRes.setPf_user_id(WrbCoopDESUtil.desEncrypt(localDesKey, users.getId().toString()));
                registerRes.setRetmsg("风车理财用户注册:注册成功");
                registerRes.setRetcode(UserRegisterConstant.SUCCESS);
                registerRes.setPf_user_name(StringUtils.isEmpty(users.getUsername()) ? users.getPhone() : users.getUsername());
                log.info("风车理财：用户注册成功");

            } catch (Exception e) {
                log.info("风车理财用户注册:失败--->平台注册异常");
                registerRes.setRetmsg("风车理财用户注册:失败--->平台注册异常");
                registerRes.setRetcode(UserRegisterConstant.ORTHER_ERROR);
            }
        } while (false);
        return registerRes;
    }

    /**
     * 风车理财登录绑定
     *
     * @param request
     * @param response
     * @param bindLoginReq
     * @return
     */
    @Override
    public ResponseEntity<VoBasicUserInfoResp> bindLogin(HttpServletRequest request,
                                                         HttpServletResponse response,
                                                         BindLoginReq bindLoginReq) {
        log.info("===============进入风车理绑定用户登录===============");
        log.info("用户名：" + bindLoginReq.getUserName() + ",密文：" + bindLoginReq.getParams());
        log.info("==================================================");
        VoLoginReq voLoginReq = new VoLoginReq();
        voLoginReq.setAccount(bindLoginReq.getUserName());
        voLoginReq.setPassword(bindLoginReq.getPassword());
        ResponseEntity entity = userBiz.login(request, response, voLoginReq);


        //登陸成功
        if (entity.getStatusCode() == HttpStatus.OK) {
            log.info("================用户登录成功===============");
            VoBasicUserInfoResp userInfoResp = (VoBasicUserInfoResp) entity.getBody();
            try {
                String desDecryptStr = StrToJsonStrUtil.commonDecryptStr(bindLoginReq.getParams());
                log.info("解密后的参数：param:" + desDecryptStr);
                UserRegisterReq userRegisterReq = GSON.fromJson(desDecryptStr, new TypeToken<UserRegisterReq>() {
                }.getType());
                Users user = userService.findByAccount(bindLoginReq.getUserName());
                String userName = StringUtils.isEmpty(user.getUsername()) ? user.getPhone() : user.getUsername();
                //拼接参数
                String requestParam = "wrb_user_id=" + userRegisterReq.getWrb_user_id() +
                        "&pf_user_id=" + WrbCoopDESUtil.desEncrypt(localDesKey, user.getId().toString()) +
                        "&pf_user_name=" + userName +
                        "&reg_time=" + DateHelper.dateToString(user.getCreatedAt());
                //加密参数
                String requestEncryptParam = WrbCoopDESUtil.desEncrypt(desKey, requestParam);
                Map<String, String> requestMaps = Maps.newHashMap();
                requestMaps.put("from", "gfb");
                requestMaps.put("param", URLEncoder.encode(requestEncryptParam, "utf-8"));
                requestMaps.put("ts", System.currentTimeMillis() + "");
                log.info("请求风车理财 验证绑定是否成功，参数:" + JacksonHelper.obj2json(requestMaps));

                Map<String, String> headerMap = Maps.newHashMap();
                headerMap.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
                String result = OKHttpHelper.postForm(address + "/wrb/ps_bind.json", requestMaps, headerMap);
                try {
                    Map<String, Object> resultMap = JacksonHelper.json2map(result);
                    //绑定验证成功
                    if (Integer.valueOf(resultMap.get("retcode").toString()) == VoBaseResp.OK) {

                        log.info("风车理财绑定成功");
                        //风车理财绑定成功 将风车理财id 关联到本地用户
                        user.setWindmillId(userRegisterReq.getWrb_user_id());
                        //如果当前用户本地身份信息为空 将风车理财的用户身份保存到本地
                        user.setRealname(StringUtils.isEmpty(user.getRealname()) ? userRegisterReq.getTrue_name() : user.getRealname());
                        user.setCardId(StringUtils.isEmpty(user.getCardId()) ? userRegisterReq.getId_no() : user.getCardId());
                        user.setPhone(StringUtils.isEmpty(user.getPhone()) ? userRegisterReq.getMobile() : user.getPhone());
                        if (!StringUtils.isEmpty(userRegisterReq.getEmail())) {
                            user.setEmail(userRegisterReq.getEmail());
                        }
                        userService.save(user);


                        final String token = jwtTokenHelper.generateToken(user, 3);
                        response.addHeader(tokenHeader, String.format("%s %s", prefix, token));


                        // 触发登录队列
                        MqConfig mqConfig = new MqConfig();
                        mqConfig.setTag(MqTagEnum.LOGIN);
                        mqConfig.setQueue(MqQueueEnum.RABBITMQ_USER_ACTIVE);
                        mqConfig.setSendTime(DateHelper.addSeconds(new Date(), 10));
                        ImmutableMap<String, String> body = ImmutableMap.of(MqConfig.MSG_USER_ID, user.getId().toString());
                        mqConfig.setMsg(body);
                        mqHelper.convertAndSend(mqConfig);

                        VoBasicUserInfoResp voBasicUserInfoResp = VoBaseResp.ok("操作成功", VoBasicUserInfoResp.class);
                        //
                        if (StringUtils.isEmpty(userRegisterReq.getTarget_url())) {
                            voBasicUserInfoResp.setTarget_url(h5Domain + "?token=" + token);
                        } else {
                            voBasicUserInfoResp.setTarget_url(userRegisterReq.getTarget_url() + "?token=" + token);
                        }

                        return ResponseEntity.ok(voBasicUserInfoResp);
                    } else {
                        log.info("风车理财绑定失败原因:" + resultMap.get("retmsg").toString());
                        return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "风车理财验证绑定失败", VoBasicUserInfoResp.class));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "风车理财验证绑定失败", VoBasicUserInfoResp.class));
                }
            } catch (Exception e) {
                e.printStackTrace();
                return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "风车理财验证绑定失败", VoBasicUserInfoResp.class));
            }
        } else {
            log.info("================用户登录失败===============");
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "用户登录失败", VoBasicUserInfoResp.class));

        }

    }

    /**
     * 用户登录
     *
     * @param request
     * @return
     */
    @Override
    public String login(HttpServletRequest request, HttpServletResponse response) {
        String from = request.getParameter("sign");
        if (!from.equals(from)) {
            return "/load_error";
        }
        String getParamStr = request.getParameter("param");
        try {
            log.info("=============风车理财用户登录请求=============");
            String jsonStr = StrToJsonStrUtil.commonDecryptStr(getParamStr);
            Map<String, Object> resultMaps = GSON.fromJson(jsonStr, new TypeToken<Map<String, Object>>() {
            }.getType());

            String ticket = resultMaps.get("ticket").toString();
            //用户要访问的链接
            String targetUrl = resultMaps.get("target_url").toString();
            //加密请亲请求参数 装配参数
            String requestParam = "ticket=" + ticket;
            String requestParamStr = WrbCoopDESUtil.desEncrypt(desKey, requestParam);
            Map<String, String> checkTicketMap = Maps.newHashMap();
            checkTicketMap.put("param", URLEncoder.encode(requestParamStr, "utf-8"));
            checkTicketMap.put("from", shortName);
            checkTicketMap.put("ts", System.currentTimeMillis() + "");
            Map<String, String> headerMap = Maps.newHashMap();
            headerMap.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            //请求风车理财验证ticket
            String checkResult = OKHttpHelper.get(address + "/wrb/ps_check_ticket.json", checkTicketMap, headerMap);

            Map<String, Object> checkResultMap = GSON.fromJson(checkResult, new TypeToken<Map<String, Object>>() {
            }.getType());
            //验证成功 ticket有效
            if (new Double(checkResultMap.get("retcode").toString()).longValue() != VoBaseResp.OK) {
                log.info("风车理财检查ticket 返回失败");
                log.info("返回信息:" + checkResult);
                return "/load_error";
            }
            log.info("风车理财ticket检验成功");
            log.info("打印成功信息:" + checkResult);
            //解密用户id
            Long userId = Long.valueOf(WrbCoopDESUtil.desDecrypt(localDesKey, checkResultMap.get("pf_user_id").toString()));
            //风车理财用户id
            String wrbUserId = checkResultMap.get("wrb_user_id").toString();
            Specification<Users> specification = Specifications.<Users>and()
                    .eq("id", userId)
                    .eq("windmillId", wrbUserId)
                    .build();
            List<Users> users = userService.findList(specification);
            if (CollectionUtils.isEmpty(users)) {
                log.info("本地数据中没有找到风车理财返回的用户");
                return "/load_error";
            }
            Users tempUser = users.get(0);
            final String token = jwtTokenHelper.generateToken(tempUser, 3);
            response.addHeader(tokenHeader, String.format("%s %s", prefix, token));
            tempUser.setPlatform(3);
            if (StringUtils.isEmpty(tempUser.getPushId())) {   // 产生一次永久保存
                tempUser.setPushId(UUID.randomUUID().toString().replace("-", ""));  // 设置唯一标识
            }
            tempUser.setIp(IpHelper.getIpAddress(request)); // 设置ip
            userService.save(tempUser);   // 记录登录信息

            // 触发登录队列
            MqConfig mqConfig = new MqConfig();
            mqConfig.setTag(MqTagEnum.LOGIN);
            mqConfig.setQueue(MqQueueEnum.RABBITMQ_USER_ACTIVE);
            mqConfig.setSendTime(DateHelper.addSeconds(new Date(), 10));
            ImmutableMap<String, String> body = ImmutableMap.of(MqConfig.MSG_USER_ID, tempUser.getId().toString());
            mqConfig.setMsg(body);
            mqHelper.convertAndSend(mqConfig);

            log.info("token : " + token);
            if (StringUtils.isEmpty(targetUrl)) {
                targetUrl = h5Domain;
            } else {
                targetUrl = decode(targetUrl, "utf-8");
            }
            if (targetUrl.contains("?")) {
                return targetUrl + "&token=" + token;
            } else {
                return targetUrl + "?token=" + token;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "load_error";
        }
    }


}
