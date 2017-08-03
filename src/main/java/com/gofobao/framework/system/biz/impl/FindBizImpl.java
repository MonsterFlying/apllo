package com.gofobao.framework.system.biz.impl;

import com.gofobao.framework.system.biz.FindBiz;
import com.gofobao.framework.system.service.FindService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FindBizImpl implements FindBiz {

    @Autowired
    FindService findService;
}
