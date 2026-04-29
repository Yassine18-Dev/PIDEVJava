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

    public record GameCharacter(
            String id,
            String name,
            String title,
            String blurb,
            List<String> tags,
            String role,
            String imageUrl,
            String splashUrl
    ) {
        @Override
        public String toString() { return name + " — " + title; }
    }

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    private final ObjectMapper mapper = new ObjectMapper();

    private List<GameCharacter> cachedChampions = null;
    private List<GameCharacter> cachedAgents    = null;
    private final Map<String, GameCharacter> championById = new ConcurrentHashMap<>();

    private String currentVersion = "14.20.1";

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

    public List<GameCharacter> fetchAllChampions() throws Exception {
        if (cachedChampions != null) return cachedChampions;

        try {
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

        } catch (Exception ex) {
            System.err.println("⚠ Impossible de joindre Data Dragon — utilisation du fallback hors-ligne");
            cachedChampions = buildOfflineChampionsFallback();
            for (GameCharacter c : cachedChampions) championById.put(c.name(), c);
            return cachedChampions;
        }
    }

    public GameCharacter findChampion(String idOrName) {
        if (idOrName == null) return null;
        GameCharacter cached = championById.get(idOrName);
        if (cached != null) return cached;
        if (cachedChampions != null) {
            for (GameCharacter c : cachedChampions) {
                if (c.name().equalsIgnoreCase(idOrName) || c.id().equalsIgnoreCase(idOrName))
                    return c;
            }
        }
        return null;
    }

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

    public List<String> getAllRoles() {
        return List.of("All", "Fighter", "Mage", "Assassin", "Tank", "Marksman", "Support");
    }

    public List<GameCharacter> fetchAllAgents() throws Exception {
        if (cachedAgents != null) return cachedAgents;

        try {
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
            cachedAgents = list;
            System.out.println("✅ " + list.size() + " agents Valorant chargés");
            return list;

        } catch (Exception ex) {
            System.err.println("⚠ Impossible de joindre valorant-api — utilisation du fallback hors-ligne");
            cachedAgents = buildOfflineAgentsFallback();
            for (GameCharacter c : cachedAgents) championById.put(c.name(), c);
            return cachedAgents;
        }
    }

    public List<GameCharacter> fetchByGame(String game) throws Exception {
        if (game == null) return new ArrayList<>();
        return switch (game.toLowerCase()) {
            case "lol", "league of legends" -> fetchAllChampions();
            case "valorant"                 -> fetchAllAgents();
            default -> new ArrayList<>();
        };
    }

    /** Appel HTTP avec retry (3 tentatives) et timeout 30s. */
    private JsonNode get(String url) throws Exception {
        Exception lastError = null;
        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                        .timeout(Duration.ofSeconds(30))
                        .header("User-Agent", "ArenaMind/1.0")
                        .GET().build();
                var resp = http.send(req, BodyHandlers.ofString());
                if (resp.statusCode() != 200)
                    throw new RuntimeException("HTTP " + resp.statusCode());
                return mapper.readTree(resp.body());
            } catch (Exception e) {
                lastError = e;
                System.err.println("⚠ Tentative " + attempt + "/3 échouée : " + e.getMessage());
                if (attempt < 3) Thread.sleep(1500);
            }
        }
        throw lastError;
    }

    // ============================================================
    // FALLBACK HORS-LIGNE — 20 champions LoL populaires
    // ============================================================
    private List<GameCharacter> buildOfflineChampionsFallback() {
        List<GameCharacter> list = new ArrayList<>();
        // Utilise une version récente connue pour les URLs d'images
        String v = "14.20.1";

        addChamp(list, v, "Aatrox",     "the Darkin Blade",          List.of("Fighter", "Tank"));
        addChamp(list, v, "Ahri",       "the Nine-Tailed Fox",       List.of("Mage", "Assassin"));
        addChamp(list, v, "Akali",      "the Rogue Assassin",        List.of("Assassin"));
        addChamp(list, v, "Ashe",       "the Frost Archer",          List.of("Marksman", "Support"));
        addChamp(list, v, "Caitlyn",    "the Sheriff of Piltover",   List.of("Marksman"));
        addChamp(list, v, "Darius",     "the Hand of Noxus",         List.of("Fighter", "Tank"));
        addChamp(list, v, "Ezreal",     "the Prodigal Explorer",     List.of("Marksman", "Mage"));
        addChamp(list, v, "Garen",      "The Might of Demacia",      List.of("Fighter", "Tank"));
        addChamp(list, v, "Jinx",       "the Loose Cannon",          List.of("Marksman"));
        addChamp(list, v, "KaiSa",      "Daughter of the Void",      List.of("Marksman"));
        addChamp(list, v, "LeeSin",     "the Blind Monk",            List.of("Fighter", "Assassin"));
        addChamp(list, v, "Lux",        "the Lady of Luminosity",    List.of("Mage", "Support"));
        addChamp(list, v, "MasterYi",   "the Wuju Bladesman",        List.of("Assassin", "Fighter"));
        addChamp(list, v, "Riven",      "the Exile",                 List.of("Fighter", "Assassin"));
        addChamp(list, v, "Thresh",     "the Chain Warden",          List.of("Support", "Fighter"));
        addChamp(list, v, "Vayne",      "the Night Hunter",          List.of("Marksman", "Assassin"));
        addChamp(list, v, "Yasuo",      "the Unforgiven",            List.of("Fighter", "Assassin"));
        addChamp(list, v, "Yone",       "the Unforgotten",           List.of("Assassin", "Fighter"));
        addChamp(list, v, "Zed",        "the Master of Shadows",     List.of("Assassin"));
        addChamp(list, v, "Ziggs",      "the Hexplosives Expert",    List.of("Mage"));

        list.sort(Comparator.comparing(GameCharacter::name));
        System.out.println("✅ " + list.size() + " champions chargés en mode hors-ligne (fallback)");
        return list;
    }

    private void addChamp(List<GameCharacter> list, String version, String id, String title, List<String> tags) {
        String role = tags.isEmpty() ? "Unknown" : tags.get(0);
        String img  = "https://ddragon.leagueoflegends.com/cdn/" + version + "/img/champion/" + id + ".png";
        String spl  = "https://ddragon.leagueoflegends.com/cdn/img/champion/splash/" + id + "_0.jpg";
        list.add(new GameCharacter(id, formatName(id), title, "Champion populaire de League of Legends.",
                tags, role, img, spl));
    }

    /** Convert "MasterYi" -> "Master Yi", "KaiSa" -> "Kai'Sa", "LeeSin" -> "Lee Sin". */
    private String formatName(String id) {
        return switch (id) {
            case "MasterYi" -> "Master Yi";
            case "LeeSin"   -> "Lee Sin";
            case "KaiSa"    -> "Kai'Sa";
            default         -> id;
        };
    }

    // ============================================================
    // FALLBACK HORS-LIGNE — Agents Valorant populaires
    // ============================================================
    private List<GameCharacter> buildOfflineAgentsFallback() {
        List<GameCharacter> list = new ArrayList<>();
        String[][] agents = {
                {"Jett",      "Duelist",    "A representative agent."},
                {"Phoenix",   "Duelist",    "A representative agent."},
                {"Reyna",     "Duelist",    "A representative agent."},
                {"Raze",      "Duelist",    "A representative agent."},
                {"Sage",      "Sentinel",   "A representative agent."},
                {"Cypher",    "Sentinel",   "A representative agent."},
                {"Killjoy",   "Sentinel",   "A representative agent."},
                {"Sova",      "Initiator",  "A representative agent."},
                {"Breach",    "Initiator",  "A representative agent."},
                {"Skye",      "Initiator",  "A representative agent."},
                {"Brimstone", "Controller", "A representative agent."},
                {"Omen",      "Controller", "A representative agent."},
                {"Viper",     "Controller", "A representative agent."}
        };
        for (String[] a : agents) {
            // Image placeholder via DiceBear si valorant-api inaccessible
            String img = "https://api.dicebear.com/7.x/bottts/png?seed=" + a[0] + "&size=200";
            list.add(new GameCharacter(a[0], a[0], "Agent Valorant", a[2],
                    List.of(a[1]), a[1], img, img));
        }
        list.sort(Comparator.comparing(GameCharacter::name));
        System.out.println("✅ " + list.size() + " agents chargés en mode hors-ligne (fallback)");
        return list;
    }
}