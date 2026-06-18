import engine.Board;
import engine.MoveGenerator;
import ai.Searcher;
import ui.ConsoleUtils;
import ui.ChessNotationParser;

import java.util.ArrayList;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);
        Board board = new Board();

        ArrayList<String> history = new ArrayList<>();

        String last = "";
        int moveNum = 1;

        // startup
        ConsoleUtils.clearScreen();
        ConsoleUtils.printWelcomeHeader();
        ConsoleUtils.printGameBoard(board);
        ConsoleUtils.printGameStatus(board, last, moveNum);

        while (true) {

            if (MoveGenerator.isInCheckmate(board)) {
                String winner = board.whiteToMove ? "Black" : "White";
                System.out.println("CHECKMATE. " + winner + " wins!");
                break;
            }

            if (MoveGenerator.isInStalemate(board)) {
                System.out.println("STALEMATE. The game is a draw.");
                break;
            }


            String input = ConsoleUtils.getPlayerInput(sc);
            String cmd = input.toLowerCase();

            if (cmd.equals("quit")) {
                System.out.println("Quit game. Thanks for playing!");
                break;
            }

            if (cmd.equals("help")) {
                ConsoleUtils.printHelp();
                continue;
            }

            if (cmd.equals("moves")) {
                ConsoleUtils.printAvailableMoves(board);
                continue;
            }

            if (cmd.equals("eval")) {
                ConsoleUtils.printEvaluationSummary(board);
                continue;
            }

            if (cmd.equals("history")) {

                if (history.size() == 0) {
                    System.out.println("No moves yet.");
                } else {

                    String text = "";

                    for (String m : history)
                        text += m + " ";

                    System.out.println("Move history: " + text);
                }

                continue;
            }

            if (cmd.equals("print")) {

                ConsoleUtils.clearScreen();
                ConsoleUtils.printGameBoard(board);
                ConsoleUtils.printGameStatus(board, last, moveNum);

                continue;
            }


            if (cmd.equals("ai")) {

                System.out.println("AI is thinking...");

                String ai = Searcher.findBestMove(board, 4);

                if (ai != null && board.makeMove(ai)) {

                    history.add(ai);
                    last = ai;
                    moveNum++;

                    ConsoleUtils.clearScreen();
                    ConsoleUtils.printGameBoard(board);
                    ConsoleUtils.printGameStatus(board, last, moveNum);

                } else {
                    System.out.println("AI could not find a legal move.");
                }

                continue;
            }


            String move = null;

            if (board.isPseudoLegal(input)) {

                move = input;

            } else {

                String parsed = ChessNotationParser.parseNotationToUCI(input, board);

                if (parsed != null && board.isPseudoLegal(parsed))
                    move = parsed;
            }


            if (move == null) {

                System.out.println("Invalid move. Use chess notation (e4, Nf3, O-O) or UCI format (e2e4).\n");

                ConsoleUtils.printAvailableMoves(board);
                continue;
            }


            if (!board.makeMove(move)) {

                System.out.println("Move is illegal in the current position.\n");

                ConsoleUtils.printAvailableMoves(board);
                continue;
            }


            history.add(move);
            last = move;
            moveNum++;

            // redraw
            ConsoleUtils.clearScreen();
            ConsoleUtils.printGameBoard(board);
            ConsoleUtils.printGameStatus(board, last, moveNum);


            if (!board.whiteToMove) {

                if (MoveGenerator.isInCheckmate(board)) {
                    System.out.println("\nCHECKMATE. White wins!");
                    break;
                }

                if (MoveGenerator.isInStalemate(board)) {
                    System.out.println("\nSTALEMATE. The game is a draw.");
                    break;
                }

                System.out.println("\nAI is thinking...");

                String aiMove = Searcher.findBestMove(board, 5);

                if (aiMove != null && board.makeMove(aiMove)) {

                    history.add(aiMove);

                    last = aiMove;
                    moveNum++;

                    ConsoleUtils.clearScreen();
                    ConsoleUtils.printGameBoard(board);
                    ConsoleUtils.printGameStatus(board, last, moveNum);

                    System.out.println("AI played: " + aiMove);

                } else {

                    System.out.println("Could not find a legal move.");

                }

                // old testing
                // System.out.println(Searcher.findBestMove(board, 3));
            }
        }

        sc.close();
    }
}