package com.gofobao.framework.tender.vo.response;

import lombok.Data;

import java.util.Date;

/**
 * Created by Zeke on 2017/6/29.
 */
@Data
public class VoFindAutoTender {
    private Integer id;
    private Boolean status;
    private Integer userId;
    private Integer lowest;
    private Integer borrowTypes;
    private Integer repayFashions;
    private Integer tender0;
    private Integer tender1;
    private Integer tender3;
    private Integer  tender4;
    private Integer mode;
    private Integer tenderMoney;
    private Integer timelimitFirst;
    private Integer timelimitLast;
    private Integer timelimitType;
    private Integer aprFirst;
    private Integer aprLast;
    private Integer saveMoney;
    private Integer order;
    private Date autoAt;
    private Date createdAt;
    private Date updatedAt;
    private Integer useMoney;
    private Integer noUseMoney;
    private Integer virtualMoney;
    private Integer collection;
    private Integer payment;
}
