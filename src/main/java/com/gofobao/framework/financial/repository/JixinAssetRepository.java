package com.gofobao.framework.financial.repository;

import com.gofobao.framework.financial.entity.JixinAsset;
import com.gofobao.framework.financial.entity.LocalAndRemoteAssetInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface JixinAssetRepository extends JpaRepository<JixinAsset, Long>,JpaSpecificationExecutor<JixinAsset> {
    JixinAsset findTopByAccountId(String cardnbr);


    @Query(value = "  SELECT " +
            "      t1.use_money + t1.no_use_money AS localMoney, " +
            "      t1.updated_at AS localUpdateDatetime, " +
            "      t2.curr_money AS remoteMoney, " +
            "      t2.update_time AS remoteUpdateDatetime, " +
            "      IFNULL(t3.username, '') AS userName, " +
            "      t3.phone AS phone, " +
            "      t3.realname AS realname " +
            "  FROM " +
            "      gfb_yesterday_asset AS t1 " +
            "          LEFT JOIN " +
            "      jixin_asset AS t2 ON t1.user_id = t2.user_id " +
            "          LEFT JOIN " +
            "      gfb_users AS t3 ON t1.user_id = t3.id " +
            " ORDER BY ?#{#pageable} ",

            countQuery =  "SELECT COUNT(t1.user_id)" +
                    "  FROM " +
                    "      gfb_yesterday_asset AS t1 " +
                    "          LEFT JOIN " +
                    "      jixin_asset AS t2 ON t1.user_id = t2.user_id " +
                    "          LEFT JOIN " +
                    "      gfb_users AS t3 ON t2.user_id = t3.id",
            nativeQuery = true
    )
    Page<Object[]> findAllForPrint(Pageable pageable);
}
