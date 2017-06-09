package com.gofobao.framework.tender.repository;

import com.gofobao.framework.tender.entity.Tender;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by Max on 17/5/16.
 */
@Repository
public interface TenderRepository extends JpaRepository<Tender,Long>,JpaSpecificationExecutor<Tender> {






}
