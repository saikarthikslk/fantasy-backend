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
    @Autowired
    ActivityRepo repo ;



    public static  List<String> list = List.of("bench","substitutes","playing XI");

    public Boolean validate(List<String> keys ){
        boolean[] isvalid = {true} ;
        list.forEach(x->{
            if(!keys.contains(x)) {
                isvalid[0]=false;
            }
        });
        return  isvalid[0];
    }
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
    public int updatepoints(){
       return playerPointsrepo.updatepoints();
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
    public Map<String,String> checkForRoleReplacements(List<Pointdto> pointdtos , List<PlayerEntity> playerEntityMap , Integer matchid , List<String> picked,List<String> replacementids){

        Map<String,String> replacemeap = new HashMap<>();
        Map<String, Double> pt1 = pointdtos.stream().collect(Collectors.toMap(Pointdto::getPlayerid, Pointdto::getPoints, (a, b) -> a));
        List<PlayerEntity> playerEntities = playerEntityMap.stream().filter(x -> pt1.containsKey(x.getId())).collect(Collectors.toList());
        playerEntities.forEach(x -> {
            if (pt1.containsKey(x.getId())) {
                x.setTotalpoints(pt1.get(x.getId()));
            }
        });
        Map<String,PlayerEntity> map = playerEntities.stream().collect(Collectors.toMap(x->x.getId(),x->x,(a,b)->a));
        MatchState matchState = matchStaterepo.getstate(matchid);
        if(matchState!=null && matchState.getIsannounced()) {
            playerEntities = playerEntities.stream().filter(x -> {
                String category = x.getCategory();
                if (category != null) {
                    return !category.equalsIgnoreCase("bench");

                }
                return false;
            }).collect(Collectors.toList());
        }

        Map<String,List<PlayerEntity>> rolebasedmap = new HashMap<>();
        playerEntities = playerEntities.stream().filter(x->!picked.contains(x.getId())).collect(Collectors.toList());
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
        playerEntities.forEach(x->{
            if(!rolebasedmap.containsKey(x.getType())){
                rolebasedmap.put(x.getType(),new ArrayList<>());
            }
            rolebasedmap.get(x.getType()).add(x);
        });

        Map<Integer,Integer> teamcount = new HashMap<>();
        picked.forEach(x->{
            if(replacementids.contains(x)) {
                return;
            }
            PlayerEntity playerEntity = map.get(x);
            Integer id = playerEntity.getTeam().getTeamId();
            if(!teamcount.containsKey(id)){
                teamcount.put(id,0);
            }
            teamcount.put(id,teamcount.get(id)+1);
        });
        List<String> selected = new ArrayList<>();
        replacementids.forEach(x3->{
            PlayerEntity playerEntity = map.get(x3);
            if(playerEntity == null) {
                return;
            }
            if(rolebasedmap.containsKey(playerEntity.getType())) {
                List<PlayerEntity> playerEntities1 = rolebasedmap.get(playerEntity.getType());
               for (PlayerEntity entity : playerEntities1) {
                   Integer id = entity.getTeam().getTeamId();
                   if(!selected.contains(entity.getId()) && teamcount.get(id)+1<8){
                       replacemeap.put(x3,entity.getId());
                       selected.add(entity.getId()) ;
                       teamcount.put(id,teamcount.get(id)+1);
                       break;
                   }
               }
            }

        });
        return replacemeap;

    }
    public List<PlayerEntity> getbestreplacements(List<Pointdto> pointdtos , List<PlayerEntity> playerEntityMap , Integer matchid , List<PlayerEntity> picked){
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
            if(matchState!=null && matchState.getIsannounced()) {
                playerEntities = playerEntities.stream().filter(x -> {
                    String category = x.getCategory();
                    if (category != null) {
                        return !category.equalsIgnoreCase("bench");

                    }
                    return false;
                }).collect(Collectors.toList());
            }
            List<PlayerEntity > selected = new ArrayList<>(picked);
            selected.forEach(x->{
                if(!e.containsKey(x.getTeam().getTeamId())) {
                    e.put(x.getTeam().getTeamId(),new ArrayList<>());
                }
                e.get(x.getTeam().getTeamId()).add(x);
                e1.put(x.getType(),1);
            });
            List<String> ids= selected.stream().map(x->x.getId()).collect(Collectors.toList());
            playerEntities = playerEntities.stream().filter(x->!ids.contains(x.getId())).collect(Collectors.toList());
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
            return selected;

        } catch (Exception e) {
            System.out.println(e.getMessage() );
        }
        return null;



    }

    public Map<String,List<Playerstat>> fetchstats(List<PlayerEntity> players){
        List<Playerstat>  playerstats= playerPointsrepo.getPoints(players.stream().map(PlayerEntity::getId).collect(Collectors.toList()));
        Map<String,List<Playerstat>> stats = new HashMap<>();
        playerstats.forEach(x->{
            if(!stats.containsKey(x.getPlayerid())) {
                stats.put(x.getPlayerid(),new ArrayList<>());

            }
            stats.get(x.getPlayerid()).add(x);
        });
        return stats;
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
                matchSelection.setViews(repo.getviewcount(id));
                matchSelection.setStats(fetchstats(playerfuture.get()));


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
                if(sq1.size() > 2  && validate(sq1.keySet().stream().toList()) ){
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
        this.playerPointsrepo.updatepoints();
        this.fetchliveorcompleted().forEach(x->{
            this.saveplayers(x.getMatchId(),false);
        });
        return true;
    }

}
