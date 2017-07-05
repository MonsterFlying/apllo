package com.gofobao.framework.helper;

import com.gofobao.framework.api.contants.ChannelContant;
import com.gofobao.framework.core.helper.RandomHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.Base64Utils;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by Administrator on 2017/6/20 0020.
 */
@Slf4j
@Component
public class ThirdAccountPasswordHelper {
    private static final String KEY = "gofobao@0701.wwww.COM";
    @Autowired
    RedisHelper redisHelper ;

    @Value("${gofobao.javaDomain}")
    String javaDomain ;

    private static final String PREFIX = "THIRD_PASSWORD_" ;

    private static final SymmetricalCoderHelper coder = new SymmetricalCoderHelper();
    /**
     *  找回密码链接
     * @param userId
     * @return
     */
    public String getThirdAcccountResetPasswordUrl(HttpServletRequest request, Long userId){
        try{
            String algorithm = SymmetricalCoderHelper.ALGORITHM_RC4;
            byte[] key = coder.initKey(algorithm) ;
            String random = RandomHelper.generateNumberCode(12);
            byte[] data = String.format("%s-%s", random, userId).getBytes("UTF-8");
            byte[] result = coder.encrypt(algorithm, data, key);
            String encode = Base64Utils.encodeToUrlSafeString(result);
            String url = String.format("%s%s%s/%s", javaDomain, "/pub/third/password/",  encode, ChannelContant.getchannel(request)) ;
            redisHelper.put(String.format("%s_%s", PREFIX, encode), Base64Utils.encodeToUrlSafeString(key), 10 * 60) ;
            log.info("忘记密码链接" + url);
            return url ;
        }catch (Exception e){
            throw  new RuntimeException("生成找回密码错误") ;
        }
    }


    /**
     *  初始化密码链接
     * @param userId
     * @return
     */
    public String getThirdAcccountInitPasswordUrl(HttpServletRequest request, Long userId){
        try{
            String algorithm = SymmetricalCoderHelper.ALGORITHM_RC4;
            byte[] key = coder.initKey(algorithm) ;
            String random = RandomHelper.generateNumberCode(12);
            byte[] data = String.format("%s-%s", random, userId).getBytes("UTF-8");
            byte[] result = coder.encrypt(algorithm, data, key);
            String encode = Base64Utils.encodeToUrlSafeString(result);
            String url = String.format("%s%s%s/%s", javaDomain, "/pub/initPassword/",  encode, ChannelContant.getchannel(request)) ;
            redisHelper.put(String.format("%s_%s", PREFIX, encode), Base64Utils.encodeToUrlSafeString(key), 10 * 60) ;
            log.info("初始化密码链接" + url);
            return url ;
        }catch (Exception e){
            throw  new RuntimeException("生成找回密码错误") ;
        }
    }

    /**
     * 从请求中获取uid
     * @param encode
     * @return
     */
    public Long getUserId(String encode){
        try{
            log.info("忘记密码加密" + encode);
            // 从redis 获取 判断是否非法请求
            String redisKey = String.format("%s_%s", PREFIX, encode);
            String keyStr = redisHelper.get(redisKey, null);
            if(StringUtils.isEmpty(keyStr)) {
                return null ;
            }
            redisHelper.remove(redisKey);

            String algorithm = SymmetricalCoderHelper.ALGORITHM_RC4;
            byte[] key = Base64Utils.decodeFromUrlSafeString(keyStr) ;
            byte[] data = Base64Utils.decodeFromUrlSafeString(encode);
            byte[] decrypt = coder.decrypt(algorithm, data, key);
            String decode = new String(decrypt, "UTF-8");
            if(StringUtils.isEmpty(decode)){
                return null ;
            }
            return Long.parseLong(decode.split("-")[1]);
        }catch (Exception e){
            throw  new RuntimeException("找回密码获取USERID异常") ;
        }
    }
}
