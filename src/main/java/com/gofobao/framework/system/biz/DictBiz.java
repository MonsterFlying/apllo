package com.gofobao.framework.system.biz;

import com.gofobao.framework.system.vo.response.VoServiceResp;
import org.springframework.http.ResponseEntity;

/**
 * Created by Administrator on 2017/6/20 0020.
 */
public interface DictBiz {


    ResponseEntity<VoServiceResp> service();

}
