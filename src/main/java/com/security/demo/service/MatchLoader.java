package com.security.demo.service;

import com.security.demo.DBmodel.MatchInfoEntity;
import com.security.demo.DBmodel.MatchState;
import com.security.demo.model.Matchinfo;
import com.security.demo.repo.MatchStaterepo;
import com.security.demo.repo.Matchrepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class MatchLoader {
    @Autowired
    Matchrepo matchrepo;
    @Autowired
    HttpCaller httpCaller;

    @Autowired
    LoadGameService gameService;
    @Autowired
    MatchesService matchesService;

    private static List<CompletableFuture> list = new ArrayList<>();
    private static Map<Integer,String> map = new HashMap<>();
    public static List<Integer> matchs = new ArrayList<>();
    public void  runmatch(){

        LoadGameService.sleep(5);
        System.out.println("running");
        List<MatchInfoEntity> matchInfoEntities =  matchrepo.findAll().stream().filter(x->{

            Long start = (x.getStartDate() - System.currentTimeMillis()   )/1000 ;
            if( x.getState().equals("Upcoming") &&  start <=1800 && !map.containsKey(x.getMatchId())) {
                map.put(x.getMatchId(),"Starting");
                matchs.add(x.getMatchId());
                return true;
            } else if (x.getState().equals("Live") && !map.containsKey(x.getMatchId()) ) {
                map.put(x.getMatchId(),"Live");
                matchs.add(x.getMatchId());
                return true;
            }
            return false;

        }).collect(Collectors.toList());
        if(matchInfoEntities.size() >0) {
            matchInfoEntities.forEach( (x) -> {
                CompletableFuture<Boolean> future = CompletableFuture.supplyAsync(() -> {

                    try {
                        gameService.pullscorecard(x.getMatchId());
                    } catch (IOException e) {
                        System.out.println("Error occured while running match " + x.getMatchId());

                    } catch (InterruptedException e) {
                        System.out.println("Error occured while running match " + x.getMatchId());

                    }
                    return true;
                });
                list.add(future);
            } ) ;

           CompletableFuture<Void> all =  CompletableFuture.allOf(list.toArray(new CompletableFuture[0]));
           all.join();
           System.out.println("Matches Completed");

        }
        map = new HashMap<>();
        list = new ArrayList<>();
    }
    public void fetchmatchsabouttostart() throws IOException, InterruptedException {
       List<MatchInfoEntity> matchInfoEntities =  matchrepo.findAll().stream().filter(x->{

           Long start = (x.getStartDate() - System.currentTimeMillis()   )/1000 ;
           if( start >0 && start <= 5400) {
              return true;
           }
           return false;

       }).collect(Collectors.toList());


       for (MatchInfoEntity matchInfoEntity : matchInfoEntities) {
           if(matchInfoEntity.getIsloaded() ==  null) {
               matchInfoEntity.setIsloaded(1);
           }
           if( matchInfoEntity.getIsloaded() <=3) {
               httpCaller.loadSquaddata(matchInfoEntity.getMatchId());
               matchInfoEntity.setIsloaded(matchInfoEntity.getIsloaded() + 1);
               matchrepo.save(matchInfoEntity);

           }

       }
       matchs = new ArrayList<>();
    }
    @Autowired
    MatchStaterepo matchStaterepo;
    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(10); // 👈 multiple threads
        scheduler.setThreadNamePrefix("scheduler-");
        scheduler.initialize();
        return scheduler;
    }

    public void loadSquads(){
        if(matchs.size() > 0 ){
            matchs.forEach(id->{
               MatchState matchState =  matchStaterepo.getstate(id);
               if(matchState != null ) {
                   Optional<MatchInfoEntity> en = matchrepo.findById(id);
                   if(en.isPresent() && en.get().getStatus().equals("Upcoming")) {
                       System.out.println("fetching new squad data");
                       Boolean stat = matchesService.saveplayers(id, true);
                       if(stat) {
                           matchState.setIsannounced(true);
                           matchStaterepo.save(matchState);
                       }
                   }
               }
            });
        }

    }



}
