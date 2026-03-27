package com.security.demo.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.security.demo.DBmodel.MatchInfoEntity;
import com.security.demo.DBmodel.PlayerEntity;
import com.security.demo.DBmodel.TeamEntity;
import com.security.demo.DBmodel.VenueEntity;
import com.security.demo.model.Match;
import com.security.demo.model.PLayerwrapper;
import com.security.demo.model.Player;
import com.security.demo.model.Root;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class HttpCaller {


    public static String  API_KEY="rrtueYl8btuhcwO0UxN4rrbK8vpyB5z0sGD9vIVU8pDiqLOXHa";
//    QTnGbSZd7gRJ3lrDOmEVhgL9Wbq4PyRVZzptBo7LzDWZ2LEGiX
    public static String  API_HOST="Cricbuzz-Official-Cricket-API.allthingsdev.co";
    public static  String API_ENDPOINT= "038d223b-aca5-4096-8eb1-184dd0c09513";
    public static String url = "https://Cricbuzz-Official-Cricket-API.proxy-production.allthingsdev.co/";
    public static String freeurl = "https://www.cricbuzz.com/api/mcenter/commentary-pagination/";

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
    public void fetchsavematches() throws IOException, InterruptedException {


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
                .header("redirect","follow")
                .uri(URI.create(freeurl+query))
                .GET()
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

        }
        return responsetext;

    }
    public void loadTeamData() {
        List<TeamEntity> teamEntities = teamRepo.findAll();
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



}
