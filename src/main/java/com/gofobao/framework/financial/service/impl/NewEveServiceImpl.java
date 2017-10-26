package com.gofobao.framework.financial.service.impl;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.financial.entity.NewEve;
import com.gofobao.framework.financial.repository.NewEveRepository;
import com.gofobao.framework.financial.service.NewEveService;
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
public class NewEveServiceImpl implements NewEveService {

    @Autowired
    NewEveRepository newEveRepository;

    @Override
    public NewEve findTopByOrderno(String orderno) {
        return newEveRepository.findTopByOrderno(orderno);
    }

    @Override
    public NewEve save(NewEve newEve) {
        return newEveRepository.save(newEve);
    }

    @Override
    public long countByTranstypeAndQueryTime(String transtype, String date) {
        return newEveRepository.countByTranstypeAndQueryTime(transtype, date);
    }

    @Override
    public NewEve findTopByOrdernoAndQueryTime(String orderno, String date) {
        return newEveRepository.findTopByOrdernoAndQueryTime(orderno, date);
    }

    @Override
    public NewEve findTopByCendtAndTranno(String cendt, String tranno) {
        return newEveRepository.findTopByCendtAndTranno(cendt, tranno);
    }

    @Override
    public List<NewEve> findByTranstypeAndQueryTime(String transtype, String date, Pageable pageable) {
        return newEveRepository.findByTranstypeAndQueryTime(transtype, date, pageable);
    }

    @Override
    public List<NewEve> findAll(Specification<NewEve> specification) {
        return newEveRepository.findAll(specification);
    }

    @Override
    public Page<Object[]> findLocalAssetChangeRecord(String beginDate, String endDate, Pageable pageable) {
        return newEveRepository.findByCreateTime(beginDate, endDate, pageable);
    }

    @Override
    public Page<Object[]> findRemoteByQueryTime(String date, Pageable pageable) {
        return newEveRepository.findRemoteByQueryTime(date, pageable);
    }

    @Override
    public List<NewEve> findAllByTranTypeAndDateAndUserId(String type, Long userId, Date date) throws Exception {
        Date nowDate = new Date();
        if (DateHelper.diffInDays(nowDate, DateHelper.beginOfDate(date), false) != 0) {
            // 此处隔天, 直接查询eve对账文件
            Specification<NewEve> newEveSpecification = Specifications
                    .<NewEve>and()
                    .eq("cardnbr", userId)
                    .eq("transtype", type)
                    .eq("queryTime", DateHelper.dateToString(date, DateHelper.DATE_FORMAT_YMD_NUM))
                    .build();
            List<NewEve> eveLists = this.findAll(newEveSpecification);
            Optional<List<NewEve>> result = Optional.ofNullable(eveLists);
            return result.orElse(Lists.newArrayList());
        } else {
            throw new Exception("eve 只能查询隔天即信流水记录");
        }
    }
}
