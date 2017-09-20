package com.gofobao.framework;

import com.gofobao.framework.asset.biz.AssetSynBiz;
import com.gofobao.framework.helper.DateHelper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class AssetTests {

    final Gson GSON = new GsonBuilder().create();


    @Autowired
    AssetSynBiz assetSynBiz ;

    @Test
    public void test01() throws Exception {
        Date synDate = DateHelper.stringToDate("2017-09-12", DateHelper.DATE_FORMAT_YMD) ;
        assetSynBiz.doAdminSynAsset(16858L, synDate) ;
    }
}
