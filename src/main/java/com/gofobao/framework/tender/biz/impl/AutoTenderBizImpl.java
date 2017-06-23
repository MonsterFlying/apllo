package com.gofobao.framework.tender.biz.impl;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.common.response.CommonRespMsgStatesContants;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.*;
import com.gofobao.framework.member.vo.response.VoHtmlResp;
import com.gofobao.framework.tender.biz.AutoTenderBiz;
import com.gofobao.framework.tender.contants.AutoTenderContants;
import com.gofobao.framework.tender.contants.BorrowContants;
import com.gofobao.framework.tender.entity.AutoTender;
import com.gofobao.framework.tender.service.AutoTenderService;
import com.gofobao.framework.tender.vo.request.VoDelAutoTenderReq;
import com.gofobao.framework.tender.vo.request.VoOpenAutoTenderReq;
import com.gofobao.framework.tender.vo.request.VoSaveAutoTenderReq;
import com.gofobao.framework.tender.vo.response.UserAutoTender;
import com.gofobao.framework.tender.vo.response.VoAutoTenderInfo;
import com.gofobao.framework.tender.vo.response.VoViewUserAutoTenderWarpRes;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.*;

/**
 * Created by Zeke on 2017/5/27.
 */
@Service
public class AutoTenderBizImpl implements AutoTenderBiz {
    @Autowired
    private AutoTenderService autoTenderService;
    @Autowired
    private ThymeleafHelper thymeleafHelper;

    @Override
    public ResponseEntity<VoViewUserAutoTenderWarpRes> list(Long userId) {
        try {
            VoViewUserAutoTenderWarpRes warpRes = VoBaseResp.ok("查询成功", VoViewUserAutoTenderWarpRes.class);
            List<UserAutoTender> autoTenderList = autoTenderService.list(userId);
            warpRes.setTenderList(autoTenderList);
            return ResponseEntity.ok(warpRes);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "", VoViewUserAutoTenderWarpRes.class));
        }

    }

    /**
     * 创建自动投标规则
     *
     * @param voSaveAutoTenderReq
     * @return
     */
    public ResponseEntity<VoBaseResp> createAutoTender(VoSaveAutoTenderReq voSaveAutoTenderReq) {
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
            AutoTender autoTender = autoTenderList.get(0);
            autoTender.setUpdatedAt(new Date());
            BeanHelper.copyParamter(voSaveAutoTenderReq, autoTender, true);
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

        Double tenderMoney = voSaveAutoTenderReq.getTenderMoney();
        if (ObjectUtils.isEmpty(tenderMoney)) {
            voSaveAutoTenderReq.setMode(0);
            voSaveAutoTenderReq.setTenderMoney(0d);
        } else {
            voSaveAutoTenderReq.setMode(1);
            voSaveAutoTenderReq.setTenderMoney(tenderMoney);
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
        voAutoTenderInfo.setLowest(NumberHelper.toDouble(autoTender.getLowest()));
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

        voAutoTenderInfo.setSaveMoney(NumberHelper.toDouble(autoTender.getSaveMoney()));
        voAutoTenderInfo.setStatus(autoTender.getStatus());
        voAutoTenderInfo.setTenderMoney(NumberHelper.toDouble(autoTender.getTenderMoney()));
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
        String content = thymeleafHelper.build("tender/autoTender", paranMap);
        VoHtmlResp resp = VoHtmlResp.ok("获取成功!", VoHtmlResp.class);
        resp.setHtml(Base64Utils.encodeToString(content.getBytes()));
        return ResponseEntity.ok(resp);
    }
}
