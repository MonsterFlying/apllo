package com.gofobao.framework.system.repository;

import com.gofobao.framework.system.entity.DictValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by Zeke on 2017/5/22.
 */
@Repository
public interface DictValueRepository extends JpaRepository<DictValue,Long>{
    List<DictValue> findByDelAndItemId(Integer del, Long itemId);

    DictValue findTopByItemIdAndValue02AndDel(Long itemId, String bankName, int i);
    DictValue findTopByItemIdAndValue01AndDel(Long itemId, String bankName, int i);

    List<DictValue> findByItemId(Long id);

}
