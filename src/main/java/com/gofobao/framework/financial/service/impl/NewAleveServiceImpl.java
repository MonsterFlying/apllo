package com.gofobao.framework.financial.service.impl;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.financial.entity.NewAleve;
import com.gofobao.framework.financial.entity.NewEve;
import com.gofobao.framework.financial.repository.NewAleveRepository;
import com.gofobao.framework.financial.service.NewAleveService;
import com.gofobao.framework.helper.DateHelper;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class NewAleveServiceImpl implements NewAleveService {
    @Autowired
    NewAleveRepository newAleveRepository;


    @Override
    public NewAleve findTopByQueryTimeAndTranno(String date, String tranno) {
        return newAleveRepository.findTopByQueryTimeAndTranno(date, tranno);
    }

    @Override
    public NewAleve save(NewAleve newAleve) {
        return newAleveRepository.save(newAleve);
    }

    @Override
    public Long count(Specification<NewAleve> specification) {
        return newAleveRepository.count(specification);
    }

    @Override
    public Page<NewAleve> findAll(Specification<NewAleve> specification, Pageable pageable) {
        return newAleveRepository.findAll(specification, pageable);
    }

    @Override
    public NewAleve findTopByReldateAndInptimeAndTranno(String reldate, String inptime, String tranno) {
        return newAleveRepository.findTopByReldateAndInptimeAndTranno(reldate, inptime, tranno);
    }

    @Override
    public List<NewAleve> findAll(Specification<NewAleve> specification) {
        return newAleveRepository.findAll(specification);
    }

    @Override
    public List<NewAleve> findAllByTranTypeAndDateAndAccountId(String type, String accountId, Date date) throws Exception {
        Date nowDate = new Date();
        if (DateHelper.diffInDays(nowDate, date, false) != 0) {
            // 此处隔天, 直接查询aleve对账文件
            Specification<NewAleve> newEveSpecification = Specifications
                    .<NewAleve>and()
                    .eq("cardnbr", accountId)
                    .eq("transtype", type)
                    .eq("queryTime", DateHelper.dateToString(date, DateHelper.DATE_FORMAT_YMD_NUM))
                    .build();
            List<NewAleve> aleveLists = this.findAll(newEveSpecification);
            Optional<List<NewAleve>> result = Optional.ofNullable(aleveLists);
            return result.orElse(Lists.newArrayList());
        } else {
            throw new Exception("aleve 只能查询隔天即信流水记录");
        }
    }
}
