package com.gofobao.framework.member.vo.response;

import com.gofobao.framework.api.model.account_details_query.AccountDetailsQueryItem;
import com.gofobao.framework.core.vo.VoBaseResp;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by master on 2017/9/7.
 */
@Data
public class UserAccountThirdTxRes extends VoBaseResp {

        private List<AccountDetailsQueryItem> detailsQueryItems= new ArrayList<>(0);

        private Integer totalCount=0;


}
