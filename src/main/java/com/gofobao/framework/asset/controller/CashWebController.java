package com.gofobao.framework.asset.controller;

import com.gofobao.framework.asset.biz.CashDetailLogBiz;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 *
 * Created by Max on 17/6/8.
 */
@Controller
@Api(value = "提现")
public class CashWebController {

    @Autowired
    private CashDetailLogBiz cashDetailLogBiz;



    @GetMapping("/pub/cash/show/{seqNo}")
    public String showCash(@PathVariable("seqNo") String  seqNo, Model model){
        return cashDetailLogBiz.showCash(seqNo, model) ;
    }
}
