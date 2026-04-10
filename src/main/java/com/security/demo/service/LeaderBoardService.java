package com.security.demo.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.security.demo.DBmodel.*;
import com.security.demo.model.*;
import com.security.demo.repo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class LeaderBoardService {
    public static ObjectMapper mapper = new ObjectMapper();
    @Autowired
    CustomTeamrepo customTeamrepo;
    @Autowired
    Userrepo userrepo;

    @Autowired
    PlayerRepo playerRepo;

    @Autowired
    PlayerPointsrepo playerPointsrepo;
    @Autowired
    MatchesService matchesService;

    @Autowired
    MatchStaterepo staterepo;
    @Autowired
    Matchrepo matchrepo;

    public static List<OverallPoints> points = new ArrayList<>();

    public Map<Integer,List<TeamPoints>> scores = new HashMap<>();

    public List<TeamPoints> getmatches(Integer matchid , String  email) throws JsonProcessingException {


        return getpoints(matchid , email);
    }
    public List<TeamPoints> getpoints(Integer matchid , String  email) throws JsonProcessingException {
        List<CustomTeamEntity> customTeam =  customTeamrepo.findbymatchid(matchid);
        if(customTeam.isEmpty()) {
            return  new ArrayList<>();
        }
        Map<String,PlayerPoints> pp = playerPointsrepo.getpointsbymatchid(matchid).stream().collect(Collectors.toMap(PlayerPoints::getPlayerid, x->x , (x, b)->x));
        MatchInfoEntity entity = matchrepo.findById(matchid).get();
        String state = entity.getState();
        List<TeamPoints> teamPoints = new ArrayList<>();
        for (CustomTeamEntity c1 : customTeam) {
            User user = userrepo.findByEmail(c1.getEmail());
            Map<String,PlayerEntity> PL = playerRepo.findAll().stream().collect(Collectors.toMap(PlayerEntity::getId, x->x , (x, b)->x));
            Map<String,Object> team = mapper.readValue(c1.getTeam(),new TypeReference<Map<String, Object>>() {});
            TeamPoints points = new TeamPoints();
            points.setMatchid(matchid);
            points.setName(user.getGamename());
            points.setEmail(user.getEmail());
            points.setCaptain(team.get("captainPlayerId").toString());
            points.setVcaptain(team.get("viceCaptainPlayerId").toString());
            List<Object> players = (List<Object>) team.get("properties");
            Double totalpoints=  0.0;
            List<Playerchosen> playerchosens = new ArrayList<>();
            for (Object overall : players) {
                Map<String,Object> player = (Map<String, Object>) overall;
                String playerid = player.get("playerid").toString();
                PlayerEntity playerEntity = PL.get(playerid);

                Playerchosen playerchosen = new Playerchosen();
                playerchosen.setPlayerid(playerid);
                playerchosen.setUrl(playerEntity.getImageId()+"");
                double pt= pp.get(playerchosen.getPlayerid())== null?0: pp.get(playerchosen.getPlayerid()).getTotalpoints();
                playerchosen.setTeam(playerEntity.getTeam().getTeamId()+"");
                playerchosen.setType(playerEntity.getType());
                playerchosen.setName(playerEntity.getName());
                playerchosen.setPoints(pt);

                if(playerchosen.getPlayerid().equals(points.getCaptain())) {
                    playerchosen.setPoints(pt*2);
                } else if (playerchosen.getPlayerid().equals(points.getVcaptain())) {
                    playerchosen.setPoints(pt*1.5);

                }
                totalpoints = totalpoints+playerchosen.getPoints();

                playerchosens.add(playerchosen);

            }

            if(state.equalsIgnoreCase("Upcoming")) {
                if(email.equals(c1.getEmail())) {
                    points.setPlayerEntities(playerchosens);
                    points.setDid(c1.getId());
                }
            }else  {
                points.setPlayerEntities(playerchosens);
                points.setDid(c1.getId());

            }
            points.setTotalpoints(totalpoints);
            points.setImageurl(user.getProfielpic());
            teamPoints.add(points);
        }

        teamPoints.sort(Comparator.comparing(TeamPoints::getTotalpoints).reversed());
        IntStream.range(1,teamPoints.size()+1).forEach(i -> {
            teamPoints.get(i-1).setPosition(i);
        });

        return teamPoints;
    }
        public MatchState fetchscore(Integer matchid) {
        MatchState matchState =  staterepo.getstate(matchid);
        if(matchState == null) {
            matchState = new MatchState();
            matchState.setMatchstatus("UPCOMING");
            return matchState;
        }
        else {
            return matchState;
        }
    }
    public List<OverallPoints> overall() throws JsonProcessingException {
        if(LeaderBoardService.points.size() > 0 ){
            return  LeaderBoardService.points;
        }
        List<MatchInfoEntity >matchInfoEntities = matchesService.fetchliveorcompleted();
        List<OverallPoints> points = new ArrayList<>();
        Map<String,OverallPoints> map = new HashMap<>();
        for (MatchInfoEntity match : matchInfoEntities) {
            List<TeamPoints> points1 =  getmatches(match.getMatchId(),"");
            for (TeamPoints points2 : points1) {
                String name = points2.getEmail();
                if(!map.containsKey(name)) {
                    map.put(name, new OverallPoints());
                    map.get(name).setName(points2.getName());
                    map.get(name).setEmail(points2.getEmail());
                    map.get(name).setImageurl(points2.getImageurl());
                    map.get(name).setStats(new ArrayList<>());
                }
                map.get(name).setTotalpoints(map.get(name).getTotalpoints() + points2.getTotalpoints());
                ChildStats childStats  = new ChildStats();
                childStats.setDid(points2.getDid());
                childStats.setMatchid(points2.getMatchid());
                childStats.setPoints(points2.getTotalpoints());
                childStats.setPosition(points2.getPosition());
                childStats.setT1(match.getTeam1());
                childStats.setT2(match.getTeam2());
                childStats.setTimestamp(match.getStartDate());
                map.get(name).getStats().add(childStats);
            }
        }
        map.forEach((a,b)->{
         b.getStats().sort(Comparator.comparing(ChildStats::getTimestamp).reversed());
        });

        points = new ArrayList<>(map.values());
        points.sort(Comparator.comparing(OverallPoints::getTotalpoints).reversed());
        LeaderBoardService.points=points;
        return points;

    }
}
