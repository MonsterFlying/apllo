package com.gofobao.framework.tender.service;

import com.gofobao.framework.tender.entity.AutoTender;
import org.springframework.data.domain.Example;

/**
 * Created by Zeke on 2017/5/27.
 */
public interface AutoTenderService {

    boolean insert(AutoTender autoTender);

    boolean updateById(AutoTender autoTender);

    boolean updateByExample(AutoTender autoTender, Example<AutoTender> example);
}
