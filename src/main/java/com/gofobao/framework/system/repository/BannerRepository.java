package com.gofobao.framework.system.repository;

import com.gofobao.framework.system.entity.Banner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by admin on 2017/6/14.
 */
@Repository
public interface BannerRepository extends JpaRepository<Banner,Long> ,JpaSpecificationExecutor<Banner> {

    List<Banner>findByStatusAndTerminalOrderByIdDesc(byte status,Integer terminal);
}