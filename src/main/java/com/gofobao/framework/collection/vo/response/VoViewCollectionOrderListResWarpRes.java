package com.gofobao.framework.collection.vo.response;

import com.gofobao.framework.core.vo.VoBaseResp;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by admin on 2017/6/6.
 */
@Data
public class VoViewCollectionOrderListResWarpRes extends VoBaseResp {
    private List<VoViewCollectionOrderList> listRes=new ArrayList<>();
}
