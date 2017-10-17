package com.gofobao.framework.api.model.corpration_query;

import com.gofobao.framework.api.repsonse.JixinBaseResponse;
import lombok.Data;

/**
 * 初始化密码响应
 * Created by Max on 17/5/23.
 */
@Data
public class CorprationQueryResponse extends JixinBaseResponse {
    /**
     * 电子账号
     */
    private String accountId ;

    /**
     * 证件类型
     */
    private String idType ;

    /**
     * 证件号码
     */
    private String idNo ;

    /**
     * 姓名
     */
    private String name ;

    /**
     * 手机号
     */
    private String mobile ;

    /**
     * 对公账号
     */
    private String caccount ;

    /**
     * 营业执照编号
     */
    private String busId ;

    /**
     * 税务登记号
     */
    private String taxId ;
}
