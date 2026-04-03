package com.security.demo.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.security.demo.config.User;
import com.security.demo.model.DreamTeam;
import com.security.demo.service.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/teams")
public class TeamController {

    @Autowired
    TeamService teamService;
    @PostMapping("/create")
    public boolean createTeam(@AuthenticationPrincipal User user, @RequestBody DreamTeam dreamTeam) throws JsonProcessingException {
      String email = user.getUsername();

      return teamService.createTeam(dreamTeam,email);
    }




}
