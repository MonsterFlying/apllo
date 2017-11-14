package com.gofobao.framework.comment.service.impl;

import com.gofobao.framework.comment.entity.TopicTopRecord;
import com.gofobao.framework.comment.repository.TopicTopRecordRepository;
import com.gofobao.framework.comment.service.TopicTopRecordService;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TopicTopRecordServiceImpl implements TopicTopRecordService {

    @Autowired
    TopicTopRecordRepository topicTopRecordRepository;

    @Override
    public List<TopicTopRecord> findByUserIdAndSourceIdAndSourceType(Long userId, Long soucreId, Integer sourceType) {
        return topicTopRecordRepository.findByUserIdAndSourceIdAndSourceType(userId, soucreId, sourceType);
    }

    @Override
    public void delete(List<TopicTopRecord> topicTopRecordList) {
        topicTopRecordRepository.deleteInBatch(topicTopRecordList);
    }

    @Override
    public TopicTopRecord save(TopicTopRecord topicTopRecord) {
        return topicTopRecordRepository.save(topicTopRecord);
    }

    @Override
    public List<TopicTopRecord> find(Specification<TopicTopRecord> specification) {
        Optional<List<TopicTopRecord>> optional = Optional.ofNullable(topicTopRecordRepository.findAll(specification));
        return optional.orElse(Lists.newArrayList());
    }
}
