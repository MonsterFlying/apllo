package com.gofobao.framework.api.model.with_daw;

import com.gofobao.framework.api.repsonse.JixinBaseResponse;
import lombok.Data;

/**
 * Created by Zeke on 2017/5/16.
 */
@Data
public class WithDrawResponse extends JixinBaseResponse {
    private String accountId ;
    private String txAmount ;
    private String acqRes ;
}
