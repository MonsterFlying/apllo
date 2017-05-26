package com.gofobao.framework.borrow.vo.request;
import lombok.Data;
import org.springframework.data.querydsl.QPageRequest;

/**
 * Created by admin on 2017/5/17.
 */

@Data
public class VoBorrowListReq extends QPageRequest {

    private Integer type;



}
