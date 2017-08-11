package com.gofobao.framework.marketing.repository;

import com.gofobao.framework.marketing.entity.Marketing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MarketingRepository extends JpaRepository<Marketing, Long> {
}
