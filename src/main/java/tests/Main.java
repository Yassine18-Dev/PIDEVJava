package tests;

import entities.Player;
import entities.Team;
import services.PlayerService;
import services.TeamService;

import java.sql.SQLException;

public class Main {

    public static void main(String[] args) {

        PlayerService playerService = new PlayerService();
        TeamService teamService = new TeamService();

        try {

            if (teamService.recuperer().isEmpty()) {
                System.out.println("========== INSERT TEAMS ==========");
                teamService.ajouter(new Team("Team Alpha", "lol"));
                teamService.ajouter(new Team("Team Beta", "lol"));
                teamService.ajouter(new Team("Team Gamma", "valorant"));
            }

            if (playerService.recuperer().isEmpty()) {
                System.out.println("\n========== INSERT PLAYERS ==========");
                playerService.ajouter(new Player("Oke", "oke@esprit.tn", "password123", "lol", "Diamond II", 53, 1));
                playerService.ajouter(new Player("ProGamer", "pro@game.com", "password123", "lol", "Challenger", 100, 1));
                playerService.ajouter(new Player("Elite99", "elite@game.com", "password123", "valorant", "Immortal III", 0, -1));
                playerService.ajouter(new Player("FIFA_King", "fifa@game.com", "password123", "fifa", "Gold I", 850, -1));
            }

            System.out.println("\n========== LISTE DES TEAMS ==========");
            teamService.recuperer().forEach(System.out::println);

            System.out.println("\n========== LISTE DES PLAYERS ==========");
            playerService.recuperer().forEach(System.out::println);

            System.out.println("\n========== GET PLAYER BY ID (1) ==========");
            playerService.getPlayerById(1).ifPresentOrElse(
                    System.out::println,
                    () -> System.out.println("Player non trouvé")
            );

            System.out.println("\n========== SEARCH PLAYER BY USERNAME ('pro') ==========");
            playerService.searchByUsername("pro").forEach(System.out::println);

            System.out.println("\n========== PLAYERS LOL ==========");
            playerService.filterByGame("lol").forEach(System.out::println);

            System.out.println("\n========== TRI PLAYERS PAR RANK DESC ==========");
            playerService.sortByRankDesc().forEach(System.out::println);

            System.out.println("\n========== FREE AGENTS ==========");
            playerService.getFreeAgents().forEach(System.out::println);

            System.out.println("\n========== TOP 3 PLAYERS ==========");
            playerService.getTop3Players().forEach(System.out::println);

            System.out.println("\n========== TEAMS TRIÉES PAR POWER SCORE ==========");
            teamService.sortByPowerScoreDesc().forEach(System.out::println);

            System.out.println("\n========== TEAMS QUI RECRUTENT ==========");
            teamService.getRecruitingTeams().forEach(System.out::println);

            System.out.println("\n========== TEAMS COMPLÈTES ==========");
            teamService.getFullTeams().forEach(System.out::println);

        } catch (SQLException e) {
            System.err.println("Erreur SQL : " + e.getMessage());
        }
    }
}