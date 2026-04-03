package com.security.demo.service;

import com.security.demo.DBmodel.CustomTeamEntity;
import com.security.demo.DBmodel.MatchInfoEntity;
import com.security.demo.DBmodel.PlayerEntity;
import com.security.demo.model.*;
import com.security.demo.repo.CustomTeamrepo;
import com.security.demo.repo.Matchrepo;
import com.security.demo.repo.PlayerRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
public class MatchesService {
    @Autowired
    Matchrepo matchrepo;
    @Autowired
    PlayerRepo playerRepo;

    @Autowired
    CustomTeamrepo customTeamrepo;



    public List<Match> fetchMatches(){
        return matchrepo.findAll().stream().map(this::toMatchInfo).toList();
    }
    public List<MatchInfoEntity> fetchliveorcompleted(){
        List<String> strings = List.of("Completed");
      return matchrepo.fetchmatchescompletedorlive(strings);
    }
    public MatchSelection fetchPlayers(Integer id, String email){
        Optional<MatchInfoEntity> match = matchrepo.findById(id);
       MatchSelection matchSelection = new MatchSelection();
       matchSelection.setPlayers(new ArrayList<>());
        if(match.isPresent()) {
            List<Integer> ids = new ArrayList<>();
            ids.add(match.get().getTeam1().getTeamId());
            ids.add(match.get().getTeam2().getTeamId());
            try {
                CompletableFuture<List<PlayerEntity>> playerfuture = CompletableFuture.supplyAsync(()->playerRepo.getPlayers(ids));
                CompletableFuture<CustomTeamEntity> teamCompletableFuture = CompletableFuture.supplyAsync(()->customTeamrepo.findbymatchidandemail(id,email));
                CompletableFuture<?> all = CompletableFuture.allOf(playerfuture,teamCompletableFuture);
                all.join();
                matchSelection.setDreamTeam(teamCompletableFuture.get());
                matchSelection.setPlayers(playerfuture.get());
            }catch (Exception e) {
                System.out.println("Failed to retrive match data");
            }

        }
        return matchSelection;
    }
    private Match toMatchInfo(MatchInfoEntity e) {
        Team team1 = new Team();
        team1.setTeamId(e.getTeam1().getTeamId());
        team1.setTeamName(e.getTeam1().getTeamName());
        team1.setTeamSName(e.getTeam1().getTeamSName());
        team1.setImageId(e.getTeam1().getImageId());

        Team team2 = new Team();
        team2.setTeamId(e.getTeam2().getTeamId());
        team2.setTeamName(e.getTeam2().getTeamName());
        team2.setTeamSName(e.getTeam2().getTeamSName());
        team2.setImageId(e.getTeam2().getImageId());

        Venue venue = new Venue();
        venue.setId(e.getVenueInfo().getId());
        venue.setGround(e.getVenueInfo().getGround());
        venue.setCity(e.getVenueInfo().getCity());
        venue.setTimezone(e.getVenueInfo().getTimezone());

        Match info = new Match();
        info.setMatchId(e.getMatchId());
        info.setSeriesId(e.getSeriesId());
        info.setSeriesName(e.getSeriesName());
        info.setMatchDesc(e.getMatchDesc());
        info.setMatchFormat(e.getMatchFormat());
        info.setStartDate(e.getStartDate());
        info.setEndDate(e.getEndDate());
        info.setState(e.getState());
        info.setStatus(e.getStatus());
        info.setTeam1(team1);
        info.setTeam2(team2);
        info.setVenueInfo(venue);

        return info;
    }
}
