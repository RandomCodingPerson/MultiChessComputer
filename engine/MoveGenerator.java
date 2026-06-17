package engine;

import java.util.ArrayList;
import java.util.List;

public class MoveGenerator {

    private static final long FILE_A = 0x0101010101010101L;
    private static final long FILE_B = 0x0202020202020202L;
    private static final long FILE_G = 0x4040404040404040L;
    private static final long FILE_H = 0x8080808080808080L;
    private static final long RANK_4 = 0x00000000FF000000L;
    private static final long RANK_5 = 0x000000FF00000000L;

    private static final long[] KNIGHT_ATK = new long[64];
    private static final long[] KING_ATK   = new long[64];
    private static final long[] WPAWN_ATK  = new long[64];
    private static final long[] BPAWN_ATK  = new long[64];

    static { initAttacks(); }

    public static List<String> generatePseudoLegalMoves(Board board) {
        List<String> moves = new ArrayList<>();
        boolean white = board.whiteToMove;
        long friendly = white ? board.whitePieces : board.blackPieces;
        long enemy    = white ? board.blackPieces : board.whitePieces;
        long occupied = board.allPieces;
        int off = white ? 0 : 6;

        addPawnMoves(board, white, occupied, enemy, moves);

        long kn = board.pieceBitboards[1 + off];
        while (kn != 0) {
            int sq = Long.numberOfTrailingZeros(kn);
            expandBits(sq, KNIGHT_ATK[sq] & ~friendly, moves);
            kn &= kn - 1;
        }

        long diag = board.pieceBitboards[2 + off] | board.pieceBitboards[4 + off];
        while (diag != 0) {
            int sq = Long.numberOfTrailingZeros(diag);
            expandBits(sq, diagRays(sq, occupied) & ~friendly, moves);
            diag &= diag - 1;
        }

        long orth = board.pieceBitboards[3 + off] | board.pieceBitboards[4 + off];
        while (orth != 0) {
            int sq = Long.numberOfTrailingZeros(orth);
            expandBits(sq, orthRays(sq, occupied) & ~friendly, moves);
            orth &= orth - 1;
        }

        long kg = board.pieceBitboards[5 + off];
        if (kg != 0) {
            int kSq = Long.numberOfTrailingZeros(kg);
            expandBits(kSq, KING_ATK[kSq] & ~friendly, moves);
            if (white) {
                if ((board.castlingRights & 1) != 0 && (occupied & 0x60L) == 0
                        && !isSquareAttacked(board, 4, false) && !isSquareAttacked(board, 5, false))
                    moves.add("e1g1");
                if ((board.castlingRights & 2) != 0 && (occupied & 0x0EL) == 0
                        && !isSquareAttacked(board, 4, false) && !isSquareAttacked(board, 3, false))
                    moves.add("e1c1");
            } else {
                if ((board.castlingRights & 4) != 0 && (occupied & 0x6000000000000000L) == 0
                        && !isSquareAttacked(board, 60, true) && !isSquareAttacked(board, 61, true))
                    moves.add("e8g8");
                if ((board.castlingRights & 8) != 0 && (occupied & 0x0E00000000000000L) == 0
                        && !isSquareAttacked(board, 60, true) && !isSquareAttacked(board, 59, true))
                    moves.add("e8c8");
            }
        }
        return moves;
    }

    private static void addPawnMoves(Board board, boolean white, long occupied, long enemy, List<String> moves) {
        int off = white ? 0 : 6;
        long pawns = board.pieceBitboards[off];
        while (pawns != 0) {
            int from = Long.numberOfTrailingZeros(pawns);
            long bit  = 1L << from;
            if (white) {
                long p1 = (bit << 8) & ~occupied;
                if (p1 != 0) {
                    pawnMove(from, from + 8, moves);
                    long p2 = (p1 << 8) & ~occupied & RANK_4;
                    if (p2 != 0) moves.add(sq2str(from) + sq2str(from + 16));
                }
                if (((bit << 7) & enemy & ~FILE_H) != 0) pawnMove(from, from + 7, moves);
                if (((bit << 9) & enemy & ~FILE_A) != 0) pawnMove(from, from + 9, moves);
            } else {
                long p1 = (bit >>> 8) & ~occupied;
                if (p1 != 0) {
                    pawnMove(from, from - 8, moves);
                    long p2 = (p1 >>> 8) & ~occupied & RANK_5;
                    if (p2 != 0) moves.add(sq2str(from) + sq2str(from - 16));
                }
                if (((bit >>> 9) & enemy & ~FILE_H) != 0) pawnMove(from, from - 9, moves);
                if (((bit >>> 7) & enemy & ~FILE_A) != 0) pawnMove(from, from - 7, moves);
            }
            if (board.enPassantSquare != -1) {
                long epBit = 1L << board.enPassantSquare;
                if (white) {
                    if (((bit << 7) & ~FILE_H & epBit) != 0) moves.add(sq2str(from) + sq2str(board.enPassantSquare));
                    if (((bit << 9) & ~FILE_A & epBit) != 0) moves.add(sq2str(from) + sq2str(board.enPassantSquare));
                } else {
                    if (((bit >>> 9) & ~FILE_H & epBit) != 0) moves.add(sq2str(from) + sq2str(board.enPassantSquare));
                    if (((bit >>> 7) & ~FILE_A & epBit) != 0) moves.add(sq2str(from) + sq2str(board.enPassantSquare));
                }
            }
            pawns &= pawns - 1;
        }
    }

    private static void pawnMove(int from, int to, List<String> moves) {
        String base = sq2str(from) + sq2str(to);
        if (to >= 56 || to <= 7) {
            moves.add(base + "q"); moves.add(base + "r");
            moves.add(base + "b"); moves.add(base + "n");
        } else { moves.add(base); }
    }

    public static boolean isSquareAttacked(Board board, int sq, boolean byWhite) {
        int off = byWhite ? 0 : 6;
        long all = board.allPieces;
        long pawns = board.pieceBitboards[off];
        if (((byWhite ? BPAWN_ATK[sq] : WPAWN_ATK[sq]) & pawns) != 0) return true;
        if ((KNIGHT_ATK[sq] & board.pieceBitboards[1 + off]) != 0) return true;
        if ((KING_ATK[sq]   & board.pieceBitboards[5 + off]) != 0) return true;
        long dSliders = board.pieceBitboards[2 + off] | board.pieceBitboards[4 + off];
        if ((diagRays(sq, all) & dSliders) != 0) return true;
        long oSliders = board.pieceBitboards[3 + off] | board.pieceBitboards[4 + off];
        if ((orthRays(sq, all) & oSliders) != 0) return true;
        return false;
    }

    private static long diagRays(int sq, long occ) {
        long a = 0L; int r = sq / 8, f = sq % 8;
        for (int d = 1; r+d < 8 && f+d < 8; d++) { a |= 1L<<(sq+d*9); if(((occ>>(sq+d*9))&1)==1) break; }
        for (int d = 1; r+d < 8 && f-d >= 0; d++) { a |= 1L<<(sq+d*7); if(((occ>>(sq+d*7))&1)==1) break; }
        for (int d = 1; r-d >= 0 && f+d < 8; d++) { a |= 1L<<(sq-d*7); if(((occ>>(sq-d*7))&1)==1) break; }
        for (int d = 1; r-d >= 0 && f-d >= 0; d++) { a |= 1L<<(sq-d*9); if(((occ>>(sq-d*9))&1)==1) break; }
        return a;
    }

    private static long orthRays(int sq, long occ) {
        long a = 0L; int r = sq / 8, f = sq % 8;
        for (int d = 1; r+d < 8; d++) { a |= 1L<<(sq+d*8); if(((occ>>(sq+d*8))&1)==1) break; }
        for (int d = 1; r-d >= 0; d++) { a |= 1L<<(sq-d*8); if(((occ>>(sq-d*8))&1)==1) break; }
        for (int d = 1; f+d < 8;  d++) { a |= 1L<<(sq+d);   if(((occ>>(sq+d))  &1)==1) break; }
        for (int d = 1; f-d >= 0; d++) { a |= 1L<<(sq-d);   if(((occ>>(sq-d))  &1)==1) break; }
        return a;
    }

    private static void expandBits(int from, long targets, List<String> moves) {
        String fs = sq2str(from);
        while (targets != 0) {
            int to = Long.numberOfTrailingZeros(targets);
            moves.add(fs + sq2str(to));
            targets &= targets - 1;
        }
    }

    public static String sq2str(int sq) {
        return "" + (char)('a' + sq % 8) + (char)('1' + sq / 8);
    }

    public static String squareToCoord(int sq) { return sq2str(sq); }

    public static boolean isInCheck(Board board, boolean white) {
        long kbb = white ? board.pieceBitboards[Board.WK] : board.pieceBitboards[Board.BK];
        if (kbb == 0L) return false;
        return isSquareAttacked(board, Long.numberOfTrailingZeros(kbb), !white);
    }

    public static boolean isInCheckmate(Board board) {
        if (!generatePseudoLegalMoves(board).isEmpty()) return false;
        return isInCheck(board, board.whiteToMove);
    }

    public static boolean isInStalemate(Board board) {
        if (!generatePseudoLegalMoves(board).isEmpty()) return false;
        return !isInCheck(board, board.whiteToMove);
    }

    private static void initAttacks() {
        for (int i = 0; i < 64; i++) {
            long b = 1L << i;
            long kn = 0L;
            if ((b & ~FILE_A & ~FILE_B) != 0) { kn |= (b << 6)  | (b >>> 10); }
            if ((b & ~FILE_A) != 0)           { kn |= (b << 15) | (b >>> 17); }
            if ((b & ~FILE_H) != 0)           { kn |= (b << 17) | (b >>> 15); }
            if ((b & ~FILE_G & ~FILE_H) != 0) { kn |= (b << 10) | (b >>> 6);  }
            KNIGHT_ATK[i] = kn;

            long kg = (b << 8) | (b >>> 8);
            if ((b & ~FILE_A) != 0) kg |= (b >>> 1) | (b << 7) | (b >>> 9);
            if ((b & ~FILE_H) != 0) kg |= (b <<  1) | (b << 9) | (b >>> 7);
            KING_ATK[i] = kg;

            long wp = 0L;
            if ((b & ~FILE_A) != 0) wp |= b << 7;
            if ((b & ~FILE_H) != 0) wp |= b << 9;
            WPAWN_ATK[i] = wp;

            long bp = 0L;
            if ((b & ~FILE_A) != 0) bp |= b >>> 9;
            if ((b & ~FILE_H) != 0) bp |= b >>> 7;
            BPAWN_ATK[i] = bp;
        }
    }
}