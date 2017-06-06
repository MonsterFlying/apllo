package com.gofobao.framework.common.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

/**
 * 响应状态类
 * Created by Max on 17/2/14.
 */
@ApiModel(value = "RespMsg", description = "信息响应")
public class RespMsg<T> implements Serializable {
    /**
     * 状态类
     */
    @ApiModelProperty(name = "status", value = "状态类")
    private AbstractRespMsgState status;

    @ApiModelProperty(name = "seq", value = "当前为第几次提交")
    private int seq = 0;
    /**
     * 消息体
     */
    @ApiModelProperty(name = "body", value = "消息体")
    private T body;


    public int getSeq() {
        return seq;
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }

    public AbstractRespMsgState getStatus() {
        return status;
    }

    public void setStatus(AbstractRespMsgState status) {
        this.status = status;
    }

    public T getBody() {
        return body;
    }

    public void setBody(T body) {
        this.body = body;
    }
}
