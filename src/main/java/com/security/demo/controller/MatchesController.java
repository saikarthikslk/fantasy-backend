package com.security.demo.controller;


import com.security.demo.DBmodel.MatchInfoEntity;
import com.security.demo.DBmodel.PlayerEntity;
import com.security.demo.config.User;
import com.security.demo.model.Match;
import com.security.demo.model.MatchSelection;
import com.security.demo.model.Player;
import com.security.demo.service.MatchesService;
import org.antlr.v4.runtime.misc.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/matches")
@CrossOrigin(origins = "*")
public class MatchesController {
    @Autowired
    MatchesService matchesService;

    @GetMapping("/fetch")
    public List<Match> fetchMatches(){
        return matchesService.fetchMatches();

    }

    @GetMapping("/fetch/{id}")
    public MatchSelection fetchplayers(@PathVariable("id") Integer  matchid , @AuthenticationPrincipal User user){
        return matchesService.fetchPlayers(matchid,user.getUsername());

    }
    @GetMapping("/sync")
    public boolean fetchplayers( @AuthenticationPrincipal User user){
        return matchesService.syncdata();

    }
}
