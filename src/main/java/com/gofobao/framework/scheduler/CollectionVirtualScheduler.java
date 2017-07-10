package com.gofobao.framework.scheduler;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.award.service.VirtualService;
import com.gofobao.framework.collection.entity.VirtualCollection;
import com.gofobao.framework.common.capital.CapitalChangeEntity;
import com.gofobao.framework.common.capital.CapitalChangeEnum;
import com.gofobao.framework.common.data.DataObject;
import com.gofobao.framework.common.data.LeSpecification;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.NumberHelper;
import com.gofobao.framework.helper.project.CapitalChangeHelper;
import com.gofobao.framework.tender.entity.Tender;
import com.gofobao.framework.tender.service.TenderService;
import com.sun.deploy.security.CachedCertificatesHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Zeke on 2017/7/10.
 */
@Component
@Slf4j
public class CollectionVirtualScheduler {

    @Autowired
    private VirtualService virtualService;
    @Autowired
    private TenderService tenderService;
    @Autowired
    private CapitalChangeHelper capitalChangeHelper;

    public void process() {
        do {
            if (NumberHelper.toInt(DateHelper.dateToString(new Date(), "HH")) < 20) {
                break;
            }

            Specification<VirtualCollection> vcs = Specifications
                    .<VirtualCollection>and()
                    .predicate(new LeSpecification("collectionAt", new DataObject(DateHelper.endOfDate(new Date()))))
                    .build();

            Specification<Tender> ts = null;
            Pageable pageable = null;
            List<Long> tenderIds = null;
            int pageIndex = 0;
            int pageSize = 50;
            List<VirtualCollection> virtualCollectionList = null;
            List<Tender> tenderList = null;
            CapitalChangeEntity entity = null;
            do {
                tenderIds = new ArrayList<>();
                pageable = new PageRequest(pageIndex++, pageSize, new Sort(Sort.Direction.ASC, "id"));

                virtualCollectionList = virtualService.findList(vcs, pageable);
                for (VirtualCollection virtualCollection : virtualCollectionList) {
                    tenderIds.add(virtualCollection.getTenderId());
                }

                ts = Specifications
                        .<Tender>and()
                        .in("id", tenderIds.toArray())
                        .build();
                tenderList = tenderService.findList(ts);

                for (VirtualCollection virtualCollection : virtualCollectionList) {
                    for (Tender tender : tenderList) {
                        if (String.valueOf(virtualCollection.getTenderId()).equals(String.valueOf(tender.getId()))) {
                            entity = new CapitalChangeEntity();
                            entity.setUserId(tender.getUserId());
                            entity.setType(CapitalChangeEnum.IncomeRepayment);
                            entity.setMoney(virtualCollection.getInterest());
                            entity.setInterest(virtualCollection.getInterest());
                            entity.setRemark("系统对体验标的还款");
                            try {
                                capitalChangeHelper.capitalChange(entity);
                            } catch (Exception e) {
                                log.error("CollectionVirtualScheduler error:", e);
                            }

                            entity = new CapitalChangeEntity();
                            entity.setUserId(tender.getUserId());
                            entity.setType(CapitalChangeEnum.CollectionLower);
                            entity.setMoney(virtualCollection.getInterest());
                            entity.setInterest(virtualCollection.getInterest());
                            entity.setRemark("体验标回款，扣除待收");
                            try {
                                capitalChangeHelper.capitalChange(entity);
                            } catch (Exception e) {
                                log.error("CollectionVirtualScheduler error:", e);
                            }

                            virtualCollection.setStatus(1);
                            virtualCollection.setCollectionAtYes(new Date());
                            virtualCollection.setCollectionMoney(virtualCollection.getInterest());
                            virtualCollection.setCollectionMoneyYes(virtualCollection.getInterest());
                            virtualService.save(virtualCollection);
                        }
                    }
                }
            } while (virtualCollectionList.size() >= pageSize);
        } while (false);
    }
}
