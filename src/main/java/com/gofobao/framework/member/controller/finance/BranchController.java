package com.gofobao.framework.member.controller.finance;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.member.biz.BranchBiz;
import com.gofobao.framework.member.entity.Branch;
import com.gofobao.framework.member.vo.response.BranchWarpRes;
import com.gofobao.framework.security.contants.SecurityContants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Created by master on 2017/10/17.
 */
@RestController
@RequestMapping
public class BranchController {

    @Autowired
    private BranchBiz branchBiz;


    @RequestMapping(path = "finance/branch/list", method = RequestMethod.GET)
    public ResponseEntity<BranchWarpRes> list(@RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        return branchBiz.list(userId);
    }

    @RequestMapping(path = "finance/branch/save", method = RequestMethod.POST)
    public ResponseEntity<VoBaseResp> save(@RequestAttribute(SecurityContants.USERID_KEY) Long userId,
                                           @RequestParam("branchId") Integer branchId) {
        Branch branch = new Branch();
        branch.setId(branchId);
        return branchBiz.save(userId, branch);
    }


}
