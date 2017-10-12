package com.gofobao.framework;

import com.gofobao.framework.asset.biz.AssetSynBiz;
import com.gofobao.framework.financial.biz.NewAleveBiz;
import com.gofobao.framework.financial.biz.NewEveBiz;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.ExceptionEmailHelper;
import com.gofobao.framework.scheduler.biz.FundStatisticsBiz;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.Date;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class AssetTests {

    final Gson GSON = new GsonBuilder().create();


    @Autowired
    AssetSynBiz assetSynBiz;

    @Autowired
    ExceptionEmailHelper exceptionEmailHelper;

    @Autowired
    NewAleveBiz newAleveBiz;

    @Autowired
    NewEveBiz newEveBiz;

    @Test
    public void test01() throws Exception {
        Date synDate = DateHelper.stringToDate("2017-09-12", DateHelper.DATE_FORMAT_YMD);
        assetSynBiz.doAdminSynAsset(16858L, synDate);
    }

    @Test
    public void test02() throws Exception {
        exceptionEmailHelper.sendErrorMessage("测试多人发送", "测试多人发送");
    }

    @Test
    public void test03() throws Exception {
        String url = "http://gofobao-admin.dev/504";
        try {
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.exchange(url, HttpMethod.POST, null, Map.class);
        } catch (Exception e) {
            log.error("请求异常", e.getMessage());
            if (e instanceof HttpClientErrorException) {
                if (e.getMessage().contains("405")) {
                    log.error("405");
                }
            } else if (e instanceof HttpServerErrorException) {
                if (e.getMessage().contains("502")) {
                    log.error("502");
                } else if (e.getMessage().contains("504")) {
                    log.error("504");
                }
            }
        }
    }

    @Autowired
    FundStatisticsBiz fundStatisticsBiz;

    @Test
    public void test04() throws Exception {
        FluentIterable<File> filter = Files.fileTreeTraverser().breadthFirstTraversal(new File("D:/statistice")).filter(new Predicate<File>() {
            public boolean apply(File input) {
                return input.isFile();
            }
        });

        int pageSize = filter.size();
        for (int pageIndex = 0; pageIndex < pageSize; pageIndex++) {
            File file = filter.get(pageIndex);
            String fileName = file.getName();
            log.error("============" + fileName);

            int index = file.getName().lastIndexOf("-");
            String date = file.getName().substring(index + 1);
            log.error("============" + date);

            if (file.getName().contains("ALEVE")) {
                newAleveBiz.importDatabase(date, fileName);
                newAleveBiz.calculationCurrentInterest(date);
            } else if (file.getName().contains("EVE")) {
                newEveBiz.importEveDataToDatabase(date, fileName);
            } else {
                log.error("===============ddd======");
            }
        }
    }

    @Test
    public void test05() throws Exception {
        String date = "20171001";
        newEveBiz.audit(date);
    }

}
