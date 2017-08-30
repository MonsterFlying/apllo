package com.gofobao.framework.financial.service.impl;

import com.gofobao.framework.financial.entity.Eve;
import com.gofobao.framework.financial.repository.EveRepository;
import com.gofobao.framework.financial.service.EveService;
import org.springframework.beans.factory.annotation.Autowired;
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
}
