package com.gofobao.framework.tender.vo.response;

import com.gofobao.framework.borrow.vo.response.VoBorrowTenderUserRes;
import com.gofobao.framework.core.vo.VoBaseResp;
import lombok.Data;

import java.util.Collections;
import java.util.List;

/**
 * Created by admin on 2017/6/6.
 */
@Data
public class VoBorrowTenderUserWarpListRes extends VoBaseResp {

    private List<VoBorrowTenderUserRes> voBorrowTenderUser=Collections.EMPTY_LIST;
}
