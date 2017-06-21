package com.gofobao.framework.system.service;

import com.gofobao.framework.system.entity.SysVersion;

import java.util.List;

/**
 * Created by admin on 2017/6/21.
 */
public interface SysVersionService {

    List<SysVersion> list(Integer terminal);
}
