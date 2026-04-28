package services;

import entities.Player;
import entities.Team;
import utils.ConfigLoader;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;

public class DiscordNotificationService {

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(8)).build();

    private String resolveWebhook(Team team) {
        if (team != null && team.getDiscordWebhookUrl() != null && !team.getDiscordWebhookUrl().isBlank())
            return team.getDiscordWebhookUrl();
        String def = ConfigLoader.get("discord.default.webhook", "");
        return def.isBlank() ? null : def;
    }

    public boolean notifyPlayerJoined(Team team, Player player) {
        String url = resolveWebhook(team);
        if (url == null) return false;

        String json = """
            {
              "username": "ArenaMind Bot",
              "embeds": [{
                "title": "🎉 NOUVEAU JOUEUR DANS L'ÉQUIPE",
                "description": "**%s** vient de rejoindre **%s** !",
                "color": 2078674,
                "fields": [
                  {"name": "🎮 Pseudo",   "value": "%s",   "inline": true},
                  {"name": "🏆 Rank",     "value": "%s",   "inline": true},
                  {"name": "📊 LP",       "value": "%d",   "inline": true},
                  {"name": "👥 Roster",   "value": "%d/%d", "inline": true},
                  {"name": "💪 Power",    "value": "%d",   "inline": true},
                  {"name": "🎯 Jeu",      "value": "%s",   "inline": true}
                ],
                "footer": {"text": "ArenaMind • Esport Team Manager"}
              }]
            }""".formatted(
                escape(player.getUsername()),
                escape(team.getName()),
                escape(player.getUsername()),
                escape(player.getRank() != null ? player.getRank() : "Unranked"),
                player.getLeaguePoints(),
                team.getCurrentPlayers(), team.getMaxPlayers(),
                team.getPowerScore(),
                escape(team.getGame() != null ? team.getGame().toUpperCase() : "")
        );
        return send(url, json);
    }

    public boolean notifyPlayerLeft(Team team, String playerName) {
        String url = resolveWebhook(team);
        if (url == null) return false;

        String json = """
            {
              "username": "ArenaMind Bot",
              "embeds": [{
                "title": "🚪 DÉPART DE L'ÉQUIPE",
                "description": "**%s** a quitté **%s**.",
                "color": 15548997,
                "footer": {"text": "ArenaMind • Esport Team Manager"}
              }]
            }""".formatted(escape(playerName), escape(team.getName()));
        return send(url, json);
    }

    public boolean testWebhook(String webhookUrl, String teamName) {
        if (webhookUrl == null || webhookUrl.isBlank()) return false;
        String json = """
            {
              "username": "ArenaMind Bot",
              "embeds": [{
                "title": "✅ WEBHOOK ACTIVÉ",
                "description": "Le webhook de **%s** est correctement configuré !",
                "color": 5763719,
                "footer": {"text": "ArenaMind • Test de connexion"}
              }]
            }""".formatted(escape(teamName));
        return send(webhookUrl, json);
    }

    private boolean send(String url, String json) {
        try {
            HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(10))
                    .POST(BodyPublishers.ofString(json)).build();
            int status = http.send(req, BodyHandlers.ofString()).statusCode();
            boolean ok = status == 204 || status == 200;
            if (ok) System.out.println("✅ Discord notification envoyée");
            else    System.err.println("❌ Discord HTTP " + status);
            return ok;
        } catch (Exception e) {
            System.err.println("❌ Discord error: " + e.getMessage());
            return false;
        }
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }
}