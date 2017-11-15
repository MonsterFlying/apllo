package com.gofobao.framework.comment.service.impl;

import com.gofobao.framework.comment.entity.TopicsIntegralRecord;
import com.gofobao.framework.comment.repository.TopicsIntegralRecordRepository;
import com.gofobao.framework.comment.service.TopicsIntegralRecordService;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TopicsIntegralRecordServiceImpl implements TopicsIntegralRecordService {
    @Autowired
    TopicsIntegralRecordRepository topicsIntegralRecordRepository;

    @Override
    public List<TopicsIntegralRecord> findAll(Specification<TopicsIntegralRecord> topicsIntegralRecordSpecification, Pageable pageable) {
        Page<TopicsIntegralRecord> page = topicsIntegralRecordRepository.findAll(topicsIntegralRecordSpecification, pageable);
        Optional<List<TopicsIntegralRecord>> optional = Optional.ofNullable(page.getContent());
        return optional.orElse(Lists.newArrayList());
    }
}
