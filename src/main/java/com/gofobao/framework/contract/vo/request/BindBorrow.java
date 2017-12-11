package com.gofobao.framework.contract.vo.request;

import lombok.Data;

/**
 * @author master
 * @date 2017/11/15
 */
@Data
public class BindBorrow {

    private Long productId;

    private Integer templateId;

    private Integer tradeType;

    private String customField;
}
