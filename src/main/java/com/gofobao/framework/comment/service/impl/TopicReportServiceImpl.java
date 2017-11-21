package com.gofobao.framework.comment.service.impl;

import com.gofobao.framework.comment.entity.TopicReport;
import com.gofobao.framework.comment.repository.TopicReportRepository;
import com.gofobao.framework.comment.service.TopicReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TopicReportServiceImpl implements TopicReportService {

    @Autowired
    TopicReportRepository topicReportRepository;

    @Override
    public TopicReport findTopByUserIdOrderByIdDesc(Long userId) {
        return topicReportRepository.findTopByUserIdOrderByIdDesc(userId);
    }

    @Override
    public List<TopicReport> findByUserIdAndSourceIdAndSourceType(Long userId, Long soucreId, Integer sourceType) {
        return topicReportRepository.findByUserIdAndSourceIdAndSourceType(userId, soucreId, sourceType);
    }

    @Override
    public TopicReport save(TopicReport topicReport) {
        return topicReportRepository.save(topicReport);
    }
}
