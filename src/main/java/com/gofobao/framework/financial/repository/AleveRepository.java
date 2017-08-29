package com.gofobao.framework.financial.repository;

import com.gofobao.framework.financial.entity.Aleve;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AleveRepository extends JpaRepository<Aleve, Long> {
}
