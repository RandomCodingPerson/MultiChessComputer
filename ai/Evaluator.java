package ai;

import engine.Board;
import engine.MoveGenerator;
import engine.Move;

public class Evaluator {

    static final int PAWN_VAL = 100;
    static final int KNIGHT_VAL = 320;
    static final int BISHOP_VAL = 330;  // 330 not 320, bishops slightly edge out knights
    static final int ROOK_VAL = 500;
    static final int QUEEN_VAL = 900;
    static final int KING_VAL = 20000;

    private static final int MOB_WEIGHT = 8;
    private static final int DOUBLED_PEN = 25;
    private static final int ISO_PEN = 20;
    private static final int PASSED_BONUS = 40;

    private static final int[] PAWN_TABLE = {
        0, 0, 0, 0, 0, 0, 0, 0,
        50, 50, 50, 50, 50, 50, 50, 50,
        10, 10, 20, 30, 30, 20, 10, 10,
        5, 5, 10, 25, 25, 10, 5, 5,
        0, 0, 0, 20, 20, 0, 0, 0,
        5, -5, -10, 0, 0, -10, -5, 5,
        5, 10, 10, -20, -20, 10, 10, 5,
        0, 0, 0, 0, 0, 0, 0, 0
    };

    private static final int[] KNIGHT_TABLE = {
        -50, -40, -30, -30, -30, -30, -40, -50,
        -40, -20, 0, 0, 0, 0, -20, -40,
        -30, 0, 10, 15, 15, 10, 0, -30,
        -30, 5, 15, 20, 20, 15, 5, -30,
        -30, 0, 15, 20, 20, 15, 0, -30,
        -30, 5, 10, 15, 15, 10, 5, -30,
        -40, -20, 0, 5, 5, 0, -20, -40,
        -50, -40, -30, -30, -30, -30, -40, -50
    };

    private static final int[] BISHOP_TABLE = {
        -20,-10,-10,-10,-10,-10,-10,-20,
        -10,  0,  0,  0,  0,  0,  0,-10,
        -10,  0,  5, 10, 10,  5,  0,-10,
        -10,  5,  5, 10, 10,  5,  5,-10,
        -10,  0, 10, 10, 10, 10,  0,-10,
        -10, 10, 10, 10, 10, 10, 10,-10,
        -10,  5,  0,  0,  0,  0,  5,-10,
        -20,-10,-10,-10,-10,-10,-10,-20
    };

    private static final int[] ROOK_TBL = {
        0,0,5,10,10,5,0,0, 0,0,5,10,10,5,0,0,
        0,0,5,10,10,5,0,0, 0,0,5,10,10,5,0,0,
        0,0,5,10,10,5,0,0, 0,0,5,10,10,5,0,0,
        0,0,5,10,10,5,0,0, 0,0,5,10,10,5,0,0
    };

    private static final int[] QUEEN_TBL = {
        -10,-5,0,5,5,0,-5,-10,
        -5,0,5,10,10,5,0,-5,
        0,5,10,15,15,10,5,0,
        5,10,15,20,20,15,10,5,
        5,10,15,20,20,15,10,5,
        0,5,10,15,15,10,5,0,
        -5,0,5,10,10,5,0,-5,
        -10,-5,0,5,5,0,-5,-10
    };

    private static final int[] KING_MG = {
        -30,-40,-40,-50,-50,-40,-40,-30,
        -30,-40,-40,-50,-50,-40,-40,-30,
        -30,-40,-40,-50,-50,-40,-40,-30,
        -30,-40,-40,-50,-50,-40,-40,-30,
        -20,-30,-30,-40,-40,-30,-30,-20,
        -10,-20,-20,-20,-20,-20,-20,-10,
        20, 20,  0,  0,  0,  0, 20, 20,
        20, 30, 10,  0,  0, 10, 30, 20
    };

    private static final int[] KING_EG = {
        -50,-30,-30,-30,-30,-30,-30,-50,
        -30,-10,  0,  0,  0,  0,-10,-30,
        -30,  0, 20, 30, 30, 20,  0,-30,
        -30,  0, 30, 40, 40, 30,  0,-30,
        -30,  0, 30, 40, 40, 30,  0,-30,
        -30,  0, 20, 30, 30, 20,  0,-30,
        -30,-10,  0,  0,  0,  0,-10,-30,
        -50,-30,-30,-30,-30,-30,-30,-50
    };

    public static int evaluate(Board board) {
        int mg = 0;
        int eg = 0;

        mg += tallyPieces(board.pieceBitboards[Board.WP], PAWN_VAL, PAWN_TABLE, true);
        mg += tallyPieces(board.pieceBitboards[Board.WN], KNIGHT_VAL, KNIGHT_TABLE, true);
        mg += tallyPieces(board.pieceBitboards[Board.WB], BISHOP_VAL, BISHOP_TABLE, true);
        mg += tallyPieces(board.pieceBitboards[Board.WR], ROOK_VAL, ROOK_TBL, true);
        mg += tallyPieces(board.pieceBitboards[Board.WQ], QUEEN_VAL, QUEEN_TBL, true);

        eg += tallyPieces(board.pieceBitboards[Board.WP], PAWN_VAL, PAWN_TABLE, true);
        eg += tallyPieces(board.pieceBitboards[Board.WN], KNIGHT_VAL, KNIGHT_TABLE, true);
        eg += tallyPieces(board.pieceBitboards[Board.WB], BISHOP_VAL, BISHOP_TABLE, true);
        eg += tallyPieces(board.pieceBitboards[Board.WR], ROOK_VAL, ROOK_TBL, true);
        eg += tallyPieces(board.pieceBitboards[Board.WQ], QUEEN_VAL, QUEEN_TBL, true);

        mg -= tallyPieces(board.pieceBitboards[Board.BP], PAWN_VAL, PAWN_TABLE, false);
        mg -= tallyPieces(board.pieceBitboards[Board.BN], KNIGHT_VAL, KNIGHT_TABLE, false);
        mg -= tallyPieces(board.pieceBitboards[Board.BB], BISHOP_VAL, BISHOP_TABLE, false);
        mg -= tallyPieces(board.pieceBitboards[Board.BR], ROOK_VAL, ROOK_TBL, false);
        mg -= tallyPieces(board.pieceBitboards[Board.BQ], QUEEN_VAL, QUEEN_TBL, false);

        eg -= tallyPieces(board.pieceBitboards[Board.BP], PAWN_VAL, PAWN_TABLE, false);
        eg -= tallyPieces(board.pieceBitboards[Board.BN], KNIGHT_VAL, KNIGHT_TABLE, false);
        eg -= tallyPieces(board.pieceBitboards[Board.BB], BISHOP_VAL, BISHOP_TABLE, false);
        eg -= tallyPieces(board.pieceBitboards[Board.BR], ROOK_VAL, ROOK_TBL, false);
        eg -= tallyPieces(board.pieceBitboards[Board.BQ], QUEEN_VAL, QUEEN_TBL, false);

        int matLeft =
            Long.bitCount(board.pieceBitboards[Board.WN] | board.pieceBitboards[Board.BN]) +
            Long.bitCount(board.pieceBitboards[Board.WB] | board.pieceBitboards[Board.BB]) +
            Long.bitCount(board.pieceBitboards[Board.WR] | board.pieceBitboards[Board.BR]) * 2 +
            Long.bitCount(board.pieceBitboards[Board.WQ] | board.pieceBitboards[Board.BQ]) * 4;
        int maxPhase = 24;
        int phase = Math.min(matLeft, maxPhase);

        long wk = board.pieceBitboards[Board.WK];
        long bk = board.pieceBitboards[Board.BK];
        if (wk != 0) {
            int s = Long.numberOfTrailingZeros(wk);
            mg += KING_MG[s];
            eg += KING_EG[s];
        }
        if (bk != 0) {
            int s = Long.numberOfTrailingZeros(bk);
            mg -= KING_MG[s ^ 56];
            eg -= KING_EG[s ^ 56];
        }

        boolean side = board.whiteToMove;
        board.whiteToMove = true;
        int whiteMob = MoveGenerator.generatePseudoLegalMoves(board).size();
        board.whiteToMove = false;
        int blackMob = MoveGenerator.generatePseudoLegalMoves(board).size();
        board.whiteToMove = side;
        mg += MOB_WEIGHT * (whiteMob - blackMob);

        long wp = board.pieceBitboards[Board.WP];
        long bp = board.pieceBitboards[Board.BP];

        for (int f = 0; f < 8; f++) {
            long fmask = 0x0101010101010101L << f;
            int wc = Long.bitCount(wp & fmask);
            int bc = Long.bitCount(bp & fmask);
            if (wc > 1) mg -= DOUBLED_PEN * (wc - 1);
            if (bc > 1) mg += DOUBLED_PEN * (bc - 1);
            long adj = (f > 0 ? fmask >> 1 : 0L) | (f < 7 ? fmask << 1 : 0L);
            if (wc > 0 && (wp & adj) == 0) mg -= ISO_PEN * wc;
            if (bc > 0 && (bp & adj) == 0) mg += ISO_PEN * bc;
        }

        // passed pawns
        long bb = wp;
        while (bb != 0) {
            int sq = Long.numberOfTrailingZeros(bb);
            int f = sq % 8, r = sq / 8;
            long fwd = 0L;
            for (int rr = r + 1; rr < 8; rr++)
                for (int ff = Math.max(0, f-1); ff <= Math.min(7, f+1); ff++)
                    fwd |= 1L << (rr * 8 + ff);
            if ((bp & fwd) == 0) mg += PASSED_BONUS;
            bb &= bb - 1;
        }
        bb = bp;
        while (bb != 0) {
            int sq = Long.numberOfTrailingZeros(bb);
            int f = sq % 8, r = sq / 8;
            long fwd = 0L;
            for (int rr = r - 1; rr >= 0; rr--)
                for (int ff = Math.max(0, f-1); ff <= Math.min(7, f+1); ff++)
                    fwd |= 1L << (rr * 8 + ff);
            if ((wp & fwd) == 0) mg -= PASSED_BONUS;
            bb &= bb - 1;
        }

        // TODO: bishop pair bonus

        return ((mg * phase) + (eg * (maxPhase - phase))) / maxPhase;
    }

    static int tallyPieces(long bits, int base, int[] table, boolean white) {
        int n = 0;
        while (bits != 0) {
            int sq = Long.numberOfTrailingZeros(bits);
            n += base;
            if (table != null)
                n += table[white ? sq : sq ^ 56];
            bits &= bits - 1;
        }
        return n;
    }

    public static int getPieceValue(int sq, Board board) {
        long bit = 1L << sq;
        if ((board.pieceBitboards[Board.WP] & bit) != 0) return PAWN_VAL;
        if ((board.pieceBitboards[Board.WN] & bit) != 0) return KNIGHT_VAL;
        if ((board.pieceBitboards[Board.WB] & bit) != 0) return BISHOP_VAL;
        if ((board.pieceBitboards[Board.WR] & bit) != 0) return ROOK_VAL;
        if ((board.pieceBitboards[Board.WQ] & bit) != 0) return QUEEN_VAL;
        if ((board.pieceBitboards[Board.WK] & bit) != 0) return KING_VAL;
        if ((board.pieceBitboards[Board.BP] & bit) != 0) return PAWN_VAL;
        if ((board.pieceBitboards[Board.BN] & bit) != 0) return KNIGHT_VAL;
        if ((board.pieceBitboards[Board.BB] & bit) != 0) return BISHOP_VAL;
        if ((board.pieceBitboards[Board.BR] & bit) != 0) return ROOK_VAL;
        if ((board.pieceBitboards[Board.BQ] & bit) != 0) return QUEEN_VAL;
        if ((board.pieceBitboards[Board.BK] & bit) != 0) return KING_VAL;
        return 0;
    }
}