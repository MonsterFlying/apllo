package com.gofobao.framework.member.service;

import com.gofobao.framework.asset.vo.response.VoAssetDetailResp;
import com.gofobao.framework.asset.vo.response.VoExpenditureResp;
import com.gofobao.framework.comment.vo.response.VoCommonDataStatistic;
import com.gofobao.framework.member.entity.UserCache;
import com.gofobao.framework.member.entity.Users;
import com.gofobao.framework.member.vo.response.VoSiteSumBalanceResp;
import com.gofobao.framework.member.vo.response.pc.AssetStatistic;
import com.gofobao.framework.member.vo.response.pc.ExpenditureDetail;
import com.gofobao.framework.member.vo.response.pc.IncomeEarnedDetail;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;

import java.util.Date;
import java.util.List;

/**
 * Created by Zeke on 2017/5/19.
 */
public interface UserCacheService {

    /**
     * 根据id查询UserCache
     *
     * @param id
     * @return
     */
    UserCache findById(Long id);

    UserCache findByUserIdLock(Long userId);

    UserCache save(UserCache userCache);

    UserCache updateById(UserCache userCache);

    List<UserCache> findList(Specification<UserCache> specification);

    List<UserCache> findList(Specification<UserCache> specification, Sort sort);

    List<UserCache> findList(Specification<UserCache> specification, Pageable pageable);

    long count(Specification<UserCache> specification);


    /**
     * pc 用户资产统计
     *
     * @param userId
     * @return
     */
    AssetStatistic assetStatistic(Long userId);


    /**
     * 已转收益统计
     *
     * @param userId
     * @return
     */
    ResponseEntity<IncomeEarnedDetail> incomeEarned(Long userId);

    /**
     * 已支出明细统计
     *
     * @param userId
     * @return
     */
    ResponseEntity<ExpenditureDetail> expenditureDetail(Long userId);

    /**
     * 判断用户是佛为新用户
     * 是否投过官方标或者渠道标
     *
     * @param user
     * @return
     */
    boolean isNew(Users user);


    List<UserCache> findByUserIds(List<Long> userIds);

    /**
     * 用户支出详情
     *
     * @param userId
     * @return
     */
    ResponseEntity<VoExpenditureResp> expendMoeny(Long userId);

    /**
     * 净资产详情
     *
     * @param userId
     * @return
     */
    ResponseEntity<VoAssetDetailResp> netAssetDetail(Long userId);

    /**
     * 车贷,渠道 标待收
     * @param type
     * @param recheckAt
     * @return
     */
    List<VoCommonDataStatistic> findWaitCollection(Integer type, Date recheckAt);


    /**
     * 车贷, 渠道待还
     * @param type
     * @param recheckAt
     * @return
     */
    List<VoCommonDataStatistic>findWaitRepayment(Integer type,Date recheckAt);

    /**
     * 按时间点查询网站用余额
     * @param date
     * @return
     */
    ResponseEntity<VoSiteSumBalanceResp> findByDate(Date date);


}
