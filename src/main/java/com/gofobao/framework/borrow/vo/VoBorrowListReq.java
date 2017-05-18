package com.gofobao.framework.borrow.vo;

import com.gofobao.framework.common.page.Page;
import lombok.Data;

/**
 * Created by admin on 2017/5/17.
 */

@Data
public class VoBorrowListReq extends Page{

    private Integer type;

    public VoBorrowListReq(){}

}
