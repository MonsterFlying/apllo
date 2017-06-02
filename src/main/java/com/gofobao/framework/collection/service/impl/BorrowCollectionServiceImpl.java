package com.gofobao.framework.collection.service.impl;

import com.gofobao.framework.collection.entity.BorrowCollection;
import com.gofobao.framework.collection.repository.BorrowCollectionRepository;
import com.gofobao.framework.collection.service.BorrowCollectionService;
import com.gofobao.framework.helper.BeanHelper;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Optional;

/**
 * Created by Zeke on 2017/6/2.
 */
@Service
public class BorrowCollectionServiceImpl implements BorrowCollectionService {

    @Autowired
    private BorrowCollectionRepository borrowCollectionRepository;

    public List<BorrowCollection> findList(Specification<BorrowCollection> specification, Pageable pageable) {
        Page<BorrowCollection> page = borrowCollectionRepository.findAll(specification, pageable);
        return page.getContent();
    }

    public List<BorrowCollection> findList(Specification<BorrowCollection> specification, Sort sort) {
        return borrowCollectionRepository.findAll(specification,sort);
    }

    public boolean updateBySpecification(BorrowCollection borrowCollection, Specification<BorrowCollection> specification) {
        List<BorrowCollection> borrowCollectionList = borrowCollectionRepository.findAll(specification);
        Optional<List<BorrowCollection>> optional = Optional.ofNullable(borrowCollectionList);
        optional.ifPresent(list -> list.forEach(obj -> {
            BeanHelper.copyParamter(borrowCollection,obj,true);
        }));
        return !CollectionUtils.isEmpty(borrowCollectionRepository.save(borrowCollectionList));
    }
}
