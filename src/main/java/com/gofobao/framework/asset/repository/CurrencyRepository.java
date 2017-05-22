package com.gofobao.framework.asset.repository;

import com.gofobao.framework.asset.entity.Currency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by Zeke on 2017/5/22.
 */
@Repository
public interface CurrencyRepository extends JpaRepository<Currency,Long>{
}
