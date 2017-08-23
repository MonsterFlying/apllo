package com.gofobao.framework.common.qiniu.storage;

import com.gofobao.framework.common.qiniu.http.Response;

/**
 * 定义了文件上传结束回调接口
 */
public interface UpCompletionHandler {
    void complete(String key, Response r);
}
