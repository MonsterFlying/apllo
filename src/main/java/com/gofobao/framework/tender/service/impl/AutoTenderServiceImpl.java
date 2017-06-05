package com.gofobao.framework.tender.service.impl;

import com.github.pagehelper.PageHelper;
import com.gofobao.framework.helper.BeanHelper;
import com.gofobao.framework.tender.entity.AutoTender;
import com.gofobao.framework.tender.repository.AutoTenderRepository;
import com.gofobao.framework.tender.service.AutoTenderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.PluralJoin;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Created by Zeke on 2017/5/27.
 */
@Service
public class AutoTenderServiceImpl implements AutoTenderService {

    @Autowired
    private AutoTenderRepository autoTenderRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public boolean insert(AutoTender autoTender) {
        if (ObjectUtils.isEmpty(autoTender)) {
            return false;
        }
        autoTender.setId(null);
        return !ObjectUtils.isEmpty(autoTenderRepository.save(autoTender));
    }

    public boolean updateById(AutoTender autoTender) {
        if (ObjectUtils.isEmpty(autoTender) || ObjectUtils.isEmpty(autoTender.getId())) {
            return false;
        }
        return !ObjectUtils.isEmpty(autoTenderRepository.save(autoTender));
    }

    public boolean updateByExample(AutoTender autoTender, Example<AutoTender> example) {
        if (ObjectUtils.isEmpty(autoTender) || ObjectUtils.isEmpty(example.getProbe())) {
            return false;
        }

        List<AutoTender> autoTenderList = autoTenderRepository.findAll(example);
        List<AutoTender> updAutoTenders = new ArrayList<>();

        Optional<List<AutoTender>> autoTenderOptions = Optional.ofNullable(autoTenderList);
        autoTenderOptions.ifPresent(o -> o.forEach(temp -> {
            BeanHelper.copyParamter(autoTender, temp, true);
            updAutoTenders.add(temp);//更新对象
        }));
        return CollectionUtils.isEmpty(autoTenderRepository.save(updAutoTenders));
    }

    public List<AutoTender> findT(int pageIndex,int pageSize) {
        StringBuffer sql = new StringBuffer("select t  from gfb_auto_tender t left join gfb_asset a on t.user_id = a.user_id where 1=1");
        Query query = entityManager.createNativeQuery(sql.toString(),AutoTender.class);
        List<AutoTender> autoTenderList = query.getResultList();
        PageHelper.startPage(pageIndex,pageSize);
        return null;
    }
}
