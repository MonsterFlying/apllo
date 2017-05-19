package com.gofobao.framework;

import com.gofobao.framework.api.helper.CertHelper;
import com.gofobao.framework.api.helper.JixinManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

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

	@Value("${gofobao.javaDamain}")
	private String domain ;

	@Autowired
	CertHelper certHelper ;

	@Autowired
	JixinManager jixinManager ;


	@Test
	public void contextLoads() {

    }

}
