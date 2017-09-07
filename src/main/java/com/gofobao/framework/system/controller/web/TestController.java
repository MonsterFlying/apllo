package com.gofobao.framework.system.controller.web;


import com.gofobao.framework.borrow.biz.BorrowBiz;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.marketing.biz.MarketingBiz;
import com.gofobao.framework.system.biz.*;
import com.gofobao.framework.system.entity.Suggest;
import com.gofobao.framework.system.vo.request.VoArticleReq;
import com.gofobao.framework.system.vo.response.*;
import com.gofobao.framework.tender.service.TenderService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController
public class TestController {

    @Autowired
    FindBiz findBiz;

    @Autowired
    DictBiz dictBiz;

    @Autowired
    private BorrowBiz borrowBiz;

    @Autowired
    private TenderService tenderService;

/*

    @GetMapping("/test/marketing/tender")
    public ResponseEntity<VoFindIndexResp> index() {

        borrowBiz.touchMarketingByTender();


        return findBiz.index();
    }
*/


}
