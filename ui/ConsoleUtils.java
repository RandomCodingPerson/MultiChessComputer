package ui;

import engine.Board;
import engine.MoveGenerator;
import ai.Evaluator;

import java.util.List;
import java.util.Scanner;

public class ConsoleUtils {

    private static final String[] PIECE_CHARS = {"P","N","B","R","Q","K","p","n","b","r","q","k"};

    public static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    public static void printWelcomeHeader() {
        System.out.println();
        System.out.println("==========================================");
        System.out.println("         MULTI CC (Chess Computer)        ");
        System.out.println("==========================================");
        System.out.println("Welcome! Play moves in algebraic notation");
        System.out.println(" (ex. Kf3, Nc2, O-O, etc.) or use commands.");
        System.out.println("Type 'help' any time for a command summary.");
    }

    public static void printHelp() {
        System.out.println("\nAvailable commands:");
        System.out.println("  help      - this message");
        System.out.println("  print     - redraw the board");
        System.out.println("  moves     - list all legal moves");
        System.out.println("  history   - show moves played so far");
        System.out.println("  eval      - show engine's current assessment");
        System.out.println("  ai        - let the engine pick a move");
        System.out.println("  quit      - exit");
        System.out.println("\nMove formats:");
        System.out.println("  Algebraic: e4, Nf3, Bc4, exd5, O-O, O-O-O");
        System.out.println("  UCI:       e2e4, e7e5, e1g1, e8c8, e7e8q\n");
    }

    public static String getPlayerInput(Scanner sc) {
        System.out.print("Command or move > ");
        return sc.hasNextLine() ? sc.nextLine().trim() : "";
    }

    public static void printGameBoard(Board board) {
        System.out.println();
        System.out.println("      a   b   c   d   e   f   g   h");
        System.out.println("    +---+---+---+---+---+---+---+---+");
        for (int rank = 7; rank >= 0; rank--) {
          System.out.print(" " + (rank + 1) + "  |");
          for (int file = 0; file < 8; file++) {
            int sq = rank * 8 + file;
            String sym = " ";
            for (int p = 0; p < 12; p++) {
              if ((board.pieceBitboards[p] & (1L << sq)) != 0) { sym = PIECE_CHARS[p]; break; }
            }
            System.out.print(" " + sym + " |");
          }
          System.out.println("  " + (rank + 1));
          System.out.println("    +---+---+---+---+---+---+---+---+");
        }
        System.out.println("      a   b   c   d   e   f   g   h\n");
    }

    public static void printGameStatus(Board board, String lastMove, int moveNum) {
        String side = board.whiteToMove ? "White" : "Black";
        int cnt = MoveGenerator.generatePseudoLegalMoves(board).size();
        System.out.println(moveNum + ". " + side + " to move (" + cnt + " legal moves available)");
        String last = (lastMove == null || lastMove.isEmpty()) ? "None" : (moveNum - 1) + lastMove;
        System.out.println("Last move: " + last);
        printEvaluationSummary(board);
    }

    public static void printEvaluationSummary(Board board) {
        int raw = Evaluator.evaluate(board);
        double pawns = raw / 100.0;
        String verdict = raw == 0 ? "Equal position" : (raw > 0 ? "White is better" : "Black is better");
        System.out.println("Assessment: " + verdict + " (" + pawns + ")");
    }

    public static void printAvailableMoves(Board board) {
        List<String> moves = MoveGenerator.generatePseudoLegalMoves(board);
        if (moves.isEmpty()) { System.out.println("No legal moves available."); return; }
        System.out.println("Legal moves (" + moves.size() + "):");
        for (int i = 0; i < moves.size(); i++) {
            System.out.printf("%-7s", moves.get(i));
            if ((i + 1) % 6 == 0) System.out.println();
        }
        if (moves.size() % 6 != 0) System.out.println();
        System.out.println();
    }
}