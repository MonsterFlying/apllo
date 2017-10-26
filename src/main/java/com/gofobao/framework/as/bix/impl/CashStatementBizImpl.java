package com.gofobao.framework.as.bix.impl;

import com.gofobao.framework.as.bix.CashStatementBiz;
import com.google.gson.Gson;
import org.springframework.stereotype.Component;

@Component
public class CashStatementBizImpl implements CashStatementBiz {

     public enum CashType {
        smallCash("7820"), bigCash("");

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
