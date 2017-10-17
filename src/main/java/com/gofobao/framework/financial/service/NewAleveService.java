package com.gofobao.framework.financial.service;

import com.gofobao.framework.financial.entity.NewAleve;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public interface NewAleveService {

    NewAleve findTopByQueryTimeAndTranno(String date, String tranno);

    NewAleve save(NewAleve newAleve);

    Long count(Specification<NewAleve> specification);

    Page<NewAleve> findAll(Specification<NewAleve> specification, Pageable pageable);

    NewAleve findTopByReldateAndInptimeAndTranno(String reldate, String inptime, String tranno);

    List<NewAleve> findAll(Specification<NewAleve> specification);

}
