package com.gofobao.framework.asset.vo.response;

import com.gofobao.framework.core.vo.VoBaseResp;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/6/13 0013.
 */
@ApiModel
@Data
public class VoCashLogWrapResp extends VoBaseResp{
    private List<VoCashLogResp> data = new ArrayList<>() ;
}
