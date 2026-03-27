package com.security.demo.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.security.demo.DBmodel.CustomTeamEntity;
import com.security.demo.model.DreamTeam;
import com.security.demo.repo.CustomTeamrepo;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;

@Service
public class TeamService {
    private static ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private CustomTeamrepo customTeamrepo;
    public boolean createTeam(DreamTeam dreamTeam, String email) throws JsonProcessingException {
        customTeamrepo.deleteifexists(email);
        CustomTeamEntity customTeam = new CustomTeamEntity();
        customTeam.setEmail(email);
        customTeam.setCreated_at(Timestamp.from(Instant.now()));
        customTeam.setMatch_id(dreamTeam.getMatchid());
        customTeam.setTeam(objectMapper.writeValueAsString(dreamTeam.getProperties()));
        customTeamrepo.save(customTeam);
        return true;
    }
}
