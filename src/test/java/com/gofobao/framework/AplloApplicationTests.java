package com.gofobao.framework;

import com.gofobao.framework.api.helper.CertHelper;
import com.gofobao.framework.core.helper.RandomHelper;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.OKHttpHelper;
import com.gofobao.framework.helper.StringHelper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;
import java.util.TreeMap;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class AplloApplicationTests {
/**
 *   version: 10 #版本号
 instCode: "00170001" #机构代码
 bankCode: "30050000" #交易机构代码
 */

    @Value("${jixin.version}")
	private String version;

    @Value("${jixin.instCode}")
	private String instCode ;

	@Value("${jixin.bankCode}")
	private String bankCode ;

	@Value("${jixin.url}")
	private String url ;

	@Autowired
	CertHelper certHelper ;


	@Test
	public void contextLoads() {
		Map<String, String> reqMap = new TreeMap<>() ;

		reqMap.put("version", version);
		reqMap.put("instCode", instCode);
		reqMap.put("bankCode", bankCode);
		reqMap.put("txDate", DateHelper.getDate());
		reqMap.put("txTime", DateHelper.getTime());
		reqMap.put("seqNo", RandomHelper.generateNumberCode(6));
		reqMap.put("channel", "000002");
		reqMap.put("txCode", "accountOpen");
		reqMap.put("idType","01");
		reqMap.put("idNo","110101199801013791");
		reqMap.put("name","李四");
		reqMap.put("mobile","18171025630");
		reqMap.put("cardNo","6222988812340037");
		reqMap.put("email","");
		reqMap.put("acctUse","00000");
		reqMap.put("acqRes","");

        String sign = StringHelper.mergeMap(reqMap);
        reqMap.put("sign", certHelper.doSign(sign));

        String s = OKHttpHelper.postJson(url, new Gson().toJson(reqMap), null);

        log.info(s);


    }

}
