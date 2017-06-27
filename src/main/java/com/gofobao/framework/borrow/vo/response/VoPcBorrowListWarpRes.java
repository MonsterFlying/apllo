package com.gofobao.framework.borrow.vo.response;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.google.common.collect.Lists;
import lombok.Data;

import java.util.List;

/**
 * Created by admin on 2017/6/27.
 */
@Data
public class VoPcBorrowListWarpRes extends VoBaseResp {

        private List<VoPcBorrowList> lists= Lists.newArrayList();

}
