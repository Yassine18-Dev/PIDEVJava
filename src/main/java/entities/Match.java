package entities;

public class Match {
    private int id;
    private String equipe1;
    private String equipe2;
    private String dateMatch;
    private String heureMatch;
    private String score;
    private int tournoiId;
    private int team1Id;
    private int team2Id;

    public Match() {
    }

    public Match(int id, String equipe1, String equipe2, String dateMatch, String heureMatch, String score, int tournoiId) {
        this.id = id;
        this.equipe1 = equipe1;
        this.equipe2 = equipe2;
        this.dateMatch = dateMatch;
        this.heureMatch = heureMatch;
        this.score = score;
        this.tournoiId = tournoiId;
    }

    public Match(String equipe1, String equipe2, String dateMatch, String heureMatch, String score, int tournoiId) {
        this.equipe1 = equipe1;
        this.equipe2 = equipe2;
        this.dateMatch = dateMatch;
        this.heureMatch = heureMatch;
        this.score = score;
        this.tournoiId = tournoiId;
    }

    public Match(int id, String equipe1, String equipe2, String dateMatch, String heureMatch, String score, int tournoiId, int team1Id, int team2Id) {
        this.id = id;
        this.equipe1 = equipe1;
        this.equipe2 = equipe2;
        this.dateMatch = dateMatch;
        this.heureMatch = heureMatch;
        this.score = score;
        this.tournoiId = tournoiId;
        this.team1Id = team1Id;
        this.team2Id = team2Id;
    }

    public Match(String equipe1, String equipe2, String dateMatch, String heureMatch, String score, int tournoiId, int team1Id, int team2Id) {
        this.equipe1 = equipe1;
        this.equipe2 = equipe2;
        this.dateMatch = dateMatch;
        this.heureMatch = heureMatch;
        this.score = score;
        this.tournoiId = tournoiId;
        this.team1Id = team1Id;
        this.team2Id = team2Id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public String getEquipe1() {
        return equipe1;
    }

    public void setEquipe1(String equipe1) {
        this.equipe1 = equipe1;
    }

    public String getEquipe2() {
        return equipe2;
    }

    public void setEquipe2(String equipe2) {
        this.equipe2 = equipe2;
    }

    public String getDateMatch() {
        return dateMatch;
    }

    public void setDateMatch(String dateMatch) {
        this.dateMatch = dateMatch;
    }

    public String getHeureMatch() {
        return heureMatch;
    }

    public void setHeureMatch(String heureMatch) {
        this.heureMatch = heureMatch;
    }

    public String getDateHeureMatch() {
        if (heureMatch == null || heureMatch.isBlank()) {
            return dateMatch;
        }
        return dateMatch + " à " + heureMatch;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public int getTournoiId() {
        return tournoiId;
    }

    public void setTournoiId(int tournoiId) {
        this.tournoiId = tournoiId;
    }

    public int getTeam1Id() {
        return team1Id;
    }

    public void setTeam1Id(int team1Id) {
        this.team1Id = team1Id;
    }

    public int getTeam2Id() {
        return team2Id;
    }

    public void setTeam2Id(int team2Id) {
        this.team2Id = team2Id;
    }

    @Override
    public String toString() {
        return equipe1 + " VS " + equipe2
                + "\nDate : " + getDateHeureMatch()
                + " | Score : " + score
                + " | Tournoi ID : " + tournoiId;
    }
    private String etat = "A_VENIR";

    public String getEtat() {
        return etat;
    }

    public void setEtat(String etat) {
        this.etat = etat;
    }
    public java.time.LocalDateTime getDateTimeMatch() {
        try {
            if (dateMatch == null || dateMatch.isBlank() || heureMatch == null || heureMatch.isBlank()) {
                return null;
            }
            return java.time.LocalDateTime.parse(dateMatch + "T" + heureMatch);
        } catch (Exception e) {
            return null;
        }
    }

    public String getEtatAuto() {
        java.time.LocalDateTime dt = getDateTimeMatch();

        if (dt == null) {
            return etat == null || etat.isBlank() ? "A_VENIR" : etat;
        }

        if (dt.isAfter(java.time.LocalDateTime.now())) {
            return "A_VENIR";
        }

        if ("TERMINE".equals(etat)) {
            return "TERMINE";
        }

        return "EN_COURS";
    }
}