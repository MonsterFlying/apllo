package com.gofobao.framework.api.model.card_bind_details_query;

import lombok.Data;

@Data
public class CardBindItem {
    private String cardNo ;
    private String txnDate ;
    private String txnTime ;
    private String canclDate ;
    private String canclTime ;
}
