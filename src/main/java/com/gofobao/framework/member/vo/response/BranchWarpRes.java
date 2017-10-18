package com.gofobao.framework.member.vo.response;

import com.gofobao.framework.core.vo.VoBaseResp;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by master on 2017/10/17.
 */
@Data
public class BranchWarpRes extends VoBaseResp {

    private List<VoBranch> branches = new ArrayList<>(0);

    @Data
    public class VoBranch {
        private Integer id;

        private String branchName;

    }

}
