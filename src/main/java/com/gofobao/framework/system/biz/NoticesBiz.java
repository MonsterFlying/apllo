package com.gofobao.framework.system.biz;

import com.gofobao.framework.system.entity.Notices;
import com.gofobao.framework.system.vo.request.VoNoticesReq;
import com.gofobao.framework.system.vo.response.VoViewNoticesInfoWarpRes;
import com.gofobao.framework.system.vo.response.VoViewUserNoticesWarpRes;
import org.springframework.http.ResponseEntity;

/**
 * Created by Max on 17/6/5.
 */
public interface NoticesBiz {

    /**
     * 保存站内信
     * @param notices
     * @return
     */
    boolean save(Notices notices);

    /**
     * 站内信列表
     * @param voNoticesReq
     * @return
     */
    ResponseEntity<VoViewUserNoticesWarpRes> list(VoNoticesReq voNoticesReq);


    /**
     * 站内信内容
     * @param voNoticesReq
     * @return
     */
    ResponseEntity<VoViewNoticesInfoWarpRes> info(VoNoticesReq voNoticesReq);
}
