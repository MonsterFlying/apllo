package com.gofobao.framework.tender.vo.request;

import com.gofobao.framework.core.vo.VoBaseReq;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by Zeke on 2017/6/1.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VoCreateThirdTenderReq extends VoBaseReq {
    /**
     * 投标用户id
     */
    private Long userId;
    /**
     * 交易金额
     */
    private String txAmount;
    /**
     * 标的号
     */
    private String productId;
    /**
     * 是否冻结金额
     */
    private String frzFlag;
    /**
     * 请求方保留
     */
    private String acqRes;
}
