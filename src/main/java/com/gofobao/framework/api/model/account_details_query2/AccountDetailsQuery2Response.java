package com.gofobao.framework.api.model.account_details_query2;

import com.gofobao.framework.api.repsonse.JixinBaseResponse;
import lombok.Data;

/**
 * Created by Zeke on 2017/5/16.
 */
@Data
public class AccountDetailsQuery2Response extends JixinBaseResponse {
    private String accountId ;
    private String startDate ;
    private String endDate ;
    private String type ;
    private String name ;
    private String subPacks ;
}
