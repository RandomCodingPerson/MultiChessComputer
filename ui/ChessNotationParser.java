package ui;

import engine.Board;
import engine.MoveGenerator;

import java.util.List;
import java.util.ArrayList;

public class ChessNotationParser {

    public static String parseNotationToUCI(String notation, Board board) {
        if (notation == null || notation.isEmpty()) return null;
        String mv = notation.trim();
        if (mv.equals("O-O") || mv.equals("0-0"))
            return board.whiteToMove ? "e1g1" : "e8g8";
        if (mv.equals("O-O-O") || mv.equals("0-0-0"))
            return board.whiteToMove ? "e1c1" : "e8c8";
        List<String> legal = MoveGenerator.generatePseudoLegalMoves(board);
        List<String> hits  = candidates(mv, legal, board);
        if (hits.size() == 1) return hits.get(0);
        return null; // ambiguous or no match
    }

    private static List<String> candidates(String notation, List<String> legalMoves, Board board) {
        List<String> out = new ArrayList<>();
        boolean isCapture = notation.contains("x");
        int eqIdx = notation.indexOf('=');
        String needsPromo = (eqIdx > 0) ? notation.substring(eqIdx + 1).toLowerCase() : null;
        String clean = notation.replaceAll("[x=+#]", "").trim();
        for (String uci : legalMoves) {
            if (tryMatch(clean, uci, board, isCapture, needsPromo))
                out.add(uci);
        }
        return out;
    }

    private static boolean tryMatch(String san, String uci, Board board, boolean needsCap, String needsPromo) {
        int fromSq = parseCoord(uci.substring(0, 2));
        int toSq   = parseCoord(uci.substring(2, 4));
        String uciPromo = uci.length() > 4 ? uci.substring(4) : "";
        if (needsPromo != null && !uciPromo.equals(needsPromo)) return false;
        String toStr = "" + (char)('a' + toSq % 8) + (char)('1' + toSq / 8);
        int piece = getPieceOn(board, fromSq);
        if (piece < 0) return false;
        String ltr = letter(piece);
        if (ltr.isEmpty()) {
            if (!needsCap && san.length() == 2 && san.equals(toStr)) return true;
            if (needsCap  && san.length() >= 3 && san.endsWith(toStr)) return true;
            return false;
        }
        if (san.contains(ltr)) {
            String tail = san.substring(san.indexOf(ltr) + 1).replaceAll("[+#]", "");
            if (tail.endsWith(toStr)) return true;
        }
        return false;
    }

    private static int getPieceOn(Board board, int sq) {
        long bit = 1L << sq;
        for (int i = 0; i < 12; i++)
            if ((board.pieceBitboards[i] & bit) != 0) return i;
        return -1;
    }

    private static String letter(int p) {
        switch (p) {
            case Board.WN: case Board.BN: return "N";
            case Board.WB: case Board.BB: return "B";
            case Board.WR: case Board.BR: return "R";
            case Board.WQ: case Board.BQ: return "Q";
            case Board.WK: case Board.BK: return "K";
            default: return "";
        }
    }

    private static int parseCoord(String s) {
        if (s.length() != 2) return -1;
        int f = s.charAt(0) - 'a';
        int r = s.charAt(1) - '1';
        if (f < 0 || f > 7 || r < 0 || r > 7) return -1;
        return r * 8 + f;
    }
}