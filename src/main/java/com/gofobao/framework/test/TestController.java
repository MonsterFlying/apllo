package com.gofobao.framework.test;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.api.contants.ChannelContant;
import com.gofobao.framework.api.contants.JixinResultContants;
import com.gofobao.framework.api.helper.JixinManager;
import com.gofobao.framework.api.helper.JixinTxCodeEnum;
import com.gofobao.framework.api.model.account_details_query.AccountDetailsQueryRequest;
import com.gofobao.framework.api.model.account_details_query.AccountDetailsQueryResponse;
import com.gofobao.framework.api.model.balance_query.BalanceQueryRequest;
import com.gofobao.framework.api.model.balance_query.BalanceQueryResponse;
import com.gofobao.framework.asset.entity.Asset;
import com.gofobao.framework.asset.service.AssetService;
import com.gofobao.framework.common.assets.AssetChange;
import com.gofobao.framework.common.assets.AssetChangeProvider;
import com.gofobao.framework.common.assets.AssetChangeTypeEnum;
import com.gofobao.framework.common.rabbitmq.MqConfig;
import com.gofobao.framework.common.rabbitmq.MqHelper;
import com.gofobao.framework.common.rabbitmq.MqQueueEnum;
import com.gofobao.framework.common.rabbitmq.MqTagEnum;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.NumberHelper;
import com.gofobao.framework.member.entity.UserThirdAccount;
import com.gofobao.framework.member.service.UserThirdAccountService;
import com.gofobao.framework.migrate.FileHelper;
import com.gofobao.framework.system.entity.ThirdBatchLog;
import com.gofobao.framework.system.service.ThirdBatchLogService;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.output.FileWriterWithEncoding;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by Zeke on 2017/6/21.
 */
@RestController
@Api(description = "测试")
@RequestMapping
@Slf4j
public class TestController {
    @Autowired
    MqHelper mqHelper;
    final Gson GSON = new GsonBuilder().create();
    @Autowired
    private ThirdBatchLogService thirdBatchLogService;
    @Autowired
    AssetChangeProvider assetChangeProvider;
    @Autowired
    private UserThirdAccountService userThirdAccountService;
    @Autowired
    private AssetService assetService;
    @Autowired
    private JixinManager jixinManager;

    @RequestMapping("/test/pub/batch/deal/{sourceId}/{batchNo}")
    public void batchDeal(@PathVariable("sourceId") String sourceId, @PathVariable("batchNo") String batchNo, @PathVariable("langlang") String langlang) {
        if (langlang.equals("langlang")) {
            Specification<ThirdBatchLog> tbls = Specifications
                    .<ThirdBatchLog>and()
                    .eq("batchNo", batchNo)
                    .eq("sourceId", sourceId)
                    .build();
            List<ThirdBatchLog> thirdBatchLogList = thirdBatchLogService.findList(tbls);
            MqConfig mqConfig = new MqConfig();
            mqConfig.setQueue(MqQueueEnum.RABBITMQ_THIRD_BATCH);
            mqConfig.setTag(MqTagEnum.BATCH_DEAL);

            ImmutableMap<String, String> body = ImmutableMap
                    .of(MqConfig.SOURCE_ID, sourceId,
                            MqConfig.BATCH_NO, batchNo,
                            MqConfig.MSG_TIME, DateHelper.dateToString(new Date()),
                            MqConfig.ACQ_RES, thirdBatchLogList.get(0).getAcqRes()
                    );

            mqConfig.setMsg(body);
            try {
                log.info(String.format("tenderThirdBizImpl thirdBatchRepayAllRunCall send mq %s", GSON.toJson(body)));
                mqHelper.convertAndSend(mqConfig);
            } catch (Throwable e) {
                log.error("tenderThirdBizImpl thirdBatchRepayAllRunCall send mq exception", e);
            }
        }
    }

    @RequestMapping("/test/pub/amendAsset/{langlang}")
    public void amendAsset(@PathVariable("langlang") String langlang) {
        if (langlang.equals("langlang")) {
            String seqNo = assetChangeProvider.getSeqNo(); // 资产记录流水号
            String groupSeqNo = assetChangeProvider.getGroupSeqNo(); // 资产记录分组流水号
            AssetChange assetChange = new AssetChange();
            assetChange.setMoney(77994);
            assetChange.setUserId(100009l);
            assetChange.setRemark(String.format("验证服可用金额数据修正，金额：%s元，userId：%s", 779.94, 100009));
            assetChange.setSeqNo(seqNo);
            assetChange.setGroupSeqNo(groupSeqNo);
            assetChange.setSourceId(100009l);
            assetChange.setType(AssetChangeTypeEnum.amendUseMoney);
            try {
                assetChangeProvider.commonAssetChange(assetChange);
            } catch (Exception e) {
                log.error(String.format("资金变动失败：%s", assetChange));
            }

            assetChange = new AssetChange();
            assetChange.setMoney(8656);
            assetChange.setUserId(100002l);
            assetChange.setRemark(String.format("验证服数据可用金额修正，金额：%s元，userId：%s", 86.56, 100002));
            assetChange.setSeqNo(seqNo);
            assetChange.setGroupSeqNo(groupSeqNo);
            assetChange.setSourceId(100002l);
            assetChange.setType(AssetChangeTypeEnum.amendUseMoney);
            try {
                assetChangeProvider.commonAssetChange(assetChange);
            } catch (Exception e) {
                log.error(String.format("资金变动失败：%s", assetChange));
            }

            assetChange = new AssetChange();
            assetChange.setMoney(1834);
            assetChange.setUserId(100001l);
            assetChange.setRemark(String.format("验证服数据可用金额修正，金额：%s元，userId：%s", 18.34, 100001));
            assetChange.setSeqNo(seqNo);
            assetChange.setGroupSeqNo(groupSeqNo);
            assetChange.setSourceId(100001l);
            assetChange.setType(AssetChangeTypeEnum.amendUseMoney);
            try {
                assetChangeProvider.commonAssetChange(assetChange);
            } catch (Exception e) {
                log.error(String.format("资金变动失败：%s", assetChange));
            }

            assetChange = new AssetChange();
            assetChange.setMoney(756921);
            assetChange.setUserId(100001l);
            assetChange.setRemark(String.format("验证服数据冻结金额修正，金额：%s元，userId：%s", 7, 569.21, 100001));
            assetChange.setSeqNo(seqNo);
            assetChange.setGroupSeqNo(groupSeqNo);
            assetChange.setSourceId(100001l);
            assetChange.setType(AssetChangeTypeEnum.amendNotUseMoney);
            try {
                assetChangeProvider.commonAssetChange(assetChange);
            } catch (Exception e) {
                log.error(String.format("资金变动失败：%s", assetChange));
            }

            assetChange = new AssetChange();
            assetChange.setMoney(691685);
            assetChange.setUserId(100001l);
            assetChange.setRemark(String.format("验证服数据待还金额修正，金额：%s元，userId：%s", 6, 916.85, 100001));
            assetChange.setSeqNo(seqNo);
            assetChange.setGroupSeqNo(groupSeqNo);
            assetChange.setSourceId(100001l);
            assetChange.setType(AssetChangeTypeEnum.amendPayment);
            try {
                assetChangeProvider.commonAssetChange(assetChange);
            } catch (Exception e) {
                log.error(String.format("资金变动失败：%s", assetChange));
            }
        }
    }

    @RequestMapping("/test/pub/amendAsset/{accountId}/{langlang}")
    public void assetDetail(@PathVariable("accountId") String accountId, @PathVariable("langlang") String langlang) {
        if (langlang.equals("langlang")) {
            BalanceQueryRequest balanceQueryRequest = new BalanceQueryRequest();
            balanceQueryRequest.setChannel(ChannelContant.HTML);
            balanceQueryRequest.setAccountId(accountId);
            BalanceQueryResponse balanceQueryResponse = jixinManager.send(JixinTxCodeEnum.BALANCE_QUERY, balanceQueryRequest, BalanceQueryResponse.class);
            System.out.println(balanceQueryResponse);

            AccountDetailsQueryRequest request = new AccountDetailsQueryRequest();
            request.setAccountId(accountId);
            request.setStartDate("20170828");
            request.setEndDate("20171006");
            request.setChannel(ChannelContant.HTML);
            request.setType("0"); // 转入
            //request.setTranType("7820"); // 线下转账的
            request.setPageSize(String.valueOf(30));
            request.setPageNum(String.valueOf(1));
            AccountDetailsQueryResponse response = jixinManager.send(JixinTxCodeEnum.ACCOUNT_DETAILS_QUERY, request, AccountDetailsQueryResponse.class);
            System.out.println(response);
        }
    }

  /*  @RequestMapping(value = "/test/csvDownLoad", method = RequestMethod.GET)
    public void csvDownLoad(HttpServletResponse httpServletResponse) throws Exception {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String fileName = "test-";
        fileName += dateFormat.format(new Date());
        fileName += ".csv";

        httpServletResponse.setContentType("application/octet-stream");
        httpServletResponse.addHeader("Content-Disposition", "attachment; filename=" + fileName);
        BufferedInputStream bis = null;
        BufferedOutputStream out = null;
        String path = "d:/tmp.csv";
        File file = new File(path);
        FileWriterWithEncoding fwwe =new FileWriterWithEncoding(file,"UTF-8");
        BufferedWriter bw = new BufferedWriter(fwwe);

        int index = 0;
        int pageNum = 50;

        //1.查询已开户用户
        Specification<UserThirdAccount> usas = Specifications
                .<UserThirdAccount>and()
                .eq("del", 0)
                .build();
        List<UserThirdAccount> userThirdAccountList = new ArrayList<>();
        StringBuffer text = new StringBuffer();
        bw.write("会员id,用户名,正式数据库可用资金,正式数据库冻结资金,存管系统可用资金,存管系统冻结资金,可用差异,冻结差异");
        do {
            userThirdAccountList = userThirdAccountService.findList(usas, new PageRequest(index, pageNum));
            Set<Long> userIds = userThirdAccountList.stream().map(UserThirdAccount::getUserId).collect(Collectors.toSet());
            //2.查询资产记录
            Specification<Asset> as = Specifications
                    .<Asset>and()
                    .in("userId", userIds.toArray())
                    .build();
            List<Asset> assetList = assetService.findList(as);
            Map<Long, Asset> assetMaps = assetList.stream().collect(Collectors.toMap(Asset::getUserId, Function.identity()));

            for (UserThirdAccount userThirdAccount : userThirdAccountList) {

                Asset asset = assetMaps.get(userThirdAccount.getUserId());

                //3.查询存管账户是否为空
                BalanceQueryRequest balanceQueryRequest = new BalanceQueryRequest();
                balanceQueryRequest.setChannel(ChannelContant.HTML);
                balanceQueryRequest.setAccountId(userThirdAccount.getAccountId());
                BalanceQueryResponse balanceQueryResponse = jixinManager.send(JixinTxCodeEnum.BALANCE_QUERY, balanceQueryRequest, BalanceQueryResponse.class);
                if ((ObjectUtils.isEmpty(balanceQueryResponse)) || !balanceQueryResponse.getRetCode().equals(JixinResultContants.SUCCESS)) {
                    String msg = ObjectUtils.isEmpty(balanceQueryResponse) ? "当前网络异常, 请稍后尝试!" : balanceQueryResponse.getRetMsg();
                    log.error(String.format("资金同步: %s,userId:%s", msg, userThirdAccount.getUserId()));
                }

                double currBal = NumberHelper.toDouble(balanceQueryResponse.getCurrBal()) * 100;
                double availBal = NumberHelper.toDouble(balanceQueryResponse.getAvailBal()) * 100;
                double freezeBal = currBal - availBal;
                text.append(asset.getUserId() + ",");
                text.append(userThirdAccount.getName() + ",");
                text.append(asset.getUseMoney() + ",");
                text.append(asset.getNoUseMoney() + ",");
                text.append(availBal + ",");
                text.append(freezeBal + ",");
                text.append(asset.getUseMoney() - availBal + ",");
                text.append(asset.getNoUseMoney() - freezeBal + ",");
                bw.write(text.toString());
            }

            index++;
        } while (userThirdAccountList.size() >= pageNum);

        bw.close();
        fwwe.close();
        try {
            bis = new BufferedInputStream(new FileInputStream(file));
            out = new BufferedOutputStream(httpServletResponse.getOutputStream());
            byte[] buff = new byte[2048];
            while (true) {
                int bytesRead;
                if (-1 == (bytesRead = bis.read(buff, 0, buff.length))){
                    break;
                }
                out.write(buff, 0, bytesRead);
            }
            file.deleteOnExit();
        }
        catch (IOException e) {
            throw e;
        }
        finally{
            try {
                if(bis != null){
                    bis.close();
                }
                if(out != null){
                    out.flush();
                    out.close();
                }
            }
            catch (IOException e) {
                throw e;
            }
        }

    }*/

   /* @RequestMapping("/test/pub/exp/asset")
    public ResponseEntity expCsv() {
        HttpHeaders h = new HttpHeaders();
        h.add("Content-Type", "text/csv; charset=GBK");
        h.setContentDispositionFormData("filename", "foobar.csv");
        StringBuffer dateStr = test();
        return new ResponseEntity(dateStr, h, HttpStatus.OK);
    }

    private StringBuffer test() {

        int index = 0;
        int pageNum = 50;

        //1.查询已开户用户
        Specification<UserThirdAccount> usas = Specifications
                .<UserThirdAccount>and()
                .eq("del", 0)
                .build();
        List<UserThirdAccount> userThirdAccountList = new ArrayList<>();
        StringBuffer text = new StringBuffer();
        text.append(String.format("会员id,用户名,正式数据库可用资金,正式数据库冻结资金,存管系统可用资金,存管系统冻结资金,可用差异,冻结差异\n"));
        do {
            userThirdAccountList = userThirdAccountService.findList(usas, new PageRequest(index, pageNum));
            Set<Long> userIds = userThirdAccountList.stream().map(UserThirdAccount::getUserId).collect(Collectors.toSet());
            //2.查询资产记录
            Specification<Asset> as = Specifications
                    .<Asset>and()
                    .in("userId", userIds.toArray())
                    .build();
            List<Asset> assetList = assetService.findList(as);
            Map<Long, Asset> assetMaps = assetList.stream().collect(Collectors.toMap(Asset::getUserId, Function.identity()));

            for (UserThirdAccount userThirdAccount : userThirdAccountList) {

                Asset asset = assetMaps.get(userThirdAccount.getUserId());

                //3.查询存管账户是否为空
                BalanceQueryRequest balanceQueryRequest = new BalanceQueryRequest();
                balanceQueryRequest.setChannel(ChannelContant.HTML);
                balanceQueryRequest.setAccountId(userThirdAccount.getAccountId());
                BalanceQueryResponse balanceQueryResponse = jixinManager.send(JixinTxCodeEnum.BALANCE_QUERY, balanceQueryRequest, BalanceQueryResponse.class);
                if ((ObjectUtils.isEmpty(balanceQueryResponse)) || !balanceQueryResponse.getRetCode().equals(JixinResultContants.SUCCESS)) {
                    String msg = ObjectUtils.isEmpty(balanceQueryResponse) ? "当前网络异常, 请稍后尝试!" : balanceQueryResponse.getRetMsg();
                    log.error(String.format("资金同步: %s,userId:%s", msg, userThirdAccount.getUserId()));
                }

                double currBal = NumberHelper.toDouble(balanceQueryResponse.getCurrBal()) * 100;
                double availBal = NumberHelper.toDouble(balanceQueryResponse.getAvailBal()) * 100;
                double freezeBal = currBal - availBal;
                text.append(asset.getUserId() + ",");
                text.append(userThirdAccount.getName() + ",");
                text.append(asset.getUseMoney() + ",");
                text.append(asset.getNoUseMoney() + ",");
                text.append(availBal + ",");
                text.append(freezeBal + ",");
                text.append(asset.getUseMoney() - availBal + ",");
                text.append(asset.getNoUseMoney() - freezeBal + ",");
                text.append("\n");
            }

            index++;
        } while (userThirdAccountList.size() >= pageNum);

        return text;
    }*/
}
