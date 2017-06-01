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
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.GenerateInviteCodeHelper;
import com.gofobao.framework.integral.entity.Integral;
import com.gofobao.framework.integral.service.IntegralService;
import com.gofobao.framework.member.biz.UserBiz;
import com.gofobao.framework.member.entity.UserCache;
import com.gofobao.framework.member.entity.UserInfo;
import com.gofobao.framework.member.entity.Users;
import com.gofobao.framework.member.enums.RegisterSourceEnum;
import com.gofobao.framework.member.service.UserCacheService;
import com.gofobao.framework.member.service.UserInfoService;
import com.gofobao.framework.member.service.UserService;
import com.gofobao.framework.member.vo.request.VoRegisterReq;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

/**
 * Created by Zeke on 2017/5/19.
 */
@Service
@Slf4j
public class UserBizImpl implements UserBiz{
    static final Gson GSON = new Gson() ;

    @Autowired
    UserService userService ;

    @Autowired
    UserInfoService userInfoService ;

    @Autowired
    AssetService assetService ;

    @Autowired
    UserCacheService userCacheService ;

    @Autowired
    IntegralService integralService ;

    @Autowired
    CurrencyService currencyService ;

    @Autowired
    MqHelper mqHelper ;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<VoBaseResp> register(HttpServletRequest request, VoRegisterReq voRegisterReq) throws Exception{
        // 1.手机处理
        boolean notPhoneState = userService.notExistsByPhone(voRegisterReq.getPhone());
        if(!notPhoneState){
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "当前手机已经在平台注册！")) ;
        }

        // 2.用户名处理
        if(!StringUtils.isEmpty(voRegisterReq.getUserName())){

            boolean notUserName = userService.notExistsByUserName(voRegisterReq.getUserName()) ;
            if(!notUserName){
                return ResponseEntity
                        .badRequest()
                        .body(VoBaseResp.error(VoBaseResp.ERROR, "当前用户名已经在平台注册！")) ;
            }
        }

        Integer parentId = 0 ;
        if(!StringUtils.isEmpty(voRegisterReq.getInviteCode())){
            // 3.推荐人处理
            Users invitedUser = userService.findByInviteCode(voRegisterReq.getInviteCode()) ;
            if(ObjectUtils.isEmpty(invitedUser)){
                return ResponseEntity
                        .badRequest()
                        .body(VoBaseResp.error(VoBaseResp.ERROR, "无效的邀请码！")) ;
            }

            parentId = invitedUser.getId().intValue() ;
        }

        // 处理注册来源
        String source = voRegisterReq.getSource() ;
        Integer channel = RegisterSourceEnum.getIndex(source.toLowerCase()) ;
        Date now = new Date() ;
        // 插入数据
        Users users = new Users() ;
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

        users = userService.save(users) ;
        if((ObjectUtils.isEmpty(users)) || (ObjectUtils.isEmpty(users.getId()))){
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "服务器开小差了， 请稍候再试！")) ;
        }

        if(!registerExtend(users.getId())){
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "服务器开小差了， 请稍候再试！")) ;
        }

        // 4.触发注册事件
        MqConfig mqConfig = new MqConfig();
        mqConfig.setQueue(MqQueueEnum.RABBITMQ_USER_ACTIVE);
        mqConfig.setTag(MqTagEnum.USER_ACTIVE_REGISTER);
        ImmutableMap<String, String> body = ImmutableMap
                .of(MqConfig.MSG_USER_ID, users.getId().toString(), MqConfig.MSG_TIME, DateHelper.dateToString(now)) ;
        mqConfig.setMsg(body);
        boolean mqState;
        try {
            log.info(String.format("userBizImpl register send mq %s", GSON.toJson(body)));
            mqState = mqHelper.convertAndSend(mqConfig) ;
        }catch (Exception e){
            log.error("userBizImpl register send mq exception", e);
            throw new Exception(e) ;
        }

        if(!mqState){
            log.error("userBizImpl register send mq error");
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "服务器开小差了，请稍候再试！")) ;

        }
        return ResponseEntity.ok(VoBaseResp.ok("注册成功")) ;
    }

    /**
     * 注册后续操作
     * @param userId
     * @return
     */
    private boolean registerExtend(Long userId) throws Exception{
        Date now = new Date() ;
        UserInfo userInfo = new UserInfo() ;
        userInfo.setUserId(userId);
        userInfo.setUpdateAt(now);
        userInfo = userInfoService.save(userInfo) ;
        if((ObjectUtils.isEmpty(userInfo))){
            return false ;
        }

        Asset asset = new Asset() ;
        asset.setUserId(userId);
        asset = assetService.save(asset) ;
        if(ObjectUtils.isEmpty(asset)){
            return false ;
        }

        UserCache userCache = new UserCache() ;
        userCache.setUserId(userId);
        userCache = userCacheService.save(userCache) ;
        if(ObjectUtils.isEmpty(userCache)){
            return false ;
        }

        Integral integral = new Integral() ;
        integral.setUserId(userId);
        integral = integralService.save(integral) ;
        if(ObjectUtils.isEmpty(integral)){
            return false ;
        }

        Currency currency = new Currency() ;
        currency.setUserId(userId);
        currency =  currencyService.save(currency) ;
        if(ObjectUtils.isEmpty(currency)){
            return false ;
        }

        return true ;
    }
}
