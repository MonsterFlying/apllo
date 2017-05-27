package com.gofobao.framework;

import com.gofobao.framework.tender.entity.AutoTender;
import com.gofobao.framework.tender.repository.AutoTenderRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Example;

import java.util.HashMap;
import java.util.Map;

import org.springframework.test.context.junit4.SpringRunner;
@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class AplloApplicationTests {

    @Autowired
    private AutoTenderRepository autoTenderRepository;

    @Test
    public void contextLoads() {
        //2720
        AutoTender autoTender = new AutoTender();
        autoTender.setId(2720L);
        Example<AutoTender> mapExample = Example.of(autoTender);
        System.out.println(autoTenderRepository.count(mapExample));
    }

}
