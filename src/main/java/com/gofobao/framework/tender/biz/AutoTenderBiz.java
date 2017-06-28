package com.gofobao.framework.tender.biz;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.member.vo.response.VoHtmlResp;
import com.gofobao.framework.tender.vo.request.VoDelAutoTenderReq;
import com.gofobao.framework.tender.vo.request.VoGetAutoTenderList;
import com.gofobao.framework.tender.vo.request.VoOpenAutoTenderReq;
import com.gofobao.framework.tender.vo.request.VoSaveAutoTenderReq;
import com.gofobao.framework.tender.vo.response.VoAutoTenderInfo;
import com.gofobao.framework.tender.vo.response.VoViewAutoTenderList;
import com.gofobao.framework.tender.vo.response.VoViewUserAutoTenderWarpRes;
import org.springframework.http.ResponseEntity;

/**
 * Created by Zeke on 2017/5/27.
 */
public interface AutoTenderBiz {
    ResponseEntity<VoViewUserAutoTenderWarpRes> list(Long userId);

    /**
     * 创建自动投标规则
     *
     * @param voSaveAutoTenderReq
     * @return
     */
    ResponseEntity<VoBaseResp> createAutoTender(VoSaveAutoTenderReq voSaveAutoTenderReq);

    /**
     * 创建自动投标规则
     *
     * @param voSaveAutoTenderReq
     * @return
     */
    ResponseEntity<VoBaseResp> updateAutoTender(VoSaveAutoTenderReq voSaveAutoTenderReq);

    /**
     * 查询自动投标详情
     *
     * @param autoTenderId
     * @param userId
     * @return
     */
    ResponseEntity<VoAutoTenderInfo> queryAutoTenderInfo(Long autoTenderId, Long userId);

    /**
     * 开启自动投标
     *
     * @param voOpenAutoTenderReq
     * @return
     */
    ResponseEntity<VoBaseResp> openAutoTender(VoOpenAutoTenderReq voOpenAutoTenderReq);

    /**
     * 删除自动投标跪着
     *
     * @param voDelAutoTenderReq
     * @return
     */
    ResponseEntity<VoBaseResp> delAutoTender(VoDelAutoTenderReq voDelAutoTenderReq);

    /**
     * 自动投标说明
     *
     * @return
     * @throws Exception
     */
    ResponseEntity<VoHtmlResp> autoTenderDesc();

    /**
     * 获取自动投标列表
     *
     * @param voGetAutoTenderList
     * @return
     * @throws Exception
     */
    public ResponseEntity<VoViewAutoTenderList> getAutoTenderList(VoGetAutoTenderList voGetAutoTenderList) throws Exception;
}
