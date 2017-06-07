package com.gofobao.framework.common.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 响应状态类
 * Created by Max on 17/2/14.
 */
@ApiModel(value = "status", description = "响应消息头部")
public abstract class AbstractRespMsgState {
    /**
     * 状态码
     */
    @ApiModelProperty(name = "code", value = "状态码")
    private long code;

    /**
     * 状态码描述
     */
    @ApiModelProperty(name = "msg", value = "状态码描述")
    private String msg;

    /**
     * 时间戳
     */
    @ApiModelProperty(name = "st", value = "时间戳")
    private long st = System.currentTimeMillis();


    public AbstractRespMsgState(long code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public AbstractRespMsgState() {

    }


    public long getCode() {
        return code;
    }

    public void setCode(long code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public long getSt() {
        return st;
    }

    public void setSt(long st) {
        this.st = st;
    }
}
