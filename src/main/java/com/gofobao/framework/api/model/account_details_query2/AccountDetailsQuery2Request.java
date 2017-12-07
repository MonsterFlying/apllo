package com.gofobao.framework.api.model.account_details_query2;

import com.gofobao.framework.api.request.JixinBaseRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by Zeke on 2017/5/16.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountDetailsQuery2Request extends JixinBaseRequest {
    private String accountId ;
    private String startDate ;
    private String endDate ;
    private String type ;
    private String tranType ;
    /**
     * 空：首次查询；
     1：翻页查询；
     其它：非法；
     */
    private String rtnInd;
    private String inpDate;
    private String inpTime;
    private String relDate;
    private String traceNo;
}
