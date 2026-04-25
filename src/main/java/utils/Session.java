package utils;

import entities.Player;

public class Session {
    private static Player currentPlayer;

    public static Player  getCurrentPlayer()         { return currentPlayer; }
    public static void    setCurrentPlayer(Player p) { currentPlayer = p; }
    public static boolean isLoggedIn()               { return currentPlayer != null; }
}