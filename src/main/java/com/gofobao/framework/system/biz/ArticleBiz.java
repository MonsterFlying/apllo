package com.gofobao.framework.system.biz;

import com.gofobao.framework.system.vo.request.VoArticleReq;
import com.gofobao.framework.system.vo.response.VoViewArticleInfoWarpRes;
import com.gofobao.framework.system.vo.response.VoViewArticleWarpRes;
import org.springframework.http.ResponseEntity;

/**
 * Created by admin on 2017/7/7.
 */
public interface ArticleBiz {
    ResponseEntity<VoViewArticleWarpRes> list(VoArticleReq voArticleReq);

    ResponseEntity<VoViewArticleInfoWarpRes> info(Long id);

}
