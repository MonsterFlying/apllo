package com.gofobao.framework.asset.service.impl;

import com.gofobao.framework.asset.entity.Area;
import com.gofobao.framework.asset.repository.AreaRepository;
import com.gofobao.framework.asset.service.AreaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by admin on 2017/8/19.
 */
@Component
public class AreaServiceImpl implements AreaService {

    @Autowired
    private AreaRepository areaRepository;

    @Override
    public List<Area> findAll() {

        return areaRepository.findAll();
    }
}
