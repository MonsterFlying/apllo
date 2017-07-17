package com.gofobao.framework.repayment.vo.response;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Created by Zeke on 2017/7/17.
 */
@Data
public class VoBuildThirdRepayResp {
    /* 存管还款项列表 */
    private List<Map<String, Object>> thirdRepayList;
}
