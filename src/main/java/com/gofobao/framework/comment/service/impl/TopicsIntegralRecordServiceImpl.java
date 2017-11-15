package com.gofobao.framework.comment.service.impl;

import com.gofobao.framework.comment.repository.TopicsIntegralRecordRepository;
import com.gofobao.framework.comment.service.TopicsIntegralRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TopicsIntegralRecordServiceImpl implements TopicsIntegralRecordService {
    @Autowired
    TopicsIntegralRecordRepository topicsIntegralRecordRepository ;
}
