package com.gofobao.framework;

import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.bean.ProducerBean;
import com.gofobao.framework.core.mq.config.AliyunOnsConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class AplloApplication {

	public static void main(String[] args) {
		SpringApplication.run(AplloApplication.class, args);
	}


	@Autowired
	public ProducerBean smsProducerBean ;

	@GetMapping("/send")
	public void controller(){
		Message message = new Message(AliyunOnsConfiguration.TOPIC_SMS, "register",  "mq send timer message test".getBytes()) ;
		smsProducerBean.send(message) ;
	}
}
