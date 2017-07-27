package com.gofobao.framework.common.integral;

import lombok.Data;

/**
 * Created by Zeke on 2017/6/7.
 */
@Data
public class IntegralChangeEntity {
    /**
     * 用户id
     */
    private Long userId;
    /**
     * 变动积分
     */
    private Long value;
    /**
     * 积分表东类型
     */
    private IntegralChangeEnum type;
}
