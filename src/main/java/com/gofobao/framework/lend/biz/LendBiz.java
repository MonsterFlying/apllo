package com.gofobao.framework.lend.biz;

import com.gofobao.framework.common.page.Page;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.lend.vo.request.VoCreateLend;
import com.gofobao.framework.lend.vo.request.VoEndLend;
import com.gofobao.framework.lend.vo.request.VoLend;
import com.gofobao.framework.lend.vo.response.VoViewLendListWarpRes;
import org.springframework.http.ResponseEntity;

/**
 * Created by admin on 2017/6/6.
 */
public interface LendBiz {

    ResponseEntity<VoViewLendListWarpRes> list(Page page);

    /**
     * 发布有草出借
     *
     * @param voCreateLend
     * @return
     */
    ResponseEntity<VoBaseResp> create(VoCreateLend voCreateLend) throws Exception;

    /**
     * 结束有草出借
     *
     * @param voEndLend
     * @return
     */
    ResponseEntity<VoBaseResp> end(VoEndLend voEndLend) throws Exception;

    /**
     * 有草出借摘草
     *
     * @param voLend
     * @return
     */
    ResponseEntity<VoBaseResp> lend(VoLend voLend) throws Exception;
}
