package com.gofobao.framework.windmill.borrow.biz;

import com.gofobao.framework.windmill.borrow.vo.response.ByDayStatistics;
import com.gofobao.framework.windmill.borrow.vo.response.UserAccountStatistics;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by admin on 2017/8/3.
 */
public interface WindmillStatisticsBiz {
    /**
     * 5.4汇总数据查询接口
     * @param request
     * @return
     */
    ByDayStatistics byDayStatistics(HttpServletRequest request);





    /**
     * 5.5账户信息查询接口
     * @param request
     * @return
     */
    UserAccountStatistics userStatistics(HttpServletRequest request);

}
