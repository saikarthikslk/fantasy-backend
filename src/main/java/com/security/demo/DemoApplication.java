package com.security.demo;

import com.security.demo.controller.NotificationController;
import com.security.demo.repo.TeamRepo;
import com.security.demo.service.HttpCaller;
import com.security.demo.service.LoadGameService;
import com.security.demo.service.MatchLoader;
import com.security.demo.service.MatchesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@SpringBootApplication
@EnableScheduling
public class DemoApplication {



	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	@Autowired
	HttpCaller httpCaller;
	@Autowired
	LoadGameService gameService;

	@Autowired
	MatchLoader matchLoader;

	private static List<CompletableFuture> futures = new ArrayList<>();


	@Scheduled(fixedRate = 18000000)
	public void run() throws IOException, InterruptedException {
//		httpCaller.fetchsavematches();
//		httpCaller.loadTeamData();
//		httpCaller.loadSquaddata();


	}

	//fetch daily matches
	@Scheduled(fixedRate = 86400000)
	public void run1() throws IOException, InterruptedException {
		httpCaller.fetchsavematches();

	}
//	//run every 30mins
	@Scheduled(fixedRate = 900000)
	public void run2() throws IOException, InterruptedException {
		matchLoader.fetchmatchsabouttostart();

	}
//
//	run every 30mins
	@Scheduled(fixedRate = 900000)
	public void rungame() throws IOException, InterruptedException {
		matchLoader.runmatch();
	}

	@Scheduled(fixedRate = 90000)
	public void loadsquads() throws IOException, InterruptedException {
		matchLoader.loadSquads();
	}

//
//	@Autowired
//	NotificationController notificationController;
//





}
