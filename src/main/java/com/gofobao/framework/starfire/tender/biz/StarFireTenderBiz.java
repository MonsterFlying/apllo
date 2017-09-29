package com.gofobao.framework.starfire.tender.biz;

import com.gofobao.framework.starfire.tender.vo.request.BorrowCollectionRecords;
import com.gofobao.framework.starfire.tender.vo.request.UserTenderQuery;
import com.gofobao.framework.starfire.tender.vo.response.UserBorrowCollectionRecordsRes;
import com.gofobao.framework.starfire.tender.vo.response.UserTenderRes;

/**
 * Created by master on 2017/9/28.
 */
public interface StarFireTenderBiz {


    /**
     *用户投资记录查询接口
     * @param userTenderQuery
     * @return
     */
    UserTenderRes userTenderList(UserTenderQuery userTenderQuery);

    /**
     *标的回款信息查询接
     * @param borrowCollectionRecords
     * @return
     */
    UserBorrowCollectionRecordsRes borrowCollections(BorrowCollectionRecords borrowCollectionRecords);



}
