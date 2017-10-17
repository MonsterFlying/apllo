package com.gofobao.framework.member.service.impl;

import com.gofobao.framework.member.entity.Branch;
import com.gofobao.framework.member.repository.BranchRepository;
import com.gofobao.framework.member.service.BranchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by master on 2017/10/17.
 */

@Component
public class BranchServiceImpl implements BranchService {

    @Autowired
    private BranchRepository branchRepository;

    @Override
    public List<Branch> list(Branch branch) {
        Example<Branch> branchExample = Example.of(branch, null);
        return branchRepository.findAll(branchExample);
    }
}
