package com.security.demo;

import com.security.demo.service.HttpCaller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.IOException;

@SpringBootApplication
@EnableScheduling
public class DemoApplication {



	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	@Autowired
	HttpCaller httpCaller;
	@Scheduled(fixedRate = 1000000)
	public void run() throws IOException, InterruptedException {
		httpCaller.fetchdata("139489/1/1772979600000");
	}

}
