package com.gofobao.framework.core.ons.config;

import lombok.Data;

import java.util.Date;

/**
 * aliyun ons message warpper
 * Created by Max on 2017/5/17.
 */
@Data
public class OnsMessage {
    /** 一级分类 */
    private String tipic ;
    /** 二级分类*/
    private String tag ;
    /** 消息体 */
    private String body ;
    /** 延迟发送时间（秒） */
    private long delayTime ;
    /** 定时工作时间*/
    private Date startDoWork ;
}
