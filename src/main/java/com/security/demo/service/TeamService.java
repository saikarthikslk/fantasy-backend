package com.security.demo.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.security.demo.DBmodel.CustomTeamEntity;
import com.security.demo.DBmodel.MatchInfoEntity;
import com.security.demo.DBmodel.PlayerEntity;
import com.security.demo.DBmodel.PlayerPoints;
import com.security.demo.model.*;
import com.security.demo.repo.CustomTeamrepo;
import com.security.demo.repo.Matchrepo;
import com.security.demo.repo.PlayerPointsrepo;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TeamService {
    private static ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private CustomTeamrepo customTeamrepo;
    @Autowired
    private MatchesService matchesService;
    @Autowired
    PlayerPointsrepo playerPointsrepo;
    @Autowired
    Matchrepo matchrepo;
    public boolean createTeam(DreamTeam dreamTeam, String email) throws JsonProcessingException {
        Optional<MatchInfoEntity> entity= matchrepo.findById(dreamTeam.getMatchid());
        if(entity.isEmpty()) {
            return false ;
        }
        if(!entity.get().getState().equals("Upcoming")) {
            return false;
        }
        customTeamrepo.deleteifexistsbym(email,dreamTeam.getMatchid());
        CustomTeamEntity customTeam = new CustomTeamEntity();
        customTeam.setEmail(email);
        customTeam.setCreated_at(Timestamp.from(Instant.now()));
        customTeam.setMatch_id(dreamTeam.getMatchid());
        customTeam.setTeam(objectMapper.writeValueAsString(dreamTeam));
        customTeamrepo.save(customTeam);
        return true;
    }
    public TeamPoints fetchteam(Integer matchid , Integer dreamid) throws JsonProcessingException {

        CustomTeamEntity customTeam = customTeamrepo.findById(dreamid).get();
        MatchSelection matchSelection =  matchesService.fetchPlayers(matchid,customTeam.getEmail());
        CustomTeamEntity dreamTeam = matchSelection.getDreamTeam();
        Map<String,Object> team  =  objectMapper.readValue(dreamTeam.getTeam(),
                new TypeReference<Map<String, Object>>() {});
        Map<String,PlayerEntity> playerEntity = matchSelection.getPlayers().stream().collect(Collectors.toMap(x->x.getId(),x->x,(a,b)->a));
        TeamPoints teamPoints = new TeamPoints();
        List<Playerchosen> playerchosens = new ArrayList<>();
        teamPoints.setMatchid(matchid);
        teamPoints.setDid(dreamid);
        teamPoints.setCaptain(team.get("captainPlayerId").toString());
        teamPoints.setVcaptain(team.get("viceCaptainPlayerId").toString());
        teamPoints.setPlayerEntities(playerchosens);
        teamPoints.setTotalpoints(0.0);
        List<Map<String, Object>> props = (List<Map<String, Object>>) team.get("properties");
        Map<String, PlayerPoints> pp = playerPointsrepo.getpointsbymatchid(matchid).stream().collect(Collectors.toMap(PlayerPoints::getPlayerid, x->x , (x, b)->x));

        Double points = 0.0;
        for (Map<String,Object> stringObjectMap : props) {
            String id  = stringObjectMap.get("playerid").toString();
            String type=  stringObjectMap.get("type").toString();
            PlayerEntity playerEntity1 = playerEntity.get(id);
            Playerchosen p = new Playerchosen();
            p.setTeam(playerEntity1.getTeam().getTeamSName());
            p.setName(playerEntity1.getName());
            p.setPlayerid(id);
            p.setPoints(0.0);
            if(pp.containsKey(p.getPlayerid())){
                p.setPoints(pp.get(p.getPlayerid()).getTotalpoints());
            }
            p.setType(type);
            if(p.getPlayerid().equals(teamPoints.getCaptain()) ){
                p.setPoints(p.getPoints() * 2);
            } else if (p.getPlayerid().equals(teamPoints.getVcaptain())) {
                p.setPoints(p.getPoints()* 1.5);
            }
            points = points + p.getPoints();
            p.setUrl(playerEntity1.getImageId()+"");
            playerchosens.add(p);
        }
        teamPoints.setTotalpoints(points);
        return teamPoints;
    }



}
