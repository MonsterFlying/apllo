package com.gofobao.framework.api.model.credit_details_query;

import lombok.Data;

@Data
public class CreditDetailsQueryItem {
  private String productId  ;
  private String buyDate  ;
  private String orderId  ;
  private String txAmount  ;
  private String yield  ;
  private String forIncome  ;
  private String intTotal  ;
  private String income  ;
  private String incFlag  ;
  private String endDate  ;
  private String state  ;
}
