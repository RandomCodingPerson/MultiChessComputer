package ui;

import engine.Board;
import engine.MoveGenerator;
import ai.Searcher;

import java.util.Scanner;

public class UCIHandler {

    private static Board board = new Board();

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("Java Bitboard Chess Engine Ready. Type 'uci' to begin or play directly.");
        while (sc.hasNextLine()) {
            String line = sc.nextLine().trim();
            switch (line) {
                case "quit": sc.close(); return;
                case "uci":
                    System.out.println("id name MultiCC");
                    System.out.println("id author me");
                    System.out.println("uciok");
                    break;
                case "isready": System.out.println("readyok"); break;
                case "d": case "print": board.printBoard(); break;
                default:
                    if (line.startsWith("position")) applyPosition(line);
                    else if (line.startsWith("go"))   runSearch(line);
                    break;
            }
        }
        sc.close();
    }

    private static void applyPosition(String cmd) {
        if (cmd.contains("startpos")) {
            board.importFEN("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
        } else if (cmd.contains("fen")) {
            String fen = cmd.substring(cmd.indexOf("fen") + 4);
            if (fen.contains("moves")) fen = fen.substring(0, fen.indexOf("moves")).trim();
            board.importFEN(fen);
        }
        if (cmd.contains("moves")) {
            String[] parts = cmd.substring(cmd.indexOf("moves") + 6).split(" ");
            for (String mv : parts)
                if (!mv.trim().isEmpty()) board.makeMove(mv.trim());
        }
    }

    private static void runSearch(String cmd) {
        int depth = 4;
        if (cmd.contains("depth")) {
            String[] parts = cmd.split(" ");
            for (int i = 0; i < parts.length - 1; i++) {
                if (parts[i].equals("depth")) {
                    try {
                        depth = Integer.parseInt(parts[i + 1]);
                    } catch (NumberFormatException e) {
                        System.err.println("bad depth value: " + parts[i + 1]);
                    }
                }
            }
        }
        String best = Searcher.findBestMove(board, depth);
        System.out.println("bestmove " + best);
    }
}