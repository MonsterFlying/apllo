package com.gofobao.framework.financial.service;

import com.gofobao.framework.financial.entity.NewAleve;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public interface NewAleveService {

    NewAleve findTopByQueryTimeAndTranno(String date, String tranno);

    NewAleve save(NewAleve newAleve);

    Long count(Specification<NewAleve> specification);

    Page<NewAleve> findAll(Specification<NewAleve> specification, Pageable pageable);
}
