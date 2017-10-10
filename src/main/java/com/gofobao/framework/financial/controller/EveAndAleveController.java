package com.gofobao.framework.financial.controller;

import com.gofobao.framework.common.qiniu.util.StringUtils;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.financial.biz.NewAleveBiz;
import com.gofobao.framework.financial.biz.NewEveBiz;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * EVE 和 ALEVE
 */
@RestController
public class EveAndAleveController {

    @Autowired
    NewEveBiz newEveBiz;

    @Autowired
    NewAleveBiz newAleveBiz;


    /**
     * 下载eve文件
     * @param date 下载文件时间
     * @return
     */
    @GetMapping(value = "/pub/download/eve/{date}")
    public ResponseEntity<VoBaseResp> downloadEve(@PathVariable(name = "date") String date) {
        if (StringUtils.isNullOrEmpty(date)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "下载时间不能为空")) ;
        }

        boolean result = newEveBiz.downloadEveFileAndSaveDB(date);
        if(result){
            return ResponseEntity.ok(VoBaseResp.ok("下载成功")) ;
        }else{
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "文件下载失败")) ;
        }
    }

    /**
     *  下载aleve文件
     * @param date 文件时间
     * @return
     */

    @GetMapping(value = "/pub/download/aleve/{date}")
    public ResponseEntity<VoBaseResp> downloadAleve(@PathVariable(name = "date") String date) {
        if (StringUtils.isNullOrEmpty(date)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "下载时间不能为空")) ;
        }

        boolean result = newAleveBiz.downloadNewAleveFileAndImportDatabase(date);
        if(result){
            return ResponseEntity.ok(VoBaseResp.ok("下载成功")) ;
        }else{
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "文件下载失败")) ;
        }
    }
}
