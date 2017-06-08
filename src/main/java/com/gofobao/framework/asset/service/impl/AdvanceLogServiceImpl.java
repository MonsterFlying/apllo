package com.gofobao.framework.asset.service.impl;

import com.gofobao.framework.asset.entity.AdvanceLog;
import com.gofobao.framework.asset.repository.AdvanceLogRepository;
import com.gofobao.framework.asset.service.AdvanceLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.List;

/**
 * Created by Zeke on 2017/6/7.
 */
@Service
public class AdvanceLogServiceImpl implements AdvanceLogService{

    @Autowired
    private AdvanceLogRepository advanceLogRepository;

    public boolean insert(AdvanceLog advanceLog){
        if (ObjectUtils.isEmpty(advanceLog)){
            return false;
        }
        advanceLog.setId(null);
        advanceLogRepository.save(advanceLog);
        return true;
    }

    public boolean updateById(AdvanceLog advanceLog){
        if (ObjectUtils.isEmpty(advanceLog) || ObjectUtils.isEmpty(advanceLog.getId())){
            return false;
        }
        advanceLogRepository.save(advanceLog);
        return true;
    }

    public AdvanceLog findById(Long id){
        return advanceLogRepository.findOne(id);
    }

    public AdvanceLog findByIdLock(Long id){
        return advanceLogRepository.findById(id);
    }

    public List<AdvanceLog> findList(Specification<AdvanceLog> specification){
        return advanceLogRepository.findAll(specification);
    }

    public List<AdvanceLog> findList(Specification<AdvanceLog> specification, Sort sort){
        return advanceLogRepository.findAll(specification,sort);
    }

    public List<AdvanceLog> findList(Specification<AdvanceLog> specification, Pageable pageable){
        return advanceLogRepository.findAll(specification,pageable).getContent();
    }

    public long count(Specification<AdvanceLog> specification){
        return count(specification);
    }
}
