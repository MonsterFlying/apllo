package com.gofobao.framework.lend.service.impl;

import com.gofobao.framework.lend.entity.Lend;
import com.gofobao.framework.lend.repository.LendRepository;
import com.gofobao.framework.lend.service.LendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

/**
 * Created by Zeke on 2017/6/1.
 */
@Service
public class LendServiceImpl implements LendService {

    @Autowired
    private LendRepository lendRepository;

    public Lend insert(Lend lend) {
        if (ObjectUtils.isEmpty(lend)) {
            return null;
        }
        lend.setId(null);
        return lendRepository.save(lend);
    }

    public boolean updateById(Lend lend) {
        if (ObjectUtils.isEmpty(lend) || ObjectUtils.isEmpty(lend.getId())) {
            return false;
        }
        return !ObjectUtils.isEmpty(lendRepository.save(lend));
    }

    public Lend findByIdLock(Long id){
        return lendRepository.findById(id);
    }

    public Lend findById(Long id){
        return lendRepository.findOne(id);
    }
}
