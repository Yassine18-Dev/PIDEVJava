package services;

import entities.Player;
import entities.Team;
import utils.Mydatabase;

import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class TeamRadarService {

    private final Connection cnx = Mydatabase.getInstance().getCnx();

    public record SkillStats(double avg, double stdDev, int teamSize) {}

    public record TeamAnalysis(
            Map<String, SkillStats> stats,        // moyenne + écart-type par compétence
            String   weakestSkill,
            double   weakestValue,
            String   strongestSkill,
            double   strongestValue,
            double   teamCohesion,                // 0-100, plus c'est haut plus c'est homogène
            String   recommendation
    ) {}

    /**
     * Analyse complète de l'équipe : moyennes, écart-types, points faibles/forts, cohésion, reco.
     */
    public TeamAnalysis analyze(Team team) throws SQLException {
        Map<String, SkillStats> stats = new LinkedHashMap<>();

        // Calcul SQL agrégé : AVG + STDDEV + COUNT
        String sql = "SELECT " +
                "  AVG(vision)        AS avg_vis,  STDDEV(vision)        AS std_vis,  " +
                "  AVG(communication) AS avg_com,  STDDEV(communication) AS std_com,  " +
                "  AVG(teamplay)      AS avg_team, STDDEV(teamplay)      AS std_team, " +
                "  AVG(reflex)        AS avg_ref,  STDDEV(reflex)        AS std_ref,  " +
                "  AVG(shooting)      AS avg_shoot,STDDEV(shooting)      AS std_shoot," +
                "  COUNT(*) AS team_size " +
                "FROM player WHERE team_id = ?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, team.getId());
            ResultSet rs = ps.executeQuery();
            if (!rs.next() || rs.getInt("team_size") == 0) {
                return new TeamAnalysis(stats, "—", 0, "—", 0, 0,
                        "Aucun joueur dans l'équipe.");
            }
            int size = rs.getInt("team_size");
            stats.put("Vision",        new SkillStats(rs.getDouble("avg_vis"),   rs.getDouble("std_vis"),   size));
            stats.put("Communication", new SkillStats(rs.getDouble("avg_com"),   rs.getDouble("std_com"),   size));
            stats.put("Teamplay",      new SkillStats(rs.getDouble("avg_team"),  rs.getDouble("std_team"),  size));
            stats.put("Réflexes",      new SkillStats(rs.getDouble("avg_ref"),   rs.getDouble("std_ref"),   size));
            stats.put("Tir",           new SkillStats(rs.getDouble("avg_shoot"), rs.getDouble("std_shoot"), size));
        }

        // Détection point faible et point fort
        String weakest = "—", strongest = "—";
        double weakestValue = Double.MAX_VALUE, strongestValue = Double.MIN_VALUE;
        for (var e : stats.entrySet()) {
            double v = e.getValue().avg();
            if (v < weakestValue)   { weakestValue   = v; weakest   = e.getKey(); }
            if (v > strongestValue) { strongestValue = v; strongest = e.getKey(); }
        }

        // Cohésion = 100 - moyenne des écart-types (plus c'est uniforme, mieux c'est)
        double avgStdDev = stats.values().stream().mapToDouble(SkillStats::stdDev).average().orElse(0);
        double cohesion  = Math.max(0, 100 - avgStdDev * 2.5); // facteur d'amplification visuelle

        return new TeamAnalysis(stats, weakest, weakestValue, strongest, strongestValue,
                cohesion, generateRecommendation(weakest, weakestValue, cohesion));
    }

    /** Recommandations par compétence faible. */
    private String generateRecommendation(String weakSkill, double weakValue, double cohesion) {
        StringBuilder sb = new StringBuilder();

        switch (weakSkill) {
            case "Vision" -> sb.append("🔍 Votre équipe manque de vision. Recrutez un Support ou un Jungler avec une bonne map awareness.");
            case "Communication" -> sb.append("📢 La communication fait défaut. Recrutez un shotcaller ou un IGL expérimenté.");
            case "Teamplay" -> sb.append("🤝 Travaillez la coordination. Recrutez un joueur expérimenté en teamfight.");
            case "Réflexes" -> sb.append("⚡ Réflexes en dessous du niveau attendu. Recrutez un mécanicien (mid laner ou duelist).");
            case "Tir" -> sb.append("🎯 La précision de tir est faible. Recrutez un ADC / Hitscan / sniper de haut niveau.");
            default -> sb.append("Continuez à entraîner votre équipe.");
        }

        sb.append(String.format(" (Score actuel : %.1f/100)", weakValue));

        if (cohesion < 50) {
            sb.append("\n⚠ Cohésion faible (").append(String.format("%.0f", cohesion))
                    .append("/100) : votre équipe est très déséquilibrée entre forts et faibles.");
        } else if (cohesion >= 80) {
            sb.append("\n✅ Excellente cohésion (").append(String.format("%.0f", cohesion))
                    .append("/100) : votre équipe est très homogène.");
        }

        return sb.toString();
    }
}