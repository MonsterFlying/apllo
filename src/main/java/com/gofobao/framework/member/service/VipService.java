package com.gofobao.framework.member.service;

import com.gofobao.framework.member.entity.Vip;

/**
 * Created by Administrator on 2017/6/16 0016.
 */
public interface VipService {
    Vip findTopByUserIdAndStatus(Long userId, int status);

    Boolean save(Vip vip);

}
