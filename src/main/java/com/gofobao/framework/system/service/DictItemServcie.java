package com.gofobao.framework.system.service;

import com.gofobao.framework.system.entity.DictItem;

/**
 * Created by Max on 17/6/6.
 */
public interface DictItemServcie {

    DictItem findTopByAliasCodeAndDel(String aliasCode, int del);
}
