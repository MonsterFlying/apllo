package com.gofobao.framework.system.repository;

import com.gofobao.framework.system.entity.DictItem;
import com.gofobao.framework.system.entity.DictValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by Zeke on 2017/5/22.
 */
@Repository
public interface DictValueRepository extends JpaRepository<DictValue,Long>{
    List<DictValue> findByIsDelAndItemId(boolean isDel, Long itemId);
}
