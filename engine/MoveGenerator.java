package engine;

import java.util.ArrayList;
import java.util.List;

public class MoveGenerator {

    private static final long fileA = 0x0101010101010101L;
    private static final long fileB = 0x0202020202020202L;
    private static final long fileG = 0x4040404040404040L;
    private static final long fileH = 0x8080808080808080L;

    private static final long rank4 = 0x00000000FF000000L;
    private static final long rank5 = 0x000000FF00000000L;

    private static final long[] knightAtk = new long[64];
    private static final long[] kingAtk = new long[64];
    private static final long[] whitePawnAtk = new long[64];
    private static final long[] blackPawnAtk = new long[64];

    static { initAttacks(); }


    public static List<String> generatePseudoLegalMoves(Board b) {

        List<String> moves = new ArrayList<>();

        boolean white = b.whiteToMove;
        long friendly = white ? b.whitePieces : b.blackPieces;
        long enemy = white ? b.blackPieces : b.whitePieces;
        long occ = b.allPieces;
        int off = white ? 0 : 6;

        addPawnMoves(b, white, occ, enemy, moves);

        long bb = b.pieceBitboards[1 + off];
        while (bb != 0) {
            int sq = Long.numberOfTrailingZeros(bb);
            expandBits(sq, knightAtk[sq] & ~friendly, moves);
            bb &= bb - 1;
        }

        bb = b.pieceBitboards[2 + off] | b.pieceBitboards[4 + off];
        while (bb != 0) {
            int sq = Long.numberOfTrailingZeros(bb);
            expandBits(sq, diagRays(sq, occ) & ~friendly, moves);
            bb &= bb - 1;
        }

        bb = b.pieceBitboards[3 + off] | b.pieceBitboards[4 + off];
        while (bb != 0) {
            int sq = Long.numberOfTrailingZeros(bb);
            expandBits(sq, orthRays(sq, occ) & ~friendly, moves);
            bb &= bb - 1;
        }

        long king = b.pieceBitboards[5 + off];

        if (king != 0) {
            int sq = Long.numberOfTrailingZeros(king);
            expandBits(sq, kingAtk[sq] & ~friendly, moves);

            if (white) {

                if ((b.castlingRights & 1) != 0 && (occ & 0x60L) == 0 &&
                    !isSquareAttacked(b, 4, false) && !isSquareAttacked(b, 5, false))
                    moves.add("e1g1");

                if ((b.castlingRights & 2) != 0 && (occ & 0x0EL) == 0 &&
                    !isSquareAttacked(b, 4, false) && !isSquareAttacked(b, 3, false))
                    moves.add("e1c1");

            } else {

                if ((b.castlingRights & 4) != 0 && (occ & 0x6000000000000000L) == 0 &&
                    !isSquareAttacked(b, 60, true) && !isSquareAttacked(b, 61, true))
                    moves.add("e8g8");

                if ((b.castlingRights & 8) != 0 && (occ & 0x0E00000000000000L) == 0 &&
                    !isSquareAttacked(b, 60, true) && !isSquareAttacked(b, 59, true))
                    moves.add("e8c8");
            }
        }

        return moves;
    }


    private static void addPawnMoves(Board b, boolean white, long occ, long enemy, List<String> moves) {

        int off = white ? 0 : 6;
        long pawns = b.pieceBitboards[off];

        while (pawns != 0) {

            int from = Long.numberOfTrailingZeros(pawns);
            long bit = 1L << from;

            if (white) {

                long one = (bit << 8) & ~occ;

                if (one != 0) {
                    pawnMove(from, from + 8, moves);

                    long two = (one << 8) & ~occ & rank4;
                    if (two != 0)
                        moves.add(sq2str(from) + sq2str(from + 16));
                }

                if (((bit << 7) & enemy & ~fileH) != 0)
                    pawnMove(from, from + 7, moves);

                if (((bit << 9) & enemy & ~fileA) != 0)
                    pawnMove(from, from + 9, moves);

            } else {

                long one = (bit >>> 8) & ~occ;

                if (one != 0) {
                    pawnMove(from, from - 8, moves);

                    long two = (one >>> 8) & ~occ & rank5;
                    if (two != 0)
                        moves.add(sq2str(from) + sq2str(from - 16));
                }

                if (((bit >>> 9) & enemy & ~fileH) != 0)
                    pawnMove(from, from - 9, moves);

                if (((bit >>> 7) & enemy & ~fileA) != 0)
                    pawnMove(from, from - 7, moves);
            }

            pawns &= pawns - 1;
        }
    }


    private static void pawnMove(int from, int to, List<String> moves) {

        String m = sq2str(from) + sq2str(to);

        if (to >= 56 || to <= 7) {
            moves.add(m + "q");
            moves.add(m + "r");
            moves.add(m + "b");
            moves.add(m + "n");
        } else
            moves.add(m);
    }


    public static String sq2str(int sq) {
        return "" + (char)('a' + sq % 8) + (char)('1' + sq / 8);
    }

    public static String squareToCoord(int sq) {
        return sq2str(sq);
    }


    public static boolean isInCheck(Board b, boolean white) {

        long king = white ? b.pieceBitboards[Board.WK] : b.pieceBitboards[Board.BK];

        if (king == 0)
            return false;

        return isSquareAttacked(b, Long.numberOfTrailingZeros(king), !white);
    }


    public static boolean isInCheckmate(Board b) {
        return generatePseudoLegalMoves(b).isEmpty() && isInCheck(b, b.whiteToMove);
    }


    public static boolean isInStalemate(Board b) {
        return generatePseudoLegalMoves(b).isEmpty() && !isInCheck(b, b.whiteToMove);
    }
    public static boolean isSquareAttacked(Board b, int sq, boolean white) {
    int off = white ? 0 : 6;
    if (((white ? blackPawnAtk[sq] : whitePawnAtk[sq]) & b.pieceBitboards[off]) != 0) return true;
    if ((knightAtk[sq] & b.pieceBitboards[1 + off]) != 0) return true;
    if ((kingAtk[sq] & b.pieceBitboards[5 + off]) != 0) return true;

    if ((diagRays(sq, b.allPieces) & (b.pieceBitboards[2 + off] | b.pieceBitboards[4 + off])) != 0)
        return true;
    if ((orthRays(sq, b.allPieces) & (b.pieceBitboards[3 + off] | b.pieceBitboards[4 + off])) != 0)
        return true;

    return false;
}

private static long diagRays(int sq, long occ) {
    long a = 0;
    int r = sq / 8, f = sq % 8;

    for (int d = 1; r+d < 8 && f+d < 8; d++) {
        int x = sq + d * 9;
        a |= 1L << x;
        if (((occ >> x) & 1) != 0) break;
    }
    for (int d = 1; r+d < 8 && f-d >= 0; d++) {
        int x = sq + d * 7;
        a |= 1L << x;
        if (((occ >> x) & 1) != 0) break;
    }
    for (int d = 1; r-d >= 0 && f+d < 8; d++) {
        int x = sq - d * 7;
        a |= 1L << x;
        if (((occ >> x) & 1) != 0) break;
    }
    for (int d = 1; r-d >= 0 && f-d >= 0; d++) {
        int x = sq - d * 9;
        a |= 1L << x;
        if (((occ >> x) & 1) != 0) break;
    }

    return a;
}

private static long orthRays(int sq, long occ) {
    long a = 0;
    int r = sq / 8, f = sq % 8;

    for (int d = 1; r+d < 8; d++) {
        int x = sq + d * 8;
        a |= 1L << x;
        if (((occ >> x) & 1) != 0) break;
    }
    for (int d = 1; r-d >= 0; d++) {
        int x = sq - d * 8;
        a |= 1L << x;
        if (((occ >> x) & 1) != 0) break;
    }
    for (int d = 1; f+d < 8; d++) {
        int x = sq + d;
        a |= 1L << x;
        if (((occ >> x) & 1) != 0) break;
    }
    for (int d = 1; f-d >= 0; d++) {
        int x = sq - d;
        a |= 1L << x;
        if (((occ >> x) & 1) != 0) break;
    }

    return a;
}

private static void expandBits(int from, long bits, List<String> moves) {
    String s = sq2str(from);
    while (bits != 0) {
        moves.add(s + sq2str(Long.numberOfTrailingZeros(bits)));
        bits &= bits - 1;
    }
}

private static void initAttacks() {
    for (int i = 0; i < 64; i++) {
        long b = 1L << i;

        long kn = 0;
        if ((b & ~fileA & ~fileB) != 0) kn |= (b << 6) | (b >>> 10);
        if ((b & ~fileA) != 0) kn |= (b << 15) | (b >>> 17);
        if ((b & ~fileH) != 0) kn |= (b << 17) | (b >>> 15);
        if ((b & ~fileG & ~fileH) != 0) kn |= (b << 10) | (b >>> 6);
        knightAtk[i] = kn;

        long kg = (b << 8) | (b >>> 8);
        if ((b & ~fileA) != 0) kg |= (b >>> 1) | (b << 7) | (b >>> 9);
        if ((b & ~fileH) != 0) kg |= (b << 1) | (b << 9) | (b >>> 7);
        kingAtk[i] = kg;

        if ((b & ~fileA) != 0) whitePawnAtk[i] |= b << 7;
        if ((b & ~fileH) != 0) whitePawnAtk[i] |= b << 9;
        if ((b & ~fileA) != 0) blackPawnAtk[i] |= b >>> 9;
        if ((b & ~fileH) != 0) blackPawnAtk[i] |= b >>> 7;
    }
}
}