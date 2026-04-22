package com.security.demo.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.security.demo.DBmodel.*;
import com.security.demo.config.WhatsAppSender;
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
    @Autowired
    WhatsAppSender sender;
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
                   entity.setIsauto(true);
                   tobesaved.add(entity);
               });
               customTeamrepo.saveAll(tobesaved);
           }
    }
       return team[0];


    }
    public void sendmessage (Integer mathcid){
//        userrepo.getnums().forEach (x-> sender.sendTemplateMessage("91"+x,mathcid)
//        );
    }
    public void pullscorecard(Integer matchid) throws IOException, InterruptedException {

        MatchesService.matches = new ArrayList<>();
        MatchSelection matchSelection = matchesService.fetchPlayers(matchid, "");
        System.out.println("Running Match " + matchid );
        MatchState matchState = matchStaterepo.getstate(matchid);
        if(matchState == null){
            matchState = new MatchState();
            matchState.setMatchid(matchid);
            matchState.setTimestamp(System.currentTimeMillis());
            matchStaterepo.save(matchState);
            MatchesService.matches = new ArrayList<>();
        }
        MatchInfoEntity matchInfoEntity = matchrepo.findById(matchid).get();
        matchesService.saveplayers(matchid, true);
        Integer checktoss = 2;
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
                    checktoss -=1;
                    if(checktoss ==0 ) {
                        break;
                    }
                }
                Long seconds = (System.currentTimeMillis()- matchInfoEntity.getStartDate())/1000;
                if(seconds > 5 * 3600) {
                    Map<String,Object> objectMap =  httpCaller.iscomplete(matchid+"");
                    if(objectMap.containsKey("status") ) {
                        String status = objectMap.get("status").toString();
                        String state = objectMap.get("state").toString();
                        if (state.equalsIgnoreCase("complete")) {
                            if ( (state.contains("rain") || status.contains("rain"))   &&  !status.toLowerCase().contains("won") ) {
                                matchInfoEntity.setState("Abandoned");
                                matchState.setMatchstatus(status);
                                matchrepo.save(matchInfoEntity);
                                matchStaterepo.save(matchState);
                                MatchesService.matches = new ArrayList<>();
                                return;
                            }
                        }
                    }
                }
                sleep(15);

            }
        }
        MatchesService.matches = new ArrayList<>();
        notificationController.sendEvent("refresh",matchid);
        matchStaterepo.save(matchState);
        System.out.println("Checking for Squad Data");
        while (true) {
            if(matchesService.saveplayers(matchid, true)){
//                sendmessage(matchid);

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

            if(id > 0 ){
                innings[0]=id;
                break;
            }
            sleep(30);
        }
        MatchesService.matches = new ArrayList<>();



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
                        MatchesService.matches = new ArrayList<>();
                    }
                } else  {
                    matchState.setInnings1(mapper.writeValueAsString(r1));
                    matchState.setInnings2(mapper.writeValueAsString(r2));
                   Map<String,Object> objectMap =  httpCaller.iscomplete(matchid+"");
                   if(objectMap.containsKey("state") && objectMap.get("state").toString().equalsIgnoreCase("complete")

                   &&  objectMap.get("status").toString().toLowerCase().contains("won")

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

                String i1  =  matchState.getInnings1() == null ? "": matchState.getInnings1();
                String i2 =  matchState.getInnings2() == null ? "": matchState.getInnings2();
                if(change >= 10) {
                    Map<String,Object> objectMap =  httpCaller.iscomplete(matchid+"");
                    if(objectMap.containsKey("status") ) {
                        String status = objectMap.get("status").toString();
                        String state = objectMap.get("state").toString();
                        if (state.equalsIgnoreCase("complete")) {
                            if (( state.contains("rain") || status.toLowerCase().contains("rain")  )   && !status.toLowerCase().contains("won")   ) {

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
            
                System.out.println(ikey);
                if(!ikey.equals(i1+":"+i2)) {
                    change = 0;
                    ikey = i1+":"+i2;
                    matchState.setInnings1(mapper.writeValueAsString(r1));
                    matchState.setInnings2(mapper.writeValueAsString(r2));
                    notificationController.sendEvent("refresh",matchid);
                    playerPointsrepo.saveAll(points);
                    matchStaterepo.save(matchState);

                } else {
                    change = change  + 1;

                }

                System.out.println("data saved");


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
        MatchesService.matches = new ArrayList<>();
        LeaderBoardService.points = new ArrayList<>();
        matchesService.saveplayers(matchid,false);


    }

}
