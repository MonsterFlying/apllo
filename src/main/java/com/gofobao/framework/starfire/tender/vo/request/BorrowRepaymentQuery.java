package com.gofobao.framework.starfire.tender.vo.request;

import com.gofobao.framework.starfire.common.request.BaseRequest;
import lombok.Data;

/**
 * Created by master on 2017/10/11.
 */
@Data
public class BorrowRepaymentQuery extends BaseRequest {
    private String bid_id;
}
