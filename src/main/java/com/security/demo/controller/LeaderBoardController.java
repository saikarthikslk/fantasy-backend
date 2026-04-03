package com.security.demo.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.security.demo.DBmodel.MatchState;
import com.security.demo.model.OverallPoints;
import com.security.demo.model.TeamPoints;
import com.security.demo.service.LeaderBoardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/lb")
public class LeaderBoardController {

    @Autowired
    LeaderBoardService leaderBoardService;
    @GetMapping("/overall")
    public List<OverallPoints> getsortedresults() throws JsonProcessingException {
        return leaderBoardService.overall();
    }
    @GetMapping("/scorecard/{mid}")
    public MatchState fetchscore(@PathVariable("mid") Integer mid) throws JsonProcessingException {
        return leaderBoardService.fetchscore(mid);
    }

    @GetMapping("/match/{matchid}")
    public List<TeamPoints> getsortedresults(@PathVariable("matchid") Integer matchid) throws JsonProcessingException {
        return leaderBoardService.getmatches(matchid);
    }

}
