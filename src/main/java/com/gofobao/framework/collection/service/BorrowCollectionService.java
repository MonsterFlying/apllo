package com.gofobao.framework.collection.service;

import com.gofobao.framework.collection.entity.BorrowCollection;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

/**
 * Created by Zeke on 2017/6/2.
 */
public interface BorrowCollectionService {
    List<BorrowCollection> findList(Specification<BorrowCollection> specification, Pageable pageable);

    List<BorrowCollection> findList(Specification<BorrowCollection> specification, Sort sort);

    boolean updateBySpecification(BorrowCollection borrowCollection, Specification<BorrowCollection> specification);
}
