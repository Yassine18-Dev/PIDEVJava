package services;

import java.util.UUID;

public class AvatarService {

    private static final String[] STYLES = {
            "adventurer", "avataaars", "bottts", "fun-emoji",
            "lorelei", "micah", "miniavs", "personas", "pixel-art"
    };

    /** Génère un URL DiceBear avatar aléatoire avec un seed unique. */
    public String generateRandomAvatarUrl() {
        String style = STYLES[(int) (Math.random() * STYLES.length)];
        String seed  = UUID.randomUUID().toString();
        return String.format("https://api.dicebear.com/7.x/%s/png?seed=%s&size=200", style, seed);
    }

    /** Avatar style robot gaming (déterministe à partir du pseudo). */
    public String generateFromUsername(String username) {
        if (username == null || username.isBlank())
            return generateRandomAvatarUrl();
        String safeSeed = username.replaceAll("[^a-zA-Z0-9]", "");
        if (safeSeed.isBlank()) safeSeed = UUID.randomUUID().toString();
        return String.format("https://api.dicebear.com/7.x/bottts/png?seed=%s&size=200", safeSeed);
    }

    /** Avatar pixel-art rétro gaming. */
    public String generatePixelArt(String seed) {
        return String.format("https://api.dicebear.com/7.x/pixel-art/png?seed=%s&size=200",
                seed != null ? seed : UUID.randomUUID().toString());
    }
}