package com.gofobao.framework.system.service.impl;

import com.gofobao.framework.system.repository.FindRepository;
import com.gofobao.framework.system.service.FindService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FindServiceImpl implements FindService {

    @Autowired
    FindRepository findRepository;

}

