package com.gofobao.framework.borrow.vo.response;

import com.google.common.collect.Lists;
import lombok.Data;

import java.util.List;

/**
 * Created by admin on 2017/6/10.
 */
@Data
public class VoBorrowDescRes {

    private String borrowDesc;
    private List<UserAttachmentRes> userAttachmentRes= Lists.newArrayList();
}
