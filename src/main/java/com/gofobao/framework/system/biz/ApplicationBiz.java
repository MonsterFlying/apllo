package com.gofobao.framework.system.biz;

import com.gofobao.framework.system.entity.Application;
import com.gofobao.framework.system.vo.response.ApplicationWarpRes;
import org.springframework.http.ResponseEntity;

/**
 * Created by master on 2017/10/23.
 */
public interface ApplicationBiz {

        ResponseEntity<ApplicationWarpRes> list(Application application);


}
