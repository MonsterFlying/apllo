package com.gofobao.framework.system.service;

import java.util.List;
import java.util.Map;

/**
 * 字典类
 * Created by Max on 17/3/1.
 */
public interface DictService {

    /**
     * 查找数据字典
     *
     * @param aliasCode 数据项代码
     * @return
     */
    List<Map<String, String>> queryDictList(String aliasCode) throws Exception;
}
