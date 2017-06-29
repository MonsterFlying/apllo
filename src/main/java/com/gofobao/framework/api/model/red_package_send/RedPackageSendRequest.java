package com.gofobao.framework.api.model.red_package_send;

import com.gofobao.framework.api.request.JixinBaseRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by admin on 2017/6/28.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RedPackageSendRequest extends JixinBaseRequest  {

    /**
     * 红包账号
     */
    private String accountId;

    /**
     * 红包金额
     */
    private String txAmount;

    /**
     * 接收方账号
     */
    private String forAccountId;
    /**
     *  1-使用
     *  0-不使用
     */
    private String desLineFlag;
    /**
     * 交易描述
     */
    private String desLine;

    /**
     * 选填
     */
    private String acpRes;


}
