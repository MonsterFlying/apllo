package com.gofobao.framework.member.biz.impl;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.member.biz.BranchBiz;
import com.gofobao.framework.member.entity.Branch;
import com.gofobao.framework.member.service.BranchService;
import com.gofobao.framework.member.vo.response.BranchWarpRes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by master on 2017/10/17.
 */
@Service
public class BranchBizImpl implements BranchBiz {

    @Autowired
    private BranchService branchService;


    @Override
    public ResponseEntity<BranchWarpRes> list() {
        Branch branch = new Branch();
        branch.setType(3);
        List<Branch> branches = branchService.list(branch);
        BranchWarpRes branchWarpRes = VoBaseResp.ok("查询成功", BranchWarpRes.class);

        if (CollectionUtils.isEmpty(branches)) {
            return ResponseEntity.ok(branchWarpRes);
        }
        List<BranchWarpRes.VoBranch> voBranches = new ArrayList<>(branches.size());
        branches.forEach(p -> {
            BranchWarpRes.VoBranch voBranch = branchWarpRes.new VoBranch();
            voBranch.setBranchName(p.getName());
            voBranch.setId(p.getId());
            voBranches.add(voBranch);
        });
        branchWarpRes.setBranches(voBranches);
        return ResponseEntity.ok(branchWarpRes);
    }
}
