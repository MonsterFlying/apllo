package com.gofobao.framework.borrow.biz;

import com.gofobao.framework.borrow.vo.request.VoAddNetWorthBorrow;
import com.gofobao.framework.borrow.vo.request.VoCancelBorrow;
import com.gofobao.framework.core.vo.VoBaseResp;
import org.springframework.http.ResponseEntity;

/**
 * Created by Zeke on 2017/5/26.
 */
public interface BorrowBiz {
    ResponseEntity<VoBaseResp> addNetWorth(VoAddNetWorthBorrow voAddNetWorthBorrow);

    ResponseEntity<VoBaseResp> cancelBorrow(VoCancelBorrow voCancelBorrow);
}
