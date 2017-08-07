package com.gofobao.framework.windmill.borrow.biz;

import com.gofobao.framework.windmill.borrow.vo.response.BackRecordsRes;
import com.gofobao.framework.windmill.borrow.vo.response.InvestRecordsRes;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Created by admin on 2017/8/4.
 */
public interface WindmillTenderBiz {
    /**
     * 5.6投资记录查询接口
     * @param request
     * @return
     */
    InvestRecordsRes investRecordList(HttpServletRequest request);


    /**
     * 5.7投资记录回款计划
     * @param request
     * @return
     */
    BackRecordsRes backRecordList(HttpServletRequest request);


    /**
     * 4.1投资通知
     */
    void tenderNotify(Map<String,String>paramMap);


    /**
     * 4.1投资通知
     */
    void  backMoneyNotify(Map<String,String>paramMap);

}
