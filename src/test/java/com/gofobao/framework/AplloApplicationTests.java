package com.gofobao.framework;

import com.gofobao.framework.system.service.impl.DictServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class AplloApplicationTests {

	@Autowired
	private DictServiceImpl dictService;

	@Test
	public void contextLoads() {
		try {
			System.out.println("11");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
