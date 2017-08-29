package com.gofobao.framework.financial.repository;

import com.gofobao.framework.financial.entity.Eve;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EveRepository extends JpaRepository<Eve, Long> {
}
