package com.gofobao.framework.starfire.statistics.vo.response;

import com.gofobao.framework.starfire.common.response.BaseResponse;
import lombok.Data;

import java.util.Collections;
import java.util.List;

/**
 * Created by master on 2017/9/29.
 */
@Data
public class StatisticsDataRes extends BaseResponse {

    private Integer totalCount = 1;

    private List<Records> records = Collections.EMPTY_LIST;

    @Data
    public class Records {

        private Integer lendCount;

        private Integer borrowCount;

        private String totalBidMoney;

        private String totalBackMoney;

        private String refdate;
    }
}
