package com.gofobao.framework.financial.controller;

import com.gofobao.framework.as.bix.RechargeStatementBiz;
import com.gofobao.framework.as.bix.impl.RechargeStatementBizImpl;
import com.gofobao.framework.common.constans.TypeTokenContants;
import com.gofobao.framework.common.qiniu.util.StringUtils;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.financial.biz.NewAleveBiz;
import com.gofobao.framework.financial.biz.NewEveBiz;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.project.SecurityHelper;
import com.gofobao.framework.tender.vo.request.VoRechargeReq;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.Map;

/**
 * EVE 和 ALEVE
 */
@RestController
@Slf4j
public class EveAndAleveController {

    @Autowired
    NewEveBiz newEveBiz;

    @Autowired
    NewAleveBiz newAleveBiz;

    @Autowired
    RechargeStatementBiz rechargeStatementBiz;


    /**
     * 下载eve文件
     *
     * @param date 下载文件时间
     * @return
     */
    @GetMapping(value = "/pub/download/eve/{date}")
    public ResponseEntity<VoBaseResp> downloadEve(@PathVariable(name = "date") String date) {
        if (StringUtils.isNullOrEmpty(date)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "下载时间不能为空"));
        }

        boolean result = newEveBiz.downloadEveFileAndSaveDB(date);
        if (result) {
            return ResponseEntity.ok(VoBaseResp.ok("下载成功"));
        } else {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "文件下载失败"));
        }
    }

    /**
     * 下载文件
     *
     * @param date
     * @return
     */
    @GetMapping(value = "/pub/eveAndAleve/download/{date}")
    public ResponseEntity<VoBaseResp> download(@PathVariable(name = "date") String date) {
        if (StringUtils.isNullOrEmpty(date)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "下载时间不能为空"));
        }

        newEveBiz.simpleDownload(date);
        newAleveBiz.simpleDownload(date);
        return ResponseEntity.ok(VoBaseResp.ok("下载成功"));
    }

    @PostMapping("pub/rechargeStatement/offline")
    public ResponseEntity<VoBaseResp> offlineRechargeStatement(@ModelAttribute VoRechargeReq voRechargeReq) throws Exception {
        String paramStr = voRechargeReq.getParamStr();
        if (!SecurityHelper.checkSign(voRechargeReq.getSign(), paramStr)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "离线同步充值请求失败!"));
        }

        Gson gson = new Gson();
        Map<String, String> data = gson.fromJson(paramStr, TypeTokenContants.MAP_ALL_STRING_TOKEN);
        String dateStr = data.get("date");
        if (ObjectUtils.isEmpty(dateStr)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "离线 date 为空!"));
        }

        Date date = DateHelper.stringToDate(dateStr);
        String id = data.get("id");
        if (ObjectUtils.isEmpty(id)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "离线 id 为空!"));
        }
        Long userId = Long.parseLong(id);
        boolean statementState = rechargeStatementBiz.offlineStatement(userId, date, RechargeStatementBizImpl.RechargeType.offlineRecharge);
        if (statementState) {
            log.info("离线处理线下充值成功");
        } else {
            log.error("离线处理线下充值失败");
        }
        statementState = rechargeStatementBiz.offlineStatement(userId, date, RechargeStatementBizImpl.RechargeType.onlineRecharge);
        if (statementState) {
            log.info("离线处理在线充值成功");
        } else {
            log.error("离线处理在线充值失败");
        }

        return ResponseEntity.ok(VoBaseResp.ok("同步成功"));
    }


    @PostMapping("pub/rechargeStatement/online")
    public ResponseEntity<VoBaseResp> onlineRechargeStatement(@ModelAttribute VoRechargeReq voRechargeReq) throws Exception {
        String paramStr = voRechargeReq.getParamStr();
        if (!SecurityHelper.checkSign(voRechargeReq.getSign(), paramStr)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "实时同步充值请求失败!"));
        }

        Gson gson = new Gson();
        Map<String, String> data = gson.fromJson(paramStr, TypeTokenContants.MAP_ALL_STRING_TOKEN);
        String dateStr = data.get("date");
        if (ObjectUtils.isEmpty(dateStr)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "实时 date 为空!"));
        }

        Date date = DateHelper.stringToDate(dateStr);
        String id = data.get("id");
        if (ObjectUtils.isEmpty(id)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "实时 id 为空!"));
        }
        Long userId = Long.parseLong(id);
        boolean statementState = rechargeStatementBiz.onlineStatement(userId, date, RechargeStatementBizImpl.RechargeType.offlineRecharge, true);
        if (statementState) {
            log.info("实时处理线下充值成功");
        } else {
            log.error("实时处理线下充值失败");
        }
        statementState = rechargeStatementBiz.onlineStatement(userId, date, RechargeStatementBizImpl.RechargeType.onlineRecharge, true);
        if (statementState) {
            log.info("实时处理在线充值成功");
        } else {
            log.error("实时处理在线充值失败");
        }

        return ResponseEntity.ok(VoBaseResp.ok("同步成功"));
    }


    /**
     * 发送每日对账文件
     *
     * @param date
     * @return
     */
    @GetMapping(value = "/pub/eveAndAleve/sendEmail/{date}")
    public ResponseEntity<VoBaseResp> sendEmail(@PathVariable(name = "date") String date) {
        if (StringUtils.isNullOrEmpty(date)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "下载时间不能为空"));
        }

        newEveBiz.audit(date);
        return ResponseEntity.ok(VoBaseResp.ok("下载成功"));
    }

    /**
     * 派发活期利息
     *
     * @param date
     * @return
     */
    @GetMapping(value = "/pub/eveAndAleve/currentInterest/{date}")
    public ResponseEntity<VoBaseResp> publishCurrentInterest(@PathVariable String date) {
        if (StringUtils.isNullOrEmpty(date)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "活期利息派发时间错误"));
        }

        try {
            newAleveBiz.calculationCurrentInterest(date);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "活期派发失败"));
        }
        return ResponseEntity.ok(VoBaseResp.ok("成功"));
    }

    /**
     * 下载aleve文件
     *
     * @param date 文件时间
     * @return
     */

    @GetMapping(value = "/pub/download/aleve/{date}")
    public ResponseEntity<VoBaseResp> downloadAleve(@PathVariable(name = "date") String date) {
        if (StringUtils.isNullOrEmpty(date)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "下载时间不能为空"));
        }

        boolean result = newAleveBiz.downloadNewAleveFileAndImportDatabase(date);
        if (result) {
            return ResponseEntity.ok(VoBaseResp.ok("下载成功"));
        } else {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "文件下载失败"));
        }
    }
}
