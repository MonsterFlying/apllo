package com.gofobao.framework.asset.controller;

import com.gofobao.framework.asset.biz.BankAccountBiz;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Created by Administrator on 2017/6/19 0019.
 */
@Controller
public class BankAccountWebController {

    @Autowired
    BankAccountBiz bankAccountBiz ;


    @ApiOperation("充值说明")
    @GetMapping("/pub/bank/desc")
    String index(Model model) {
        bankAccountBiz.showDesc(model) ;
        return "/bank/banklist";
    }

}
