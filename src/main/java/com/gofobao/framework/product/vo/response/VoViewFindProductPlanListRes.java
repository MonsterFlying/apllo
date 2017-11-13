package com.gofobao.framework.product.vo.response;

import com.gofobao.framework.core.vo.VoBaseResp;
import lombok.Data;

import java.util.List;

/**
 * Created by Zeke on 2017/11/10.
 */
@Data
public class VoViewFindProductPlanListRes extends VoBaseResp {
    List<VoProductPlan> productPlanList;
}
