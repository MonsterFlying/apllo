package com.gofobao.framework;

import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.repository.BorrowRepository;
import com.gofobao.framework.collection.repository.BorrowCollectionRepository;
import com.gofobao.framework.helper.BeanHelper;
import com.gofobao.framework.tender.entity.AutoTender;
import com.gofobao.framework.tender.repository.AutoTenderRepository;
import com.gofobao.framework.tender.repository.InvestRepository;
import com.gofobao.framework.tender.service.AutoTenderService;
import com.gofobao.framework.tender.service.InvestService;
import com.gofobao.framework.tender.vo.request.VoInvestListReq;
import com.gofobao.framework.tender.vo.response.VoViewBackMoney;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Example;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j

public class AplloApplicationTests {

    @Autowired
    private AutoTenderRepository autoTenderRepository;
    @Autowired
    private AutoTenderService autoTenderService;


    @Autowired
    private InvestRepository investRepository;

    @Autowired
    private BorrowRepository borrowRepository;

    @Autowired
    private BorrowCollectionRepository borrowCollectionRepository;

    @Autowired
    private InvestService investService;

    @Test
    public void test(){
        VoInvestListReq voInvestListReq=new VoInvestListReq();
        voInvestListReq.setUserId(901L);
        List<VoViewBackMoney> backMoneyList=investService.backMoneyList(voInvestListReq);
        backMoneyList.stream().forEach(VoViewBackMoney ->System.out.print(VoViewBackMoney.getBorrowName()));


    }


   /* @Test
    public void contextLoads() {

        AutoTender saveAutoTender = new AutoTender();
        saveAutoTender.setStatus(true);
        saveAutoTender.setUpdatedAt(new Date());

        AutoTender condAutoTender = new AutoTender();
        condAutoTender.setUserId(30L);
        Example<AutoTender> autoTenderExample = Example.of(condAutoTender);

        autoTenderService.updateByExample(saveAutoTender,autoTenderExample);

        //2720
        AutoTender autoTender = new AutoTender();
        autoTender.setId(2720L);
        autoTender.setAprFirst(10);

*//*        AutoTender saveAutoTender = new AutoTender();*//*
        saveAutoTender.setStatus(true);

        AutoTender autoTender1 = new AutoTender();
        autoTender1.setId(2730L);
        autoTender1.setStatus(true);


        BeanHelper.copyParamter(autoTender,autoTender1,false);


        Borrow borrow = new Borrow();
        System.out.println((Object) autoTender.getClass() == (Object) autoTender1.getClass());
        *//*Example<AutoTender> mapExample = Example.of(autoTender);
        System.out.println(autoTenderRepository.count(mapExample));*//*
    }*/

}
