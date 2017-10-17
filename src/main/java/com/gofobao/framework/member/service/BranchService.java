package com.gofobao.framework.member.service;

import com.gofobao.framework.member.entity.Branch;
import com.gofobao.framework.member.entity.Users;

import java.util.List;

/**
 * Created by master on 2017/10/17.
 */
public interface BranchService {

    /**
     *
     * @param branch
     * @return
     */
    List<Branch> list(Branch branch);

    /**
     *
     * @param user
     * @return
     */
    Boolean save(Users user);

}
