package com.gofobao.framework.windmill.borrow.service;

import com.gofobao.framework.windmill.borrow.vo.request.UserTenderLogReq;

import java.util.Map;

/**
 * Created by admin on 2017/8/4.
 */
public interface WindmillTenderService {


    /**
     * 5.6投资记录查询接口
     */
    Map<String, Object>userTenderLog(UserTenderLogReq tenderLogReq);


}
