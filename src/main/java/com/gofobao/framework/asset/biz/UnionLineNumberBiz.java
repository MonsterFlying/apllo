package com.gofobao.framework.asset.biz;

import com.gofobao.framework.asset.vo.request.VoUnionLineNoReq;
import com.gofobao.framework.asset.vo.response.pc.UnionLineNoWarpRes;
import org.springframework.http.ResponseEntity;

/**
 * Created by admin on 2017/8/21.
 */

public interface UnionLineNumberBiz {

        ResponseEntity<UnionLineNoWarpRes> list(VoUnionLineNoReq unionLineNoReq);
}
