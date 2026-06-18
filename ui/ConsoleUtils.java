package ui;
import engine.Board;
import engine.MoveGenerator;


import ai.Evaluator;
import java.util.List;
import java.util.Scanner;

public class ConsoleUtils {

    private static final String[] chars = {"P","N","B","R","Q","K","p","n","b","r","q","k"};

    public static void clearScreen() {
        System.out.print("\033[H\033[2J"); // should empty temrinal
        System.out.flush();
    }


    public static void printWelcomeHeader() {
        System.out.println();
        System.out.println("==========================================");
        System.out.println("         MULTI CC (Chess Computer)        ");
        System.out.println("==========================================");
        System.out.println("Welcome! Play moves in algebraic notation");
        System.out.println("(ex. Kf3, Nc2, O-O, etc.) or use commands.");
        System.out.println("Type 'help' any time for a command summary.");
    }

    public static void printHelp() {

    System.out.println("\nCommands:");
    System.out.println("help    - show this");
    System.out.println("print   - show board");
    System.out.println("moves   - list moves");
    System.out.println("history - move history");
    System.out.println("eval    - position eval");
    System.out.println("ai      - engine move");
    System.out.println("quit    - exit");

    System.out.println("\nMoves: Algebraic (e4, Nf3, O-O) or UCI (e2e4, b1c3)");
}


    public static String getPlayerInput(Scanner sc) {
        System.out.print("Command or move > ");
        if (sc.hasNextLine())            return sc.nextLine().trim();

        return "";
    }


    public static void printGameBoard(Board b) {
        System.out.println();
        System.out.println("      a   b   c   d   e   f   g   h");
        System.out.println("    +---+---+---+---+---+---+---+---+");

        for (int rank = 7; rank >= 0; rank--) {
            System.out.print(" " + (rank + 1) + "  |");

            for (int file = 0; file < 8; file++) {
                int sq = rank * 8 + file;
                String piece = " ";
                for (int p = 0; p < 12; p++) {
                    if ((b.pieceBitboards[p] & (1L << sq)) != 0) {
                         piece = chars[p];
                         break;
                    }
                }

                System.out.print(" " + piece + " |");
            }

            System.out.println("  " + (rank + 1));
            System.out.println("    +---+---+---+---+---+---+---+---+");
        }
        System.out.println("      a   b   c   d   e   f   g   h\n");
    }


    public static void printGameStatus(Board b, String lastMove, int moveNum) {

        String side = b.whiteToMove ? "White" : "Black";
        int amount = MoveGenerator.generatePseudoLegalMoves(b).size();

        System.out.println(moveNum + ". " + side + " to move (" + amount + " legal moves available)");
        if (lastMove == null || lastMove.isEmpty())
            System.out.println("Last move: None");
        else
            System.out.println("Last move: " + (moveNum - 1) + lastMove);

        printEvaluationSummary(b);
    }

    public static void printEvaluationSummary(Board b) {

        int score = Evaluator.evaluate(b);
        double pawns = score / 100.0;

        String text = "Equal position";

        if (score > 0)
            text = "White is better";
        else if (score < 0)
            text = "Black is better";

        System.out.println("Assessment: " + text + " (" + pawns + ")");
    }


    public static void printAvailableMoves(Board b) {

        List<String> moves = MoveGenerator.generatePseudoLegalMoves(b);

        if (moves.isEmpty()) {
            System.out.println("No legal moves available.");
            return;
        }

        System.out.println("Legal moves (" + moves.size() + "):");

        for (int i = 0; i < moves.size(); i++) {
            System.out.printf("%-7s", moves.get(i));

            if ((i + 1) % 6 == 0)
                System.out.println();
        }

        if (moves.size() % 6 != 0)
            System.out.println();

        System.out.println();
    }
}