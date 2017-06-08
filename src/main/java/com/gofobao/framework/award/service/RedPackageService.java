package com.gofobao.framework.award.service;

import com.gofobao.framework.award.vo.request.VoRedPackageReq;
import com.gofobao.framework.award.vo.response.RedPackageRes;

import java.util.List;

/**
 * Created by admin on 2017/6/7.
 */
public interface RedPackageService {


    List<RedPackageRes> list(VoRedPackageReq voRedPackageReq);
}
