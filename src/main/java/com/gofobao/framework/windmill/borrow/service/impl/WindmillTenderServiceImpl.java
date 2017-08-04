package com.gofobao.framework.windmill.borrow.service.impl;

import com.gofobao.framework.tender.contants.TenderConstans;
import com.gofobao.framework.tender.entity.Tender;
import com.gofobao.framework.windmill.borrow.service.WindmillTenderService;
import com.gofobao.framework.windmill.borrow.vo.request.UserTenderLogReq;
import com.google.common.collect.Lists;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by admin on 2017/8/4.
 */
@Component
public class WindmillTenderServiceImpl implements WindmillTenderService {

    @PersistenceContext
    private EntityManager entityManager;


    @Override
    public Map<String, Object> userTenderLog(UserTenderLogReq tenderLogReq) {

        List<Tender> resultTenders = Lists.newArrayList();

        StringBuilder sql = new StringBuilder("SELECT t from Tender t WHERE 1=1 AND t.userId=:userId t.status=:status ");
        //  如果传了id，只查询该用户这个id的记录
        if (!StringUtils.isEmpty(tenderLogReq.getInvest_record_id())) {

            //時間條件的判斷
            String startAt = "";
            String endAt = "";
            if (!StringUtils.isEmpty(tenderLogReq.getStart_time())) {
                startAt = tenderLogReq.getStart_time();
            }
            if (StringUtils.isEmpty(tenderLogReq.getEnd_time())) {
                endAt = tenderLogReq.getEnd_time();
            }

            //時間格式
            if (!StringUtils.isEmpty(startAt) || !StringUtils.isEmpty(endAt)) {
                if (!StringUtils.isEmpty(startAt) && !StringUtils.isEmpty(endAt)) {
                    sql.append(" AND t.createAt>='" + startAt + "' AND t.createAt<='" + endAt + "'");
                } else {
                    if (!StringUtils.isEmpty(startAt)) {
                        sql.append(" AND t.createAt>='" + startAt + "' ");
                    }
                    if (!StringUtils.isEmpty(endAt)) {
                        sql.append(" AND t.createAt<='" + endAt + "' ");
                    }
                }
            }
            //投資狀態判斷
            if (!StringUtils.isEmpty(tenderLogReq.getInvest_status())) {
                Integer state;
                //待回款 ||逾期
                if (tenderLogReq.getInvest_status() == 0 || tenderLogReq.getInvest_status() == 2) {
                    state = TenderConstans.BACK_MONEY;
                    //投资中
                } else if (tenderLogReq.getInvest_status() == -1) {
                    state = TenderConstans.BIDDING;
                } else {//已回款
                    state = TenderConstans.SETTLE;
                }
                sql.append(" AND t.status= " + state);
            }
            Query query = entityManager.createQuery(sql.toString(), Tender.class);
            query.setParameter("status", TenderConstans.SUCCESS);
            query.setParameter("userId", tenderLogReq.getPf_user_id());
            query.setFirstResult(tenderLogReq.getLimit());
            query.setMaxResults(tenderLogReq.getOffset());
            resultTenders = query.getResultList();
        } else {
            sql.append("t.id=:id");
            Query query = entityManager.createQuery(sql.toString(), Tender.class);
            query.setParameter("status", TenderConstans.SUCCESS);
            query.setParameter("userId", tenderLogReq.getPf_user_id());
            query.setParameter("id", tenderLogReq.getInvest_record_id());
            resultTenders = query.getResultList();
        }

        //预期中的标
        if (tenderLogReq.getInvest_status() == 2) {
            List<Long> ids = resultTenders.stream().map(m -> m.getId()).collect(Collectors.toList());

        }


        return null;
    }
}
