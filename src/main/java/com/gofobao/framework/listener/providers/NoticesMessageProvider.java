package com.gofobao.framework.listener.providers;

import com.gofobao.framework.system.biz.NoticesBiz;
import com.gofobao.framework.system.entity.Notices;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Created by Max on 17/6/1.
 */
@Component
@Slf4j
public class NoticesMessageProvider {
    @Autowired
    NoticesBiz noticesBiz;


    public static final Gson GSON = new Gson();

    /**
     * 添加站内信
     *
     * @param tag
     * @param body
     * @return
     * @throws Exception
     */
    public boolean addNoticeMessage(String tag, Map<String, String> body) throws Exception {
        Notices notices = GSON.fromJson(GSON.toJson(body), Notices.class);
        return noticesBiz.save(notices);
    }
}
