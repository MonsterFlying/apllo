package com.gofobao.framework;

import com.gofobao.framework.financial.entity.Aleve;
import com.gofobao.framework.financial.service.AleveService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class DbTests {

    @Autowired
    AleveService aleveService;

    @Test
    public void touchMarketing() {
        String transtype = "7820";  // 线下转账类型
        Pageable pageable = new PageRequest(0, 12, new Sort(new Sort.Order(Sort.Direction.DESC, "id")));
        Page<Aleve> byDateAndTranstype = aleveService.findByDateAndTranstype("20170916", transtype, pageable);

        for(Aleve aleve : byDateAndTranstype.getContent()){
            log.info(aleve.getCardnbr());
        }

    }
}
