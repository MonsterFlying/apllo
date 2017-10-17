package com.gofobao.framework.award.biz;

import com.gofobao.framework.award.vo.request.VoOpenRedPackageReq;
import com.gofobao.framework.award.vo.request.VoRedPackageReq;
import com.gofobao.framework.award.vo.response.VoViewOpenRedPackageWarpRes;
import com.gofobao.framework.award.vo.response.VoViewRedPackageWarpRes;
import com.gofobao.framework.common.assets.AssetChangeTypeEnum;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.tender.vo.request.VoPublishRedReq;
import org.springframework.http.ResponseEntity;

/**
 * Created by admin on 2017/6/7.
 */
public interface RedPackageBiz {
    /**
     * 派发
     *
     * @param userId              派发用户ID
     * @param money               用户金额(必须是分)
     * @param assetChangeTypeEnum 红包派发类型
     * @param onlyNo              唯一标识红包是否派发(可以判断是否重复派发)
     * @param remark              领取红包记录备注
     * @param sourceId            来源id, 不能为空
     * @return
     * @throws Exception
     */
    boolean commonPublishRedpack(Long userId, long money, AssetChangeTypeEnum assetChangeTypeEnum, String onlyNo, String remark, long sourceId) throws Exception ;
    /**
     *红包列表
     * @param voRedPackageReq
     * @return
     */
    ResponseEntity<VoViewRedPackageWarpRes> list(VoRedPackageReq voRedPackageReq);

    /**
     * 拆红包
     * @param voOpenRedPackageReq
     * @return
     */
    ResponseEntity<VoViewOpenRedPackageWarpRes> openRedPackage(VoOpenRedPackageReq voOpenRedPackageReq) throws Exception;


    /**
     * 后台补发红包
     * @param voPublishRedReq
     * @return
     */
    ResponseEntity<VoBaseResp> publishActivity(VoPublishRedReq voPublishRedReq) throws Exception;


    ResponseEntity<VoBaseResp> publishOpenAccountRedpack(VoPublishRedReq voPublishRedReq);


    /**
     * 根据投标记录
     * @param voPublishRedReq
     * @return
     */
    ResponseEntity<VoBaseResp> publishRedpack4TenderRecord(VoPublishRedReq voPublishRedReq);
}
