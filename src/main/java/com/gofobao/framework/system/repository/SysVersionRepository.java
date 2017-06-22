package com.gofobao.framework.system.repository;

import com.gofobao.framework.system.entity.SysVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by admin on 2017/6/21.
 */
@Repository
public interface SysVersionRepository extends JpaRepository<SysVersion,Long>,JpaSpecificationExecutor<SysVersion> {

    @Query("select v from  SysVersion v where v.terminal=?1 ORDER BY v.id DESC ")
    List<SysVersion>findByTerminal(Integer terminal);
}
