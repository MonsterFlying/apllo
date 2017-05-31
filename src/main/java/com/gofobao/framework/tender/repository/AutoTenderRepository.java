package com.gofobao.framework.tender.repository;

import com.gofobao.framework.tender.entity.AutoTender;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * Created by Zeke on 2017/5/27.
 */
public interface AutoTenderRepository extends JpaRepository<AutoTender,Long>{

}
