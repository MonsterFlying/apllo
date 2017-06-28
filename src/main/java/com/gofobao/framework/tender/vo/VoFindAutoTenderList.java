package com.gofobao.framework.tender.vo;

import com.gofobao.framework.common.page.Page;
import lombok.Data;

/**
 * Created by Zeke on 2017/6/5.
 */
@Data
public class VoFindAutoTenderList extends Page{
    private String status;
    private Long userId;
    private Long notUserId;
    private String inRepayFashions;
    private String timelimitType;
    private String gtTimelimitLast;
    private String ltTimelimitFirst;
    private Integer ltAprFirst;
    private Integer gtAprLast;
    private Long borrowId;
}
