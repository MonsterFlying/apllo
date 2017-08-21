package com.gofobao.framework.asset.repository;

import com.gofobao.framework.asset.entity.UnionLineNumber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Created by admin on 2017/8/21.
 */
public interface UnionLineNumberReopsitory extends JpaSpecificationExecutor<UnionLineNumber>,JpaRepository<UnionLineNumber,Long> {

}
