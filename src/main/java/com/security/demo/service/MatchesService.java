package com.security.demo.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.security.demo.DBmodel.*;
import com.security.demo.model.*;
import com.security.demo.repo.*;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.internal.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class MatchesService {
    @Autowired
    Matchrepo matchrepo;
    @Autowired
    PlayerRepo playerRepo;
    @Autowired
    PlayerPointsrepo playerPointsrepo;

    @Autowired
    CustomTeamrepo customTeamrepo;
    @Autowired
    HttpCaller caller;

    @Lazy
    @Autowired
    LeaderBoardService service;
    @Autowired
    MatchStaterepo matchStaterepo;




    public static  List<String> list = List.of("bench","substitutes","playing XI");


    public static  List<Match> matches = new ArrayList<>();

    public List<Match> fetchMatches(){
        if(matches.size() >0 ){
            return  matches;
        }
        List<Match> matches =  matchrepo.findAll().stream().map(this::toMatchInfo).toList();
        matches.forEach(m ->{
            if(m.getState().equals("Completed")) {
                try {
                    List<TeamPoints > points =  service.getmatches(m.getMatchId(),"");
                    MatchState state = matchStaterepo.getstate(m.getMatchId());
                    if(points.size() > 0 ) {
                        Double p = points.get(0).getTotalpoints();
                       String players = StringUtils.join(points.stream().filter(x -> Objects.equals(x.getTotalpoints(), p)).map(TeamPoints::getName).toList() , ";");
                        m.setPoints(p);
                        m.setPlayerwon(players);
                    }
                    if(state !=null ) {
                        m.setTeamWon(state.getMatchstatus());
                    }
                } catch (JsonProcessingException e) {

                }

            }
        });
        MatchesService.matches = matches;
        return matches;
    }
    public List<MatchInfoEntity> fetchliveorcompleted(){
        List<String> strings = List.of("Completed");
      return matchrepo.fetchmatchescompletedorlive(strings);
    }
    public SmartTeam getbestplayers(List<Pointdto> pointdtos , List<PlayerEntity> playerEntityMap , Integer matchid){
        if(pointdtos.size() == 0 ){
            return null;
        }
        try {

            SmartTeam smartTeam = new SmartTeam();
            smartTeam.setPlayers(new ArrayList<>());
            Map<String, Double> pt1 = pointdtos.stream().collect(Collectors.toMap(Pointdto::getPlayerid, Pointdto::getPoints, (a, b) -> a));
            List<PlayerEntity> playerEntities = playerEntityMap.stream().filter(x -> pt1.containsKey(x.getId())).collect(Collectors.toList());
            playerEntities.forEach(x -> {
                if (pt1.containsKey(x.getId())) {
                    x.setTotalpoints(pt1.get(x.getId()));
                }
            });
            playerEntities.sort((a, b) -> {
                Double p1 = 0.0;
                Double p2 = 0.0;
                if (pt1.containsKey(a.getId())) {
                    p1 = pt1.get(a.getId());
                }

                if (pt1.containsKey(b.getId())) {
                    p2 = pt1.get(b.getId());
                }

                if (p1 > p2) {
                    return -1;
                } else if (p1.equals(p2)) {
                    return 0;

                } else {
                    return 1;
                }
            });
            Map<Integer, List<PlayerEntity>> e = new HashMap<>();
            Map<String, Integer> e1 = new HashMap<>();
            MatchState matchState = matchStaterepo.getstate(matchid);
            if(matchState!=null && matchState.getIsannounced() ) {
                playerEntities = playerEntities.stream().filter(x->{
                    String category = x.getCategory();
                    if(category!=null) {
                        return !category.equalsIgnoreCase("bench");

                    }
                    return false;
                }).collect(Collectors.toList());
            }
            List<PlayerEntity> selected = new ArrayList<>();
            playerEntities.forEach(x -> {
                if (selected.size() == 11) {
                    return;
                }
                int team = x.getTeam().getTeamId();
                if (!e.containsKey(team)) {
                    e.put(team, new ArrayList<>());

                }


                if (e.get(team).size() <= 7) {

                    if (selected.size() <= 8) {
                        e1.put(x.getType(), 1);
                        selected.add(x);
                        e.get(team).add(x);

                    } else {
                        int typeleft = 4 - e1.size();
                        if (typeleft == 0) {
                            e1.put(x.getType(), 1);
                            selected.add(x);
                            e.get(team).add(x);
                        } else {
                            if(typeleft < 11 - selected.size()) {
                                selected.add(x);
                                e1.put(x.getType(), 1);
                                e.get(team).add(x);
                            }else {
                                if(!e1.containsKey(x.getType())){
                                    selected.add(x);
                                    e1.put(x.getType(), 1);
                                    e.get(team).add(x);
                                }

                            }
                        }
                    }

                }
            });
            smartTeam.setPlayers(selected);
            smartTeam.setCaptain(selected.get(0).getId());
            smartTeam.setVicecaptain(selected.get(1).getId());
            return smartTeam;
        } catch (Exception e) {
            System.out.println(e.getMessage() );
        }
        return null;



    }
    public MatchSelection fetchPlayers(Integer id, String email){
        Optional<MatchInfoEntity> match = matchrepo.findById(id);
        MatchSelection matchSelection = new MatchSelection();
       matchSelection.setPlayers(new ArrayList<>());
        if(match.isPresent()) {
            List<Integer> ids = new ArrayList<>();
            ids.add(match.get().getTeam1().getTeamId());
            ids.add(match.get().getTeam2().getTeamId());
            try {
                CompletableFuture<List<PlayerEntity>> playerfuture = CompletableFuture.supplyAsync(()->playerRepo.getPlayers(ids));
                CompletableFuture<List<Pointdto>> pt = CompletableFuture.supplyAsync(() -> playerPointsrepo.getpoints(match.get().getMatchId()));
                CompletableFuture<CustomTeamEntity> teamCompletableFuture = CompletableFuture.supplyAsync(()->customTeamrepo.findbymatchidandemail(id,email));
                CompletableFuture<?> all = CompletableFuture.allOf(playerfuture,teamCompletableFuture ,pt);
                all.join();

                matchSelection.setDreamTeam(teamCompletableFuture.get());
                matchSelection.setPlayers(playerfuture.get());
                matchSelection.setSmartTeam(getbestplayers(pt.get(),matchSelection.getPlayers(),id));

                matchSelection.setIsannounced(match.get().getIsannounced());



            }catch (Exception e) {
                System.out.println("Failed to retrive match data " + e.getMessage() + "  " +match.get().getMatchId());
            }

        }
        return matchSelection;
    }
    public boolean saveplayers(Integer id ,Boolean islive){
            Optional<MatchInfoEntity> match = matchrepo.findById(id);
        if(match.isPresent()) {
            List<Integer> ids = new ArrayList<>();
            ids.add(match.get().getTeam1().getTeamId());
            ids.add(match.get().getTeam2().getTeamId());
            try {
                CompletableFuture<List<PlayerEntity>> playerfuture = CompletableFuture.supplyAsync(()->playerRepo.getPlayers(ids));
                CompletableFuture<Map<String,List<String> > > squads = CompletableFuture.supplyAsync(() -> {
                    try {
                        return caller.getplayers(match.get().getMatchId());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
                CompletableFuture<?> all = CompletableFuture.allOf(playerfuture, squads);
                all.join();
                Map<String ,List<String>> sq1 = squads.get();
                if(sq1.size() > 2 ){
                    List<PlayerEntity> playerEntities = playerfuture.get();
                    playerEntities.forEach(x -> {
                        for (String  key  : sq1.keySet())  {
                            if(list.contains(key) && sq1.get(key).contains(x.getId())) {
                                if(islive) {
                                    x.setCategory(key);
                                }else  {
                                    x.setPrevcategory(key);
                                }
                            }

                        }
                    });
                    playerRepo.saveAll(playerEntities);
                    return true;

                }


            }catch (Exception e) {
                System.out.println("Failed to save squad data");
            }


        }
        return false;
    }

    private Match toMatchInfo(MatchInfoEntity e) {
        Team team1 = new Team();
        team1.setTeamId(e.getTeam1().getTeamId());
        team1.setTeamName(e.getTeam1().getTeamName());
        team1.setTeamSName(e.getTeam1().getTeamSName());
        team1.setImageId(e.getTeam1().getImageId());

        Team team2 = new Team();
        team2.setTeamId(e.getTeam2().getTeamId());
        team2.setTeamName(e.getTeam2().getTeamName());
        team2.setTeamSName(e.getTeam2().getTeamSName());
        team2.setImageId(e.getTeam2().getImageId());

        Venue venue = new Venue();
        venue.setId(e.getVenueInfo().getId());
        venue.setGround(e.getVenueInfo().getGround());
        venue.setCity(e.getVenueInfo().getCity());
        venue.setTimezone(e.getVenueInfo().getTimezone());

        Match info = new Match();
        info.setMatchId(e.getMatchId());
        info.setSeriesId(e.getSeriesId());
        info.setSeriesName(e.getSeriesName());
        info.setMatchDesc(e.getMatchDesc());
        info.setMatchFormat(e.getMatchFormat());
        info.setStartDate(e.getStartDate());
        info.setEndDate(e.getEndDate());
        info.setState(e.getState());
        info.setStatus(e.getStatus());
        info.setTeam1(team1);
        info.setTeam2(team2);
        info.setVenueInfo(venue);

        return info;
    }


    public boolean syncdata(){
        this.fetchliveorcompleted().forEach(x->{
            this.saveplayers(x.getMatchId(),false);
        });
        return true;
    }

}
