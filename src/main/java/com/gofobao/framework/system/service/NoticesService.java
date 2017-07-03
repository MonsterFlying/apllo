package com.gofobao.framework.system.service;

import com.gofobao.framework.system.entity.Notices;
import com.gofobao.framework.system.vo.request.VoNoticesReq;
import com.gofobao.framework.system.vo.response.NoticesInfo;
import com.gofobao.framework.system.vo.response.UserNotices;
import com.gofobao.framework.system.vo.response.VoViewUserNoticesWarpRes;

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

    List<VoViewUserNoticesWarpRes> pcList(VoNoticesReq voNoticesReq);

    /**
     *
     * @param voNoticesReq
     * @return
     */
    NoticesInfo info(VoNoticesReq voNoticesReq);
}
