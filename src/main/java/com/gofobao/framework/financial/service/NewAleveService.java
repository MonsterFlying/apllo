package com.gofobao.framework.financial.service;

import com.gofobao.framework.financial.entity.NewAleve;

public interface NewAleveService {

    NewAleve findTopByQueryTimeAndTranno(String date, String tranno);

    NewAleve save(NewAleve newAleve);

}
