package com.gofobao.framework.api.model.credit_details_query;

import com.gofobao.framework.api.request.JixinBaseRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by Zeke on 2017/5/24.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreditDetailsQueryRequest extends JixinBaseRequest{
    private String accountId;
    private String productId ;
    private String state ;
    private String startDate ;
    private String endDate ;
    private String pageNum ;
    private String pageSize ;
}
