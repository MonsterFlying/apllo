package com.gofobao.framework.lend.biz;

import com.gofobao.framework.common.page.Page;
import com.gofobao.framework.lend.vo.response.VoViewLendListWarpRes;
import org.springframework.http.ResponseEntity;

/**
 * Created by admin on 2017/6/6.
 */
public interface LendBiz {

    ResponseEntity<VoViewLendListWarpRes>list(Page page);
}
