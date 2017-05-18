package com.gofobao.framework.system.biz;

import com.gofobao.framework.system.vo.VoCaptchaImageResp;
import org.springframework.http.ResponseEntity;

/**
 * 图形工具类
 * Created by Max on 17/5/18.
 */
public interface CaptchaBiz {

    ResponseEntity<VoCaptchaImageResp> drawImageByAnon();
}
