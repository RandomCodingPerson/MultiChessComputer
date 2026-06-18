package ui;

import engine.Board;
import engine.MoveGenerator;

import java.util.ArrayList;
import java.util.List;

public class ChessNotationParser {
    public static String parseNotationToUCI(String notation, Board board) {

        if (notation == null || notation.isEmpty()) return null;

        String move = notation.trim();

        // castle
        if (move.equals("O-O") || move.equals("0-0")) {
            return board.whiteToMove ? "e1g1" : "e8g8";
        }

        if (move.equals("O-O-O") || move.equals("0-0-0")) {
            return board.whiteToMove ? "e1c1" : "e8c8";
            }


        List<String> moves = MoveGenerator.generatePseudoLegalMoves(board);
        List<String> possible = candidates(move, moves, board);

        if (possible.size() == 1)
            return possible.get(0);

        // either didnt find it or two pieces can do it
        return null;
    }

private static List<String> candidates(String notation, List<String> moves, Board b) {
        ArrayList<String> found = new ArrayList<String>();
//check if smth is being taken
        boolean capture = notation.contains("x");
// pomotion
        int promoAt = notation.indexOf("=");

        String promo = null;
        if (promoAt != -1) {
            promo = notation.substring(promoAt + 1).toLowerCase();
        }
        String cleaned = notation.replaceAll("[x=+#]", ""); //regex :PPP

        for (String move : moves) {


            if (tryMatch(cleaned, move, b, capture, promo)) {
                found.add(move);
            }
        }
        return found;
    }

// algebraic notation instead of uci since uci sucks
    private static boolean tryMatch(String san, String uci, Board b, boolean capture, String promo) {
        int from = parseCoord(uci.substring(0, 2));
        int to = parseCoord(uci.substring(2, 4));

        String uciPromo = "";

        if (uci.length() > 4) {
                uciPromo = uci.substring(4);
        }
        if (promo != null && !uciPromo.equals(promo)) return false;
        String target = "" + (char)('a' + to % 8) + (char)('1' + to / 8);
        int piece = getPieceOn(b, from);
        if (piece == -1)            return false;
        String name = letter(piece);
        if (name.equals("")) {
            if (!capture && san.length() == 2 && san.equals(target))
                return true;
            if (capture && san.endsWith(target))
                return true;

            return false;
        }
        if (san.contains(name)) {
            String rest = san.substring(san.indexOf(name) + 1);
            rest = rest.replaceAll("[+#]", "");
            if (rest.endsWith(target))
                return true;
        }

        return false;
    }



    private static int getPieceOn(Board board, int sq) {
        long mask = 1L << sq;
        for (int i = 0; i < 12; i++) {

        if ((board.pieceBitboards[i] & mask) != 0) {
            return i;
        }
        }
        return -1; //(couldnt fidn)
    }



    private static String letter(int piece) {
        switch(piece) {
            case Board.WN:
            case Board.BN:
                return "N";
            case Board.WB:
            case Board.BB:
                return "B";
            case Board.WR:
            case Board.BR:
                return "R";
            case Board.WQ:
            case Board.BQ:
                return "Q";
            case Board.WK:
            case Board.BK:
                return "K";
        }
        return "";
    }
    private static int parseCoord(String coord) {
        if (coord.length() != 2)
            return -1;



        int file = coord.charAt(0) - 'a';
        int rank = coord.charAt(1) - '1';

        if (file < 0 || file > 7 || rank < 0 || rank > 7)
            return -1;

         else return (rank * 8 + file);
    }
}