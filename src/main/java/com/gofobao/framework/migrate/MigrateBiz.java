package com.gofobao.framework.migrate;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.asset.entity.Asset;
import com.gofobao.framework.asset.service.AssetService;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.NumberHelper;
import com.gofobao.framework.member.entity.UserThirdAccount;
import com.gofobao.framework.member.entity.Users;
import com.gofobao.framework.member.service.UserService;
import com.gofobao.framework.member.service.UserThirdAccountService;
import com.google.common.io.Files;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class MigrateBiz {
    @Autowired
    UserService userService;

    @Autowired
    UserThirdAccountService userThirdAccountService;

    @Autowired
    AssetService assetService;

    private static final String MIGRATE_PATH = "D:/apollo/migrate";
    private static final String MEMBER_DIR = "member";
    private static final String BORROW_DIR = "borrow";
    /**
     * 银行编号
     */
    private static final String BANK_NO = "3005";
    /**
     * 产品编号
     */
    private static final String PROUDCT_NO = "0110";

    /**
     * 迁移结果文件
     */
    private static final String RESULT_MEMBER_FILE_PATH = "";

    /**
     * 写入存管用户存管
     */
    public void postMemberMigrateFile() {
        final Date nowDate = new Date();
        File file = new File(RESULT_MEMBER_FILE_PATH);
        if (!file.exists()) {
            log.error("文件不存在");
            return;
        }

        BufferedReader reader = null;
        try {
            reader = Files.newReader(file, StandardCharsets.UTF_8);
        } catch (FileNotFoundException e) {
            log.error("读取文件异常", e);
            return;
        }

        File errorFile = new File(RESULT_MEMBER_FILE_PATH + "_error");


        Stream<String> lines = reader.lines();
        List<String> errorUserId = new ArrayList<>();
        List<String> successUserId = new ArrayList<>();
        lines.forEach((String item) -> {
            String flag = item.substring(39, 40);
            String idStr = item.substring(104, 164);
            if ("F".equals(flag)) {
                errorUserId.add(idStr);
            } else {
                successUserId.add(idStr);
            }
        });
        Specification<Users> es = Specifications
                .<Users>and()
                .in("userId", errorUserId.toArray())
                .build();
        List<Users> errorUsers = userService.findList(es);
        Map<Long, Users> errorMap = errorUsers.stream().collect(Collectors.toMap(Users::getId, Function.identity()));
        Specification<Users> ss = Specifications
                .<Users>and()
                .in("userId", successUserId.toArray())
                .build();
        List<Users> successUsers = userService.findList(es);
        Map<Long, Users> successMap = successUsers.stream().collect(Collectors.toMap(Users::getId, Function.identity()));

        List<String> fileText = reader.lines().collect(Collectors.toList());
        BufferedWriter errorWriter = null;
        try {
            errorWriter = Files.newWriter(errorFile, StandardCharsets.UTF_8);
        } catch (FileNotFoundException e) {
            log.error("获取错误读取流程异常", e);
            return;
        }

        for (String item : fileText) {
            String flag = item.substring(39, 40);
            String idStr = item.substring(104, 164);
            errorUserId.add(idStr);
            long id = NumberHelper.toLong(idStr);  // id
            StringBuffer stringBuffer = new StringBuffer();
            String errorCode = item.substring(40, 43);
            if ("F".equals(flag)) {
                Users user = errorMap.get(id);
                String userName = user.getUsername();
                if (StringUtils.isEmpty(userName)) {
                    userName = user.getPhone();
                }
                if (StringUtils.isEmpty(userName)) {
                    userName = user.getEmail();
                }

                stringBuffer.append(id).append("|").append(user.getPhone()).append("|").append(userName).append("|").append(getErrorMsg(errorCode));
                try {
                    errorWriter.write(stringBuffer.toString());
                    errorWriter.newLine();
                } catch (IOException e) {
                    log.error("写入错误消息异常", e);
                    return;
                }
            } else {
                Users user = successMap.get(id);
                String accountId = item.substring(0, 19);  //账号
                UserThirdAccount checkeState = userThirdAccountService.findByUserId(id);
                if (ObjectUtils.isEmpty(checkeState)) {
                    UserThirdAccount userThirdAccount = new UserThirdAccount();
                    userThirdAccount.setIdNo(user.getCardId());
                    userThirdAccount.setUpdateAt(nowDate);
                    userThirdAccount.setCreateAt(nowDate);
                    userThirdAccount.setIdNo(user.getCardId());
                    userThirdAccount.setCardNoBindState(0);
                    userThirdAccount.setUserId(id);
                    userThirdAccount.setAccountId(accountId);
                    userThirdAccount.setPasswordState(0);
                    userThirdAccount.setAcctUse(0);
                    userThirdAccount.setChannel(0);
                    userThirdAccount.setIdType(1);
                    userThirdAccount.setMobile(user.getPhone());
                    userThirdAccount.setName(user.getRealname());
                    userThirdAccountService.save(userThirdAccount);
                }

            }
        }

        if (errorWriter != null) {
            try {
                errorWriter.flush();
                errorWriter.close();
            } catch (Exception e) {
                log.error("解除文件错误", e);
            }
        }
    }

    static Map<String, String> ERROR_MSSAGE = new HashMap<>();

    static {
        ERROR_MSSAGE.put("000", "成功");
        ERROR_MSSAGE.put("101", "证件类型或证件编号非法");
        ERROR_MSSAGE.put("102", "姓名字段不能为空");
        ERROR_MSSAGE.put("103", "姓名字段非法");
        ERROR_MSSAGE.put("104", "性别字段不能为空");
        ERROR_MSSAGE.put("105", "性别字段非法");
        ERROR_MSSAGE.put("106", "手机号码不能为空或已被其他客户使用");
        ERROR_MSSAGE.put("107", "该手机号已被使用");
        ERROR_MSSAGE.put("108", "该客户未满18岁或重复开户");
        ERROR_MSSAGE.put("109", "请求方用户ID不能为空");
        ERROR_MSSAGE.put("111", "未设置理财预约");
        ERROR_MSSAGE.put("112", "未设置约定存期");
        ERROR_MSSAGE.put("113", "银行未开通该基金公司");
        ERROR_MSSAGE.put("114", "靠档产品无法开立基金账户");
        ERROR_MSSAGE.put("115", "基金产品无法开立靠档账户");
        ERROR_MSSAGE.put("116", "靠档产品无法开立基金账户");
        ERROR_MSSAGE.put("117", "基金产品无法开立活期账户");
        ERROR_MSSAGE.put("118", "活期产品无法开立靠档账户");
        ERROR_MSSAGE.put("119", "活期产品无法开立基金账户");
    }

    private static String getErrorMsg(String code) {
        return ERROR_MSSAGE.get(code);
    }

    /**
     * 获取用户迁移数据
     */
    public void getMemberMigrateFile() {
        Date nowDate = new Date();
        String fileName = String.format("%s-APPZX%s-%s-%s", BANK_NO, PROUDCT_NO,
                DateHelper.dateToString(nowDate, DateHelper.DATE_FORMAT_HMS_NUM),
                DateHelper.dateToString(nowDate, DateHelper.DATE_FORMAT_YMD_NUM));
        // 创建文件
        BufferedWriter nornalWirter = null;
        try {
            File normalFile = createFile(MIGRATE_PATH, MEMBER_DIR, fileName);
            nornalWirter = Files.newWriter(normalFile, Charset.forName("UTF-8"));
        } catch (Exception e) {
            return;
        }


        BufferedWriter errorWirter = null;
        try {
            File errorFile = createFile(MIGRATE_PATH, MEMBER_DIR, fileName + "_error");
            errorWirter = Files.newWriter(errorFile, Charset.forName("UTF-8"));
        } catch (Exception e) {
            return;
        }


        int realSize = 0, pageIndex = 0, pageSize = 2000;
        do {
            Pageable pageable = new PageRequest(pageIndex, pageSize);
            Page<Users> usersPage = userService.findAll(pageable);
            if (ObjectUtils.isEmpty(usersPage)) {
                log.error("查询user失败");
                return;
            }

            realSize = usersPage.getSize();
            pageIndex++;
            List<Users> userList = usersPage.getContent();
            if (CollectionUtils.isEmpty(userList)) {
                break;
            }

            List<Long> userId = userList.stream().map(users -> users.getId()).collect(Collectors.toList());
            Specification<Asset> specification = Specifications
                    .<Asset>and()
                    .in("userId", userId.toArray())
                    .build();
            List<Asset> assetList = assetService.findList(specification);
            Map<Long, Asset> assetMap = assetList.stream().collect(Collectors.toMap(Asset::getUserId, Function.identity()));

            for (Users item : userList) {
                boolean legitimateState = true;
                StringBuffer remark = new StringBuffer();
                String userName = item.getUsername();
                if (StringUtils.isEmpty(userName)) {
                    userName = item.getPhone();
                }
                if (StringUtils.isEmpty(userName)) {
                    userName = item.getEmail();
                }

                remark.append("|").append(item.getId()).append("|").append(userName);
                if (StringUtils.isEmpty(item.getPhone())) {  // 判断是否有手机号
                    legitimateState = false;
                    remark.append("|未绑定手机号码");
                }

                if (StringUtils.isEmpty(item.getRealname())) {  // 未实名
                    legitimateState = false;
                    remark.append("|未实名");
                }

                if (StringUtils.isEmpty(item.getCardId())) {  // 判断是否有身份证并且身份比较特殊的
                    legitimateState = false;
                    remark.append("|未绑定身份证");
                } else {
                    if (item.getCardId().length() != 18) {
                        legitimateState = false;
                        remark.append("|身份证不合规");
                    }
                }

                Asset asset = assetMap.get(item.getId());
                boolean assetFlat = asset.getNoUseMoney() + asset.getUseMoney() > 0 || asset.getCollection() > 0 || asset.getPayment() > 0;
                if (!legitimateState) {
                    if (assetFlat) {
                        try {
                            errorWirter.write(remark.toString());
                            errorWirter.newLine();
                        } catch (IOException e) {
                            log.error("写入错误文件失败", e);
                            return;
                        }
                    }

                } else { // 写入正确的文件
                    if (assetFlat) {
                        try {
                            StringBuffer text = new StringBuffer();
                            text.append(format(item.getCardId(), 18));
                            text.append("01");
                            text.append(format(item.getRealname(), 60));
                            String idxSexStr = item.getCardId().substring(16, 17);
                            int idxSex = Integer.parseInt(idxSexStr) % 2;
                            String sex = (idxSex == 1) ? "2" : "1";
                            text.append(sex);
                            text.append(format(item.getPhone(), 12));
                            text.append("0");
                            text.append(format("", 40));
                            text.append(format(item.getId() + "", 60));
                            text.append(format("", 9));
                            text.append(format("", 30));
                            text.append(format("", 20));
                            text.append("2");
                            text.append(format("", 2));
                            text.append(format("", 100));
                            text.append(format("", 42));
                            text.append(format("", 18));
                            text.append(format("", 17));

                            try {
                                nornalWirter.write(text.toString());
                                nornalWirter.newLine();
                            } catch (IOException e) {
                                log.error("写入正确文件失败", e);
                                return;
                            }
                        } catch (Exception e) {
                            log.error("写入正确文件失败", e);
                            return;
                        }
                    }
                }
            }

        } while (realSize == pageSize);

        try {
            if (nornalWirter != null) {
                nornalWirter.flush();
                nornalWirter.close();
            }
            if (errorWirter != null) {
                errorWirter.flush();
                errorWirter.close();
            }
        } catch (Exception e) {
            log.error("关闭资源错误");
        }
        log.info("用户迁移文件成功");
    }

    private String format(String valueStr, int length) throws Exception {
        StringBuffer value = new StringBuffer(valueStr);
        if (value.length() < length) {
            int leafLength = length - value.length();
            for (int i = 0; i < leafLength; i++) {
                value.append(" ");
            }

            return value.toString();
        } else if (value.length() == length) {
            return value.toString();
        } else {
            throw new Exception("写入范围超标");
        }
    }

    /**
     * 创建文件
     *
     * @param pPath
     * @param sPath
     * @param fileName
     * @return
     */
    private File createFile(String pPath, String sPath, String fileName) throws Exception {
        File dir = new File(pPath + "/" + sPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File file = new File(dir, fileName);
        if (file.exists()) {
            File backDir = new File(pPath + "/back/" + sPath);
            if (!backDir.exists()) {
                backDir.mkdirs();
            }
            File backFile = new File(backDir, fileName + System.currentTimeMillis());
            try {
                Files.move(file, backFile);
            } catch (IOException e) {
                log.error("移动文件异常", e);
                throw new Exception(e);
            }
            file.delete();
            file = new File(dir, fileName);
        }

        return file;
    }

}
