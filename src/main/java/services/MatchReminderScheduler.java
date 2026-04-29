package services;

import entities.Match;
import entities.Player;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class MatchReminderScheduler {

    private final MatchService matchService = new MatchService();
    private final PlayerService playerService = new PlayerService();
    private final EmailService emailService = new EmailService();

    public void start() {
        Thread thread = new Thread(() -> {
            while (true) {
                try {
                    verifierMatchs();
                    Thread.sleep(60000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.setDaemon(true);
        thread.start();

        System.out.println("Scheduler des rappels de matchs démarré.");
    }

    private void verifierMatchs() {
        try {
            List<Match> matchs = matchService.afficherMatchsSansRappel();

            for (Match match : matchs) {

                if (match.getDateMatch() == null || match.getDateMatch().isBlank()
                        || match.getHeureMatch() == null || match.getHeureMatch().isBlank()) {
                    continue;
                }

                LocalDateTime dateMatch = LocalDateTime.parse(
                        match.getDateMatch() + "T" + match.getHeureMatch()
                );

                LocalDateTime now = LocalDateTime.now();
                long minutes = Duration.between(now, dateMatch).toMinutes();

                System.out.println("Match ID " + match.getId() + " | minutes restantes : " + minutes);

                if (minutes >= 0 && minutes <= 60) {
                    envoyerRappel(match);
                    matchService.marquerRappelEnvoye(match.getId());
                    System.out.println("Rappel envoyé pour le match ID : " + match.getId());
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void envoyerRappel(Match match) {
        try {
            List<Player> team1 = playerService.findByTeam(match.getTeam1Id());
            List<Player> team2 = playerService.findByTeam(match.getTeam2Id());

            String contenu = "Bonjour,\n\n"
                    + "Votre match commence dans 1 heure.\n\n"
                    + "Match : " + match.getEquipe1() + " VS " + match.getEquipe2() + "\n"
                    + "Date : " + match.getDateMatch() + "\n"
                    + "Heure : " + match.getHeureMatch() + "\n\n"
                    + "Merci de vous préparer à temps.";

            for (Player p : team1) {
                if (p.getEmail() != null && !p.getEmail().isBlank()) {
                    emailService.envoyerEmail(p.getEmail(), "Rappel Match", contenu);
                }
            }

            for (Player p : team2) {
                if (p.getEmail() != null && !p.getEmail().isBlank()) {
                    emailService.envoyerEmail(p.getEmail(), "Rappel Match", contenu);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}