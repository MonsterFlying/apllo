package com.gofobao.framework.collection.repository;

import com.gofobao.framework.collection.entity.BorrowCollection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by admin on 2017/5/31.
 */
@Repository
public interface BorrowCollectionRepository extends  JpaRepository<BorrowCollection,Long>,JpaSpecificationExecutor<BorrowCollection> {

        List<BorrowCollection>findByTenderId(Long tenderId);

        List<BorrowCollection> findByTenderIdIn(List<Long> tenderId);


        List<BorrowCollection>findByBorrowId(List<Long> borrowIds);
}
