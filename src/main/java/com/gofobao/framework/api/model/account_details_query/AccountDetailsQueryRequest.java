package com.gofobao.framework.api.model.account_details_query;

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
public class AccountDetailsQueryRequest extends JixinBaseRequest {
    private String accountId ;
    private String startDate ;
    private String endDate ;
    private String type ;
    private String tranType ;
    private String pageNum ;
    private String pageSize ;
}
