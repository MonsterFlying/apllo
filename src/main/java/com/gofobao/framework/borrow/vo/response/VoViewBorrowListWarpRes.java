package com.gofobao.framework.borrow.vo.response;

import com.gofobao.framework.core.vo.VoBaseResp;
import lombok.Data;

import java.util.Collections;
import java.util.List;

/**
 * Created by admin on 2017/6/6.
 */
@Data
public class VoViewBorrowListWarpRes extends VoBaseResp {
    private List<VoViewBorrowList>  voViewBorrowLists=Collections.EMPTY_LIST;
}
