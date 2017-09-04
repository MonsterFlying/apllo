package com.gofobao.framework.system.controller;

import com.gofobao.framework.message.biz.InitDBBiz;
import com.gofobao.framework.migrate.MigrateBorrowBiz;
import com.gofobao.framework.migrate.MigrateMemberBiz;
import com.gofobao.framework.migrate.MigrateProtocolBiz;
import com.gofobao.framework.migrate.MigrateTenderBiz;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 */
@RestController
@Slf4j
public class SystemController {
    @Autowired
    InitDBBiz initDBBiz;

    @Autowired
    MigrateMemberBiz migrateMemberBiz;

    @Autowired
    MigrateBorrowBiz migrateBorrowBiz;

    @Autowired
    MigrateTenderBiz migrateTenderBiz;

    @Autowired
    MigrateProtocolBiz migrateProtocolBiz;

    @GetMapping("pub/initDB/{password}")
    public void initDB(@PathVariable(value = "password") String password) {
        if (!"@GOFOBAO0701WEIBO----=====".equals(password)) {
            return;
        }

        long curTime = System.currentTimeMillis();
        initDBBiz.initDb();
        log.info("处理时间: " + (System.currentTimeMillis() - curTime));
    }

    @GetMapping("pub/initTransfer/{password}")
    public void initTransfer(@PathVariable(value = "password") String password) {
        if (!"@GOFOBAO0701WEIBO----=====".equals(password)) {
            return;
        }
        long curTime = System.currentTimeMillis();
        initDBBiz.transfer();
        log.info("处理时间: " + (System.currentTimeMillis() - curTime));
    }


    /**
     * 获取开户
     *
     * @param password
     */
    @GetMapping("pub/migrateMember/{password}")
    public void migrateMember(@PathVariable(value = "password") String password) {
        if (!"@GOFOBAO0701WEIBO----=====".equals(password)) {
            return;
        }

        long curTime = System.currentTimeMillis();
        migrateMemberBiz.getMemberMigrateFile();
        log.info("处理时间: " + (System.currentTimeMillis() - curTime));
    }


    /**
     * 提交开户数据
     *
     * @param password
     * @param filename
     */
    @GetMapping("pub/postMigrateMember/{password}/{filename}")
    public void postMigrateMember(@PathVariable(value = "password") String password,
                                  @PathVariable(value = "filename") String filename) {
        if (!"@GOFOBAO0701WEIBO----=====".equals(password)) {
            return;
        }

        long curTime = System.currentTimeMillis();
        try {
            log.info("提交用户开户数据");
            migrateMemberBiz.postMemberMigrateFile(filename);
        } catch (Exception e) {
            log.error("导入系统异常");
        }
        log.info("处理时间: " + (System.currentTimeMillis() - curTime));
    }


    @GetMapping("pub/migrateBorrow/{password}")
    public void migrateBorrow(@PathVariable(value = "password") String password) {
        if (!"@GOFOBAO0701WEIBO----=====".equals(password)) {
            return;
        }

        long curTime = System.currentTimeMillis();
        migrateBorrowBiz.getBorrowMigrateFile();
        log.info("处理时间: " + (System.currentTimeMillis() - curTime));
    }


    @GetMapping("pub/postMigrateBorrow/{password}/{filename}")
    public void postMigrateBorrow(@PathVariable(value = "password") String password,
                                  @PathVariable(value = "filename") String filename) {
        if (!"@GOFOBAO0701WEIBO----=====".equals(password)) {
            return;
        }

        long curTime = System.currentTimeMillis();
        migrateBorrowBiz.postMigrateBorrowFile(filename);
        log.info("处理时间: " + (System.currentTimeMillis() - curTime));
    }


    @GetMapping("pub/migrateTender/{password}")
    public void migrateTender(@PathVariable(value = "password") String password) {
        if (!"@GOFOBAO0701WEIBO----=====".equals(password)) {
            return;
        }

        long curTime = System.currentTimeMillis();
        migrateTenderBiz.getTenderMigrateFile();
        log.info("处理时间: " + (System.currentTimeMillis() - curTime));
    }


    @GetMapping("pub/migrateProtocol/{password}")
    public void migrateProtocol(@PathVariable(value = "password") String password) {
        if (!"@GOFOBAO0701WEIBO----=====".equals(password)) {
            return;
        }

        long curTime = System.currentTimeMillis();
        migrateProtocolBiz.getProtocolMigrateFile();
        log.info("处理时间: " + (System.currentTimeMillis() - curTime));
    }
}
