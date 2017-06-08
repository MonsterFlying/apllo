package com.gofobao.framework.award.repository;

import com.gofobao.framework.tender.entity.VirtualTender;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by admin on 2017/6/8.
 */
@Repository
public interface VirtualTenderRepasitory extends JpaRepository<VirtualTender,Long>,JpaSpecificationExecutor<VirtualTender> {

    List<VirtualTender>findByUserIdAndStatusIs(Long userId,Integer status);
}
