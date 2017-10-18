package com.gofobao.framework.member.service.impl;

import com.gofobao.framework.member.entity.Branch;
import com.gofobao.framework.member.entity.Users;
import com.gofobao.framework.member.repository.BranchRepository;
import com.gofobao.framework.member.repository.UsersRepository;
import com.gofobao.framework.member.service.BranchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.List;

/**
 * Created by master on 2017/10/17.
 */

@Component
public class BranchServiceImpl implements BranchService {

    @Autowired
    private BranchRepository branchRepository;

    @Autowired
    private UsersRepository usersRepository;

    @Override
    public List<Branch> list(Branch branch) {

        Example<Branch> ex = Example.of(branch);
        return branchRepository.findAll(ex);
    }

    @Override
    public Boolean save(Users users) {
        try {
            return ObjectUtils.isEmpty(usersRepository.save(users)) ? false : true;
        } catch (Exception e) {
            return false;
        }
    }
}
