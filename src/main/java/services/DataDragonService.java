package services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DataDragonService {

    /** Représente un champion League of Legends ou un agent Valorant. */
    public record GameCharacter(
            String id,           // ex: "Aatrox"
            String name,         // ex: "Aatrox"
            String title,        // ex: "the Darkin Blade"
            String blurb,        // description courte
            List<String> tags,   // ["Fighter", "Tank"]
            String role,         // "Fighter" (premier tag)
            String imageUrl,     // URL portrait officiel
            String splashUrl     // URL splash art HD
    ) {
        @Override
        public String toString() { return name + " — " + title; }
    }

    private final HttpClient   http   = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(8)).build();
    private final ObjectMapper mapper = new ObjectMapper();

    /** Cache pour ne pas refaire les appels API en boucle. */
    private List<GameCharacter> cachedChampions = null;
    private final Map<String, GameCharacter> championById = new ConcurrentHashMap<>();

    /** Version actuelle de Data Dragon. */
    private String currentVersion = "14.20.1";

    /** Récupère la dernière version disponible (1 fois au démarrage). */
    public String fetchLatestVersion() {
        try {
            JsonNode versions = get("https://ddragon.leagueoflegends.com/api/versions.json");
            if (versions.isArray() && versions.size() > 0)
                currentVersion = versions.get(0).asText();
        } catch (Exception e) {
            System.err.println("⚠ Impossible de récupérer la version DataDragon, utilise " + currentVersion);
        }
        return currentVersion;
    }

    /**
     * Récupère TOUS les champions LoL (170+) avec leurs portraits.
     * Première fois : appel API + parsing.
     * Suivantes : retour direct du cache.
     */
    public List<GameCharacter> fetchAllChampions() throws Exception {
        if (cachedChampions != null) return cachedChampions;

        fetchLatestVersion();
        String url = "https://ddragon.leagueoflegends.com/cdn/" + currentVersion + "/data/en_US/champion.json";
        JsonNode root = get(url);
        JsonNode data = root.get("data");

        List<GameCharacter> list = new ArrayList<>();
        Iterator<Map.Entry<String, JsonNode>> fields = data.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> e = fields.next();
            JsonNode champ = e.getValue();

            String id    = champ.get("id").asText();
            String name  = champ.get("name").asText();
            String title = champ.get("title").asText();
            String blurb = champ.get("blurb").asText();

            List<String> tags = new ArrayList<>();
            JsonNode tagsNode = champ.get("tags");
            if (tagsNode != null && tagsNode.isArray())
                for (JsonNode t : tagsNode) tags.add(t.asText());

            String role = tags.isEmpty() ? "Unknown" : tags.get(0);

            String imageUrl  = "https://ddragon.leagueoflegends.com/cdn/" + currentVersion
                    + "/img/champion/" + id + ".png";
            String splashUrl = "https://ddragon.leagueoflegends.com/cdn/img/champion/splash/"
                    + id + "_0.jpg";

            GameCharacter ch = new GameCharacter(id, name, title, blurb, tags, role, imageUrl, splashUrl);
            list.add(ch);
            championById.put(id, ch);
            championById.put(name, ch);
        }

        list.sort(Comparator.comparing(GameCharacter::name));
        cachedChampions = list;
        System.out.println("✅ " + list.size() + " champions LoL chargés depuis Data Dragon");
        return list;
    }

    /** Récupère un champion par son nom ou ID. */
    public GameCharacter findChampion(String idOrName) {
        if (idOrName == null) return null;
        GameCharacter cached = championById.get(idOrName);
        if (cached != null) return cached;
        // Recherche insensible à la casse
        if (cachedChampions != null) {
            for (GameCharacter c : cachedChampions) {
                if (c.name().equalsIgnoreCase(idOrName) || c.id().equalsIgnoreCase(idOrName))
                    return c;
            }
        }
        return null;
    }

    /** Filtre les champions par rôle (Fighter, Mage, Assassin, etc). */
    public List<GameCharacter> filterByRole(String role) throws Exception {
        if (cachedChampions == null) fetchAllChampions();
        if (role == null || role.isBlank() || "All".equalsIgnoreCase(role))
            return cachedChampions;
        List<GameCharacter> result = new ArrayList<>();
        for (GameCharacter c : cachedChampions) {
            if (c.tags().stream().anyMatch(t -> t.equalsIgnoreCase(role)))
                result.add(c);
        }
        return result;
    }

    /** Liste des rôles disponibles. */
    public List<String> getAllRoles() {
        return List.of("All", "Fighter", "Mage", "Assassin", "Tank", "Marksman", "Support");
    }

    /** Récupère les agents Valorant (depuis l'API officielle valorant-api.com). */
    public List<GameCharacter> fetchAllAgents() throws Exception {
        String url = "https://valorant-api.com/v1/agents?isPlayableCharacter=true";
        JsonNode root = get(url);
        JsonNode data = root.get("data");

        List<GameCharacter> list = new ArrayList<>();
        if (data != null && data.isArray()) {
            for (JsonNode agent : data) {
                String id   = agent.get("uuid").asText();
                String name = agent.get("displayName").asText();
                String desc = agent.get("description").asText();

                List<String> tags = new ArrayList<>();
                JsonNode role = agent.get("role");
                if (role != null && !role.isNull())
                    tags.add(role.get("displayName").asText());

                String roleName = tags.isEmpty() ? "Unknown" : tags.get(0);
                String iconUrl  = agent.get("displayIcon").asText();
                String fullUrl  = agent.get("fullPortrait") != null && !agent.get("fullPortrait").isNull()
                        ? agent.get("fullPortrait").asText() : iconUrl;

                GameCharacter a = new GameCharacter(id, name,
                        agent.get("developerName").asText(""), desc, tags, roleName, iconUrl, fullUrl);
                list.add(a);
                championById.put(name, a);
            }
        }
        list.sort(Comparator.comparing(GameCharacter::name));
        System.out.println("✅ " + list.size() + " agents Valorant chargés");
        return list;
    }

    /** Méthode unifiée : retourne champions LoL ou agents Valorant selon le jeu. */
    public List<GameCharacter> fetchByGame(String game) throws Exception {
        if (game == null) return new ArrayList<>();
        return switch (game.toLowerCase()) {
            case "lol", "league of legends" -> fetchAllChampions();
            case "valorant"                 -> fetchAllAgents();
            default -> new ArrayList<>(); // FIFA pas supporté
        };
    }

    private JsonNode get(String url) throws Exception {
        HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(15))
                .GET().build();
        var resp = http.send(req, BodyHandlers.ofString());
        if (resp.statusCode() != 200)
            throw new RuntimeException("Data Dragon HTTP " + resp.statusCode());
        return mapper.readTree(resp.body());
    }
}