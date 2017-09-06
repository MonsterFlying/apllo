package com.gofobao.framework.collection.vo.response.web;

import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * Created by Zeke on 2017/7/11.
 */
@ApiModel("回款详情")
@Data
public class CollectionDetail {
    private Long borrowId;
    private String name;
    private String orderStr;
    private String principal;
    private String interest;
    private String earnings;
    private String collectionAt;
}
