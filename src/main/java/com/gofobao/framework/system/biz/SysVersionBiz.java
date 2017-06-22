package com.gofobao.framework.system.biz;

import com.gofobao.framework.core.vo.VoBaseResp;
import org.springframework.http.ResponseEntity;

/**
 * Created by admin on 2017/6/21.
 */

public interface SysVersionBiz {


     ResponseEntity<VoBaseResp> list(Integer terminal,Integer clientId);
}
