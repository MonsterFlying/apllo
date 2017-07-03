package com.gofobao.framework.system.service;

import com.gofobao.framework.system.entity.Notices;
import com.gofobao.framework.system.vo.request.VoNoticesReq;
import com.gofobao.framework.system.vo.request.VoNoticesTranReq;
import com.gofobao.framework.system.vo.response.NoticesInfo;
import com.gofobao.framework.system.vo.response.UserNotices;
import com.gofobao.framework.system.vo.response.VoViewUserNoticesWarpRes;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * Created by Max on 17/6/5.
 */
public interface NoticesService {

    void save(Notices notices);


    /**
     * 站内信列表
     * @param voNoticesReq
     * @return
     */
    List<UserNotices> list(VoNoticesReq voNoticesReq);



    /**
     *
     * @param voNoticesReq
     * @return
     */
    NoticesInfo info(VoNoticesReq voNoticesReq);


    /**
     *  批量删除
     *
     * @param voNoticesTranReq
     * @return
     */
    boolean  delete(VoNoticesTranReq voNoticesTranReq);

    /**
     *   批量更新
     * @param voNoticesTranReq
     * @return
     */
    boolean update(VoNoticesTranReq voNoticesTranReq);


}
