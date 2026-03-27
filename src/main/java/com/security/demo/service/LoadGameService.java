package com.security.demo.service;


import com.security.demo.DBmodel.PlayerEntity;
import com.security.demo.model.Ball;
import com.security.demo.model.MatchSelection;
import com.security.demo.model.MatchState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class LoadGameService {
    @Autowired
    private HttpCaller httpCaller;
    @Autowired
    private MatchesService matchesService;
    public void runGame(Integer  matchid ,Long timestamp){
        MatchSelection matchSelection = matchesService.fetchPlayers(matchid,"");
        MatchState matchState = new MatchState();
        List<PlayerEntity> playerEntities  =  matchSelection.getPlayers();
        Map<String,PlayerEntity> playernamemap = new HashMap<>();
        Map<String , PlayerEntity> playeridmap = new HashMap<>();

        playerEntities.forEach(playerEntity -> {
            playernamemap.put(playerEntity.getName(),playerEntity);
            playeridmap.put(playerEntity.getId(),playerEntity);

        });
        String query ="";
        boolean islegaldelivey = true;
        boolean iscatch = false;
        boolean isrunout= false;
        boolean isbowled = false;
        boolean isstumped= false;
        boolean islbw = false;
        Integer runs = 0 ;
        Integer bounday =  0;
        boolean iswicket=  false;
        Integer extras = 0 ;
        Integer legbyes = 0 ;
        String wicketassistname = "";
        String p1 = "";
        String p2 = "";
        String b1 = "";
        boolean isoverdone = false;
        String ballid = "";
        List<Ball> balls = new ArrayList<>();
        while (true) {
            query = matchState.getMatchid()+"/"+matchState.getInnings()+"/"+matchState.getTimestamp() + "/";
            List<Map<String,Object>> resp =  httpCaller.fetchdata(query);
            for (Map<String,Object> event : resp) {
                if(event.containsKey("ballMetric")) {
                    String teamname = (String) event.get("teamName");
                    matchState.setTeamid(teamname);
                    Ball ball  = new Ball();
                    ball.setInnings(matchState.getInnings());
                    ball.setTeam(matchState.getTeamid());
                    ballid  =(String) event.get("ballMetric");
                    if(p1.isEmpty()) {
                        p1 = (String) ((Map<String, Object>) (event.get("batsmanDetails"))).get("playerId");
                    }
                    b1 =(String) (  (Map<String, Object>) ( event.get("bowlerDetails") )).get("playerId");
                    List<String> events = (List<String>) event.get("event");
                    if(events.contains("over-break")) {
                        isoverdone = true;
                    }
                    String comtext = (String) event.get("commText");
                    if(comtext.contains("<b>FOUR</b>")){
                        bounday = 4;
                    }
                    if(comtext.contains("<b>SIX</b>")){
                        bounday = 6;
                    }
                    if(comtext.contains("<b>out</b>")){
                        iswicket = true;
                        if(comtext.contains("Caught by")) {
                            iscatch = true;
                            ball.setWicketype("caught");
                            ball.setPlayerout(p1);
                            String cby = "";
                            Pattern pattern = Pattern.compile("c\\s+([A-Za-z\\s]+?)\\s+b");
                            Matcher matcher = pattern.matcher(comtext);
                            if(matcher.find()) {
                                cby = matcher.group(1);
                                wicketassistname = playernamemap.get(cby).getId();
                            }
                            ball.setStumpout(wicketassistname);
                        } else if (comtext.contains("Bowled!!")) {
                            isbowled = true;
                            ball.setWicketype("bowled");
                            ball.setPlayerout(p1);
                        } else if (comtext.contains("Run Out!!")) {
                            isrunout = true;
                            ball.setWicketype("runout");
                            ball.setPlayerout(p1);
                            Pattern pattern = Pattern.compile("run out\\s+([A-Za-z\\s]+?)\\s+b");
                            String sby = "";
                            Matcher matcher = pattern.matcher(comtext);
                            if(matcher.find()) {
                                sby = matcher.group(1);
                                wicketassistname = playernamemap.get(sby).getId();
                            }
                            ball.setStumpout(wicketassistname);
                            pattern = Pattern.compile("^(\\w+)\\s+Run\\s+Out");
                            matcher = pattern.matcher(comtext);
                            if(matcher.find()) {
                                sby = matcher.group(1);
                                PlayerEntity p2name = playernamemap.get(p2);
                                if(p2name!=null && p2name.getName().toLowerCase().contains(sby.toLowerCase())){
                                    ball.setPlayerout(p2);
                                }
                            }



                        } else if (comtext.contains("Lbw!!")) {
                            islbw = true;
                            ball.setWicketype("lbw");
                            ball.setPlayerout(p1);
                        }else if (comtext.contains("Stumped!!")){
                            isstumped = true;
                            ball.setWicketype("stumps");
                            ball.setPlayerout(p1);
                            String sby = "";
                            Pattern pattern = Pattern.compile("st\\s+([A-Za-z\\s]+?)\\s+b");
                            Matcher matcher = pattern.matcher(comtext);
                            if(matcher.find()) {
                                sby = matcher.group(1);
                                wicketassistname = playernamemap.get(sby).getId();
                            }
                            ball.setStumpout(wicketassistname);
                        }else {
                            isbowled = true;
                            ball.setWicketype("bowled");
                            ball.setPlayerout(p1);
                        }

                    }
                    if(comtext.contains("<b>wide</b>")){
                        islegaldelivey = false;
                        extras =  1;
                        ball.setIswide(1);
                    }
                    for (int i =  0 ; i < 5 ; i++ ) {
                        if (comtext.contains(i+" runs")) {
                            if(comtext.contains("leg byes")) {
                                legbyes =  i;
                            }else {
                                runs = i ;
                            }
                            break;
                        }
                    }
                    Integer totalruns = runs + bounday + extras + legbyes;

                    if(totalruns > 0) {
                        matchState.setCurrentscore(matchState.getCurrentscore()+totalruns);
                    }
                    if(iswicket) {
                        matchState.setCurrentwickets(matchState.getCurrentwickets()+1);
                    }
                    if(runs%2!=0 || legbyes %2!=0 ) {
                        if(!isoverdone) {
                            String p3 = p1;
                            p1 = p2;
                            p2 = p3;
                        }
                    }else {
                        if(isoverdone) {
                            String p3 = p1;
                            p1 = p2;
                            p2 = p3;
                        }
                    }
                    ball.setP1(p1);
                    ball.setP2(p2);
                    ball.setBowler(b1);
                    ball.setBall(ballid);
                    ball.setTotalruns(totalruns);
                    ball.setRunsran(runs);
                    ball.setLegbyes(legbyes);
                    ball.setBoundary(bounday);
                    balls.add(ball);
                    matchState.setTimestamp(timestamp);

                }
            }

            timestamp = timestamp + 60000;
        }

    }




}
