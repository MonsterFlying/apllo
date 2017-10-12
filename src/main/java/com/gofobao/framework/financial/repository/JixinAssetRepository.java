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
public interface JixinAssetRepository extends JpaRepository<JixinAsset, Long>, JpaSpecificationExecutor<JixinAsset> {
    JixinAsset findTopByAccountId(String cardnbr);


  /*  @Query(value = "  SELECT " +
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
    )*/

    @Query(value = "SELECT  " +
            "    IFNULL(account.user_id, 0) AS userId, " +
            "    IFNULL(account.name, '') AS userName, " +
            "    IFNULL(account.mobile, '') AS phone, " +
            "    IFNULL(account.account_id, '') AS accountId, " +
            "    IFNULL(o_gna.curr_bal, '0') AS remoteMoney, " +
            "    CONCAT(o_gna.reldate, o_gna.inptime) AS remoteDatetime, " +
            "    (asset.no_use_money + asset.use_money) AS localMoney, " +
            "    asset.updated_at AS localDatetime " +
            "FROM " +
            "    (SELECT " +
            "        c_gna.cardnbr, c_gna.curr_bal, c_gna.reldate, c_gna.inptime " +
            "    FROM " +
            "        (SELECT  " +
            "        cardnbr, curr_bal, reldate, inptime " +
            "    FROM " +
            "        gfb_new_aleve " +
            "    ORDER BY id DESC) AS c_gna " +
            "    GROUP BY c_gna.cardnbr) AS o_gna " +
            "        INNER JOIN " +
            "    gfb_user_third_account AS account ON account.account_id = o_gna.cardnbr " +
            "        INNER JOIN " +
            "    gfb_yesterday_asset AS asset ON asset.user_id = account.user_id " +
            "ORDER BY ?#{#pageable}",
            countQuery = "SELECT COUNT(o_gna.cardnbr) " +
                    "FROM " +
                    "    (SELECT " +
                    "        c_gna.cardnbr, c_gna.curr_bal, c_gna.reldate, c_gna.inptime " +
                    "    FROM " +
                    "        (SELECT  " +
                    "        cardnbr, curr_bal, reldate, inptime " +
                    "    FROM " +
                    "        gfb_new_aleve " +
                    "    ORDER BY id DESC) AS c_gna " +
                    "    GROUP BY c_gna.cardnbr) AS o_gna " +
                    "        INNER JOIN " +
                    "    gfb_user_third_account AS account ON account.account_id = o_gna.cardnbr " +
                    "        INNER JOIN " +
                    "    gfb_yesterday_asset AS asset ON asset.user_id = account.user_id",
            nativeQuery = true
    )
    Page<Object[]> findAllForPrint(Pageable pageable);
}
