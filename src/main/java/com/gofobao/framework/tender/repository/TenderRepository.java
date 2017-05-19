package com.gofobao.framework.tender.repository;

import com.gofobao.framework.tender.entity.Tender;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by Max on 17/5/16.
 */
@Repository
public interface TenderRepository extends JpaRepository<Tender,Long> {



}
