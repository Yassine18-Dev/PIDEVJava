package entities;

public class Match {
    private int id;
    private String equipe1;
    private String equipe2;
    private String dateMatch;
    private String score;
    private int tournoiId;

    public Match() {
    }

    public Match(int id, String equipe1, String equipe2, String dateMatch, String score, int tournoiId) {
        this.id = id;
        this.equipe1 = equipe1;
        this.equipe2 = equipe2;
        this.dateMatch = dateMatch;
        this.score = score;
        this.tournoiId = tournoiId;
    }

    public Match(String equipe1, String equipe2, String dateMatch, String score, int tournoiId) {
        this.equipe1 = equipe1;
        this.equipe2 = equipe2;
        this.dateMatch = dateMatch;
        this.score = score;
        this.tournoiId = tournoiId;
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

    @Override
    public String toString() {
        return "Match{" +
                "id=" + id +
                ", equipe1='" + equipe1 + '\'' +
                ", equipe2='" + equipe2 + '\'' +
                ", dateMatch='" + dateMatch + '\'' +
                ", score='" + score + '\'' +
                ", tournoiId=" + tournoiId +
                '}';
    }
}