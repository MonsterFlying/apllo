package com.gofobao.framework.common.data;

import javax.persistence.criteria.*;

/**
 * Created by Zeke on 2017/5/31.
 */
public class LeSpecification<T> extends BaseSpecification<T> {

    protected String property;
    protected DataObject dataObject;

    public LeSpecification(String property, DataObject dataObject) {
        this.property = property;
        this.dataObject = dataObject;
    }

    @Override
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
        From from = this.getRoot(this.property, root);
        String field = this.getProperty(this.property);
        return criteriaBuilder.lessThanOrEqualTo(from.get(field), dataObject.getVal());
    }

}
