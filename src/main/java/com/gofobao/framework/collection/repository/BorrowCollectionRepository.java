package com.gofobao.framework.collection.repository;

import com.gofobao.framework.collection.entity.BorrowCollection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Created by Zeke on 2017/6/2.
 */
@Repository
public interface BorrowCollectionRepository extends JpaRepository<BorrowCollection, Long> ,JpaSpecificationExecutor<BorrowCollection>{
}
