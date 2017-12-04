package com.gofobao.framework.member.service.impl;

import com.gofobao.framework.common.constans.TypeTokenContants;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.NumberHelper;
import com.gofobao.framework.helper.RedisHelper;
import com.gofobao.framework.helper.StringHelper;
import com.gofobao.framework.member.entity.Users;
import com.gofobao.framework.member.repository.UsersRepository;
import com.gofobao.framework.member.service.UserService;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.Query;
import java.util.*;

/**
 * 用户实体类
 * Created by Max on 20.03.16.
 */
@Service
@Slf4j
public class UserServiceImpl implements UserService {
    @Autowired
    private UsersRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    RedisHelper redisHelper;

    Gson gson = new Gson();

    /**
     * 修复待收利息管理费redis缓存key
     */
    final String repairKey = "REPAIR_WAIT_EXPENDITURE_INTEREST_MANAGE";

    final String repairNumKey = "REPAIR_NUM";

    final String repairDateKey = "REPAIR_DATE";

    @Override
    public List<Users> listUser(Users users) {
        Example<Users> usersExample = Example.of(users);
        List<Users> usersList = userRepository.findAll(usersExample);
        return Optional.ofNullable(usersList).orElse(Collections.EMPTY_LIST);
    }

    @Override
    public Users findByAccount(String account) {
        List<Users> usersList = userRepository.findByUsernameOrPhoneOrEmail(account, account, account);

        if (!CollectionUtils.isEmpty(usersList)) {
            return usersList.get(0);
        }
        return null;
    }


    @Override
    public Users findById(Long id) {
        return userRepository.findOne(id);
    }


    @Override
    public boolean notExistsByPhone(String phone) {
        List<Users> usersList = userRepository.findByPhone(phone);
        return CollectionUtils.isEmpty(usersList);

    }

    /**
     * 带锁查询会员
     *
     * @param userId
     * @return
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Override
    public Users findByIdLock(Long userId) {
        return userRepository.findOne(userId);
    }

    @Override
    public boolean notExistsByUserName(String userName) {
        List<Users> users = userRepository.findByUsername(userName);
        return CollectionUtils.isEmpty(users);
    }

    @Override
    public Users findByInviteCodeOrPhoneOrUsername(String inviteCode) {
        List<Users> users = userRepository.findByInviteCodeOrPhoneOrUsername(inviteCode, inviteCode, inviteCode);
        if (CollectionUtils.isEmpty(users)) {
            return null;
        } else {
            return users.get(0);
        }

    }

    @Override
    public Users save(Users users) {
        return userRepository.save(users);
    }

    @Override
    public boolean notExistsByEmail(String email) {
        Users users = new Users();
        users.setEmail(email);
        Example<Users> example = Example.of(users);
        return !userRepository.exists(example);
    }

    /**
     * 检查是否实名
     *
     * @param users
     * @return
     */
    @Override
    public boolean checkRealname(Users users) {
        if (ObjectUtils.isEmpty(users)) {
            return false;
        }
        return !(ObjectUtils.isEmpty(users.getCardId()) || ObjectUtils.isEmpty(users.getUsername()));
    }

    @Override
    public List<Users> findList(Specification<Users> specification) {
        return userRepository.findAll(specification);
    }

    @Override
    public List<Users> findList(Specification<Users> specification, Sort sort) {
        return userRepository.findAll(specification, sort);
    }

    @Override
    public List<Users> findList(Specification<Users> specification, Pageable pageable) {
        return userRepository.findAll(specification, pageable).getContent();
    }

    @Override
    public long count(Specification<Users> specification) {
        return userRepository.count(specification);
    }


    @Override
    public List<Users> serviceUser() {
        String sql = "SELECT t1.* " +
                "FROM `gfb_users` t1 JOIN gfb_role_user t2 ON t1.id = t2.user_id WHERE t2.role_id in (3, 4)";
        Query query = entityManager.createNativeQuery(sql, Users.class);
        return query.getResultList();
    }

    @Override
    public Page<Users> findAll(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    /**
     * 验证身份证是否存在
     *
     * @param idCard
     * @return
     */
    @Override
    public boolean notExistsByIdCard(String idCard) {
        Users users = new Users();
        users.setCardId(idCard);
        Example<Users> example = Example.of(users);
        return !userRepository.exists(example);
    }


    @Override
    public List<Users> findByIdIn(List<Long> ids) {


        return userRepository.findByIdIn(ids);
    }

    @Override
    public void repairWaitExpenditureInterestManage() {
        String repairStr = null;
        try {
            repairStr = redisHelper.get(repairKey, "");

            Map<String, String> repairMap = new HashMap();
            /*修复时间*/
            Date repairDate = new Date();
            /*修复次数*/
            int repairNum = 0;
            if (!StringUtils.isEmpty(repairStr)) {
                repairMap = gson.fromJson(repairStr, TypeTokenContants.MAP_ALL_STRING_TOKEN);
                repairDate = DateHelper.stringToDate(repairMap.get(repairDateKey));
                repairNum = NumberHelper.toInt(repairMap.get(repairNumKey));
            }

            if (System.currentTimeMillis() > DateHelper.addMinutes(repairDate, 20).getTime() || repairNum > 4) {

                String sql = "UPDATE gfb_user_cache p1 RIGHT JOIN\n" +
                        "  (\n" +
                        "    SELECT\n" +
                        "      t2.user_id,\n" +
                        "      sum(round(t1.interest * 0.1)) AS sum_interest\n" +
                        "    FROM gfb_borrow_collection t1\n" +
                        "      LEFT JOIN gfb_borrow_tender t2 ON t1.tender_id = t2.id\n" +
                        "      LEFT JOIN gfb_borrow t3 ON t2.borrow_id = t3.id\n" +
                        "    WHERE t1.status = 0\n" +
                        "          AND t1.transfer_flag = 0\n" +
                        "          AND t2.status = 1\n" +
                        "          AND t2.transfer_flag <> 2\n" +
                        "          AND t3.status = 3\n" +
                        "          AND t3.type IN (0, 4)\n" +
                        "          AND t3.recheck_at < '2017-11-01 00:00:00'\n" +
                        "    GROUP BY t2.user_id\n" +
                        "  ) p2 ON p1.user_id = p2.user_id\n" +
                        "    SET p1.wait_expenditure_interest_manage = sum_interest;";
                jdbcTemplate.update(sql);
                redisHelper.put(repairKey, "");
            } else {
                repairMap.put(repairNumKey, String.valueOf(++repairNum));
                repairMap.put(repairDateKey, DateHelper.dateToString(repairDate));
            }

            redisHelper.put(repairKey, gson.toJson(repairMap));
        } catch (Exception e) {
            e.printStackTrace();
            try {
                redisHelper.remove(repairKey);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }
}
