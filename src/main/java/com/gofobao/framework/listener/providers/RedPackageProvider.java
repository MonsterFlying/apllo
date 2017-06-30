package com.gofobao.framework.listener.providers;

import com.gofobao.framework.award.entity.Activity;
import com.gofobao.framework.award.entity.ActivityRedPacket;
import com.gofobao.framework.award.entity.ActivityRedPacketLog;
import com.gofobao.framework.award.repository.RedPackageLogRepository;
import com.gofobao.framework.award.repository.RedPackageRepository;
import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.repository.BorrowRepository;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.member.entity.Users;
import com.gofobao.framework.member.repository.UsersRepository;
import com.gofobao.framework.tender.entity.Tender;
import com.gofobao.framework.tender.repository.TenderRepository;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.security.SecureRandom;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by admin on 2017/6/29.
 */

@Component
@Slf4j
public class RedPackageProvider {

    @Value("${jixin.redPacketAccountId}")
    private String redPacketAccountId;
    @Autowired
    CommonSmsProvider commonSmsProvider;
    @Autowired
    private BorrowRepository borrowRepository;

    @Autowired
    private TenderRepository tenderRepository;
    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private RedPackageRepository redPackageRepository;

    @Autowired
    private RedPackageLogRepository redPackageLogRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private List<Integer> sources = Lists.newArrayList(new Integer(0), new Integer(1), new Integer(2), new Integer(9));
    /**
     * 邀请别人投资送红包
     *
     * @param resultMaps
     */
    @Transactional(rollbackFor = Exception.class)
    private void inviteUserTender(Map<String, Object> resultMaps) {
        if (ObjectUtils.isEmpty(resultMaps)) {
            log.error("邀请别人投资送红包处理参数为空");
            return;
        }

        Gson gson = new Gson();
        log.info(String.format("邀请别人投资送红包处理 %s", gson.toJson(resultMaps)));


        // 判断活动是否开启
        Activity activity = verifyActivityIsactive(resultMaps);
        if (ObjectUtils.isEmpty(activity)) {
            return;
        }

        Long tenderId = (Long) resultMaps.get("tenderId");
        Tender borrowTender = tenderRepository.findOne(tenderId);
        if (ObjectUtils.isEmpty(borrowTender) || (borrowTender.getStatus() == 0)) {
            log.info(String.format("邀请别人投资送红包：投标记录为空"));
            return;
        }

        // 判断PC
        if (!borrowTender.getIsAuto()) {
            if (borrowTender.getSource().equals(0)) {
                log.info(String.format("当前用户属于pc投标，不再赠送范围 %s", gson.toJson(borrowTender)));
                return;
            }
        }

        Long borrowId = borrowTender.getBorrowId();
        Borrow borrow = borrowRepository.findOne(borrowId);
        if ((!verifyBorrow(borrow)) || (ObjectUtils.isEmpty(borrow.getSuccessAt()))) {
            log.info("邀请别人投资送红包: 当前标不是/官方标/渠道标/没有满标");
            return;
        }

        Users user = usersRepository.findOne(borrowTender.getUserId());
        if ((ObjectUtils.isEmpty(user)) || (user.getIsLock()) || (!verifySource(user.getSource()))) {
            log.info(String.format("邀请别人投资送红包：当前用户不符合邀请 %s", gson.toJson(user)));
            return;
        }

        Date createdAt = user.getCreatedAt();
        if (DateHelper.diffInDays(activity.getBeginAt(), createdAt, false) > 1) {
            log.info(String.format("邀请别人投资送红包：被邀请人不在活动邀请时间 %s", gson.toJson(user)));
            return;
        }

        if (ObjectUtils.isEmpty(user.getParentId())) {
            log.info(String.format("邀请别人投资送红包：当前用户没有投资人 %s", gson.toJson(user)));
            return;
        }

        Users parent = usersRepository.findOne(user.getParentId().longValue());
        if ((ObjectUtils.isEmpty(parent)) || (parent.getIsLock()) || (!verifySource(parent.getSource()))) {
            log.info(String.format("邀请别人投资送红包：当前用户不符合邀请 %s", gson.toJson(user)));
            return;
        }

        // 派发红包
        int money = getRandomRedpack(borrowTender.getValidMoney(), activity, borrow.getTimeLimit());
        Date now = new Date();
        ActivityRedPacket redPacket = new ActivityRedPacket();
        redPacket.setActivityId(activity.getId());
        redPacket.setActivityName(activity.getTitle());
        redPacket.setBeginAt(now);
        redPacket.setEndAt(DateHelper.addDays(now, 60)); // 有效结束时间 + 60 ；
        redPacket.setCreateTime(now);
        redPacket.setDel(0);
        redPacket.setStatus(0);
        redPacket.setMoney(money);
        redPacket.setUpdateDate(now);
        redPacket.setUserId(parent.getId());
        redPacket.setUserName(getUserName(parent));
        redPacket.setIparam1("");
        redPacket.setVparam1("");
        redPacket.setIparam2("");
        redPacket.setVparam2("");
        redPacket.setIparam3("");
        redPacket.setVparam3(String.format("邀请别人投资送红包：被派发人ID: %s ,派发人账户名：%s, 时间：%s ,标ID：%s, 标的名称：%s， 投标金额：%s, 投标人Id：%s,  投标人账户: %s",
                parent.getId(),
                getUserName(parent),
                DateHelper.dateToString(now),
                borrow.getId(),
                borrow.getName(),
                borrowTender.getValidMoney(),
                user.getId(),
                getUserName(user)
        ));

        ActivityRedPacket redPacket1 = redPackageRepository.save(redPacket);
        if (!ObjectUtils.isEmpty(redPacket1)) {
            log.error(String.format("红包写入数据库失败: %s", gson.toJson(redPacket)));
            return;
        }

        ActivityRedPacketLog redPacketLog = new ActivityRedPacketLog();
        redPacketLog.setCreateTime(new Date());
        redPacketLog.setTermnal(0);
        redPacketLog.setRedPacketId(redPacket.getId());
        redPacketLog.setUserId(user.getId());
        redPacketLog.setUserName(getUserName(user));
        redPacketLog.setIparam1(0);
        redPacketLog.setIparam2(0);
        redPacketLog.setVparam1("");
        redPacketLog.setVparam2("");

        ActivityRedPacketLog redPacketLog1 = redPackageLogRepository.saveAndFlush(redPacketLog);
        if (ObjectUtils.isEmpty(redPacketLog1)) {
            log.error(String.format("红包日志写入数据库失败: %s", gson.toJson(redPacketLog)));
            return;
        }

        //===============================
        // 站内性发送
        //===============================
    }

    /**
     * 老用户投标
     *
     * @param resultMaps
     */
    @Transactional(rollbackFor = Exception.class)
    private void oldUserTender(Map<String, Object> resultMaps) {
        if (ObjectUtils.isEmpty(resultMaps)) {
            log.error("老用户投标红包处理参数为空");
            return;
        }

        Gson gson = new Gson();
        log.info(String.format("老用户投标处理 %s", gson.toJson(resultMaps)));


        // 判断活动是否开启
        Activity activity = verifyActivityIsactive(resultMaps);
        if (ObjectUtils.isEmpty(activity)) {
            return;
        }

        Long tenderId = (Long) resultMaps.get("tenderId");
        Tender borrowTender = tenderRepository.findOne(tenderId);
        if (ObjectUtils.isEmpty(borrowTender) || (borrowTender.getStatus() == 0)) {
            log.info(String.format("老用户投标红包：投标记录为空"));
            return;
        }

        // 判断PC
        if (!borrowTender.getIsAuto()) {
            if (borrowTender.getSource().equals(0)) {
                log.info(String.format("当前用户属于pc投标，不再赠送范围 %s", gson.toJson(borrowTender)));
                return;
            }
        }

        Long borrowId = borrowTender.getBorrowId();
        Borrow borrow = borrowRepository.findOne(borrowId);
        if ((!verifyBorrow(borrow)) || (ObjectUtils.isEmpty(borrow.getSuccessAt()))) {
            log.info("老用户投标红包: 当前标不是/官方标/渠道标/没有满标");
            return;
        }

        Users user = usersRepository.findOne(borrowTender.getUserId());
        if ((ObjectUtils.isEmpty(user)) || (user.getIsLock()) || (!verifySource(user.getSource()))) {
            log.info(String.format("老用户投标红包：当前用户不符合邀请 %s", gson.toJson(user)));
            return;
        }

        // 派发红包
        int money = getRandomRedpack(borrowTender.getValidMoney(), activity, borrow.getTimeLimit());
        Date now = new Date();
        ActivityRedPacket redPacket = new ActivityRedPacket();
        redPacket.setActivityId(activity.getId());
        redPacket.setActivityName(activity.getTitle());
        redPacket.setBeginAt(now);
        redPacket.setEndAt(DateHelper.addDays(now, 60)); // 有效结束时间 + 60 ；
        redPacket.setCreateTime(now);
        redPacket.setDel(0);
        redPacket.setStatus(0);
        redPacket.setMoney(money);
        redPacket.setUpdateDate(now);
        redPacket.setUserId(user.getId());
        redPacket.setUserName(getUserName(user));
        redPacket.setIparam1("");
        redPacket.setVparam1("");
        redPacket.setIparam2("");
        redPacket.setVparam2("");
        redPacket.setIparam3("");
        redPacket.setVparam3(String.format("老用户投标红包：被派发人ID: %s ,派发人账户名：%s, 时间：%s ,标ID：%s, 标的名称：%s， 投标金额：%s",
                user.getId(),
                getUserName(user),
                DateHelper.dateToString(now),
                borrow.getId(),
                borrow.getName(),
                borrowTender.getValidMoney()
        ));
        try {
            redPackageRepository.save(redPacket);
        } catch (Exception e) {
            log.error(String.format("红包写入数据库失败: %s", gson.toJson(redPacket)));
            return;
        }

        ActivityRedPacketLog redPacketLog = new ActivityRedPacketLog();
        redPacketLog.setCreateTime(now);
        redPacketLog.setTermnal(0);
        redPacketLog.setRedPacketId(redPacket.getId());
        redPacketLog.setUserId(user.getId());
        redPacketLog.setUserName(getUserName(user));
        redPacketLog.setIparam1(0);
        redPacketLog.setIparam2(0);
        redPacketLog.setVparam1("");
        redPacketLog.setVparam2("");
        try {
            redPackageLogRepository.save(redPacketLog);
        } catch (Exception e) {
            log.error(String.format("红包日志写入数据库失败: %s", gson.toJson(redPacketLog)));
            return;
        }


        //===============================
        // 站内性发送
        //===============================

        log.info("老用户投标红包成功");

    }

    /**
     * 发送新用户投标红包
     *
     * @param resultMaps
     */
    @Transactional(rollbackFor = Exception.class)
    private void newUserTender(Map<String, Object> resultMaps) {
        if (ObjectUtils.isEmpty(resultMaps)) {
            log.error("新手投标红包处理参数为空");
            return;
        }

        Gson gson = new Gson();
        log.info(String.format("新手投标处理 %s", gson.toJson(resultMaps)));


        // 判断活动是否开启
        Activity activity = verifyActivityIsactive(resultMaps);
        if (ObjectUtils.isEmpty(activity)) {
            return;
        }

        Long tenderId = (Long) resultMaps.get("tenderId");
        String transactionId = (String) resultMaps.get("transactionId");
        Tender borrowTender = tenderRepository.findOne(tenderId);
        if (ObjectUtils.isEmpty(borrowTender) || ((borrowTender.getStatus() == 0))) {
            log.info(String.format("新手投标红包：投标记录为空"));
            return;
        }

        // 判断PC
        if (!borrowTender.getIsAuto()) {
            if (borrowTender.getSource().equals(0)) {
                log.info(String.format("当前用户属于pc投标，不再赠送范围 %s", gson.toJson(borrowTender)));
                return;
            }
        }

        Long borrowId = borrowTender.getBorrowId();
        Borrow borrow = borrowRepository.findOne(borrowId);
        if ((!borrow.getIsNovice() || (!verifyBorrow(borrow)))) {
            log.info("新手投标红包: 当前标不是新手标/官方标/渠道标");
            return;
        }

        Users user = usersRepository.findOne(borrowTender.getUserId());
        if ((ObjectUtils.isEmpty(user)) || (user.getIsLock()) || (!verifySource(user.getSource()))) {
            log.info(String.format("新手投标红包：当前用户不符合邀请 %s", gson.toJson(user)));
            return;
        }

        // 派发红包
        int money = getRandomRedpack(borrowTender.getValidMoney(), activity, borrow.getTimeLimit());
        Date now = new Date();
        ActivityRedPacket redPacket = new ActivityRedPacket();
        redPacket.setActivityId(activity.getId());
        redPacket.setActivityName(activity.getTitle());
        redPacket.setBeginAt(now);
        redPacket.setEndAt(DateHelper.addDays(now, 60)); // 有效结束时间 + 60 ；
        redPacket.setCreateTime(now);
        redPacket.setDel(0);
        redPacket.setStatus(0);
        redPacket.setMoney(money);
        redPacket.setUpdateDate(now);
        redPacket.setUserId(user.getId());
        redPacket.setUserName(getUserName(user));
        redPacket.setIparam1("redPackageTransactionId");
        redPacket.setVparam1(transactionId);
        redPacket.setIparam2("");
        redPacket.setVparam2("");
        redPacket.setIparam3("");
        redPacket.setVparam3(String.format("新手投标红包：被派发人ID: %s ,派发人账户名：%s, 时间：%s ,标ID：%s, 标的名称：%s， 投标金额：%s",
                user.getId(),
                getUserName(user),
                DateHelper.dateToString(now),
                borrow.getId(),
                borrow.getName(),
                borrowTender.getValidMoney()
        ));
        try {
            redPackageRepository.save(redPacket);
        } catch (Exception e) {
            log.error(String.format("红包写入数据库失败: %s", gson.toJson(redPacket)));
            return;

        }


        ActivityRedPacketLog redPacketLog = new ActivityRedPacketLog();
        redPacketLog.setCreateTime(now);
        redPacketLog.setTermnal(0);
        redPacketLog.setRedPacketId(redPacket.getId());
        redPacketLog.setUserId(user.getId());
        redPacketLog.setUserName(getUserName(user));
        redPacketLog.setIparam1(0);
        redPacketLog.setIparam2(0);
        redPacketLog.setVparam1("");
        redPacketLog.setVparam2("");
        try {
            redPackageLogRepository.saveAndFlush(redPacketLog);
        } catch (Exception e) {
            log.error(String.format("红包日志写入数据库失败: %s", gson.toJson(redPacketLog)));
            return;
        }

        //===============================
        // 站内性发送
        //===============================

        log.info("新手投标红包成功");
    }

    /**
     * -
     * 实名派发红包活动列表
     *
     * @param resultMaps
     */
    @Transactional(rollbackFor = Exception.class)
    private void newUserRealName(Map<String, Object> resultMaps) {
        if (ObjectUtils.isEmpty(resultMaps)) {
            log.error("实名派送红包处理参数为空");
            return;
        }

        Gson gson = new Gson();
        log.info(String.format("实名红包处理 %s", gson.toJson(resultMaps)));


        Integer parentId = (int) resultMaps.get("parentId");
        if (parentId == 0) {
            log.error("实名红包派发失败：没有推荐人");
            return;
        }

        // 判断活动是否开启
        Activity activity = verifyActivityIsactive(resultMaps);
        if (ObjectUtils.isEmpty(activity)) {
            return;
        }

        // 获取用户Id
        String userId = resultMaps.get("userId").toString();

        Users user = usersRepository.findOne(Long.valueOf(userId));
        if (!verifySource(user.getSource())) {
            log.error("实名红包派发失败：实名人为渠道用户");
            return;
        }

        Users parent = usersRepository.findOne(user.getParentId().longValue());
        if (ObjectUtils.isEmpty(parent)) {
            log.error("实名红包派发失败：查询数据库，没有发现推荐人");
            return;
        }

        if (parent.getIsLock()) {
            log.error("实名红包派发失败：当前推荐人为锁定状态");
            return;
        }

        //===============================
        // 派发红包
        //===============================
        int money = getRandomRedpack(0, activity, 0); // 生成红包金额
        Date now = new Date();
        ActivityRedPacket redPacket = new ActivityRedPacket();
        redPacket.setActivityId(activity.getId());
        redPacket.setActivityName(activity.getTitle());
        redPacket.setBeginAt(now);
        redPacket.setEndAt(DateHelper.addDays(now, 60)); // 有效结束时间 + 60 ；
        redPacket.setCreateTime(now);
        redPacket.setDel(0);
        redPacket.setStatus(0);
        redPacket.setMoney(money);
        redPacket.setUpdateDate(now);
        redPacket.setUserId(parent.getId());
        redPacket.setUserName(getUserName(parent));
        redPacket.setIparam1("");
        redPacket.setVparam1("");
        redPacket.setIparam2("");
        redPacket.setVparam2("");
        redPacket.setIparam3("");
        redPacket.setVparam3(String.format("注册红包：注册人ID: %s ,注册账户名：%s, 时间：%s ,推荐人ID：%s, 推荐人姓名：%s",
                userId,
                getUserName(user),
                DateHelper.dateToString(now),
                parentId,
                getUserName(parent)
        ));
        try {
            redPackageRepository.save(redPacket);
        } catch (Exception e) {
            log.error(String.format("红包写入数据库失败: %s", gson.toJson(redPacket)));
            return;
        }

        ActivityRedPacketLog redPacketLog = new ActivityRedPacketLog();
        redPacketLog.setCreateTime(new Date());
        redPacketLog.setTermnal(0);
        redPacketLog.setRedPacketId(redPacket.getId());
        redPacketLog.setUserId(parent.getId());
        redPacketLog.setUserName(getUserName(parent));
        redPacketLog.setIparam1(0);
        redPacketLog.setIparam2(0);
        redPacketLog.setVparam1("");
        redPacketLog.setVparam2("");
        try {
            redPackageLogRepository.save(redPacketLog);
        } catch (Exception e) {
            log.error(String.format("红包日志写入数据库失败: %s", gson.toJson(redPacketLog)));
            return;

        }

        //===============================
        // 站内性发送
        //===============================

        log.info("发送实名红包成功");
    }


    private boolean verifyBorrow(Borrow borrow) {
        return 0 == borrow.getType() || 4 == borrow.getType();
    }

    private boolean verifySource(Integer source) {
        return sources.contains(source);

    }

    private String getUserName(Users user) {
        String username = user.getUsername();
        if (StringUtils.isEmpty(username)) {
            username = user.getPhone();
        }

        if (StringUtils.isEmpty(username)) {
            username = user.getEmail();
        }

        return username;
    }

    /**
     * 获取随机金额
     *
     * @param money    用户金额
     * @param activity 活动规则
     * @param limit    期限
     * @return
     */
    private static int getRandomRedpack(int money, Activity activity, int limit) {
        if (ObjectUtils.isEmpty(activity)) {
            log.error("生成红包金额失败： 活动为空");
            return 0;
        }


        Integer max = activity.getMax();
        Integer min = activity.getMin();
        int random = 0;
        if (activity.getType() != 4) {
            SecureRandom secureRandom = new SecureRandom();
            int mid = secureRandom.nextInt(100);
            // 80 %
            if (mid <= 50) {
                mid = secureRandom.nextInt(20);
            } else if (mid <= 70) {
                mid = secureRandom.nextInt(70);
                if (mid <= 60) {
                    mid = secureRandom.nextInt(50);
                }
            } else if (mid <= 85) {
                mid = secureRandom.nextInt(85);
                if (mid <= 70) {
                    mid = secureRandom.nextInt(70);
                }
            } else {
                mid = secureRandom.nextInt(100);
                if (mid <= 85) {
                    mid = secureRandom.nextInt(70);
                }
            }
            random = new Double((max - min) * (mid / 100d) + min).intValue();
        }
        System.out.println(random);
        if (activity.getType() == 1) { // 实名
            return random;
        } else if (activity.getType() == 2) { // 新手标
            return new Double((random * money) / 10000D).intValue();
        } else if (activity.getType() == 4) { // 邀请用户投资
            return new Double((money * (0.02 / 12D)) * limit).intValue();
        } else if (activity.getType() == 3) { // 老用户投资
            return new Double((random * money) / 10000D).intValue();
        } else {
            return 0;
        }

    }


    /**
     * 验证活动是否有效
     *
     * @param resultMaps
     * @return
     */
    public Activity verifyActivityIsactive(Map<String, Object> resultMaps) {
        String type = (String) resultMaps.get("type");
        String time = (String) resultMaps.get("time");
        Date date = DateHelper.stringToDate(time);

        String sql = "SELECT a FROM Activity AS a WHERE a.type=?1 AND a.isOpen=?2 AND a.del=?3 AND (a.beginAt >= ?4 <=a.endAt)";
        Query query = entityManager.createNamedQuery(sql);
        query.setParameter(1, type);
        query.setParameter(2, 1);
        query.setParameter(3, 0);
        query.setParameter(4, date);
        List<Activity> activities = query.getResultList();
        if (CollectionUtils.isEmpty(activities)) {
            log.error(String.format("没有找到红包活动 类型：%s ,时间： %s", type, time));
            return null;
        }

        return activities.get(0);
    }


}
