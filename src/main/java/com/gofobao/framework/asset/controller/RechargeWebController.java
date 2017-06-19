package com.gofobao.framework.asset.controller;

import com.gofobao.framework.asset.biz.AssetBiz;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by Administrator on 2017/6/19 0019.
 */
@Api
@Controller
public class RechargeWebController {

    @Autowired
    AssetBiz assetBiz ;

    @PostMapping("/pub/recharge/show/{seqNo}")
    public String rechargeShow(HttpServletRequest request, Model model, @PathVariable("seqNo") String seqNo) throws Exception {
        return assetBiz.rechargeShow(request, model, seqNo) ;
    }

}
