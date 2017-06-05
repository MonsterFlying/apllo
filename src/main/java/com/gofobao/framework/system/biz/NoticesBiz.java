package com.gofobao.framework.system.biz;

import com.gofobao.framework.system.entity.Notices;

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
}
