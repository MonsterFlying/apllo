package com.gofobao.framework.tender.repository;

import com.gofobao.framework.tender.entity.Transfer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;
import java.util.List;

/**
 * Created by Zeke on 2017/7/31.
 */
@Repository
public interface TransferRepository extends JpaSpecificationExecutor<Transfer>, JpaRepository<Transfer, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Transfer findById(long id);

    /**
     * 根据债券ids查询债券集合
     *
     * @param ids
     * @return
     */
    List<Transfer> findByIdIn(List<Long> ids);


    List<Transfer> findByBorrowId(Long borrowId);

    /**
     * 首页理财 流转标列表
     *
     * @param pageable
     * @return
     */
    @Query("select transfer from Transfer transfer " +
            "where " +
            "(transfer.state=1 or (transfer.state=2 and transfer.apr<1500)) " +
            "and  " +
            "transfer.type =0 " +
            "AND " +
            "transfer.releaseAt is not null " +
            "order by " +
            "transfer.state asc , " +
            "transfer.transferMoneyYes/transfer.transferMoney,transfer.id desc" )
    Page<Transfer> findByStateIsOrStateIsAndAprThanLee(Pageable pageable);


    /**
     * 首页全部
     *
     * @return
     */
    @Query("SELECT transfer from Transfer  transfer\n" +
            "where\n" +
            "(transfer.state= 1)\n" +
            "and\n" +
            "transfer.type =0\n" +
            "and\n" +
            "transfer.transferMoneyYes/transfer.transferMoney!=1\n " +
            "and\n" +
            "transfer.releaseAt is not null\n"+
            "and\n" +
            "transfer.successAt is null\n" +
            "order by\n" +
            "transfer.state asc,\n" +
            "transfer.transferMoneyYes/transfer.transferMoney ,transfer.id desc ")
    List<Transfer> indexList();
}
