package com.gofobao.framework.financial.service;

import com.gofobao.framework.financial.entity.Aleve;

import java.util.List;

/**
 * eve服务
 */
public interface AleveService {

    List<Aleve> findByTranno(String tranno);

    Aleve save(Aleve aleve);

}
