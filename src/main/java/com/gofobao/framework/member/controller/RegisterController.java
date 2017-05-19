package com.gofobao.framework.member.controller;

import com.gofobao.framework.api.contants.AcctUseContant;
import com.gofobao.framework.api.contants.ChannelContant;
import com.gofobao.framework.api.contants.IdTypeContant;
import com.gofobao.framework.api.helper.JixinManager;
import com.gofobao.framework.api.helper.JixinTxCodeEnum;
import com.gofobao.framework.api.model.openusers.AccountOpenRequest;
import com.gofobao.framework.api.model.openusers.AccountOpenResponse;
import com.gofobao.framework.member.service.UserService;
import com.gofobao.framework.member.vo.request.VoRegisterReq;
import com.gofobao.framework.member.vo.response.VoRegisterCallResp;
import com.gofobao.framework.member.vo.response.VoRegisterResp;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by Zeke on 2017/5/18.
 */
@RestController
@RequestMapping("/pub/user/")
@Slf4j
public class RegisterController {

    @Autowired
    private UserService userService;

    @Value("${gofobao.javaDomain}")
    private String javaDomain ;

    @Autowired
    JixinManager jixinManager ;

    /**
     * 注册用户回调
     * @param request
     * @return
     */
    @RequestMapping(value = "/registerCallBack")
    public void registerCallBack(HttpServletRequest request, HttpServletResponse response){
            response.setCharacterEncoding("UTF-8");
            TypeToken<AccountOpenResponse> accountOpenResponseTypeToken = new TypeToken<AccountOpenResponse>(){};
            AccountOpenResponse accountOpenResponse = jixinManager.callback(request,response, accountOpenResponseTypeToken) ;

    }

    /**
     * 注册用户
     * @param response
     * @return
     */
    @GetMapping(value = "/register")
    public void register(HttpServletResponse response){
        AccountOpenRequest request = new AccountOpenRequest() ;
        request.setChannel(ChannelContant.HTML);
        request.setIdType(IdTypeContant.ID_CARD);
        request.setIdNo("310114198407240819");
        request.setName("卜唯渊");
        request.setMobile("18964826795");
        request.setCardNo("6226628812120004");
        request.setAcctUse(AcctUseContant.GENERAL_ACCOUNT);
        request.setNotifyUrl(String.format("%s%s", javaDomain, "/pub/user/registerCallBack"));
        request.setRetUrl(javaDomain);
        request.setEmail("");
        request.setUserIP("");
        request.setAcqRes("1");
        String html = jixinManager.getHtml(JixinTxCodeEnum.OPEN_ACCOUNT, request);
        log.info(html) ;
        response.setContentType("text/html; charset=UTF-8");
        try(PrintWriter writer = response.getWriter()){
            writer.write(html);
        }catch (Exception e){
            log.error("请求异常", e);
        }
    }
}
