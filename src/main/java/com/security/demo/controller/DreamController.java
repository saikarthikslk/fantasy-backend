package com.security.demo.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.security.demo.model.TeamPoints;
import com.security.demo.service.TeamService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dream")
public class DreamController {
    @Autowired
    private TeamService teamService;

    @GetMapping("/{matchid}/{dreamid}")
    public TeamPoints fetchdreamteam(@PathVariable("matchid") Integer matchid, @PathVariable("dreamid") Integer dreamid) throws JsonProcessingException {
        return teamService.fetchteam(matchid,dreamid);
    }
}
