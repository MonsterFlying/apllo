package com.gofobao.framework;

import com.gofobao.framework.borrow.repository.BorrowRepository;
import com.gofobao.framework.collection.repository.BorrowCollectionRepository;
import com.gofobao.framework.tender.entity.AutoTender;
import com.gofobao.framework.tender.repository.AutoTenderRepository;
import com.gofobao.framework.tender.repository.InvestRepository;
import com.gofobao.framework.tender.service.AutoTenderService;
import com.gofobao.framework.tender.service.InvestService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
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

@PersistenceContext
private EntityManager entityManager;

    @Test
    public void test(){

        StringBuffer sql = new StringBuffer("select t.*  from gfb_auto_tender t left join gfb_asset a on t.user_id = a.user_id where 1=1 ");

        Query query = entityManager.createNativeQuery(sql.toString(),AutoTender.class);

        List<AutoTender> autoTenderList = query.getResultList();

        System.out.println(autoTenderList);
    }


    @Test
    public void contextLoads() {

    }

}
