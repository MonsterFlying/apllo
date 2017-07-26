package com.gofobao.framework.api.model.card_unbind;

import com.gofobao.framework.api.request.JixinBaseRequest;
import lombok.Data;

/**
 * Created by admin on 2017/5/17.
 */
@Data
public class CardUnbindRequest extends JixinBaseRequest{

    /**
     * 电子账号
     */
    private String  accountId;

    /**
     *证件类型
     */
    private String idType;

    /**
     *证件号码
     */
    private String idNo;
    /**
     *姓名
     */
    private String name;
    /**
     *手机号
     */
    private String mobile;

    /**
     * 银行卡
     */
    private String cardNo;

    /**
     * 请求方保留
     */
    private String acqRes ;


}
