package engine;

import java.util.Random;

public class Zobrist {

    private static final long[][] KEYS = new long[12][64];
    private static final long[] CASTLE = new long[16];
    private static final long[] EP = new long[9];
    private static final long SIDE;

    static {
        Random rng = new Random(23081997L); // fixed seed, don't change
        for (int p = 0; p < 12; p++)
            for (int sq = 0; sq < 64; sq++)
                KEYS[p][sq] = rng.nextLong();
        SIDE = rng.nextLong();
        for (int i = 0; i < 16; i++) CASTLE[i] = rng.nextLong();
        for (int i = 0; i < 9; i++) EP[i] = rng.nextLong();
    }

    public static long calculateHash(Board board) {
        long h = 0L;
        for (int p = 0; p < 12; p++) {
            long bb = board.pieceBitboards[p];
            while (bb != 0) {
                int sq = Long.numberOfTrailingZeros(bb);
                h ^= KEYS[p][sq];
                bb &= bb - 1;
            }
        }
        if (!board.whiteToMove) h ^= SIDE;
        return h;
    }

    public static long togglePiece(long h, int piece, int sq) { return h ^ KEYS[piece][sq]; }

    public static long toggleTurn(long h) { return h ^ SIDE; }

    public static long toggleCastling(long h, int oldR, int newR) {
        return h ^ CASTLE[oldR] ^ CASTLE[newR];
    }

    public static long toggleEnPassant(long h, int oldF, int newF) {
        return h ^ EP[oldF] ^ EP[newF];
    }
}