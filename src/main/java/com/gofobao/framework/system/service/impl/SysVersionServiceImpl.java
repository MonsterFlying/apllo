package com.gofobao.framework.system.service.impl;

import com.gofobao.framework.system.entity.SysVersion;
import com.gofobao.framework.system.repository.SysVersionRepository;
import com.gofobao.framework.system.service.SysVersionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;

/**
 * Created by admin on 2017/6/21.
 */
@Component
public class SysVersionServiceImpl implements SysVersionService {

    @Autowired
    private SysVersionRepository sysVersionRepository;

    @Override
    public List<SysVersion> list(Integer terminal) {
        List<SysVersion>sysVersions=sysVersionRepository.findByTerminal(terminal);
        if(CollectionUtils.isEmpty(sysVersions)){
            return Collections.EMPTY_LIST;
        }
        return sysVersions;
    }
}
