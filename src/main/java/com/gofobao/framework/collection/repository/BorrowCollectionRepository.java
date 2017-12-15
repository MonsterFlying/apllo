package com.gofobao.framework.collection.repository;

import com.gofobao.framework.collection.entity.BorrowCollection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by admin on 2017/5/31.
 */
@Repository
public interface BorrowCollectionRepository extends JpaRepository<BorrowCollection, Long>, JpaSpecificationExecutor<BorrowCollection> {

    /**
     * 根据tenderId查询回款
     *
     * @param tenderId
     * @return
     */
    List<BorrowCollection> findByTenderId(Long tenderId);

    /**
     * 根据tenderId集合查询回款集合
     *
     * @param tenderId
     * @return
     */
    List<BorrowCollection> findByTenderIdIn(List<Long> tenderId);


    List<BorrowCollection> findByBorrowId(List<Long> borrowIds);


    @Query("SELECT collections from  BorrowCollection  collections\n " +
            "WHERE\n" +
                "collections.userId=?1\n" +
            "and\n" +
                "(DATE_FORMAT(collections.collectionAt,'%Y%m%d')=CURRENT_DATE\n" +
                    "OR\n" +
                "DATE_FORMAT(collections.collectionAtYes,'%Y-%m-%d')=CURRENT_DATE)")
    List<BorrowCollection> todayBorrowCollections(Long userId);
}
