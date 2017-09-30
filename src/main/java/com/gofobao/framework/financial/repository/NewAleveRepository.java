package com.gofobao.framework.financial.repository;

import com.gofobao.framework.financial.entity.NewAleve;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface NewAleveRepository extends JpaRepository<NewAleve, Long>, JpaSpecificationExecutor<NewAleve> {

    NewAleve findTopByQueryTimeAndTranno(String date, String tranno);

    NewAleve findTopByReldateAndInptimeAndTranno(String reldate, String inptime, String tranno);
}
