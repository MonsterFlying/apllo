package com.gofobao.framework.common.data;

import lombok.AllArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import javax.persistence.criteria.*;

/**
 * Created by Zeke on 2017/5/31.
 */
@AllArgsConstructor
public class GtSpecification<T> implements Specification<T> {
    private String property;
    private final DataObject object;

    @Override
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
        From from = this.getRoot(this.property, root);
        String field = this.getProperty(this.property);
        return criteriaBuilder.greaterThan(from.get(field),object.getVal());
    }

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
