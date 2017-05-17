package com.gofobao.framework.core.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Created by Max on 17/5/17.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VoBaseResp implements Serializable {
    private long state ;
    public static final long OK = 0;
    public static final long ERROR = 1;
}
