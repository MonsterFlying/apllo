package com.gofobao.framework.core.vo;

import com.gofobao.framework.helper.DateHelper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Max on 17/5/17.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VoBaseResp implements Serializable {
    private long state ;
    private String msg ;
    private String time ;
    public static final long OK = 0;
    public static final long ERROR = 1;


    public static VoBaseResp ok(String msg){
        return new VoBaseResp(OK, msg, DateHelper.dateToString(new Date()));
    }


    public static VoBaseResp error(long state, String msg){
        return new VoBaseResp(state, msg, DateHelper.dateToString(new Date()));
    }
}
