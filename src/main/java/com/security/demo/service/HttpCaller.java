package com.security.demo.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.security.demo.DBmodel.MatchInfoEntity;
import com.security.demo.DBmodel.PlayerEntity;
import com.security.demo.DBmodel.TeamEntity;
import com.security.demo.DBmodel.VenueEntity;
import com.security.demo.model.*;
import com.security.demo.repo.Matchrepo;
import com.security.demo.repo.PlayerRepo;
import com.security.demo.repo.TeamRepo;
import com.security.demo.repo.Venuerepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class HttpCaller {


    public static String  API_KEY="QTnGbSZd7gRJ3lrDOmEVhgL9Wbq4PyRVZzptBo7LzDWZ2LEGiX";
//    QTnGbSZd7gRJ3lrDOmEVhgL9Wbq4PyRVZzptBo7LzDWZ2LEGiX
//    rrtueYl8btuhcwO0UxN4rrbK8vpyB5z0sGD9vIVU8pDiqLOXHa
    public static String  API_HOST="Cricbuzz-Official-Cricket-API.allthingsdev.co";
    public static  String API_ENDPOINT= "038d223b-aca5-4096-8eb1-184dd0c09513";
    public static String url = "https://Cricbuzz-Official-Cricket-API.proxy-production.allthingsdev.co/";
    public static String freeurl = "https://www.cricbuzz.com/api/mcenter/commentary-pagination/";
    public static String boardurl = "https://www.cricbuzz.com/api/mcenter/$/miniscore";
    public static HttpClient client = HttpClient.newHttpClient();
    public static    ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private Matchrepo matchRepo;
    @Autowired
    private Venuerepo venueRepo;
    @Autowired
    private TeamRepo teamRepo;
    @Autowired
    private PlayerRepo playerRepo;
    @Autowired
    private Venuerepo repo;
    public void fetchsavematches() throws IOException, InterruptedException {
      List<Integer> matchids=   matchRepo.findAll().stream().map(x->x.getMatchId()).collect(Collectors.toList());
        List<Integer> venuids=   repo.findAll().stream().map(x->x.getId()).collect(Collectors.toList());
        HttpRequest request = HttpRequest.newBuilder()
                .header("Content-Type", "application/json")
                .header("x-apihub-key", API_KEY)
                .header("x-apihub-host", API_HOST)
                .header("x-apihub-endpoint",API_ENDPOINT)
                .header("redirect","follow")
                .uri(URI.create(url+"series/9241"))
                .GET()
                .build();

        HttpResponse<String> response = HttpCaller.client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("Status Code: " + response.statusCode());

        if (response.statusCode() == 200) {
          String body = response.body();
          try {

              Root root = mapper.readValue(body, Root.class);
              root.getMatchDetails().forEach( x -> {
                  x.getMatchDetailsMap().getMatch().forEach( y -> {
                      Match info =  y.getMatchInfo();
                      if(matchids.contains(info.getMatchId()) &&  venuids.contains(info.getVenueInfo().getId()) ){
                          return;
                      }
                      TeamEntity team1 = new TeamEntity(
                              info.getTeam1().getTeamId(),
                              info.getTeam1().getTeamName(),
                              info.getTeam1().getTeamSName(),
                              info.getTeam1().getImageId()
                      );

                      matchRepo.deletebytid(team1.getTeamId());
                      team1 = teamRepo.save(team1);// returns managed entity with ID

                      // save team2
                      TeamEntity team2 = new TeamEntity(
                              info.getTeam2().getTeamId(),
                              info.getTeam2().getTeamName(),
                              info.getTeam2().getTeamSName(),
                              info.getTeam2().getImageId()
                      );

                      matchRepo.deletebytid(team2.getTeamId());
                      team2 = teamRepo.save(team2);

                      // save venue
                      VenueEntity venue = new VenueEntity(
                              info.getVenueInfo().getId(),
                              info.getVenueInfo().getGround(),
                              info.getVenueInfo().getCity(),
                              info.getVenueInfo().getTimezone()
                      );
                      matchRepo.deletebyvid(venue.getId());
                      venue = venueRepo.save(venue);



                      // save match — FK refs point to saved team1, team2, venue
                      MatchInfoEntity match = new MatchInfoEntity();
                      match.setMatchId(info.getMatchId());
                      match.setSeriesId(info.getSeriesId());
                      match.setSeriesName(info.getSeriesName());
                      match.setMatchDesc(info.getMatchDesc());
                      match.setMatchFormat(info.getMatchFormat());
                      match.setStartDate(info.getStartDate());
                      match.setEndDate(info.getEndDate());
                      match.setState(info.getState());
                      match.setStatus(info.getStatus());
                      match.setTeam1(team1);     // FK → team.team_id
                      match.setTeam2(team2);     // FK → team.team_id
                      match.setVenueInfo(venue); // FK → venue.id
                      matchRepo.deletebymid(match.getMatchId());
                      matchRepo.save(match);

                      System.out.println("Adding new match   " + match.getMatchId() );
                      loadTeamData(List.of(team1,team2));
                      System.out.println("Loading Squad data ");
                      loadSquaddata(match.getMatchId());
                  });
              });
          }catch (Exception e) {
              System.out.println("invalid response");
          }

        }


    }
    public void loadmatch(Integer matchid) throws IOException, InterruptedException {


        HttpRequest request = HttpRequest.newBuilder()
                .header("Content-Type", "application/json")
                .header("x-apihub-key", API_KEY)
                .header("x-apihub-host", API_HOST)
                .header("x-apihub-endpoint",API_ENDPOINT)
                .header("redirect","follow")
                .uri(URI.create(url+"series/9241"))
                .GET()
                .build();

        HttpResponse<String> response = HttpCaller.client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("Status Code: " + response.statusCode());

        if (response.statusCode() == 200) {
            String body = response.body();
            try {

                Root root = mapper.readValue(body, Root.class);
                root.getMatchDetails().forEach( x -> {
                    x.getMatchDetailsMap().getMatch().forEach( y -> {
                        Match info =  y.getMatchInfo();
                        TeamEntity team1 = new TeamEntity(
                                info.getTeam1().getTeamId(),
                                info.getTeam1().getTeamName(),
                                info.getTeam1().getTeamSName(),
                                info.getTeam1().getImageId()
                        );
                        team1 = teamRepo.save(team1);  // returns managed entity with ID

                        // save team2
                        TeamEntity team2 = new TeamEntity(
                                info.getTeam2().getTeamId(),
                                info.getTeam2().getTeamName(),
                                info.getTeam2().getTeamSName(),
                                info.getTeam2().getImageId()
                        );
                        team2 = teamRepo.save(team2);

                        // save venue
                        VenueEntity venue = new VenueEntity(
                                info.getVenueInfo().getId(),
                                info.getVenueInfo().getGround(),
                                info.getVenueInfo().getCity(),
                                info.getVenueInfo().getTimezone()
                        );
                        venue = venueRepo.save(venue);

                        // save match — FK refs point to saved team1, team2, venue
                        MatchInfoEntity match = new MatchInfoEntity();
                        match.setMatchId(info.getMatchId());
                        match.setSeriesId(info.getSeriesId());
                        match.setSeriesName(info.getSeriesName());
                        match.setMatchDesc(info.getMatchDesc());
                        match.setMatchFormat(info.getMatchFormat());
                        match.setStartDate(info.getStartDate());
                        match.setEndDate(info.getEndDate());
                        match.setState(info.getState());
                        match.setStatus(info.getStatus());
                        match.setTeam1(team1);     // FK → team.team_id
                        match.setTeam2(team2);     // FK → team.team_id
                        match.setVenueInfo(venue); // FK → venue.id

                        matchRepo.save(match);
                    });
                });
            }catch (Exception e) {
                System.out.println("invalid response");
            }

        }


    }
    public List<Map<String, Object>> fetchdata(String query){
        HttpRequest request = HttpRequest.newBuilder()
                .header("Content-Type", "application/json")
                .header("User-Agent", "Mozilla/5.0")
                .uri(URI.create(freeurl+query))

                .version(HttpClient.Version.HTTP_1_1).GET()
                .build();

        List<Map<String, Object>> responsetext = new ArrayList();
        try {
            HttpResponse<String> response = HttpCaller.client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                responsetext =
                        mapper.readValue(response.body(),
                                new TypeReference<List<Map<String, Object>>>() {});
            }

        }catch (Exception e) {
            System.out.println(e.getMessage());

        }
        return responsetext;

    }
    public Map<String,Object> fetchtoss(String matchid){
        HttpRequest request = HttpRequest.newBuilder()
                .header("Content-Type", "application/json")
                .header("User-Agent", "Mozilla/5.0")
                .uri(URI.create(boardurl.replace("$",matchid)))

                .version(HttpClient.Version.HTTP_1_1).GET()
                .build();

        Map<String, Object> responsetext = new HashMap<>();
        try {
            HttpResponse<String> response = HttpCaller.client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                responsetext =
                        mapper.readValue(response.body(),
                                new TypeReference<Map<String, Object>>() {});
            }


            Map<String,Object> objectMap = (Map<String, Object>) responsetext.get("matchHeader");
            if(objectMap.containsKey("tossResults")) {
                Map<String,Object> toss = (Map<String, Object>) objectMap.get("tossResults");

                if(toss.size() >0) {

                    String teamid = toss.get("tossWinnerId").toString();
                    String decision = toss.get("decision").toString();
                    responsetext.put("status",objectMap.get("status").toString());
                    responsetext.put("decision",decision);
                    responsetext.put("teamid",teamid);
                    return responsetext;

                }
            }
            return new HashMap<>();


        }catch (Exception e) {
            System.out.println(e.getMessage());

        }
        return new HashMap<>();


    }
    public Integer fetchInnigs(String matchid){
        HttpRequest request = HttpRequest.newBuilder()
                .header("Content-Type", "application/json")
                .header("User-Agent", "Mozilla/5.0")
                .uri(URI.create(boardurl.replace("$",matchid)))

                .version(HttpClient.Version.HTTP_1_1).GET()
                .build();

        Map<String, Object> responsetext = new HashMap<>();
        try {
            HttpResponse<String> response = HttpCaller.client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                responsetext =
                        mapper.readValue(response.body(),
                                new TypeReference<Map<String, Object>>() {});
            }


            Map<String,Object> objectMap = (Map<String, Object>) responsetext.get("miniscore");
            if(objectMap!=null && objectMap.size() > 0) {
                Integer inningsId =Integer.parseInt (objectMap.get("inningsId").toString());

                return inningsId;
            }
            return -1;


        }catch (Exception e) {
            System.out.println(e.getMessage());

        }
        return -1;


    }
    public Map<String,Object> iscomplete(String matchid){
        HttpRequest request = HttpRequest.newBuilder()
                .header("Content-Type", "application/json")
                .header("User-Agent", "Mozilla/5.0")
                .uri(URI.create(boardurl.replace("$",matchid)))

                .version(HttpClient.Version.HTTP_1_1).GET()
                .build();

        Map<String, Object> responsetext = new HashMap<>();
        try {
            HttpResponse<String> response = HttpCaller.client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                responsetext =
                        mapper.readValue(response.body(),
                                new TypeReference<Map<String, Object>>() {});
            }


            Map<String,Object> objectMap = (Map<String, Object>) responsetext.get("matchHeader");
            if(objectMap!=null && objectMap.containsKey("state")) {
                String state = objectMap.get("state").toString();
                Map<String,Object> o1 = (Map<String, Object>) responsetext.get("miniscore");
                if(o1!=null && o1.containsKey("status")) {
                    String status = o1.get("status").toString();
                    responsetext.put("status",status);
                }

                responsetext.put("state",state);

                return responsetext;
            }
            return new HashMap<>();


        }catch (Exception e) {
            System.out.println(e.getMessage());

        }
        return new HashMap<>();


    }
    public void loadTeamData(List<TeamEntity> teamEntities) {
        List<PlayerEntity> playerEntities = new ArrayList<>();
        teamEntities.forEach(x->{
            int id = x.getTeamId();
            HttpRequest request = HttpRequest.newBuilder()
                    .header("Content-Type", "application/json")
                    .header("x-apihub-key", API_KEY)
                    .header("x-apihub-host", API_HOST)
                    .header("x-apihub-endpoint",API_ENDPOINT)
                    .header("redirect","follow")
                    .uri(URI.create(url+String.format("team/%s/players",id)))
                    .GET()
                    .build();

            HttpResponse<String> response = null;
            try {
                response = HttpCaller.client.send(request, HttpResponse.BodyHandlers.ofString());
                System.out.println("Status Code: " + response.statusCode());

                if (response.statusCode() == 200) {
                    PLayerwrapper players = mapper.readValue(response.body(), PLayerwrapper.class);

                    String[] type = {"BATSMEN"};
                    players.getPlayers().forEach(player -> {
                        if(player.getId() == null) {
                            type[0] = player.getName();
                        }else {
                            PlayerEntity entity = new PlayerEntity(
                                    player.getId(),
                                    player.getName(),
                                    player.getImageId(),
                                    player.getBattingStyle(),
                                    player.getBowlingStyle()
                            );
                            entity.setType(type[0]);
                            entity.setTeam(x);
                            playerEntities.add(entity);
                        }
                    });


                }

            } catch (IOException | InterruptedException ignored) {

            }


        });
        playerRepo.saveAll(playerEntities);

    }
    public void loadSquaddata(Integer matchid) {
        List<MatchInfoEntity> matchInfoEntities =List.of( matchRepo.findById(matchid).get());
        List<String> playerEntities1 = playerRepo.findAll().stream().map(PlayerEntity::getId).toList();
        List<PlayerEntity> playerEntitiestobesaved = new ArrayList<>();
         Map<String,TeamEntity> teams = teamRepo.findAll().stream().collect(Collectors.toMap(x->x.getTeamSName() , x -> x , (a,b)->a));
        matchInfoEntities.forEach(x->{
            int id = x.getMatchId();
            HttpRequest request = HttpRequest.newBuilder()
                    .header("Content-Type", "application/json")
                    .header("x-apihub-key", API_KEY)
                    .header("x-apihub-host", API_HOST)
                    .header("x-apihub-endpoint",API_ENDPOINT)
                    .header("redirect","follow")
                    .uri(URI.create(url+String.format("match/%s/squads",id)))
                    .GET()
                    .build();

            HttpResponse<String> response = null;
            try {
                response = HttpCaller.client.send(request, HttpResponse.BodyHandlers.ofString());
                System.out.println("Status Code: " + response.statusCode());

                if (response.statusCode() == 200) {
                    MatchData players = mapper.readValue(response.body(), MatchData.class);
                    players.getTeam1().getPlayers()
                            .forEach(
                                    p1-> {
                                        if(!p1.getCategory().equals("playing XI") && !p1.getCategory().equals("substitutes") && !p1.getCategory().equals("bench")) {
                                            return;
                                        }

                                        p1.getPlayer().forEach(p2-> {
                                            if(!playerEntities1.contains(p2.getId())) {
                                                PlayerEntity entity = new PlayerEntity(
                                                        p2.getId(),
                                                        p2.getName(),
                                                        p2.getImageId(),
                                                        p2.getBattingStyle(),
                                                        p2.getBowlingStyle()
                           );
                                                if(p2.getRole().contains("Allrounder")) {
                                                    entity.setType("ALL ROUNDER");
                                                } else if (p2.getRole().contains("Bowl")) {
                                                    entity.setType("BOWLER");
                                                } else if (p2.getRole().contains("WK")) {
                                                    entity.setType("WICKET KEEPER");
                                                }{
                                                    entity.setType("BATSMEN");
                                                }

                                                entity.setTeam(teams.get(p2.getTeamname()));
                                                playerEntitiestobesaved.add(entity);


                                            }
                                        });
                                    }
                            );
                    players.getTeam2().getPlayers()
                            .forEach(
                                    p1-> {
                                        if(!p1.getCategory().equals("playing XI") && !p1.getCategory().equals("substitutes") && !p1.getCategory().equals("bench")) {
                                            return;
                                        }
                                        p1.getPlayer().forEach(p2-> {
                                            if(!playerEntities1.contains(p2.getId())) {
                                                PlayerEntity entity = new PlayerEntity(
                                                        p2.getId(),
                                                        p2.getName(),
                                                        p2.getImageId(),
                                                        p2.getBattingStyle(),
                                                        p2.getBowlingStyle()
                                                );
                                                if(p2.getRole().contains("Allrounder")) {
                                                    entity.setType("ALL ROUNDER");
                                                } else if (p2.getRole().contains("Bowl")) {
                                                    entity.setType("BOWLER");
                                                } else if (p2.getRole().contains("WK")) {
                                                    entity.setType("WICKET KEEPER");
                                                }{
                                                    entity.setType("BATSMEN");
                                                }

                                                entity.setTeam(teams.get(p2.getTeamname()));
                                                playerEntitiestobesaved.add(entity);


                                            }
                                        });
                                    }
                            );
                }

            } catch (IOException | InterruptedException ignored) {
            }
        });
        playerRepo.saveAll(playerEntitiestobesaved);

    }


}
