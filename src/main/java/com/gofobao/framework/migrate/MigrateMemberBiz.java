package com.gofobao.framework.migrate;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.asset.entity.Asset;
import com.gofobao.framework.asset.service.AssetService;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.NumberHelper;
import com.gofobao.framework.helper.StringHelper;
import com.gofobao.framework.member.entity.UserCache;
import com.gofobao.framework.member.entity.UserThirdAccount;
import com.gofobao.framework.member.entity.Users;
import com.gofobao.framework.member.service.UserCacheService;
import com.gofobao.framework.member.service.UserService;
import com.gofobao.framework.member.service.UserThirdAccountService;
import com.google.common.io.Files;
import com.google.gson.Gson;
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
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class MigrateMemberBiz {
    @Autowired
    UserService userService;

    @Autowired
    UserThirdAccountService userThirdAccountService;

    @Autowired
    AssetService assetService;

    private static final String MIGRATE_PATH = "D:/apollo/migrate";
    private static final String MEMBER_DIR = "member";
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
    private static final String RESULT_MEMBER_FILE_PATH = "D:/Apollo/migrate/member_result/3005-APPZX0110RES-130027-20170418";

    @Autowired
    UserCacheService userCacheService;

    /**
     * 写入存管用户存管
     */
    public void postMemberMigrateFile() throws Exception {
        final Date nowDate = new Date();
        File file = new File(RESULT_MEMBER_FILE_PATH);
        if (!file.exists()) {
            log.error("文件不存在");
            return;
        }

        BufferedReader reader = null;
        try {
            reader = Files.newReader(file, Charset.forName("gbk"));
        } catch (FileNotFoundException e) {
            log.error("读取文件异常", e);
            return;
        }

        File errorFile = new File(RESULT_MEMBER_FILE_PATH + "_error");


        Stream<String> lines = reader.lines();

        Map<Long, String> errorUserIdMap = new HashMap<>();
        Map<Long, String> successUserIdMap = new HashMap<>();
        lines.forEach((String item) -> {
            byte[] gbks = null;
            try {
                gbks = item.getBytes("gbk");
                String flag = FormatHelper.getStrForGBK(gbks, 39, 40);
                String idStr = FormatHelper.getStrForGBK(gbks, 104, 164);
                Long id = NumberHelper.toLong(idStr);

                if ("F".equals(flag)) {
                    String errorCode = FormatHelper.getStrForGBK(gbks, 40, 43);
                    errorUserIdMap.put(id, ERROR_MSSAGE.get(errorCode));
                } else {
                    String accountId = FormatHelper.getStrForGBK(gbks, 0, 19);
                    successUserIdMap.put(id, accountId);
                }
            } catch (Exception e) {
                log.error("转码错误", e);
                return;
            }
        });


        List<String> errorUserId = new ArrayList(errorUserIdMap.keySet());
        List<String> successUserId = new ArrayList(successUserIdMap.keySet());
        Specification<Users> es = Specifications
                .<Users>and()
                .in("id", errorUserId.toArray())
                .build();

        List<Users> errorUsers = userService.findList(es);

        if (!CollectionUtils.isEmpty(errorUsers)) {
            BufferedWriter errorWriter = null;
            try {
                errorWriter = Files.newWriter(errorFile, StandardCharsets.UTF_8);
            } catch (FileNotFoundException e) {
                log.error("获取错误读取流程异常", e);
                return;
            }

            for (Users item : errorUsers) {
                String msg = errorUserIdMap.get(item.getId());
                StringBuffer stringBuffer = new StringBuffer();
                String userName = item.getUsername();
                if (StringUtils.isEmpty(userName)) {
                    userName = item.getPhone();
                }
                if (StringUtils.isEmpty(userName)) {
                    userName = item.getEmail();
                }
                stringBuffer
                        .append(item.getId())
                        .append("|")
                        .append(userName)
                        .append("|")
                        .append(msg);
                try {
                    errorWriter.write(stringBuffer.toString());
                    errorWriter.newLine();
                } catch (IOException e) {
                    log.error("写入用户开户错误消息异常", e);
                    return;
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

        Specification<UserThirdAccount> uts = Specifications
                .<UserThirdAccount>and()
                .in("userId", successUserId.toArray())
                .build();
        List<UserThirdAccount> userThirdAccountList = userThirdAccountService.findList(uts);
        Map<Long, UserThirdAccount> userThirdAccountMap = userThirdAccountList.stream().collect(Collectors.toMap(UserThirdAccount::getUserId, Function.identity()));

        Specification<Users> ss = Specifications
                .<Users>and()
                .in("id", successUserId.toArray())
                .build();
        List<Users> successUsers = userService.findList(ss);
        List<UserThirdAccount> userThirdAccountAll = new ArrayList<>() ;
        for (Users user : successUsers) {
            String accountId = successUserIdMap.get(user.getId());
            if (StringUtils.isEmpty(accountId)) {
                throw new Exception("当前用户账号为空");
            }

            UserThirdAccount checkeState = userThirdAccountMap.get(user.getId()) ;
            if (ObjectUtils.isEmpty(checkeState)) {
                UserThirdAccount userThirdAccount = new UserThirdAccount();
                userThirdAccount.setIdNo(user.getCardId());
                userThirdAccount.setUpdateAt(nowDate);
                userThirdAccount.setCreateAt(nowDate);
                userThirdAccount.setIdNo(user.getCardId());
                userThirdAccount.setCardNoBindState(0);
                userThirdAccount.setUserId(user.getId());
                userThirdAccount.setAccountId(accountId);
                userThirdAccount.setPasswordState(0);
                userThirdAccount.setAcctUse(0);
                userThirdAccount.setChannel(0);
                userThirdAccount.setIdType(1);
                userThirdAccount.setDel(0);
                userThirdAccount.setMobile(user.getPhone());
                userThirdAccount.setName(user.getRealname());
                userThirdAccountAll.add(userThirdAccount) ;
            }
        }

        userThirdAccountService.save(userThirdAccountAll) ;
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

    public static String getEncoding(String str) {
        String encodes[] = {"GB2312", "ISO-8859-1", "UTF-8", "GBK"};
        for (String encode : encodes) {
            try {
                if (str.equals(new String(str.getBytes(encode), encode))) {
                    return encode;
                }
            } catch (Exception exception) {
            }
        }
        return "";
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
        BufferedWriter gbk = null;
        try {
            File normalFile = FileHelper.createFile(MIGRATE_PATH, MEMBER_DIR, fileName);
            gbk = Files.newWriter(normalFile, Charset.forName("gbk"));
        } catch (Exception e) {
            return;
        }


        BufferedWriter errorWirter = null;
        FileWriter fileWriter = null;
        try {
            File errorFile = FileHelper.createFile(MIGRATE_PATH, MEMBER_DIR, fileName + "_error");
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


            Specification<UserCache> userCacheSpecification = Specifications
                    .<UserCache>and()
                    .in("userId", userId.toArray())
                    .build();
            List<UserCache> userCacheList = userCacheService.findList(userCacheSpecification);
            Map<Long, UserCache> userCacheMap = userCacheList.stream().collect(Collectors.toMap(UserCache::getUserId, Function.identity()));
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

                remark.append("|").append(item.getId()).append("|").append(userName).append("|");
                if (StringUtils.isEmpty(item.getPhone())) {  // 判断是否有手机号
                    legitimateState = false;
                    remark.append("[未绑定手机号码]");
                }

                if (StringUtils.isEmpty(item.getRealname())) {  // 未实名
                    legitimateState = false;
                    remark.append("[未实名]");
                }

                if (StringUtils.isEmpty(item.getCardId())) {  // 判断是否有身份证并且身份比较特殊的
                    legitimateState = false;
                    remark.append("[未绑定身份证]");
                } else {
                    if (item.getCardId().length() != 18) {
                        legitimateState = false;
                        remark.append("[身份证不合规]");
                    }
                }

                if (item.getIsLock()) {
                    legitimateState = false;
                    remark.append("[账户被冻结]");
                }

                Asset asset = assetMap.get(item.getId());
                boolean assetFlat = asset.getNoUseMoney() + asset.getUseMoney() > 0 || asset.getCollection() > 0 || asset.getPayment() > 0;
                if (assetFlat || "borrower".equals(item.getType())) {
                    if (!legitimateState) {
                        try {
                            // 查询充值总额 / 和提现总额
                            UserCache userCache = userCacheMap.get(item.getId());
                            Long rechargeTotal = userCache.getRechargeTotal();
                            if (ObjectUtils.isEmpty(rechargeTotal)) {
                                rechargeTotal = 0L;
                            }
                            Long cashTotal = userCache.getCashTotal();
                            if (ObjectUtils.isEmpty(cashTotal)) {
                                cashTotal = 0L;
                            }

                            remark.append("|")
                                    .append(StringHelper.formatDouble((asset.getUseMoney() + asset.getNoUseMoney()) / 100D, true))
                                    .append("|")
                                    .append(DateHelper.dateToString(asset.getUpdatedAt()))
                                    .append("|")
                                    .append(item.getIsLock() ? "冻结" : "未冻结")
                                    .append("|")
                                    .append(StringHelper.formatDouble(asset.getCollection() / 100D, true))
                                    .append("|")
                                    .append(StringHelper.formatDouble(asset.getPayment() / 100D, true))
                                    .append("|")
                                    .append(StringHelper.formatDouble(rechargeTotal / 100D, true))
                                    .append("|")
                                    .append(StringHelper.formatDouble(cashTotal / 100D, true));
                            errorWirter.write(remark.toString());
                            errorWirter.newLine();
                        } catch (IOException e) {
                            log.error("写入错误文件失败", e);
                            return;
                        }
                    } else { // 写入正确的文件
                        try {
                            String idxSexStr = item.getCardId().substring(16, 17);
                            int idxSex = Integer.parseInt(idxSexStr) % 2;
                            String sex = (idxSex == 1) ? "2" : "1";
                            String idStr = item.getId() + "";
                            StringBuffer text = new StringBuffer();
                            text.append(FormatHelper.appendByTail(item.getCardId(), 18));
                            text.append(FormatHelper.appendByTail("01", 2));
                            text.append(FormatHelper.appendByTail(item.getRealname(), 60));
                            text.append(FormatHelper.appendByTail(sex, 1));
                            text.append(FormatHelper.appendByTail(item.getPhone(), 12));
                            text.append(FormatHelper.appendByTail("0", 1));
                            text.append(FormatHelper.appendByTail("", 40));
                            text.append(FormatHelper.appendByTail(idStr, 60));
                            text.append(FormatHelper.appendByTail("", 9));
                            text.append(FormatHelper.appendByTail("", 30));
                            text.append(FormatHelper.appendByTail("", 20));
                            text.append(FormatHelper.appendByTail("2", 1));
                            text.append(FormatHelper.appendByTail("", 2));
                            text.append(FormatHelper.appendByTail("", 100));
                            text.append(FormatHelper.appendByTail("", 42));
                            text.append(FormatHelper.appendByTail("", 18));
                            text.append(FormatHelper.appendByTail("", 17));
                            try {
                                gbk.write(text.toString());
                                gbk.newLine();
                            } catch (IOException e) {
                                log.error("写入正确文件失败", e);
                                return;
                            }
                        } catch (Exception e) {
                            log.error(new Gson().toJson(item));
                            log.error("写入正确文件失败", e);
                            return;
                        }

                    }
                }
            }

        } while (realSize == pageSize);

        try {
            if (gbk != null) {
                gbk.flush();
                gbk.close();
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


    private char[] getChars(byte[] bytes) {
        Charset cs = Charset.forName("gbk");
        ByteBuffer bb = ByteBuffer.allocate(bytes.length);
        bb.put(bytes);
        bb.flip();
        CharBuffer cb = cs.decode(bb);

        return cb.array();
    }


}
