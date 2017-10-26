package com.gofobao.framework.as.bix.impl;

import com.gofobao.framework.api.helper.JixinManager;
import com.gofobao.framework.api.helper.JixinTxDateHelper;
import com.gofobao.framework.as.bix.CashStatementBiz;
import com.gofobao.framework.asset.entity.CashDetailLog;
import com.gofobao.framework.asset.service.CashDetailLogService;
import com.gofobao.framework.asset.service.RechargeDetailLogService;
import com.gofobao.framework.common.assets.AssetChangeProvider;
import com.gofobao.framework.common.rabbitmq.MqHelper;
import com.gofobao.framework.financial.entity.NewEve;
import com.gofobao.framework.financial.service.NewAleveService;
import com.gofobao.framework.financial.service.NewEveService;
import com.gofobao.framework.helper.ExceptionEmailHelper;
import com.gofobao.framework.member.entity.UserThirdAccount;
import com.gofobao.framework.member.service.UserService;
import com.gofobao.framework.member.service.UserThirdAccountService;
import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
@Slf4j
public class CashStatementBizImpl implements CashStatementBiz {
    @Autowired
    private UserService userService;

    @Autowired
    private CashDetailLogService cashDetailLogService ;

    @Autowired
    AssetChangeProvider assetChangeProvider;

    @Autowired
    JixinTxDateHelper jixinTxDateHelper;

    @Autowired
    UserThirdAccountService userThirdAccountService;

    @Autowired
    NewEveService newEveService;

    @Autowired
    NewAleveService newAleveService;

    @Autowired
    JixinManager jixinManager;

    @Autowired
    ExceptionEmailHelper exceptionEmailHelper;

    @Autowired
    MqHelper mqHelper;



    @Override
    public boolean offlineStatement(Long userId, Date date, CashType cashType) throws Exception {
        // 查询即信流水
        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(userId);
        Preconditions.checkNotNull(userThirdAccount, "userthirdAccount record is null");
        Preconditions.checkNotNull(date, "date is null");
        // 投资标的
        List<NewEve> thirdCashRecordList = null ;
        try {
            thirdCashRecordList = newEveService.findAllByTranTypeAndDateAndUserId(cashType.getType(), userId, date);
        }catch (Exception e){
            log.error("查询即信交易流水异常", e);
        }

        List<CashDetailLog> cashDetailLogs = findLocalCashRecords(userThirdAccount, date, cashType) ;


        return false;
    }

    /**
     * 查询本地提现记录
     * @param userThirdAccount
     * @param date
     * @param cashType
     * @return
     */
    private List<CashDetailLog> findLocalCashRecords(UserThirdAccount userThirdAccount, Date date, CashType cashType) {



        return null;
    }

    @Override
    public boolean onlineStatement(Long userId, Date date, CashType cashType, boolean force) throws Exception {
        return false;
    }

    public enum CashType {
        /**
         * 小额提现 2616
         */
        smallCash("2820"),

        /**
         * 大额提现
         */
        bigCash("2820");

         CashType(String type) {
            this.type = type;
        }

        private String type;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }

    private final static Gson gson = new Gson();
}
