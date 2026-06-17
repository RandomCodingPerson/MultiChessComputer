import engine.Board;
import engine.MoveGenerator;
import ai.Searcher;
import ui.ConsoleUtils;
import ui.ChessNotationParser;

import java.util.ArrayList;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        Board board = new Board();

        ArrayList<String> moveHistory = new ArrayList<>();

        String lastMovePlayed = "";
        int moveNumber = 1;

        // startup
        ConsoleUtils.clearScreen();
        ConsoleUtils.printWelcomeHeader();
        ConsoleUtils.printGameBoard(board);
        ConsoleUtils.printGameStatus(board, lastMovePlayed, moveNumber);

        while (true) {

            // endgame

            if (MoveGenerator.isInCheckmate(board)) {

                String winner;

                if (board.whiteToMove)
                    winner = "Black";
                else
                    winner = "White";

                System.out.println("CHECKMATE. " + winner + " wins!");
                break;
            }

            if (MoveGenerator.isInStalemate(board)) {
                System.out.println("STALEMATE. The game is a draw.");
                break;
            }

            String currentInput = ConsoleUtils.getPlayerInput(scanner);
            String command = currentInput.toLowerCase();

            if (command.equals("quit")) {
                System.out.println("Quit game. Thanks for playing!");
                break;
            }

            if (command.equals("help")) {
                ConsoleUtils.printHelp();
                continue;
            }

            if (command.equals("moves")) {
                ConsoleUtils.printAvailableMoves(board);
                continue;
            }

            if (command.equals("eval")) {
                ConsoleUtils.printEvaluationSummary(board);
                continue;
            }

            if (command.equals("history")) {

                if (moveHistory.size() == 0) {
                    System.out.println("No moves yet.");
                } else {

                    String historyText = "";

                    for (String move : moveHistory) {
                        historyText += move + " ";
                    }

                    System.out.println("Move history: " + historyText);
                }

                continue;
            }

            if (command.equals("print")) {

                ConsoleUtils.clearScreen();

                ConsoleUtils.printGameBoard(board);
                ConsoleUtils.printGameStatus(board, lastMovePlayed, moveNumber);

                continue;
            }

            if (command.equals("ai")) {

                System.out.println("AI is thinking...");

                String computerMove = Searcher.findBestMove(board, 4);

                if (computerMove != null) {

                    boolean moveWorked = board.makeMove(computerMove);

                    if (moveWorked) {

                        moveHistory.add(computerMove);

                        lastMovePlayed = computerMove;
                        moveNumber++;

                        ConsoleUtils.clearScreen();

                        ConsoleUtils.printGameBoard(board);
                        ConsoleUtils.printGameStatus(board, lastMovePlayed, moveNumber);

                    } else {
                        System.out.println("AI could not find a legal move.");
                    }

                } else {
                    System.out.println("AI could not find a legal move.");
                }

                continue;
            }

            String moveToPlay = null;

            if (board.isPseudoLegal(currentInput)) {

                moveToPlay = currentInput;

            } else {

                String convertedMove = ChessNotationParser.parseNotationToUCI(currentInput, board);

                if (convertedMove != null) {

                    if (board.isPseudoLegal(convertedMove)) {
                        moveToPlay = convertedMove;
                    }

                }
            }

            if (moveToPlay == null) {

                System.out.println("Invalid move. Use chess notation (e4, Nf3, O-O) or UCI format (e2e4).\n");

                ConsoleUtils.printAvailableMoves(board);

                continue;
            }

            boolean moveWasSuccessful = board.makeMove(moveToPlay);

            if (!moveWasSuccessful) {

                System.out.println("Move is illegal in the current position.\n");

                ConsoleUtils.printAvailableMoves(board);

                continue;
            }

            moveHistory.add(moveToPlay);

            lastMovePlayed = moveToPlay;

            moveNumber++;

            // redraw
            ConsoleUtils.clearScreen();

            ConsoleUtils.printGameBoard(board);
            ConsoleUtils.printGameStatus(board, lastMovePlayed, moveNumber);

            // System.out.println(board.currentHash);
            // System.out.println("move played = " + moveToPlay);

            if (!board.whiteToMove) {

                if (MoveGenerator.isInCheckmate(board)) {

                    String winner;

                    if (board.whiteToMove)
                        winner = "Black";
                    else
                        winner = "White";

                    System.out.println("\nCHECKMATE. " + winner + " wins!");

                    break;
                }

                if (MoveGenerator.isInStalemate(board)) {

                    System.out.println("\nSTALEMATE. The game is a draw.");

                    break;
                }

                System.out.println("\nAI is thinking...");

                String aiMove = Searcher.findBestMove(board, 4);

                if (aiMove != null) {

                    boolean worked = board.makeMove(aiMove);

                    if (worked) {

                        moveHistory.add(aiMove);

                        lastMovePlayed = aiMove;

                        moveNumber++;

                        ConsoleUtils.clearScreen();

                        ConsoleUtils.printGameBoard(board);
                        ConsoleUtils.printGameStatus(board, lastMovePlayed, moveNumber);

                        System.out.println("AI played: " + aiMove);

                    } else {

                        System.out.println("Could not find a legal move.");

                    }

                } else {

                    System.out.println("Could not find a legal move.");

                }

                // old ai stuff
                // String testMove = Searcher.findBestMove(board, 3);
                // System.out.println(testMove);
            }
        }

        // cleanup
        scanner.close();
    }
}