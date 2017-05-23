package com.gofobao.framework.integral.vo.response;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.DateHelper;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * Created by Zeke on 2017/5/22.
 */
@ApiModel
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VoListIntegralResp extends VoBaseResp{

    /**
     * 总积分
     */
    private Integer totalIntegral;

    /**
     * 有效积分
     */
    private Integer availableIntegral;

    /**
     * 已使用积分
     */
    private Integer invalidIntegral;

    /**
     * 兑换比例
     */
    private String takeRates;

    /**
     * 积分列表
     */
    private List<VoIntegral> voIntegralList;

}
