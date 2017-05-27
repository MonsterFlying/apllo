package com.gofobao.framework.tender.service;

import com.gofobao.framework.tender.entity.AutoTender;

/**
 * Created by Zeke on 2017/5/27.
 */
public interface AutoTenderService {

    boolean insert(AutoTender autoTender);

    boolean update(AutoTender autoTender);
}
