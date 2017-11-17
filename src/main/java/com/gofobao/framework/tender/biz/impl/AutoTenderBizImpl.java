package com.gofobao.framework.tender.biz.impl;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.service.BorrowService;
import com.gofobao.framework.common.constans.TypeTokenContants;
import com.gofobao.framework.common.rabbitmq.MqConfig;
import com.gofobao.framework.common.rabbitmq.MqHelper;
import com.gofobao.framework.common.rabbitmq.MqQueueEnum;
import com.gofobao.framework.common.rabbitmq.MqTagEnum;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.*;
import com.gofobao.framework.helper.project.SecurityHelper;
import com.gofobao.framework.member.entity.UserThirdAccount;
import com.gofobao.framework.member.service.UserThirdAccountService;
import com.gofobao.framework.member.vo.response.VoHtmlResp;
import com.gofobao.framework.tender.biz.AutoTenderBiz;
import com.gofobao.framework.tender.contants.AutoTenderContants;
import com.gofobao.framework.tender.contants.BorrowContants;
import com.gofobao.framework.tender.entity.AutoTender;
import com.gofobao.framework.tender.service.AutoTenderService;
import com.gofobao.framework.tender.vo.request.*;
import com.gofobao.framework.tender.vo.response.*;
import com.gofobao.framework.tender.vo.response.web.PcAutoTender;
import com.gofobao.framework.tender.vo.response.web.VoViewPcAutoTenderWarpRes;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.*;

/**
 * Created by Zeke on 2017/5/27.
 */
@Service
@Slf4j
public class AutoTenderBizImpl implements AutoTenderBiz {
    final Gson gson = new GsonBuilder().create();
    @Autowired
    private AutoTenderService autoTenderService;
    @Autowired
    private ThymeleafHelper thymeleafHelper;
    @Autowired
    private BorrowService borrowService;
    @Autowired
    UserThirdAccountService userThirdAccountService;
    @Autowired
    MqHelper mqHelper;

    @Override
    public ResponseEntity<VoViewUserAutoTenderWarpRes> list(Long userId) {
        try {
            VoViewUserAutoTenderWarpRes warpRes = VoBaseResp.ok("查询成功", VoViewUserAutoTenderWarpRes.class);
            List<UserAutoTender> autoTenderList = autoTenderService.list(userId);
            warpRes.setTenderList(autoTenderList);
            return ResponseEntity.ok(warpRes);
        } catch (Throwable e) {
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "", VoViewUserAutoTenderWarpRes.class));
        }

    }

    /**
     * 发送自动投标
     *
     * @param voSendAutoTender
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<VoBaseResp> sendAutoTender(VoSendAutoTender voSendAutoTender) {
        String paramStr = voSendAutoTender.getParamStr();/* pc请求提前结清参数 */
        if (!SecurityHelper.checkSign(voSendAutoTender.getSign(), paramStr)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "pc取消借款 签名验证不通过!"));
        }
        Map<String, String> paramMap = gson.fromJson(paramStr, TypeTokenContants.MAP_ALL_STRING_TOKEN);
        /* 借款id */
        long borrowId = NumberHelper.toLong(paramMap.get("borrowId"));
        Borrow borrow = borrowService.findById(borrowId);
        Preconditions.checkNotNull(borrow, "借款记录不存在!");
        // 自动投标前提:
        // 1.没有设置标密码
        // 2.车贷标, 渠道标, 流转表
        // 3.标的年化率为 800 以上
        Integer borrowType = borrow.getType();
        ImmutableList<Integer> autoTenderBorrowType = ImmutableList.of(0, 1, 4);
        if ((ObjectUtils.isEmpty(borrow.getPassword()))
                && (autoTenderBorrowType.contains(borrowType)) && borrow.getApr() > 800) {
            Date checkDate = DateHelper.stringToDate(String.format("%s 20:00:00", DateHelper.dateToString(new Date(), DateHelper.DATE_FORMAT_YMD)));
            if (borrow.getIsNovice() && new Date().getTime() < checkDate.getTime()) {   // 对于新手标直接延迟8点后推送
                return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "新手标请在晚上8点后触发"));
            }

            if (borrow.getMoneyYes() == 0 && borrow.getIsLock()) {
                //触发自动投标队列
                MqConfig mqConfig = new MqConfig();
                mqConfig.setQueue(MqQueueEnum.RABBITMQ_TENDER);
                mqConfig.setTag(MqTagEnum.AUTO_TENDER);
                ImmutableMap<String, String> body = ImmutableMap
                        .of(MqConfig.MSG_BORROW_ID, StringHelper.toString(borrow.getId()), MqConfig.MSG_TIME, DateHelper.dateToString(new Date()));
                mqConfig.setMsg(body);
                try {
                    log.info(String.format("borrowProvider autoTender send mq %s", gson.toJson(body)));
                    mqHelper.convertAndSend(mqConfig);
                    return ResponseEntity.ok(VoBaseResp.ok("触发成功!"));
                } catch (Throwable e) {
                    log.error("borrowProvider autoTender send mq exception", e);
                    return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "触发失败!"));
                }

            } else if (borrow.getMoneyYes() > 0 && borrow.getIsLock()) {
                borrow.setIsLock(false);
                borrow.setUpdatedAt(new Date());
                borrowService.save(borrow);
            }
        }
        return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "触发失败!"));
    }

    /**
     * 创建自动投标规则
     *
     * @param voSaveAutoTenderReq
     * @return
     */
    public ResponseEntity<VoBaseResp> createAutoTender(VoSaveAutoTenderReq voSaveAutoTenderReq) {
        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(voSaveAutoTenderReq.getUserId());
        if (ObjectUtils.isEmpty(userThirdAccount)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR_OPEN_ACCOUNT, "你还没有开通江西银行存管，请前往开通！", VoViewAutoTenderList.class));
        }

        if (userThirdAccount.getPasswordState() != 1) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR_INIT_BANK_PASSWORD, "请初始化江西银行存管账户密码！", VoViewAutoTenderList.class));
        }

        /*if (userThirdAccount.getAutoTransferState() != 1) { // 审核
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR_CREDIT_TENDER, "请先签订自动债权转让协议！", VoViewAutoTenderList.class));
        }
*/

        if (!userThirdAccount.getAutoTenderState().equals(1)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR_CREDIT_TENDER, "请先签订自动投标协议！", VoViewAutoTenderList.class));
        }

        Long userId = voSaveAutoTenderReq.getUserId();

        ResponseEntity resp = verifySaveAutoTender(voSaveAutoTenderReq);
        if (!ObjectUtils.isEmpty(resp)) {
            return resp;
        }

        Specification<AutoTender> atSpecification = Specifications
                .<AutoTender>and()
                .eq("userId", userId)
                .build();
        long count = autoTenderService.count(atSpecification);

        if (count >= 3) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "自动投标规则超过三条!"));
        }

        Date nowDate = new Date();
        AutoTender autoTender = getSaveAutoTender(voSaveAutoTenderReq);
        autoTender.setOrder(autoTenderService.getOrderNum() + 1);
        autoTender.setCreatedAt(nowDate);
        autoTender.setUpdatedAt(nowDate);
        autoTender.setAutoAt(nowDate);
        autoTender.setUserId(voSaveAutoTenderReq.getUserId());

        autoTenderService.insert(autoTender);
        return ResponseEntity.ok(VoBaseResp.ok("新增自动投标规则成功！"));
    }

    /**
     * 更新自动投标规则
     *
     * @param voSaveAutoTenderReq
     * @return
     */
    public ResponseEntity<VoBaseResp> updateAutoTender(VoSaveAutoTenderReq voSaveAutoTenderReq) {
        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(voSaveAutoTenderReq.getUserId());
        if (ObjectUtils.isEmpty(userThirdAccount)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR_OPEN_ACCOUNT, "你还没有开通江西银行存管，请前往开通！", VoViewAutoTenderList.class));
        }

        if (userThirdAccount.getPasswordState() != 1) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR_INIT_BANK_PASSWORD, "请初始化江西银行存管账户密码！", VoViewAutoTenderList.class));
        }

    /*    if (userThirdAccount.getAutoTransferState() != 1) {  // 审核
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR_CREDIT_TENDER, "请先签订自动债权转让协议！", VoViewAutoTenderList.class));
        }*/


        if (!userThirdAccount.getAutoTenderState().equals(1)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR_CREDIT_TENDER, "请先签订自动投标协议！", VoViewAutoTenderList.class));
        }


        Long tenderId = voSaveAutoTenderReq.getId();
        Long userId = voSaveAutoTenderReq.getUserId();

        ResponseEntity resp = verifySaveAutoTender(voSaveAutoTenderReq);
        if (!ObjectUtils.isEmpty(resp)) {
            return resp;
        }

        Specification<AutoTender> atSpecification = Specifications
                .<AutoTender>and()
                .eq("userId", userId)
                .eq("id", tenderId)
                .build();
        List<AutoTender> autoTenderList = autoTenderService.findList(atSpecification);
        if (autoTenderList.size() == 1) {
            AutoTender targetAutoTender = getSaveAutoTender(voSaveAutoTenderReq);
            AutoTender autoTender = autoTenderList.get(0);
            autoTender.setUpdatedAt(new Date());
            BeanHelper.copyParamter(targetAutoTender, autoTender, true);
            autoTenderService.updateById(autoTender);
        }

        return ResponseEntity.ok(VoBaseResp.ok("更新自动投标规则成功！"));
    }

    /**
     * 验证创建/更新自动投标参数
     *
     * @param voSaveAutoTenderReq
     * @return
     */
    private ResponseEntity<VoBaseResp> verifySaveAutoTender(VoSaveAutoTenderReq voSaveAutoTenderReq) {
        Integer tenderMoney = voSaveAutoTenderReq.getTenderMoney();
        if (ObjectUtils.isEmpty(tenderMoney)) {
            voSaveAutoTenderReq.setMode(0);
            voSaveAutoTenderReq.setTenderMoney(0D);
        } else {
            voSaveAutoTenderReq.setMode(1);
            voSaveAutoTenderReq.setTenderMoney(tenderMoney / 100.0D);
        }

        Integer mode = voSaveAutoTenderReq.getMode();
        if ((mode == 1) && ((ObjectUtils.isEmpty(tenderMoney)) || tenderMoney < voSaveAutoTenderReq.getLowest() || (tenderMoney < AutoTenderContants.MAX_TENDER_MONEY))) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "自动投标最大投标金额不准确!"));
        }

        // 期限类型
        Integer timelimitType = voSaveAutoTenderReq.getTimelimitType();
        Integer timelimitFirst = voSaveAutoTenderReq.getTimelimitFirst();
        Integer timelimitLast = voSaveAutoTenderReq.getTimelimitLast();
        if ((timelimitType != 0 && ObjectUtils.isEmpty(timelimitFirst)) || (timelimitType != 0 && ObjectUtils.isEmpty(timelimitLast))) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "自动投标期限选择有误!"));
        }

        if ((timelimitType != 0) && (timelimitLast < timelimitFirst)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "自动投标期限起始时间有误!"));
        }

        String[] repayFashionArr = voSaveAutoTenderReq.getRepayFashions().split(",");
        if (timelimitType == AutoTenderContants.TIME_LIMIT_TYPE_BY_MONTH) {
            if (!ArrayUtils.contains(repayFashionArr, BorrowContants.REPAY_FASHION_AYFQ) && !ArrayUtils.contains(repayFashionArr, BorrowContants.REPAY_FASHION_XXHB)) {
                return ResponseEntity
                        .badRequest()
                        .body(VoBaseResp.error(VoBaseResp.ERROR, "还款方式错误!"));
            }
        } else if (timelimitType == AutoTenderContants.TIME_LIMIT_TYPE_BY_DAY) {
            if (!ArrayUtils.contains(repayFashionArr, BorrowContants.REPAY_FASHION_YCBX)) {
                return ResponseEntity
                        .badRequest()
                        .body(VoBaseResp.error(VoBaseResp.ERROR, "还款方式错误!"));
            }
        }
        // 年化率
        Integer aprFirst = voSaveAutoTenderReq.getAprFirst();
        Integer aprLast = voSaveAutoTenderReq.getAprLast();
        if ((ObjectUtils.isEmpty(aprFirst)) || (ObjectUtils.isEmpty(aprLast)) || (aprLast < aprFirst)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "自动投标年化率有误!"));
        }

        return null;
    }

    /**
     * 获取创建/更新 自动投标对象
     *
     * @param voSaveAutoTenderReq
     * @return
     */
    private AutoTender getSaveAutoTender(VoSaveAutoTenderReq voSaveAutoTenderReq) {

        Gson gson = new GsonBuilder().create();
        AutoTender autoTender = gson.fromJson(gson.toJson(voSaveAutoTenderReq), new TypeToken<AutoTender>() {
        }.getType());

        //处理投标种类
        autoTender.setTender0(0);
        autoTender.setTender1(0);
        autoTender.setTender3(0);
        autoTender.setTender4(0);

        String borrowTypes = voSaveAutoTenderReq.getBorrowTypes();
        String[] borrowTypeArr = borrowTypes.split(",");
        Integer num = 0;
        for (String str : borrowTypeArr) {
            switch (str) {
                case "0":
                    autoTender.setTender0(1);
                    num += MathHelper.pow(2, 0);
                    break;
                case "1":
                    autoTender.setTender1(1);
                    num += MathHelper.pow(2, 1);
                    break;
                case "3":
                    autoTender.setTender3(1);
                    num += MathHelper.pow(2, 3);
                    break;
                case "4":
                    autoTender.setTender4(1);
                    num += MathHelper.pow(2, 4);
                    break;
                default:
            }
        }
        autoTender.setBorrowTypes(num);

        //处理返款方式
        String[] repayFashions = voSaveAutoTenderReq.getRepayFashions().split(",");
        num = 0;
        for (String repayStr : repayFashions) {
            switch (repayStr) {
                case "0":
                    num += MathHelper.pow(2, 0);
                    break;
                case "1":
                    num += MathHelper.pow(2, 1);
                    break;
                case "2":
                    num += MathHelper.pow(2, 2);
                    break;
                default:
            }
        }
        autoTender.setRepayFashions(num);
        return autoTender;
    }

    /**
     * 开启自动投标
     *
     * @param voOpenAutoTenderReq
     * @return
     */
    public ResponseEntity<VoBaseResp> openAutoTender(VoOpenAutoTenderReq voOpenAutoTenderReq) {

        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(voOpenAutoTenderReq.getUserId());
        if (ObjectUtils.isEmpty(userThirdAccount)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR_OPEN_ACCOUNT, "你还没有开通江西银行存管，请前往开通！", VoViewAutoTenderList.class));
        }

        if (userThirdAccount.getPasswordState() != 1) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR_INIT_BANK_PASSWORD, "请初始化江西银行存管账户密码！", VoViewAutoTenderList.class));
        }

     /*   if (userThirdAccount.getAutoTransferState() != 1) {  // 审核
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR_CREDIT_TENDER, "请先签订自动债权转让协议！", VoViewAutoTenderList.class));
        }*/


        if (!userThirdAccount.getAutoTenderState().equals(1)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR_CREDIT_TENDER, "请先签订自动投标协议！", VoViewAutoTenderList.class));
        }


        Long tenderId = voOpenAutoTenderReq.getTenderId();
        Long userId = voOpenAutoTenderReq.getUserId();

        Specification<AutoTender> atSpecification = Specifications
                .<AutoTender>and()
                .eq("userId", userId)
                .eq("id", tenderId)
                .build();
        List<AutoTender> autoTenderList = autoTenderService.findList(atSpecification);
        if (autoTenderList.size() == 1) {
            AutoTender autoTender = autoTenderList.get(0);
            if (autoTender.getStatus()) {
                autoTender.setStatus(false);
            } else {
                autoTender.setStatus(true);
            }
            autoTender.setUpdatedAt(new Date());
            autoTenderService.updateById(autoTender);
        }
        return ResponseEntity.ok(VoBaseResp.ok("开启/关闭自动投标规则成功!"));
    }

    /**
     * 删除自动投标跪着
     *
     * @param voDelAutoTenderReq
     * @return
     */
    public ResponseEntity<VoBaseResp> delAutoTender(VoDelAutoTenderReq voDelAutoTenderReq) {
        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(voDelAutoTenderReq.getUserId());
        if (ObjectUtils.isEmpty(userThirdAccount)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR_OPEN_ACCOUNT, "你还没有开通江西银行存管，请前往开通！", VoViewAutoTenderList.class));
        }

        if (userThirdAccount.getPasswordState() != 1) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR_INIT_BANK_PASSWORD, "请初始化江西银行存管账户密码！", VoViewAutoTenderList.class));
        }

        /*if (userThirdAccount.getAutoTransferState() != 1) {  // 审核
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR_CREDIT_TENDER, "请先签订自动债权转让协议！", VoViewAutoTenderList.class));
        }*/


        if (!userThirdAccount.getAutoTenderState().equals(1)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR_CREDIT_TENDER, "请先签订自动投标协议！", VoViewAutoTenderList.class));
        }


        Long tenderId = voDelAutoTenderReq.getTenderId();
        Long userId = voDelAutoTenderReq.getUserId();

        Specification<AutoTender> atSpecification = Specifications
                .<AutoTender>and()
                .eq("userId", userId)
                .eq("id", tenderId)
                .build();
        List<AutoTender> autoTenderList = autoTenderService.findList(atSpecification);
        if (autoTenderList.size() == 1) {
            autoTenderService.delete(autoTenderList.get(0).getId());
        }

        return ResponseEntity.ok(VoBaseResp.ok("删除自动投标规则成功!"));
    }

    /**
     * 查询自动投标详情
     *
     * @param autoTenderId
     * @param userId
     * @return
     */
    public ResponseEntity<VoAutoTenderInfo> queryAutoTenderInfo(Long autoTenderId, Long userId) {

        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(userId);
        if (ObjectUtils.isEmpty(userThirdAccount)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR_OPEN_ACCOUNT, "你还没有开通江西银行存管，请前往开通！", VoAutoTenderInfo.class));
        }

        if (userThirdAccount.getPasswordState() != 1) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR_INIT_BANK_PASSWORD, "请初始化江西银行存管账户密码！", VoAutoTenderInfo.class));
        }
/*
        if (userThirdAccount.getAutoTransferState() != 1) { // 审核
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR_CREDIT_TENDER, "请先签订自动债权转让协议！", VoAutoTenderInfo.class));
        }*/


        if (!userThirdAccount.getAutoTenderState().equals(1)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR_CREDIT_TENDER, "请先签订自动投标协议！", VoAutoTenderInfo.class));
        }

        if (ObjectUtils.isEmpty(autoTenderId) || ObjectUtils.isEmpty(userId)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoAutoTenderInfo.ERROR, "自动投标：参数缺少!", VoAutoTenderInfo.class));
        }
        Specification<AutoTender> specification = Specifications
                .<AutoTender>and()
                .eq("userId", userId)
                .eq("id", autoTenderId)
                .build();
        List<AutoTender> autoTenderList = autoTenderService.findList(specification);
        if (CollectionUtils.isEmpty(autoTenderList)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoAutoTenderInfo.ERROR, "自动投标：未找到自动投标规则!", VoAutoTenderInfo.class));
        }

        AutoTender autoTender = autoTenderList.get(0);
        VoAutoTenderInfo voAutoTenderInfo = VoBaseResp.ok("自动投标：详情查询成功!", VoAutoTenderInfo.class);
        voAutoTenderInfo.setAprFirst(new Integer(autoTender.getAprFirst()));
        voAutoTenderInfo.setAprLast(new Integer(autoTender.getAprLast()));

        StringBuffer borrowTypes = new StringBuffer();
        //标的类型
        if (autoTender.getTender0() == 1) {
            borrowTypes.append(",0");
        }

        if (autoTender.getTender1() == 1) {
            borrowTypes.append(",1");
        }

        if (autoTender.getTender3() == 1) {
            borrowTypes.append(",3");
        }

        if (autoTender.getTender4() == 1) {
            borrowTypes.append(",4");
        }

        if (!StringUtils.isEmpty(borrowTypes)) {
            voAutoTenderInfo.setBorrowTypes(borrowTypes.toString().substring(1));
        } else {
            voAutoTenderInfo.setBorrowTypes("");
        }

        voAutoTenderInfo.setId(autoTender.getId());
        voAutoTenderInfo.setLowest(StringHelper.formatDouble(autoTender.getLowest(), 100.0, false));
        voAutoTenderInfo.setShowLowest(StringHelper.formatDouble(autoTender.getLowest(), 100.0, true));
        voAutoTenderInfo.setMode(autoTender.getMode());
        Integer repayFashions = autoTender.getRepayFashions();  // 投标类型

        String repayFashionsStr = new StringBuffer(Integer.toBinaryString(repayFashions)).reverse().toString();
        StringBuffer repayFashionsSb = new StringBuffer();
        for (int i = 0; i < repayFashionsStr.length(); i++) {
            if ("1".equals(StringHelper.toString(repayFashionsStr.charAt(i)))) {
                repayFashionsSb.append(String.format(",%s", i));
            }
        }

        if (!StringUtils.isEmpty(repayFashionsSb)) {
            voAutoTenderInfo.setRepayFashions(repayFashionsSb.substring(1));
        } else {
            voAutoTenderInfo.setRepayFashions("");
        }

        voAutoTenderInfo.setSaveMoney(StringHelper.formatDouble(autoTender.getSaveMoney(), 100.0, false));
        voAutoTenderInfo.setShowSaveMoney(StringHelper.formatDouble(autoTender.getSaveMoney(), 100.0, true));
        voAutoTenderInfo.setStatus(autoTender.getStatus());
        voAutoTenderInfo.setTenderMoney(StringHelper.formatDouble(autoTender.getTenderMoney(), 100.0, false));
        voAutoTenderInfo.setShowTenderMoney(StringHelper.formatDouble(autoTender.getTenderMoney(), 100.0, true));
        voAutoTenderInfo.setTimelimitType(autoTender.getTimelimitType());
        voAutoTenderInfo.setTimelimitFirst(autoTender.getTimelimitFirst());
        voAutoTenderInfo.setTimelimitLast(autoTender.getTimelimitLast());

        return ResponseEntity.ok(voAutoTenderInfo);
    }

    /**
     * 自动投标说明
     *
     * @return
     * @throws Exception
     */
    public ResponseEntity<VoHtmlResp> autoTenderDesc() {
        Map<String, Object> paranMap = new HashMap<>();
        String content = thymeleafHelper.build("autoTender/autoTender", paranMap);
        VoHtmlResp resp = VoHtmlResp.ok("获取成功!", VoHtmlResp.class);
        resp.setHtml(content);
        return ResponseEntity.ok(resp);
    }

    /**
     * 获取自动投标列表
     *
     * @param voGetAutoTenderList
     * @return
     * @throws Exception
     */
    public ResponseEntity<VoViewAutoTenderList> getAutoTenderList(VoGetAutoTenderList voGetAutoTenderList) throws Exception {
        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(voGetAutoTenderList.getUserId());
        if (ObjectUtils.isEmpty(userThirdAccount)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR_OPEN_ACCOUNT, "你还没有开通江西银行存管，请前往开通！", VoViewAutoTenderList.class));
        }

        if (userThirdAccount.getPasswordState() != 1) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR_INIT_BANK_PASSWORD, "请初始化江西银行存管账户密码！", VoViewAutoTenderList.class));
        }
/*

        if (userThirdAccount.getAutoTransferState() != 1) {  // 审核
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR_CREDIT_TENDER, "请先签订自动债权转让协议！", VoViewAutoTenderList.class));
        }
*/


        if (!userThirdAccount.getAutoTenderState().equals(1)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR_CREDIT_TENDER, "请先签订自动投标协议！", VoViewAutoTenderList.class));
        }


        VoViewAutoTenderList voViewAutoTenderList = VoBaseResp.ok("查询成功", VoViewAutoTenderList.class);
        List<VoAutoTender> voAutoTenderList = new ArrayList<>();
        int pageIndex = voGetAutoTenderList.getPageIndex();
        int pageSize = voGetAutoTenderList.getPageSize();
        do {
            Specification<AutoTender> ats = Specifications
                    .<AutoTender>and()
                    .eq("userId", voGetAutoTenderList.getUserId())
                    .build();
            Pageable pageable = new PageRequest(pageIndex, pageSize, new Sort(Sort.Direction.ASC, "autoAt"));

            List<AutoTender> autoTenderList = autoTenderService.findList(ats, pageable);
            if (CollectionUtils.isEmpty(autoTenderList)) {
                break;
            }

            voAutoTenderList = gson.fromJson(gson.toJson(autoTenderList), new TypeToken<List<VoAutoTender>>() {
            }.getType());
            if (CollectionUtils.isEmpty(voAutoTenderList)) {
                break;
            }

            Date nowDate = new Date();
            for (VoAutoTender tempVoAutoTender : voAutoTenderList) {
                tempVoAutoTender.setQueueDays(DateHelper.diffInDays(nowDate, tempVoAutoTender.getAutoAt(), true));
            }
        } while (false);
        voViewAutoTenderList.setAutoTenderList(voAutoTenderList);
        return ResponseEntity.ok(voViewAutoTenderList);
    }


    @Override
    public ResponseEntity<VoViewPcAutoTenderWarpRes> pcAutoTenderList(VoGetAutoTenderList voGetAutoTenderList) throws Exception {

        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(voGetAutoTenderList.getUserId());
        if (ObjectUtils.isEmpty(userThirdAccount)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR_OPEN_ACCOUNT, "你还没有开通江西银行存管，请前往开通！", VoViewPcAutoTenderWarpRes.class));
        }

        if (userThirdAccount.getPasswordState() != 1) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR_INIT_BANK_PASSWORD, "请初始化江西银行存管账户密码！", VoViewPcAutoTenderWarpRes.class));
        }

        /*if (userThirdAccount.getAutoTransferState() != 1) {  // 审核
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR_CREDIT_TENDER, "请先签订自动债权转让协议！", VoViewPcAutoTenderWarpRes.class));
        }*/


        if (!userThirdAccount.getAutoTenderState().equals(1)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR_CREDIT_TENDER, "请先签订自动投标协议！", VoViewPcAutoTenderWarpRes.class));
        }


        VoViewPcAutoTenderWarpRes voViewAutoTenderList = VoBaseResp.ok("查询成功", VoViewPcAutoTenderWarpRes.class);
        List<PcAutoTender> pcAutoTenders = new ArrayList<>();

        do {
            Specification<AutoTender> ats = Specifications
                    .<AutoTender>and()
                    .eq("userId", voGetAutoTenderList.getUserId())
                    .build();
            List<AutoTender> autoTenderList = autoTenderService.findList(ats, new Sort(Sort.Direction.DESC, "autoAt"));
            if (CollectionUtils.isEmpty(autoTenderList)) {
                break;
            }

            autoTenderList.stream().forEach(w -> {
                PcAutoTender autoTender = new PcAutoTender();
                autoTender.setId(w.getId());
                autoTender.setAmount(w.getMode());
                autoTender.setTimeLimitType(w.getTimelimitType());
                autoTender.setIsOpen(w.getStatus());
                autoTender.setScope(StringHelper.formatMon(w.getAprFirst() / 100D) + "~" + StringHelper.formatMon(w.getAprLast() / 100D));
                autoTender.setQueueDays(DateHelper.diffInDays(new Date(), w.getAutoAt(), true));


                StringBuffer borrowTypes = new StringBuffer();
                //标的类型
                if (w.getTender0() == 1) {
                    borrowTypes.append(",0");
                }

                if (w.getTender1() == 1) {
                    borrowTypes.append(",1");
                }

                if (w.getTender3() == 1) {
                    borrowTypes.append(",3");
                }

                if (w.getTender4() == 1) {
                    borrowTypes.append(",4");
                }

                if (!StringUtils.isEmpty(borrowTypes)) {
                    autoTender.setBorrowTypes(borrowTypes.toString().substring(1));
                } else {
                    autoTender.setBorrowTypes("");
                }
                autoTender.setOrder(w.getOrder());
                autoTender.setLowest(StringHelper.formatMon(w.getLowest() / 100D));
                pcAutoTenders.add(autoTender);
            });

        } while (false);
        voViewAutoTenderList.setTenderList(pcAutoTenders);
        return ResponseEntity.ok(voViewAutoTenderList);
    }
}
