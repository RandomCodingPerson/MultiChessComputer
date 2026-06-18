package engine;
import java.util.Random;


public class Zobrist {
    private static final long[][] keys = new long[12][64];
    private static final long[] castle = new long[16];
    private static final long[] enpass = new long[9];
    private static final long side;

    static {
        Random rng = new Random(23081997L); // keep same seed
        for (int p = 0; p < 12; p++) {
            for (int sq = 0; sq < 64; sq++) {
                keys[p][sq] = rng.nextLong();
            }
        }

        side = rng.nextLong(); //basically js making new random hash
        for (int i = 0; i < 16; i++) {
            castle[i] = rng.nextLong();
        }

        for (int i = 0; i < 9; i++) {
            enpass[i] = rng.nextLong();
        }
    }


    public static long calculateHash(Board board) {
        long h = 0;

        for (int p = 0; p < 12; p++) {
            long bb = board.pieceBitboards[p];
            while (bb != 0) {
                    int sq = Long.numberOfTrailingZeros(bb);
                    h ^= keys[p][sq];

                bb &= bb - 1;
            }


        }

        if (!board.whiteToMove){
            h ^= side;};

        return h;
    }


    /*
    public static long calculateHash(Board b) {
        long h = 0;

        for (int i = 0; i < 64; i++) {
            if (b.board[i] != 0) {
                h ^= keys[b.board[i]][i];
            }
        }

        return h;
    }
    */


    public static long togglePiece(long h, int piece, int sq) {
        return h ^ keys[piece][sq]; // || if it was goated
    }

    public static long toggleTurn(long h) {
            return h ^ side;
    }


    public static long toggleCastling(long h, int oldR, int newR) {
        // xor old out, new in
        return h ^ castle[oldR] ^ castle[newR];
    }
    public static long toggleEnPassant(long h, int oldF, int newF) {
        if (oldF != newF){
            h ^= enpass[oldF];
            }
        h ^= enpass[newF];
        
        return h;
    }
}