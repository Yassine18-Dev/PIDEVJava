package services;

import entities.Player;
import utils.Mydatabase;

import java.sql.*;

public class RankAverageService {

    private final Connection cnx = Mydatabase.getInstance().getCnx();

    /** Résultat de la comparaison */
    public record RankComparison(
            int    playerLP,
            double averageLP,
            int    playersInRank,
            double diffPercent,
            String label,
            String color
    ) {
        public boolean isAbove() { return diffPercent >= 0; }
    }

    /**
     * Calcule la moyenne des LP dans le même rank ET même jeu, exclut le joueur lui-même.
     */
    public RankComparison compareToRankAverage(Player p) throws SQLException {
        String sql = "SELECT AVG(league_points) AS avg_points, COUNT(*) AS players_count " +
                "FROM player WHERE `rank` = ? AND game = ? AND id <> ?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, p.getRank());
            ps.setString(2, p.getGame());
            ps.setInt(3,    p.getId());
            ResultSet rs = ps.executeQuery();
            if (!rs.next() || rs.getObject("avg_points") == null) {
                return new RankComparison(p.getLeaguePoints(), 0, 0, 0,
                        "Aucune donnée comparable", "#9aa8b6");
            }

            double avgLP        = rs.getDouble("avg_points");
            int    playersCount = rs.getInt("players_count");
            int    playerLP     = p.getLeaguePoints();

            double diffPercent = (avgLP <= 0) ? 0 : ((playerLP - avgLP) / avgLP) * 100.0;

            String label, color;
            if (diffPercent > 0) {
                label = String.format("+%.1f%% au-dessus de la moyenne", diffPercent);
                color = "#00FF88";
            } else if (diffPercent < 0) {
                label = String.format("%.1f%% en dessous de la moyenne", Math.abs(diffPercent));
                color = "#FF4D4D";
            } else {
                label = "Pile dans la moyenne";
                color = "#1fb3d2";
            }

            return new RankComparison(playerLP, avgLP, playersCount, diffPercent, label, color);
        }
    }
}