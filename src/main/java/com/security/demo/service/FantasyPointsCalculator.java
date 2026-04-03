package com.security.demo.service;

import com.security.demo.DBmodel.PlayerPoints;
public class FantasyPointsCalculator {

    // =====================================================
    // 🏏 BATTING POINTS
    // =====================================================
    public static double calculateBattingPoints(
            Integer score,
            Integer ballsFaced,
            boolean isOut,
            Integer fours,
            Integer sixes
    ) {
        double points = 0;

        if (score == null) score = 0;
        if (ballsFaced == null) ballsFaced = 0;
        if (fours == null) fours = 0;
        if (sixes == null) sixes = 0;

        // 1 pt per run
        points += score;

        // Boundary bonus
        points += fours * 4 + 1;   // +1 per four
        points += sixes * 6 + 2;   // +2 per six

        // Milestone bonus (only highest applies)
        if (score >= 100) {
            points += 16;
        } else if (score >= 50) {
            points += 8;
        } else if (score >= 30) {
            points += 4;
        }

        // Duck penalty
        if (score == 0 && isOut) {
            points -= 2;
        }

        // Strike Rate bonus/penalty (min 10 balls faced)
        if (ballsFaced >= 10) {
            double strikeRate = (score * 100.0) / ballsFaced;

            if (strikeRate > 170) {
                points += 6;
            } else if (strikeRate >= 150.01) {
                points += 4;
            } else if (strikeRate >= 130) {
                points += 2;
            } else if (strikeRate >= 60 && strikeRate <= 70) {
                points -= 2;
            } else if (strikeRate >= 50 && strikeRate < 60) {
                points -= 4;
            } else if (strikeRate < 50) {
                points -= 6;
            }
        }

        return points;
    }

    // =====================================================
    // 🎳 BOWLING POINTS
    // =====================================================
    public static double calculateBowlingPoints(
            Integer wickets,
            Integer ballsBowled,
            Integer runsGiven,
            Integer lbw,
            Integer bowled,
            Integer maidens
    ) {
        double points = 0;

        if (wickets == null) wickets = 0;
        if (ballsBowled == null) ballsBowled = 0;
        if (runsGiven == null) runsGiven = 0;
        if (lbw == null) lbw = 0;
        if (bowled == null) bowled = 0;
        if (maidens == null) maidens = 0;

        // 25 pts per wicket
        points += wickets * 25;

        // LBW / Bowled bonus (+8 each)
        points += (lbw + bowled) * 8;

        // Wicket haul bonus (only highest applies)
        if (wickets >= 5) {
            points += 16;
        } else if (wickets >= 4) {
            points += 8;
        } else if (wickets >= 3) {
            points += 4;
        }

        // Maiden overs (+4 each)
        points += maidens * 4;

        // Economy Rate bonus/penalty (min 2 overs = 12 balls)
        if (ballsBowled >= 12) {
            double overs = ballsBowled / 6.0;
            double economy = runsGiven / overs;

            if (economy < 5) {
                points += 6;
            } else if (economy < 6) {
                points += 4;
            } else if (economy <= 7) {
                points += 2;
            } else if (economy >= 10 && economy <= 11) {
                points -= 2;
            } else if (economy > 11 && economy <= 12) {
                points -= 4;
            } else if (economy > 12) {
                points -= 6;
            }
        }

        return points;
    }

    // =====================================================
    // 🧤 FIELDING POINTS
    // =====================================================
    public static double calculateFieldingPoints(
            Integer catches,
            Integer directRunouts,
            Integer indirectRunouts,
            Integer stumpings
    ) {
        double points = 0;

        if (catches == null) catches = 0;
        if (directRunouts == null) directRunouts = 0;
        if (indirectRunouts == null) indirectRunouts = 0;
        if (stumpings == null) stumpings = 0;

        // Catches (+8 each)
        points += catches * 8;

        // Bonus for 3+ catches
        if (catches >= 3) {
            points += 4;
        }

        // Direct run-out (+12)
        points += directRunouts * 12;

        // Indirect run-out (+6)
        points += indirectRunouts * 6;

        // Stumpings (+12)
        points += stumpings * 12;

        return points;
    }

    // =====================================================
    // 🧮 CALCULATE TOTAL FROM PlayerPoints OBJECT
    // =====================================================
    public static double calculateTotalPoints(PlayerPoints p) {

        // ✅ use out field directly instead of checking multiple fields
        boolean isOut = p.isOut();

        // ✅ derive bowling dismissal bonuses from type field
        int bowled  = 0;
        int lbw     = 0;

        if (p.getType() != null) {
            switch (p.getType().toLowerCase()) {
                case "bowled" -> bowled = 1;
                case "lbw"    -> lbw    = 1;
            }
        }

        double batting = calculateBattingPoints(
                p.getScore(),
                p.getBallplayed(),
                isOut,
                p.getFours(),
                p.getSixes()
        );

        double bowling = calculateBowlingPoints(
                p.getWickets(),
                p.getBallsbowled(),
                p.getScoregiven(),
                p.getLbw(),
                p.getBowled(),
                p.getMaidens()
        );

        double fielding = calculateFieldingPoints(
                p.getCatches(),
                p.getRunouts(),
                p.getStumpouts(),
                p.getStumping()
        );

        return 4 + batting + bowling + fielding; // 4 = Playing XI bonus
    }

    // =====================================================
    // 🌟 CAPTAIN / VICE-CAPTAIN MULTIPLIER
    // =====================================================
    public static double applyRoleMultiplier(
            double points,
            boolean isCaptain,
            boolean isViceCaptain
    ) {
        if (isCaptain) return points * 2.0;
        if (isViceCaptain) return points * 1.5;
        return points;
    }
}