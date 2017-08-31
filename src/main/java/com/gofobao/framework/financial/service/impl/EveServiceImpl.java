package com.gofobao.framework.financial.service.impl;

import com.gofobao.framework.financial.entity.Eve;
import com.gofobao.framework.financial.repository.EveRepository;
import com.gofobao.framework.financial.service.EveService;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EveServiceImpl implements EveService {
    @Autowired
    EveRepository eveRepository ;

    @Override
    public List<Eve> findByRetseqnoAndSeqno(String retseqno, String seqno) {
        return eveRepository.findByRetseqnoAndSeqno(retseqno, seqno) ;
    }

    @Override
    public Eve save(Eve eve) {
        return eveRepository.save(eve) ;
    }

    @Override
    public List<Eve> findList(Specification<Eve> specification, Pageable pageable) {
        Page<Eve> page = eveRepository.findAll(specification, pageable);
        return Optional.fromNullable(page.getContent()).or(Lists.newArrayList());
    }
}
