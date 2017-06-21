package com.gofobao.framework.api.contants;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by Zeke on 2017/5/16.
 */
public class ChannelContant {
    public static final String APP = "000001";
    public static final String HTML = "000002";
    public static final String WE_CHAT = "000003";
    public static final String COUNTER = "000004";

    public static String getchannel(HttpServletRequest request){
        String requestSource = request.getHeader("requestSource");
        if(requestSource.equals("0")){
            return WE_CHAT ;
        }else if(requestSource.equals("1")){
            return APP ;
        }else if(requestSource.equals("2")){
            return APP;
        }else{
            return HTML ;
        }

    }
}
