package com.gofobao.framework.contract.vo.response;

import com.gofobao.framework.core.vo.VoBaseResp;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class BorrowContractListWarpRes extends VoBaseResp {

    private List<BorrowContractInfo> borrowContractInfoList = new ArrayList<>(0);

    @Data
    public class BorrowContractInfo {
        private Long borrowId;

        private String borrowName;

        private Long userId;

        private Long forUserId ;

        private String tenderAt;

        private String recheckAt;

        private String batchNo;
    }
}
