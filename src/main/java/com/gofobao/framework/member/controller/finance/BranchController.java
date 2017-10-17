package com.gofobao.framework.member.controller.finance;

import com.gofobao.framework.member.biz.BranchBiz;
import com.gofobao.framework.member.vo.response.BranchWarpRes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by master on 2017/10/17.
 */
@RestController
@RequestMapping
public class BranchController {

    @Autowired
    private BranchBiz branchBiz;


    @RequestMapping(path = "/branch/list", method = RequestMethod.POST)
    public ResponseEntity<BranchWarpRes> list() {
        return branchBiz.list();
    }

}
