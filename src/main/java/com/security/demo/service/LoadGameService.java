package com.security.demo.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.security.demo.DBmodel.*;
import com.security.demo.controller.NotificationController;
import com.security.demo.model.MatchSelection;
import com.security.demo.model.Matchinfo;
import com.security.demo.model.SmartTeam;
import com.security.demo.repo.*;
import org.apache.commons.text.CaseUtils;
import org.apache.commons.text.similarity.JaroWinklerSimilarity;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class LoadGameService {
    @Autowired
    private HttpCaller httpCaller;
    @Autowired
    PlayerPointsrepo playerPointsrepo;
    @Autowired
    private MatchesService matchesService;
    @Autowired
    private Matchrepo matchrepo;
    @Autowired
    private MatchStaterepo matchStaterepo;
    @Autowired
    private CustomTeamrepo customTeamrepo;
    @Autowired
    private Userrepo userrepo;
    @Autowired
    LeaderBoardService leaderBoardService;
    public static List<Integer> stoppedmatches = new ArrayList();
    private static      JaroWinklerSimilarity jw = new JaroWinklerSimilarity();

    private ObjectMapper mapper = new ObjectMapper();

    public PlayerEntity getplayer(String name , Map<String,PlayerEntity> playerEntityMap) {
        List<String> names = playerEntityMap.keySet().stream().toList();
        Double score = 0.0;
        String player = name;
        for (String n1 : names) {

            Double s2 = jw.apply(n1,name);
            if(s2 > score) {
                score = s2;
                player = n1;

            }
        }
        return playerEntityMap.get(player);

    }

    public String getkey(Map<String , Object> event  ){
        String ball = event.get("ballMetric") == null ? "" : event.get("ballMetric").toString();
        String com =  event.get("commText") == null ? "" : event.get("commText").toString() ;
        return ball + ":"+com;
    }
    @Autowired
    NotificationController notificationController;
//    public void runGame(Integer  matchid ,Long timestamp) {
//        MatchSelection matchSelection = matchesService.fetchPlayers(matchid, "");
//        MatchState matchState = new MatchState();
//        List<PlayerEntity> playerEntities = matchSelection.getPlayers();
//        Map<String, PlayerEntity> playernamemap = new HashMap<>();
//        Map<String, PlayerEntity> playeridmap = new HashMap<>();
//        List<PlayerPoints> points = playerPointsrepo.getpointsbymatchid(matchid);
//        matchState.setPoints(points);
//        matchState.setMatchid(matchid);
//        boolean isp[] ={false};
//        matchState.setTimestamp(timestamp);
//        playerEntities.forEach(playerEntity -> {
//            playernamemap.put(playerEntity.getName(), playerEntity);
//            playeridmap.put(playerEntity.getId(), playerEntity);
//            if(points.isEmpty() || isp[0]) {
//            PlayerPoints playerPoints = new PlayerPoints();
//            playerPoints.setPlayerid(playerEntity.getId());
//            playerPoints.setMatchid(matchid);
//            points.add(playerPoints);
//                isp[0]=true;
//            }
//
//        });
//        if(isp[0]) {
//            playerPointsrepo.saveAll(points);
//        }
//        Map<String,PlayerPoints> playerPointsMap = points.stream().collect(Collectors.toMap(PlayerPoints::getPlayerid, x->x,(a, b)->a));
//        String query = "";
//        boolean islegaldelivey = true;
//        boolean iscatch = false;
//        boolean isrunout = false;
//        boolean isbowled = false;
//        boolean isstumped = false;
//        boolean islbw = false;
//        Integer runs = 0;
//        Integer bounday = 0;
//        boolean iswicket = false;
//        Integer extras = 0;
//        Integer legbyes = 0;
//        String wicketassistname = "";
//        String p1 = "";
//        String p2 = "";
//        String b1 = "";
//        boolean isoverdone = false;
//        String ballid = "";
//        Pattern pattern;
//        Matcher matcher;
//        Boolean ismaidain = false;
//        Boolean innigsfone = false;
//        List<Ball> balls = new ArrayList<>();
//        Map<String, String> finsihedballs = new HashMap();
//
//        while (true) {
//            query = matchState.getMatchid() + "/" + matchState.getInnings() + "/" + matchState.getTimestamp();
//            List<Map<String, Object>> resp = httpCaller.fetchdata(query);
//            Collections.reverse(resp);
//
//            for (Map<String, Object> event : resp) {
//                String key = getkey(event);
//                if (event.containsKey("ballMetric") && !finsihedballs.containsKey(key)) {
//                    runs= 0;
//                    bounday = 0;
//                    legbyes =0 ;
//                    iswicket = false;
//                    extras = 0 ;
//                    wicketassistname = "";
//                    isoverdone = false;
//                    islegaldelivey = true;
//                    iscatch = false;
//                    isrunout = false;
//                    isbowled = false;
//                    isstumped = false;
//                    islbw = false;
//
//                    ballid = event.get("ballMetric").toString();
//                    System.out.println(ballid);
//                    if(ballid.equals("1.3")) {
//                        int y = 1;
//                    }
//
//                    finsihedballs.put(key, ballid);
//                    String teamname = (String) event.get("teamName");
//                    matchState.setTeamid(teamname);
//                    Ball ball = new Ball();
//                    ball.setInnings(matchState.getInnings());
//                    ball.setTeam(matchState.getTeamid());
//                    if(ballid.equals("17.5")){
//                        int y = 1;
//                    }
//
//                    p1 = ((Map<String, Object>) (event.get("batsmanDetails"))).get("playerId").toString();
//                    b1 = ((Map<String, Object>) (event.get("bowlerDetails"))).get("playerId").toString();
//                    List<String> events = (List<String>) event.get("event");
//                    if (events.contains("over-break")) {
//                        isoverdone = true;
//                    }
//                    String comtext = (String) event.get("commText");
//                    if(comtext.contains("maiden")) {
//                        ismaidain = true;
//                    }
//                    if (comtext.contains("<b>FOUR</b>")) {
//                        bounday = 4;
//                    }
//                    if (comtext.contains("<b>SIX</b>")) {
//                        bounday = 6;
//                    }
//                    comtext = comtext.replace("(sub)","");
//                    if (comtext.contains("<b>out</b>")) {
//                        iswicket = true;
//                        if (comtext.contains("Caught by")) {
//                            iscatch = true;
//                            ball.setWicketype("caught");
//                            ball.setPlayerout(p1);
//                            String cby = "";
//                            pattern = Pattern.compile("(?i)c\\s+([A-Za-z\\s()]+?)\\s+b");
//                            matcher = pattern.matcher(comtext);
//                            if (matcher.find()) {
//                                cby = matcher.group(1);
//                                wicketassistname = getplayer(cby, playernamemap).getId();
//                            }
//                            ball.setCatchp(wicketassistname);
//                        } else if (comtext.contains("Bowled!!")) {
//                            isbowled = true;
//                            ball.setWicketype("bowled");
//                            ball.setPlayerout(p1);
//                        } else if (comtext.contains("Run Out!!")) {
//                            isrunout = true;
//                            ball.setWicketype("runout");
//                            ball.setPlayerout(p1);
//                            pattern = Pattern.compile("(?i)run out\\s*\\(([^)]+)\\)");
//                            String sby = "";
//                            matcher = pattern.matcher(comtext);
//                            if (matcher.find()) {
//                                sby = matcher.group(1);
//                                wicketassistname = getplayer(sby, playernamemap).getId();
//                                ball.setRunoutp(wicketassistname);
//                                pattern = Pattern.compile("(?i)>.*?([A-Za-z\\s]+?)\\s+Run\\s+Out!!");
//                                matcher = pattern.matcher(comtext);
//                                if (matcher.find()) {
//                                    sby = matcher.group(1);
//                                    PlayerEntity p2name = getplayer(sby, playernamemap);
//                                    if (p2name != null && p2name.getName().toLowerCase().contains(sby.toLowerCase())) {
//                                        ball.setPlayerout(p2);
//                                    }
//                                }
//                            }
//                        } else if (comtext.contains("Lbw!!")) {
//                            islbw = true;
//                            ball.setWicketype("lbw");
//                            ball.setPlayerout(p1);
//
//
//                        } else if (comtext.contains("Stumped!!")) {
//                            isstumped = true;
//                            ball.setWicketype("stumps");
//                            ball.setPlayerout(p1);
//                           String sby = "";
//                            pattern = Pattern.compile("st\\s+([A-Za-z\\s]+?)\\s+b");
//                            matcher = pattern.matcher(comtext);
//                            if (matcher.find()) {
//                                sby = matcher.group(1);
//                                wicketassistname = getplayer(sby, playernamemap).getId();
//                            }
//                            ball.setStumpout(wicketassistname);
//                        } else {
//                            isbowled = true;
//                            ball.setWicketype("bowled");
//                            ball.setPlayerout(p1);
//                        }
//                    }
//
//
//                        if (comtext.contains("<b>wide</b>")) {
//                            islegaldelivey = false;
//                            extras = 1;
//                            ball.setIswide(1);
//                        }
//                        for (int i = 0; i < 5; i++) {
//                            if (comtext.contains(i + " run")) {
//                                if (comtext.contains("leg byes")) {
//                                    legbyes = i;
//                                } else {
//                                    runs = i;
//                                }
//                                break;
//                            }
//                        }
//                        Integer totalruns = runs + bounday + extras + legbyes;
//
//                        if (totalruns > 0) {
//                            matchState.setCurrentscore(matchState.getCurrentscore() + totalruns);
//                        }
//                        if (iswicket) {
//                            matchState.setCurrentwickets(matchState.getCurrentwickets() + 1);
//                        }
//
//                        ball.setP1(p1);
//                        ball.setP2(p2);
//                        ball.setBowler(b1);
//                        ball.setBall(ballid);
//                        ball.setTotalruns(totalruns);
//                        ball.setRunsran(runs);
//                        ball.setLegbyes(legbyes);
//                        ball.setBoundary(bounday);
//                        ball.setScore(matchState.getCurrentscore());
//                        ball.setWickets(matchState.getCurrentwickets());
//                        balls.add(ball);
//                        if(ismaidain) {
//                        playerPointsMap.get(b1).setMaidens(playerPointsMap.get(b1).getMaidens()+1);
//                        }
//
//                    PlayerPoints playerPoints =    playerPointsMap.get(p1);
//                    playerPoints.setFours( playerPoints.getFours() + bounday == 4 ? 1:0);
//                    playerPoints.setSixes( playerPoints.getSixes() + bounday == 6 ? 1:0);
//                    PlayerPoints playerPoints1 =    playerPointsMap.get(b1);
//                    List<PlayerPoints> pointstosave = new ArrayList<>();
//                        if(!iswicket  ) {
//                          playerPoints.setRuns(playerPoints.getRuns() + runs + bounday);
//                          playerPoints.setBallplayed(playerPoints.getBallplayed() + 1);
//                          playerPoints.setRunString(playerPoints.getRunString() + " " +(runs + bounday));
//                          playerPoints.setScore(playerPoints.getScore() + runs+bounday);
//                          playerPoints1.setBallsbowled(playerPoints1.getBallsbowled()  + 1);
//                          playerPoints1.setScoregiven(playerPoints1.getScoregiven() + totalruns);
//
//
//                          if(legbyes > 0 ) {
//                              playerPoints1.setBallstring(playerPoints1.getBallstring() + " L" + legbyes);
//                          } else if (ball.getIswide() == 1) {
//                              playerPoints1.setBallstring(playerPoints1.getBallstring() + " WI" +extras);
//                          }else if (ball.getIsnoball() == 1) {
//                              playerPoints1.setBallstring(playerPoints1.getBallstring() + " NB" +totalruns);
//                          }else{
//                              playerPoints1.setBallstring(playerPoints1.getBallstring() + " " +totalruns);
//                          }
//
//                        }else {
//
//                            if(isbowled) {
//                                playerPoints.setBallplayed(playerPoints.getBallplayed() + 1);
//                                playerPoints.setRunString(playerPoints.getRunString() + " B" );
//                                playerPoints.setOut(true);
//                                playerPoints.setType("BOWLED");
//                                playerPoints1.setBallsbowled(playerPoints1.getBallsbowled()  + 1);
//                                playerPoints1.setBallstring(playerPoints1.getBallstring() + " W");
//                                playerPoints1.setScoregiven(playerPoints1.getScoregiven() + totalruns);
//                            } else if (iscatch) {
//                                playerPoints.setBallplayed(playerPoints.getBallplayed() + 1);
//                                playerPoints.setRunString(playerPoints.getRunString() + " C" );
//
//                                playerPoints.setOut(true);
//                                playerPoints.setType("CATCH");
//
//                                playerPoints1.setBallsbowled(playerPoints1.getBallsbowled()  + 1);
//                                playerPoints1.setBallstring(playerPoints1.getBallstring() + " W");
//                                playerPoints1.setScoregiven(playerPoints1.getScoregiven() + totalruns);
//                                PlayerPoints cby =    playerPointsMap.get(ball.getCatchp());
//                                cby.setCatches(cby.getCatches() +  1);
//                                cby.setTotalpoints(FantasyPointsCalculator.calculateTotalPoints(cby));
//                                pointstosave.add(cby);
//                            } else if (isstumped) {
//                                playerPoints.setBallplayed(playerPoints.getBallplayed() + 1);
//                                playerPoints.setRunString(playerPoints.getRunString() + " S" );
//
//                                playerPoints.setOut(true);
//                                playerPoints.setType("STUMPED");
//
//                                playerPoints1.setBallsbowled(playerPoints1.getBallsbowled()  + 1);
//                                playerPoints1.setBallstring(playerPoints1.getBallstring() + " W");
//                                playerPoints1.setScoregiven(playerPoints1.getScoregiven() + totalruns);
//                                PlayerPoints cby =    playerPointsMap.get(ball.getStumpout());
//                                cby.setStumping(cby.getStumping() +  1);
//                                cby.setTotalpoints(FantasyPointsCalculator.calculateTotalPoints(cby));
//                                pointstosave.add(cby);
//                            } else if (isrunout) {
//
//                                PlayerPoints P1 =    playerPointsMap.get(p1);
//                                PlayerPoints P2 =    playerPointsMap.get(p2);
//                                P1.setBallplayed(P1.getBallplayed() + 1);
//                                if(Objects.equals(p1, ball.getRunoutp()))  {
//                                    P1.setRunString( P1.getRunString() + " RO" + runs );
//
//                                    playerPoints.setOut(true);
//                                    playerPoints.setType("RUNOUT");
//                                }else  {
//                                    P1.setRunString( P1.getRunString() + " " + runs );
//                                    P2.setRunString( P1.getRunString() + " RO" + runs );
//
//                                    P2.setOut(true);
//                                    P2.setType("STUMPED");
//                                    P2.setTotalpoints(FantasyPointsCalculator.calculateTotalPoints(P2));
//                                    pointstosave.add(P2);
//                                }
//
//
//                                playerPoints1.setBallsbowled(playerPoints1.getBallsbowled()  + 1);
//                                playerPoints1.setBallstring(playerPoints1.getBallstring() + " 0");
//                                playerPoints1.setScoregiven(playerPoints1.getScoregiven() + totalruns);
//
//                                PlayerPoints runoutby =    playerPointsMap.get(ball.getRunoutp());
//                                runoutby.setRunouts(runoutby.getRunouts() + 1);
//                                runoutby.setTotalpoints(FantasyPointsCalculator.calculateTotalPoints(runoutby));
//                                pointstosave.add(runoutby);
//
//                            } else {
//                                playerPoints.setBallplayed(playerPoints.getBallplayed() + 1);
//                                playerPoints.setRunString(playerPoints.getRunString() + " LB" );
//                                playerPoints.setOut(true);
//                                playerPoints.setType("LBW");
//
//                                playerPoints1.setBallsbowled(playerPoints1.getBallsbowled()  + 1);
//                                playerPoints1.setBallstring(playerPoints1.getBallstring() + " LB");
//                                playerPoints1.setScoregiven(playerPoints1.getScoregiven() + totalruns);
//                            }
//
//
//                        }
//
//                        playerPoints.setTotalpoints(FantasyPointsCalculator.calculateTotalPoints(playerPoints));
//                    playerPoints1.setTotalpoints(FantasyPointsCalculator.calculateTotalPoints(playerPoints1));
//                    pointstosave.add(playerPoints1);
//                    pointstosave.add(playerPoints);
//                   List<PlayerPoints> playerPointsList = playerPointsrepo.saveAll(pointstosave);
//                   playerPointsList.forEach(x->{
//                       playerPointsMap.put(x.getPlayerid(),x);
//                   });
//                   matchState.setPoints(playerPointsMap.values().stream().toList());
//                    if (runs % 2 != 0 || legbyes % 2 != 0) {
//                        if (!isoverdone) {
//                            String p3 = p1;
//                            p1 = p2;
//                            p2 = p3;
//                        }
//                    } else {
//                        if (isoverdone) {
//                            String p3 = p1;
//                            p1 = p2;
//                            p2 = p3;
//                        }
//                    }
//                        if(isoverdone ){
//                            System.out.println(matchState.getCurrentscore()+"/" +matchState.getCurrentwickets() +" "+ ballid);
//                        }
//                        if(matchState.getInnings() == 1) {
//                            if (matchState.getCurrentwickets().equals(10)) {
//                                matchState.setInnings(2);
//                                matchState.setInnings1(matchState.getCurrentscore() + "/" + matchState.getCurrentwickets());
//                                matchState.setInnings1state(balls);
//                                matchState.setCurrentwickets(0);
//                                matchState.setCurrentscore(0);
//                            } else if (isoverdone && ballid.contains("19.6")) {
//                                innigsfone = true;
//                                matchState.setInnings(2);
//                                matchState.setInnings1(matchState.getCurrentscore() + "/" + matchState.getCurrentwickets());
//                                matchState.setInnings1state(balls);
//                                matchState.setCurrentwickets(0);
//                                matchState.setCurrentscore(0);
//                            }
//
//                        }else  {
//                            if(matchState.getCurrentscore() > Integer.parseInt(matchState.getInnings1().split("/")[0])){
//                                matchState.setMatchstatus("Completed" );
//                            }
//                            else if(matchState.getCurrentwickets().equals(10) || (isoverdone && ballid.contains("19.6"))) {
//                                matchState.setMatchstatus("Completed" );
//
//                            }
//                        }
//
//                }
//            }
//            timestamp = timestamp + 60000;
//            matchState.setTimestamp(timestamp);
//            if(matchState.getMatchstatus().equals("Completed")) {
//                break;
//            }
//
//        }
//    }

    public void  loadmatches(){

    }
    public static void  sleep(Integer sec) {
        try {
            System.out.println("Went to Sleep at  ** "+ System.currentTimeMillis() );
            Thread.sleep(sec * 1000);
            System.out.println("Out to Sleep at  ** "+ System.currentTimeMillis() );
        }catch (Exception e) {
            System.out.println("thread got interupterd");
        }
    }
    private static int extract(String text, String key) {
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile(key + "\\s*(\\d+)")
                .matcher(text);
        return m.find() ? Integer.parseInt(m.group(1)) : 0;
    }
    public String mapPlayerType(String label) {
        switch (label) {
            case "ALL ROUNDER":   return "AR";
            case "WICKET KEEPER": return "WK";
            case "BOWLER":        return "BOWL";
            case "BATSMEN":       return "BAT";
            default:              return label; // fallback
        }
    }
    public String autoload(Integer matchid) throws JsonProcessingException {

        String team []= {""};
       List<CustomTeamEntity> entities = customTeamrepo.findbymatchid(matchid);
       Map<String,CustomTeamEntity> customTeamEntityMap = entities.stream().collect(Collectors.toMap(CustomTeamEntity::getEmail, x->x,(x, y)->x));
       List<User> users = userrepo.findAll();
       List<User> usersWhoNeedTeam = users.stream().filter(x->!customTeamEntityMap.containsKey(x.getEmail())).toList();
       if(usersWhoNeedTeam .size()  >0 ) {
           SmartTeam smartTeam = matchesService.fetchPlayers(matchid,"").getSmartTeam();
           if(smartTeam != null) {
               Map<String,Object> map = new HashMap<>();
               map.put("matchid", matchid);
               map.put("captainPlayerId",smartTeam.getCaptain());
               map.put("viceCaptainPlayerId",smartTeam.getVicecaptain());
               List<Map<String,Object>> maps = new ArrayList<>();
               smartTeam.getPlayers().forEach(x->{
                   Map<String,Object> pp = new HashMap<>();

                   pp.put("playerid", x.getId());

                   pp.put("type",mapPlayerType(x.getType()));
                   maps.add(pp);
               });
               map.put("properties",maps);
               team[0] = mapper.writeValueAsString(map);

           }
           if(team[0]!=null) {
               List<CustomTeamEntity > tobesaved = new ArrayList<>();
               usersWhoNeedTeam.forEach((u) -> {
                   CustomTeamEntity entity = new CustomTeamEntity();
                   entity.setMatch_id(matchid);
                   entity.setCreated_at(Timestamp.from(Instant.now()));
                   entity.setTeam(team[0]);
                   entity.setEmail(u.getEmail());
                   tobesaved.add(entity);
               });
               customTeamrepo.saveAll(tobesaved);
           }
    }
       return team[0];


    }
    public void pullscorecard(Integer matchid) throws IOException, InterruptedException {


        MatchSelection matchSelection = matchesService.fetchPlayers(matchid, "");
        System.out.println("Running Match " + matchid );
        MatchState matchState = matchStaterepo.getstate(matchid);
        if(matchState == null){
            matchState = new MatchState();
            matchState.setMatchid(matchid);
            matchState.setTimestamp(System.currentTimeMillis());
            matchStaterepo.save(matchState);
        }
        MatchInfoEntity matchInfoEntity = matchrepo.findById(matchid).get();

        if(matchState.getTosswonby() == null) {
            while (true) {
                Map<String,Object> response = httpCaller.fetchtoss(matchid+"");
                if(response.size() > 0) {

                    matchState.setTimestamp(System.currentTimeMillis());
                    matchState.setTosswonby(response.get("teamid").toString());
                    matchState.setMatchstatus(response.get("status").toString());
            
                    if(response.get("status").toString().toLowerCase().contains("bowl")){
                        matchState.setTeam2(matchState.getTosswonby());
                        if(matchInfoEntity.getTeam1().getTeamId() == Integer.parseInt(matchState.getTosswonby())){
                            matchState.setTeam1(matchInfoEntity.getTeam2().getTeamId()+"");
                        }else  {
                            matchState.setTeam1(matchInfoEntity.getTeam1().getTeamId()+"");
                        }

                    }else {
                        matchState.setTeam1(matchState.getTosswonby());
                        if(matchInfoEntity.getTeam1().getTeamId() == Integer.parseInt(matchState.getTosswonby())){
                            matchState.setTeam2(matchInfoEntity.getTeam2().getTeamId()+"");
                        }else  {
                            matchState.setTeam2(matchInfoEntity.getTeam1().getTeamId()+"");
                        }
                    }

                    break;
                }
                sleep(15);

            }
        }
        notificationController.sendEvent("refresh",matchid);
        matchStaterepo.save(matchState);
        System.out.println("Checking for Squad Data");
        while (true) {
            if(matchesService.saveplayers(matchid, true)){
                break;
            };
            sleep(10);
        }
        System.out.println("Announced   Squad Data");

        matchState.setIsannounced(true);
        matchInfoEntity.setIsannounced(true);
        matchStaterepo.save(matchState);
        matchrepo.save(matchInfoEntity);

        notificationController.sendEvent("refresh",matchid);
        System.out.println("Toss Won by " + matchState.getTosswonby() );
        Integer[] innings = {-1};
        while (true) {
            Integer id = httpCaller.fetchInnigs(String.valueOf(matchid));
            if(id!=-1){
                innings[0]=id;
                break;
            }
            sleep(30);
        }




        List<PlayerEntity> playerEntities = matchSelection.getPlayers();
        Map<String, PlayerEntity> playernamemap = new HashMap<>();
        Map<String, PlayerEntity> playeridmap = new HashMap<>();

        Boolean autoteam = true;
        List<PlayerPoints> points = playerPointsrepo.getpointsbymatchid(matchid);
        boolean isp[] ={false};
        matchState.setTimestamp(System.currentTimeMillis());
        playerEntities.forEach(playerEntity -> {
            playernamemap.put(playerEntity.getName(), playerEntity);
            playeridmap.put(playerEntity.getId(), playerEntity);
            if(points.isEmpty() || isp[0]) {
                PlayerPoints playerPoints = new PlayerPoints();
                playerPoints.setPlayerid(playerEntity.getId());
                playerPoints.setMatchid(matchid);
                points.add(playerPoints);
                isp[0]=true;
            }

        });
        if(isp[0]) {
            playerPointsrepo.saveAll(points);
        }
        List<Integer> innigs = List.of(1,2);
        String ikey = "";
        sleep(1);
        Map<String,PlayerPoints> playerPointsMap = points.stream().collect(Collectors.toMap(PlayerPoints::getPlayerid, x->x,(a, b)->a));
        boolean iscompleted = false;
        boolean israin = false;
        int verify  = 0 ;
        int change = 0 ;
        String url = "https://www.cricbuzz.com/live-cricket-scorecard/"+matchid;
        while (true) {
            try {
                // Connect and get the HTML document
                Document doc = Jsoup.connect(url)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                        .get();
                String key = "";
                if (matchState.getInnings() == 1) {
                    key = matchState.getTeam1();
                } else {
                    key = matchState.getTeam2();
                }
                System.out.println("running defaults");
                Map<String, Object> r1 = new HashMap<>();
                Map<String, Object> r2 = new HashMap<>();
                playerPointsMap.values().forEach(PlayerPoints::initializeDefaults);
                // Find the main innings container (Innings 1)
                for (Integer id1 : innigs) {
                    if(id1 == 1 ){
                        key = matchState.getTeam1();
                    }else {
                        key = matchState.getTeam2();
                    }
                    Element innings1 = doc.getElementById("scard-team-" + key + "-innings-" + id1);
                    List<Map<String, Object>> batting = new ArrayList<>();
                    Map<String, Object> result = new HashMap<>();
                    System.out.println("runnings");
                    if(innings1 == null) {
                       continue;
                    }
                    for (Element row : innings1.select("div.scorecard-bat-grid")) {

                        Element player = row.selectFirst("a[href*=/profiles/]");
                        if (player == null) continue;

                        Elements stats = row.select("div.flex.justify-center.items-center");
                        if (stats.size() < 5) continue;

                        Map<String, Object> b = new HashMap<>();
                        b.put("name", player.text());
                        b.put("dismissal", row.select("div.text-cbTxtSec").text());
                        b.put("runs", Integer.parseInt(stats.get(0).text()));
                        b.put("balls", Integer.parseInt(stats.get(1).text()));
                        b.put("fours", Integer.parseInt(stats.get(2).text()));
                        b.put("sixes", Integer.parseInt(stats.get(3).text()));
                        b.put("sr", Double.parseDouble(stats.get(4).text()));

                        batting.add(b);
                    }
                    result.put("batting", batting);
                    for (Map<String, Object> b : batting) {
                        String name = (String) b.get("name");
                        String dismissal = (String) b.get("dismissal");
                        Integer runs = (Integer) b.get("runs");
                        Integer balls = (Integer) b.get("balls");
                        Integer fours = (Integer) b.get("fours");
                        Integer sixes = (Integer) b.get("sixes");

                        Double sr = (Double) b.get("sr");


                        PlayerEntity playerEntity = getplayer(name, playernamemap);
                        PlayerPoints playerPoints = playerPointsMap.get(playerEntity.getId());
                        if(playerPoints == null) {
                            int y  =1 ;
                        }
                        playerPoints.setFours(fours);
                        playerPoints.setSixes(sixes);
                        playerPoints.setBallplayed(balls);
                        playerPoints.setScore(runs);
                        playerPoints.setRuns(runs);
                        playerPoints.setStrikerate(sr);
                        playerPoints.setDismissal(dismissal);
                        dismissal = dismissal.replace("(sub)", "");
                        playerPoints.setOut(true);
                        String type = "";
                        if (dismissal.contains("not out")) {
                            type = "not_out";
                        } else if (dismissal.contains("c ") && dismissal.contains(" b ")) {
                            type = "caught";
                            Pattern pt = Pattern.compile("^c\\s+(.*?)\\s+b\\s+(.*)$");
                            Matcher matcher = pt.matcher(dismissal);
                            if (matcher.find()) {
                                String fielder = matcher.group(1); // Dhruv Jurel
                                String bowler = matcher.group(2);

                                PlayerEntity playerEntity1 = getplayer(fielder, playernamemap);
                                PlayerPoints playerPoints1 = playerPointsMap.get(playerEntity1.getId());
                                playerPoints1.setCatches(playerPoints1.getCatches() + 1);

                                PlayerEntity playerEntity2 = getplayer(bowler, playernamemap);
                                PlayerPoints playerPoints2 = playerPointsMap.get(playerEntity2.getId());
                                playerPoints2.setCatchouts(playerPoints2.getCatchouts() + 1);
                            }

                        } else if (dismissal.contains("lbw")) {
                            type = "lbw";
                            Pattern pt = Pattern.compile("^lbw\\s+b\\s+(.*)$");
                            Matcher matcher = pt.matcher(dismissal);
                            if (matcher.find()) {
                                String n1 = matcher.group();
                                PlayerEntity playerEntity1 = getplayer(n1, playernamemap);
                                PlayerPoints playerPoints1 = playerPointsMap.get(playerEntity1.getId());
                                playerPoints1.setLbw(playerPoints1.getLbw() + 1);
                            }

                        } else if (dismissal.contains("b ")) {
                            type = "bowled";
                            Pattern pt = Pattern.compile("^b\\s+(.*)$");
                            Matcher matcher = pt.matcher(dismissal);
                            if (matcher.find()) {
                                String n1 = matcher.group();
                                PlayerEntity playerEntity1 = getplayer(n1, playernamemap);
                                PlayerPoints playerPoints1 = playerPointsMap.get(playerEntity1.getId());
                                playerPoints1.setBowled(playerPoints1.getBowled() + 1);
                            }

                        } else if (dismissal.contains("run out")) {
                            type = "runout";


                            Pattern pattern = Pattern.compile("^run out\\s+\\((.*?)\\)$");
                            Matcher matcher = pattern.matcher(dismissal);

                            if (matcher.find()) {

                                String fieldersRaw = matcher.group(1); // Shimron Hetmyer/Dhruv Jurel

                                // Split multiple fielders
                                String[] fielders = fieldersRaw.split("/");

                                System.out.println("Type: runout");

                                for (String f : fielders) {

                                    PlayerEntity playerEntity1 = getplayer(f, playernamemap);
                                    PlayerPoints playerPoints1 = playerPointsMap.get(playerEntity1.getId());
                                    playerPoints1.setRunouts(playerPoints1.getRunouts() + 1);

                                }
                            }

                        } else if (dismissal.contains("st ")) {
                            type = "stumped";
                            Pattern pt = Pattern.compile("^st\\s+(.*?)\\s+b\\s+(.*)$");
                            Matcher matcher = pt.matcher(dismissal);
                            if (matcher.find()) {
                                String stumpy = matcher.group(1); // Dhruv Jurel
                                String bowler = matcher.group(2);

                                PlayerEntity playerEntity1 = getplayer(stumpy, playernamemap);
                                PlayerPoints playerPoints1 = playerPointsMap.get(playerEntity1.getId());
                                playerPoints1.setStumping(playerPoints1.getStumping() + 1);

                                PlayerEntity playerEntity2 = getplayer(bowler, playernamemap);
                                PlayerPoints playerPoints2 = playerPointsMap.get(playerEntity2.getId());
                                playerPoints2.setStumpouts(playerPoints2.getStumpouts() + 1);
                            }


                        }
                        playerPoints.setType(type);


                    }


                    // ================== BOWLING ==================
                    List<Map<String, Object>> bowling = new ArrayList<>();
                    for (Element row : innings1.select("div.scorecard-bowl-grid")) {

                        Element bowler = row.selectFirst("a[href*=/profiles/]");
                        if (bowler == null) continue;

                        Elements stats = row.select("div.justify-center.items-center");
                        if (stats.size() < 4) continue;

                        Map<String, Object> b = new HashMap<>();
                        b.put("name", bowler.text());
                        b.put("overs", Double.parseDouble(stats.get(0).text()));
                        b.put("maidens", Integer.parseInt(stats.get(1).text()));
                        b.put("runs", Integer.parseInt(stats.get(2).text()));
                        b.put("wickets", Integer.parseInt(stats.get(3).text()));
                        b.put("economy", Double.parseDouble(stats.get(stats.size() - 1).text()));
                        b.put("nb", Integer.parseInt(stats.get(4).text()));
                        b.put("wides", Integer.parseInt(stats.get(5).text()));

                        bowling.add(b);
                    }

                    for (Map<String, Object> b : bowling) {
                        String name = String.valueOf(b.get("name"));

                        double overs = ((Number) b.getOrDefault("overs", 0.0)).doubleValue();
                        int maidens = ((Number) b.getOrDefault("maidens", 0)).intValue();
                        int runs = ((Number) b.getOrDefault("runs", 0)).intValue();
                        int wickets = ((Number) b.getOrDefault("wickets", 0)).intValue();
                        int nb = ((Number) b.getOrDefault("nb", 0)).intValue();
                        int wides = ((Number) b.getOrDefault("wides", 0)).intValue();
                        double economy = ((Number) b.getOrDefault("economy", 0.0)).doubleValue();

                        // Convert overs → balls (important)
                        int fullOvers = (int) overs;
                        int extraBalls = (int) Math.round((overs - fullOvers) * 10);
                        int ballsBowled = fullOvers * 6 + extraBalls;


                        PlayerEntity playerEntity = getplayer(name, playernamemap);
                        PlayerPoints playerPoints = playerPointsMap.get(playerEntity.getId());


                        playerPoints.setWickets(wickets);
                        playerPoints.setEco(economy);
                        playerPoints.setMaidens(maidens);
                        playerPoints.setScoregiven(runs);
                        playerPoints.setBallsbowled(ballsBowled);
                        playerPoints.setNb(nb);
                        playerPoints.setWides(wides);


                    }
                    result.put("bowling", bowling);


                    // ================== EXTRAS  && TOTALS  ==================

                    // Find Total row
                    Element totalRow = null;
                    Element extrasRow = null;
                    Map<String, Object> total = new HashMap<>();
                    Map<String, Object> extras = new HashMap<>();

// Loop through all flex rows and match by text
                    for (Element div : innings1.select("div.flex.justify-between")) {
                        Element label = div.selectFirst("div.font-bold");
                        if (label == null) continue;

                        String labelText = label.text().trim();
                        if (labelText.equals("Total")) totalRow = div;
                        else if (labelText.equals("Extras")) extrasRow = div;
                    }

// --- TOTAL ---
                    if (totalRow != null) {
                        Element scoreSpan = totalRow.selectFirst("span.font-bold");
                        String scoreText = scoreSpan != null ? scoreSpan.text().trim() : "0-0";

                        String[] parts = scoreText.split("-");
                        int runs = Integer.parseInt(parts[0]);
                        int wickets = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;

                        total.put("runs", runs);
                        total.put("wickets", wickets);

                        // Detail span: "(20 Overs, RR: 11)"
                        List<Element> spans = totalRow.select("span");
                        String detail = spans.size() > 1 ? spans.get(1).text() : "";

                        Pattern oversPattern = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s+Overs");
                        Matcher om = oversPattern.matcher(detail);
                        if (om.find()) total.put("overs", Double.parseDouble(om.group(1)));

                        Pattern rrPattern = Pattern.compile("RR:\\s*([0-9.]+)");
                        Matcher rm = rrPattern.matcher(detail);
                        if (rm.find()) total.put("runRate", Double.parseDouble(rm.group(1)));
                    }

                    result.put("total", total);

// --- EXTRAS ---
                    if (extrasRow != null) {
                        Element extrasSpan = extrasRow.selectFirst("span.font-bold");
                        int extrasTotal = extrasSpan != null ? Integer.parseInt(extrasSpan.text().trim()) : 0;
                        extras.put("total", extrasTotal);

                        // Detail span: "(b 0, lb 2, w 7, nb 1, p 0)"
                        List<Element> spans = extrasRow.select("span");
                        String detail = spans.size() > 1 ? spans.get(1).text() : "";

                        // Strip parentheses
                        detail = detail.replaceAll("[()]", "");

                        Pattern b = Pattern.compile("b\\s+(\\d+)");
                        Pattern lb = Pattern.compile("lb\\s+(\\d+)");
                        Pattern w = Pattern.compile("w\\s+(\\d+)");
                        Pattern nb = Pattern.compile("nb\\s+(\\d+)");
                        Pattern p = Pattern.compile("p\\s+(\\d+)");

                        Matcher m;
                        m = b.matcher(detail);
                        if (m.find()) extras.put("byes", Integer.parseInt(m.group(1)));
                        m = lb.matcher(detail);
                        if (m.find()) extras.put("legByes", Integer.parseInt(m.group(1)));
                        m = w.matcher(detail);
                        if (m.find()) extras.put("wides", Integer.parseInt(m.group(1)));
                        m = nb.matcher(detail);
                        if (m.find()) extras.put("noBalls", Integer.parseInt(m.group(1)));
                        m = p.matcher(detail);
                        if (m.find()) extras.put("penalty", Integer.parseInt(m.group(1)));
                    }

                    result.put("extras", extras);
                    System.out.println(result);
                    if(id1 == 1){
                        r1 = result;
                    }else {
                        r2= result;
                    }

                }
                System.out.println("fetched data");
                if(matchState.getInnings() == 1) {
                    matchState.setInnings1(mapper.writeValueAsString(r1));
                    Integer id = httpCaller.fetchInnigs(matchid+"");
                    if(id == 2) {
                        matchState.setInnings(2);

                    }
                    if(!iscompleted  && !Objects.equals(matchInfoEntity.getState(), "Live")) {
                        matchInfoEntity.setState("Live");
                        if(autoteam) {
                            autoload(matchid);
                            autoteam = false;
                        }
                        matchrepo.save(matchInfoEntity);
                    }
                } else  {
                    matchState.setInnings1(mapper.writeValueAsString(r1));
                    matchState.setInnings2(mapper.writeValueAsString(r2));
                   Map<String,Object> objectMap =  httpCaller.iscomplete(matchid+"");
                   if(objectMap.containsKey("state") && objectMap.get("state").toString().equalsIgnoreCase("complete")

                   && !objectMap.get("status").toString().contains("rain")
                   ) {
                       matchState.setMatchstatus(objectMap.get("status").toString());
                       iscompleted = true;
                       matchInfoEntity.setState("Completed");
                       matchrepo.save(matchInfoEntity);
                   }
                }
                matchState.setTimestamp(System.currentTimeMillis());
                points.forEach(x->{
                    x.setTotalpoints(FantasyPointsCalculator.calculateTotalPoints(x));
                });
                playerPointsrepo.saveAll(points);
                matchStaterepo.save(matchState);
                String i1  =  matchState.getInnings1() == null ? "": matchState.getInnings1();
                String i2 =  matchState.getInnings2() == null ? "": matchState.getInnings2();
                if(change >= 10) {
                    Map<String,Object> objectMap =  httpCaller.iscomplete(matchid+"");
                    if(objectMap.containsKey("status") ) {
                        String status = objectMap.get("status").toString();
                        String state = objectMap.get("state").toString();
                        if (state.equalsIgnoreCase("complete")) {
                            if (state.contains("rain") || status.contains("rain")) {

                                iscompleted = true;
                                matchInfoEntity.setState("Abandoned");
                                matchState.setMatchstatus(status);
                                matchrepo.save(matchInfoEntity);
                                matchStaterepo.save(matchState);
                            }
                        }
                    }
                    change = 0;
                    sleep(30);

                }
                if(!ikey.equals(i1+":"+i2)) {
                    ikey = i1 + ":" + i2;
                    change = 0;
                    leaderBoardService.getpoints(matchid);
                    notificationController.sendEvent("refresh",matchid);

                } else {
                    change = change  + 1;

                }


                if(iscompleted  ) {
                    if(verify == 2) {
                        break;
                    }
                    verify = verify + 1;
                    sleep(2);
                }else {
                    sleep(30);
                }
            } catch (IOException e) {
                System.err.println("Error fetching the page: " + e.getMessage());
            }
        }
        System.out.println("Match completed");
        LeaderBoardService.points = new ArrayList<>();
        matchesService.saveplayers(matchid,false);


    }

}
