package com.gofobao.framework.lend.repository;

import com.gofobao.framework.lend.entity.LendBlacklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Created by Zeke on 2017/5/26.
 */
@Repository
public interface LendBlacklistRepository extends JpaRepository<LendBlacklist, Long>,JpaSpecificationExecutor<LendBlacklist> {
}
