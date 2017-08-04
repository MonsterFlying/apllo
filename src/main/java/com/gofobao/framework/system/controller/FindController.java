package com.gofobao.framework.system.controller;


import com.gofobao.framework.system.biz.FindBiz;
import com.gofobao.framework.system.vo.response.VoFindIndexResp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FindController {

    @Autowired
    FindBiz findBiz ;

    @GetMapping("/pub/find/index")
    public ResponseEntity<VoFindIndexResp> index() {
        return  findBiz.index() ;
    }

}
