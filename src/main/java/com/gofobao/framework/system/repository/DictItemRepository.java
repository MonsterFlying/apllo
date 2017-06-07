package com.gofobao.framework.system.repository;

import com.gofobao.framework.system.entity.DictItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by Zeke on 2017/5/22.
 */
@Repository
public interface DictItemRepository extends JpaRepository<DictItem,Long>{

    List<DictItem> findByDelAndAliasCode(Integer isDel, String aliasCode);

    DictItem findTopByAliasCodeAndDel(String aliasCode, int del);
}
