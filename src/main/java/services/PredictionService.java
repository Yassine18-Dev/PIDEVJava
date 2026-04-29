package services;

import entities.Match;
import entities.Team;

public class PredictionService {

    private final TeamService teamService = new TeamService();

    public String predire(Match match) {
        try {
            Team t1 = teamService.getById(match.getTeam1Id());
            Team t2 = teamService.getById(match.getTeam2Id());

            if (t1 == null || t2 == null) {
                return "Impossible de prédire : équipes non trouvées.";
            }

            int score1 = t1.getPowerScore();
            int score2 = t2.getPowerScore();
            int total = score1 + score2;

            int chance1 = total == 0 ? 50 : (score1 * 100) / total;
            int chance2 = 100 - chance1;

            String gagnant;
            if (chance1 > chance2) {
                gagnant = t1.getName();
            } else if (chance2 > chance1) {
                gagnant = t2.getName();
            } else {
                gagnant = "Match équilibré";
            }

            return "PRÉDICTION IA LOCALE\n\n"
                    + "Match : " + t1.getName() + " VS " + t2.getName() + "\n"
                    + "PowerScore : " + score1 + " - " + score2 + "\n\n"
                    + "Chance " + t1.getName() + " : " + chance1 + "%\n"
                    + "Chance " + t2.getName() + " : " + chance2 + "%\n\n"
                    + "Gagnant probable : " + gagnant;

        } catch (Exception e) {
            return "Erreur prédiction : " + e.getMessage();
        }
    }
}