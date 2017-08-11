package com.gofobao.framework.marketing.service.impl;

import com.gofobao.framework.marketing.repository.MarketingRedpackRecordRepository;
import com.gofobao.framework.marketing.service.MarketingRedpackRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MarketingRedpackRecordServiceImpl implements MarketingRedpackRecordService {
    @Autowired
    MarketingRedpackRecordRepository marketingRedpackRecordRepository;
}
