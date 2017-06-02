package com.gofobao.framework.common.data;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import javax.persistence.criteria.*;

/**
 * Created by Zeke on 2017/6/2.
 */
public abstract class BaseSpecification<T> implements Specification<T>{

    public From getRoot(String property, Root<T> root) {
        if(property.contains(".")) {
            String joinProperty = StringUtils.split(property, ".")[0];
            return root.join(joinProperty, JoinType.LEFT);
        } else {
            return root;
        }
    }

    public String getProperty(String property) {
        return property.contains(".")?StringUtils.split(property, ".")[1]:property;
    }
}
