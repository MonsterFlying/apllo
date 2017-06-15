package com.gofobao.framework.system.service;

import com.gofobao.framework.system.entity.Notices;
import com.gofobao.framework.system.vo.request.VoNoticesReq;
import com.gofobao.framework.system.vo.response.NoticesInfo;
import com.gofobao.framework.system.vo.response.UserNotices;

import java.util.List;

/**
 * Created by Max on 17/6/5.
 */
public interface NoticesService {

    void save(Notices notices);


    List<UserNotices> list(VoNoticesReq voNoticesReq);


    NoticesInfo info(VoNoticesReq voNoticesReq);
}
