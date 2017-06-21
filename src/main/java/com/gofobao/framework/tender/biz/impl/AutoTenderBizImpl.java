package com.gofobao.framework.tender.biz.impl;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.BeanHelper;
import com.gofobao.framework.helper.MathHelper;
import com.gofobao.framework.tender.biz.AutoTenderBiz;
import com.gofobao.framework.tender.entity.AutoTender;
import com.gofobao.framework.tender.service.AutoTenderService;
import com.gofobao.framework.tender.vo.request.VoDelAutoTenderReq;
import com.gofobao.framework.tender.vo.request.VoOpenAutoTenderReq;
import com.gofobao.framework.tender.vo.request.VoSaveAutoTenderReq;
import com.gofobao.framework.tender.vo.response.UserAutoTender;
import com.gofobao.framework.tender.vo.response.VoViewUserAutoTenderWarpRes;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * Created by Zeke on 2017/5/27.
 */
@Service
public class AutoTenderBizImpl implements AutoTenderBiz {
    @Autowired
    private AutoTenderService autoTenderService;

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

        /*Date nowDate = new Date();
        AutoTender autoTender = getSaveAutoTender(voSaveAutoTender);
        autoTender.setOrder(autoTenderMapper.getOrderNum() + 1);
        autoTender.setCreatedAt(nowDate);
        autoTender.setUpdatedAt(nowDate);
        autoTender.setAutoAt(nowDate);
        autoTender.setUserId(voSaveAutoTender.getUserId());

        if (autoTenderMapper.insertSelective(autoTender) > 0) {
            rs = 0;
        }*/
        return null;
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

        return ResponseEntity.ok(VoBaseResp.ok("更新自动投标规则！"));
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
}
