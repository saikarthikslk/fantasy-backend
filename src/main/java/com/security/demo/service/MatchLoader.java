package com.security.demo.service;

import com.security.demo.DBmodel.MatchInfoEntity;
import com.security.demo.repo.Matchrepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    private static List<CompletableFuture> list = new ArrayList<>();
    private static Map<Integer,String> map = new HashMap<>();
    public void  runmatch(){

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
         System.out.println("exeception occured");
        }
        System.out.println("running");
        List<MatchInfoEntity> matchInfoEntities =  matchrepo.findAll().stream().filter(x->{

            Long start = (x.getStartDate() - System.currentTimeMillis()   )/1000 ;
            if( x.getState().equals("Upcoming") &&  start <=1800 && !map.containsKey(x.getMatchId())) {
                map.put(x.getMatchId(),"Starting");
                return true;
            } else if (x.getState().equals("Live") && !map.containsKey(x.getMatchId()) ) {
                map.put(x.getMatchId(),"Live");
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
           if( start <= 7200) {
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
    }

}
