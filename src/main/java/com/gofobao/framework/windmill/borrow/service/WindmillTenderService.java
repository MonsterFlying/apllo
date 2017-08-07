package com.gofobao.framework.windmill.borrow.service;

import com.gofobao.framework.collection.entity.BorrowCollection;
import com.gofobao.framework.tender.entity.Tender;
import com.gofobao.framework.windmill.borrow.vo.request.BackRecordsReq;
import com.gofobao.framework.windmill.borrow.vo.request.UserTenderLogReq;

import java.util.List;

/**
 * Created by admin on 2017/8/4.
 */
public interface WindmillTenderService {


    /**
     * 5.6投资记录查询接口
     */
    List<Tender> userTenderLog(UserTenderLogReq tenderLogReq);


    /**
     * 5.7投资记录回款计划
     * @param backRecordsReq
     * @return
     */
    List<BorrowCollection>backCollectionList(BackRecordsReq backRecordsReq);

}
