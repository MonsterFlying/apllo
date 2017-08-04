package com.gofobao.framework.windmill.borrow.vo.request;

import lombok.Data;

/**
 * Created by admin on 2017/8/3.
 */
@Data
public class BySomeDayReq {

    private String invest_date;

    private Integer limit;

    private Integer page;

}
