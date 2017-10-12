package com.gofobao.framework.financial.service.impl;

import com.gofobao.framework.financial.entity.JixinAsset;
import com.gofobao.framework.financial.repository.JixinAssetRepository;
import com.gofobao.framework.financial.service.JixinAssetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class JixinAssetServiceImpl implements JixinAssetService {

    @Autowired
    JixinAssetRepository jixinAssetRepository;

    @Override
    public JixinAsset findTopByAccountId(String cardnbr) {
        return jixinAssetRepository.findTopByAccountId(cardnbr);
    }

    @Override
    public JixinAsset save(JixinAsset jixinAsset) {
        return jixinAssetRepository.save(jixinAsset);
    }

    @Override
    public Page<Object[]> findAllForPrint(Pageable pageable) {

        return jixinAssetRepository.findAllForPrint(pageable) ;
    }
}
