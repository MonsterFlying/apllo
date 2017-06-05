package com.gofobao.framework.listener.providers;

import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.service.BorrowService;
import com.gofobao.framework.common.rabbitmq.MqConfig;
import com.gofobao.framework.helper.NumberHelper;
import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;

/**
 * Created by Zeke on 2017/6/2.
 */
@Component
@Slf4j
public class AutoTenderProvider {

    @Autowired
    private BorrowService borrowService;

    public boolean autoTender(Map<String, String> msg) throws Exception{
        Date nowDate = new Date();
        do {
            Long borrowId = NumberHelper.toLong(msg.get(MqConfig.MSG_BORROW_ID));

            Borrow borrow = borrowService.findByIdLock(borrowId);
            Preconditions.checkNotNull(borrow,"自动投标异常：id为"+borrowId+"借款不存在");


        }while (false);
        return false;
    }

}
