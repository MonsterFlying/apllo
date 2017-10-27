package com.gofobao.framework.wheel.user.biz.impl;

import com.gofobao.framework.common.rabbitmq.MqConfig;
import com.gofobao.framework.common.rabbitmq.MqHelper;
import com.gofobao.framework.common.rabbitmq.MqQueueEnum;
import com.gofobao.framework.common.rabbitmq.MqTagEnum;
import com.gofobao.framework.core.helper.PasswordHelper;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.GenerateInviteCodeHelper;
import com.gofobao.framework.helper.OKHttpHelper;
import com.gofobao.framework.helper.RedisHelper;
import com.gofobao.framework.member.biz.UserBiz;
import com.gofobao.framework.member.entity.Users;
import com.gofobao.framework.member.service.UserService;
import com.gofobao.framework.security.helper.JwtTokenHelper;
import com.gofobao.framework.wheel.common.ResponseConstant;
import com.gofobao.framework.wheel.user.biz.WheelUserBiz;
import com.gofobao.framework.wheel.user.vo.repsonse.CheckTicketRes;
import com.gofobao.framework.wheel.user.vo.repsonse.RegisterRes;
import com.gofobao.framework.wheel.user.vo.request.AuthLoginReq;
import com.gofobao.framework.wheel.user.vo.request.CheckTicketReq;
import com.gofobao.framework.wheel.user.vo.request.RegisterReq;
import com.gofobao.framework.wheel.util.JEncryption;
import com.gofobao.framework.windmill.util.PassWordCreate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

import static io.netty.handler.codec.http.multipart.DiskFileUpload.prefix;

/**
 * Created by master on 2017/10/27.
 */
@Slf4j
@Service
public class WheelUserBizmpl implements WheelUserBiz {

    @Autowired
    private UserService userService;

    @Autowired
    private UserBiz userBiz;

    @Autowired
    private MqHelper mqHelper;

    @Autowired
    private Gson GSON;

    @Value("${wheel.domain}")
    private String domain;

    @Value("${wheel.check_ticket_url}")
    private String checkTicKetUrl;

    @Value("${wheel.short-name}")
    private String shortName;

    @Value("${wheel.secret-key}")
    private String secretKey;

    @Autowired
    private RedisHelper redisHelper;

    @Autowired
    private JwtTokenHelper jwtTokenHelper;

    @Value("${jwt.header}")
    private String tokenHeader;

    @Value("${gofobao.h5Domain}")
    private String h5Domain;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public RegisterRes register(RegisterReq register, HttpServletRequest request) {
        Users user = userService.findByAccount(register.getMobile());
        RegisterRes registerRes = new RegisterRes();
        if (ObjectUtils.isEmpty(user)) {
            registerRes.setRetcode(1);
            registerRes.setRetmsg("手机号已注册");
            return registerRes;
        }
        try {
            Date now = new Date();
            // 插入数据
            Users users = new Users();
            users.setWheelId(register.getCl_user_id());
            users.setBindWheelAt(now);
            users.setPhone(register.getMobile());
            PassWordCreate pwc = new PassWordCreate();
            String password = pwc.createPassWord(8);
            users.setPassword(PasswordHelper.encodingPassword(password)); // 设置密码
            users.setPayPassword("");
            users.setType("");
            users.setBranch(0);
            users.setSource(14);
            users.setInviteCode(GenerateInviteCodeHelper.getRandomCode()); // 生成用户邀请码
            users.setParentId(0L);
            users.setParentAward(0);
            users.setCreatedAt(now);
            users.setUpdatedAt(now);
            users = userService.save(users);
            //保存是否成功
            if ((ObjectUtils.isEmpty(users)) ||
                    (ObjectUtils.isEmpty(users.getId())) ||
                    !userBiz.registerExtend(users.getId())) {
                registerRes.setRetcode(3);
                registerRes.setRetmsg("平台添加用户信息异常");
                log.info("车轮理财用户注册：注册失败---->平台添加用户信息异常");
                throw new Exception();
            }
            // 4.触发注册事件
            MqConfig mqConfig = new MqConfig();
            mqConfig.setQueue(MqQueueEnum.RABBITMQ_USER_ACTIVE);
            mqConfig.setTag(MqTagEnum.USER_ACTIVE_REGISTER);
            ImmutableMap<String, String> body = ImmutableMap
                    .of(MqConfig.MSG_USER_ID,
                            users.getId().toString(),
                            MqConfig.MSG_TIME,
                            DateHelper.dateToString(now));
            mqConfig.setMsg(body);
            mqConfig.setSendTime(DateHelper.addSeconds(new Date(), 30));
            boolean mqState;
            try {
                log.info(String.format("车轮理财用户注册 发送到mq body消息：%s", GSON.toJson(body)));
                mqState = mqHelper.convertAndSend(mqConfig);
            } catch (Throwable e) {
                log.error("车轮理财用户注册:发送到mq失败", e);
                mqState = false;
            }
            if (!mqState) {
                registerRes.setRetcode(3);
                registerRes.setRetmsg("车轮理财用户注册:平台注册发送MQ处理失败");
                log.info("车轮理财用户注册:平台注册发送MQ失败");
                throw new Exception();
            }
            //发送风车理财用户：注册成功的短信
            MqConfig smsConfig = new MqConfig();
            smsConfig.setQueue(MqQueueEnum.RABBITMQ_SMS);
            smsConfig.setTag(MqTagEnum.SMS_WINDMILL_USER_REGISTER);
            ImmutableMap<String, String> smsBoby = ImmutableMap
                    .of(MqConfig.PHONE,
                            register.getMobile(),
                            MqConfig.IP,
                            request.getRemoteAddr(),
                            MqConfig.PASSWORD,
                            password);
            smsConfig.setMsg(smsBoby);
            mqHelper.convertAndSend(smsConfig);
            registerRes.setRetcode(0);
            registerRes.setPf_user_id(users.getId().toString());
            log.info("风车理财：用户注册成功");
            return registerRes;
        } catch (Exception e) {
            return registerRes;
        }
    }

    /**
     * @param authLogin
     * @return
     */
    @Override
    public String AuthLogin(AuthLoginReq authLogin, HttpServletResponse response) {
        //验证票据
        CheckTicketReq checkTicketReq = new CheckTicketReq();
        checkTicketReq.setTicket(authLogin.getTicket());
        checkTicketReq.setTs(System.currentTimeMillis());
        CheckTicketRes checkTicket = checkTicket(checkTicketReq);
        log.info("打印车轮返回信息:" + GSON.toJson(checkTicket));
        //网络错误
        if (checkTicket.getRetcode().equals(ResponseConstant.NET_ERROR)) {
            return "load_error";
        } else if (checkTicket.getRetcode().equals(ResponseConstant.FAIL)) {
            log.info("ticket验证失败");
            return "load_error";
        } else {
            Users users = userService.findById(Long.valueOf(checkTicket.getPf_user_id()));
            if (!ObjectUtils.isEmpty(users)
                    && users.getPhone().equals(checkTicket.getMobile())
                    && users.getWheelId().equals(checkTicket.getCl_user_id())) {
                log.info("非法请求");
                return "load_error";
            }
            try {
                String tokenStr = redisHelper.get("JTW_" + users.getId(), null);
                if (StringUtils.isEmpty(tokenStr)) {
                    tokenStr = jwtTokenHelper.generateToken(users, Integer.valueOf(3));
                    response.addHeader(tokenHeader, String.format("%s %s", prefix, tokenStr));
                    // 触发登录队列
                    MqConfig mqConfig = new MqConfig();
                    mqConfig.setTag(MqTagEnum.LOGIN);
                    mqConfig.setQueue(MqQueueEnum.RABBITMQ_USER_ACTIVE);
                    mqConfig.setSendTime(DateHelper.addSeconds(new Date(), 10));
                    ImmutableMap<String, String> body = ImmutableMap.of(MqConfig.MSG_USER_ID, users.getId().toString());
                    mqConfig.setMsg(body);
                    mqHelper.convertAndSend(mqConfig);
                }
                return h5Domain + authLogin.getTarget_url() + "?token=" + tokenStr;
            } catch (Exception e) {
                log.info("平台生成token异常", e);
                return "load_error";
            }
        }
    }


    /**
     * @param checkTicket
     * @return
     */
    @Override
    public CheckTicketRes checkTicket(CheckTicketReq checkTicket) {
        CheckTicketRes ticketRes = new CheckTicketRes();

        String paramStr = "ts=" + checkTicket.getTs() + "&from=" + checkTicket.getTicket();
        try {
            String encryptParamStr = new String(Base64.getEncoder()
                    .encode(JEncryption.encrypt(paramStr.getBytes(), secretKey)
                            .getBytes()),
                    "utf-8");
            Map<String, String> requestMap = Maps.newHashMap();
            requestMap.put("param", encryptParamStr);
            String resultStr = OKHttpHelper.postForm(domain + checkTicKetUrl, requestMap, null);
            if (StringUtils.isEmpty(resultStr)) {
                throw new Exception();
            }
            ticketRes = GSON.fromJson(resultStr, new TypeToken<CheckTicketRes>() {
            }.getType());
            return ticketRes;
        } catch (Exception e) {
            ticketRes.setRetcode(ResponseConstant.NET_ERROR);
            ticketRes.setRetmsg("请求验证票据异常");
            log.info("请求检查车轮ticket异常");
            return ticketRes;
        }
    }


}
