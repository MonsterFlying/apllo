package com.gofobao.framework.member.biz.impl;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.member.biz.BranchBiz;
import com.gofobao.framework.member.entity.Branch;
import com.gofobao.framework.member.entity.Users;
import com.gofobao.framework.member.service.BranchService;
import com.gofobao.framework.member.service.UserService;
import com.gofobao.framework.member.vo.response.BranchWarpRes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by master on 2017/10/17.
 */
@Service
public class BranchBizImpl implements BranchBiz {

    @Autowired
    private BranchService branchService;


    @Autowired
    private UserService userService;

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

    @Override
    public ResponseEntity<VoBaseResp> save(Long userId, Branch branch) {
        Users users = userService.findById(userId);
        if (!ObjectUtils.isEmpty(users) && users.getBranch() > 0) {
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "您已设置了分公司"));
        }
        List<Branch> branches = branchService.list(branch);
        if (CollectionUtils.isEmpty(branches)) {
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "非法请求"));
        } else if (branches.get(0).getType().intValue() != 3) {
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "请输入正确的分公司"));
        }
        users.setBranch(branch.getId());
        if (branchService.save(users))
            return ResponseEntity.ok(VoBaseResp.ok("设置成功"));
        else
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "设置失败"));
    }
}
