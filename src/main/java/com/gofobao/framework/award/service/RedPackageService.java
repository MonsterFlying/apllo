package com.gofobao.framework.award.service;

import com.gofobao.framework.award.entity.ActivityRedPacket;
import com.gofobao.framework.award.vo.request.VoOpenRedPackageReq;
import com.gofobao.framework.award.vo.request.VoRedPackageReq;
import com.gofobao.framework.award.vo.response.RedPackageRes;

import java.util.List;

/**
 * Created by admin on 2017/6/7.
 */
public interface RedPackageService {

    /**
     * 红包列表
     * @param voRedPackageReq
     * @return
     */
    List<RedPackageRes> list(VoRedPackageReq voRedPackageReq);

    /**
     * 拆红包
     * @param voOpenRedPackageReq
     * @return
     */
    List<ActivityRedPacket> openRedPackage(VoOpenRedPackageReq voOpenRedPackageReq);
}
