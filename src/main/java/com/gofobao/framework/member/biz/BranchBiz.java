package com.gofobao.framework.member.biz;

import com.gofobao.framework.member.vo.response.BranchWarpRes;
import org.springframework.http.ResponseEntity;

/**
 * Created by master on 2017/10/17.
 */
public interface BranchBiz {

    ResponseEntity<BranchWarpRes> list();

}
