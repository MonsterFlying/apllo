package com.gofobao.framework.award.repository;

import com.gofobao.framework.collection.entity.VirtualCollection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by admin on 2017/6/8.
 */
@Repository
public interface VirtualCollectionRepository extends JpaRepository<VirtualCollection, Long>, JpaSpecificationExecutor<VirtualCollection> {
    List<VirtualCollection> findByTenderIdInAndStatusIs(List<Integer>tenderIdArray, Integer status);

}
