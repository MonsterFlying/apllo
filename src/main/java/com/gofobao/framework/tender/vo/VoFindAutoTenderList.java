package com.gofobao.framework.tender.vo;

import com.gofobao.framework.common.page.Page;
import lombok.Data;

/**
 * Created by Zeke on 2017/6/5.
 */
@Data
public class VoFindAutoTenderList extends Page{
    private String status;
    private String userId;
    private String notUserId;
    private String inRepayFashions;
    private String tender0;
    private String tender1;
    private String tender3;
    private String tender4;
    private String timelimitType;
    private String gtTimelimitLast;
    private String ltTimelimitFirst;
    private String ltAprFirst;
    private String gtAprLast;
    private Long borrowId;
}
