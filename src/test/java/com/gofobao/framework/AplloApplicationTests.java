package com.gofobao.framework;

import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.helper.BeanHelper;
import com.gofobao.framework.member.entity.Users;
import com.gofobao.framework.tender.entity.AutoTender;
import com.gofobao.framework.tender.repository.AutoTenderRepository;
import com.gofobao.framework.tender.service.AutoTenderService;
import com.jayway.jsonpath.Criteria;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.Query;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Queue;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class AplloApplicationTests {

    @Autowired
    private AutoTenderRepository autoTenderRepository;
    @Autowired
    private AutoTenderService autoTenderService;
    @Autowired
    private EntityManager entityManager;


    @Test
    public void contextLoads() {

        /**
         * //select u from User u where u.id = 1

         CriteriaBuilder cb = entityManager.getCriteriaBuilder();
         CriteriaQuery<User> cq = cb.createQuery(User.class);
         Root<User> root = cq.from(User.class); //from User
         cq.select(root); //select * from User
         javax.persistence.criteria.Predicate pre = cb.equal(root.get("id").as(Integer.class),id);//id=1
         cq.where(pre);//where id=1
         Query query = entityManager.createQuery(cq);//select u from User u where u.id = 1

         System.out.println(query.getResultList());
         */

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Users> criteriaQuery = criteriaBuilder.createQuery(Users.class);
        Root<Users> root = criteriaQuery.from(Users.class);
        criteriaQuery.select(root);
        List<Predicate> predicateList = new ArrayList<Predicate>();
        predicateList.add(criteriaBuilder.equal(root.get("id"), 30));
        Predicate[] predicates = new Predicate[predicateList.size()];
        criteriaQuery.where(predicateList.toArray(predicates));
        TypedQuery<Users> query = entityManager.createQuery(criteriaQuery);
        List<Users> usersList = query.getResultList();
        System.out.println(usersList);
    }

}
