package com.gofobao.framework;


import com.gofobao.framework.migrate.MigrateBorrowBiz;
import com.gofobao.framework.migrate.MigrateMemberBiz;
import com.gofobao.framework.migrate.MigrateProtocolBiz;
import com.gofobao.framework.migrate.MigrateTenderBiz;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class ApolloMigrateTests {

    @Autowired
    MigrateMemberBiz migrateMemberBiz;

    @Autowired
    MigrateBorrowBiz migrateBorrowBiz;

    @Autowired
    MigrateTenderBiz migrateTenderBiz;

    @Autowired
    MigrateProtocolBiz migrateProtocolBiz;

    /**
     * 获取会员迁移数据
     */
    @Test
    public void testGetMigrateMemberFile() {
        migrateMemberBiz.getMemberMigrateFile();
    }


    /**
     * 提交用户迁移数据
     */
    @Test
    public void testPostMigrateMemberFile() {
        try {
            migrateMemberBiz.postMemberMigrateFile();
        } catch (Exception e) {
            log.error("提交用户迁移文件失败", e);
        }
    }

    /**
     * 获取标的迁移文件
     */
    @Test
    public void testGetMigrateBorrowFile() {
        migrateBorrowBiz.getBorrowMigrateFile();
    }


    /**
     * 提交标的迁移数据
     */
    @Test
    public void testPostMigrateBorrowFile() {
        migrateBorrowBiz.postMigrateBorrowFile();
    }


    /**
     * 获取投标迁移数据
     */
    @Test
    public void testGetMigarateTenderBiz() {
        migrateTenderBiz.getTenderMigrateFile();
    }


    /**
     * 提交投标迁移数据
     */
    @Test
    public void testPostMigarateTenderBiz() {
        migrateTenderBiz.postMigrateTenderFile();
    }


    @Test
    public void testgetMigarateProtocolBiz(){
        migrateProtocolBiz.getProtocolMigrateFile();
    }

    @Test
    public void testPostMigrateProtocolBiz(){
        migrateProtocolBiz.postProtocolMigrateFile();
    }

}
