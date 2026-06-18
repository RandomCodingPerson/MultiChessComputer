package ai;

import engine.Board;
import engine.MoveGenerator;

public class Evaluator {

    static final int pawnVal = 100;
    static final int knightVal = 320;
    static final int bishopVal = 330; // maybe? slightly better
    static final int rookVal = 500;
    static final int queenVal = 900;
    static final int kingVal = 20000;

    private static final int mobWeight = 8;
    private static final int doubledPen = 25;
    private static final int isoPen = 20;
    private static final int passedBonus = 40;

    private static final int[] pawnTable = {
        0,0,0,0,0,0,0,0,
        50,50,50,50,50,50,50,50,
        10,10,20,30,30,20,10,10,
        5,5,10,25,25,10,5,5,
        0,0,0,20,20,0,0,0,
        5,-5,-10,0,0,-10,-5,5,
        5,10,10,-20,-20,10,10,5,
        0,0,0,0,0,0,0,0
    };
    private static final int[] knightTable = {
        -50,-40,-30,-30,-30,-30,-40,-50,
        -40,-20,0,0,0,0,-20,-40,
        -30,0,10,15,15,10,0,-30,
        -30,5,15,20,20,15,5,-30,
        -30,0,15,20,20,15,0,-30,
        -30,5,10,15,15,10,5,-30,
        -40,-20,0,5,5,0,-20,-40,
        -50,-40,-30,-30,-30,-30,-40,-50
    };
    private static final int[] bishopTable = {
        -20,-10,-10,-10,-10,-10,-10,-20,
        -10,0,0,0,0,0,0,-10,
        -10,0,5,10,10,5,0,-10,
        -10,5,5,10,10,5,5,-10,
        -10,0,10,10,10,10,0,-10,
        -10,10,10,10,10,10,10,-10,
        -10,5,0,0,0,0,5,-10,
        -20,-10,-10,-10,-10,-10,-10,-20
    };
    private static final int[] rookTable = {
        0,0,5,10,10,5,0,0,
        0,0,5,10,10,5,0,0,
        0,0,5,10,10,5,0,0,
        0,0,5,10,10,5,0,0,
        0,0,5,10,10,5,0,0,
        0,0,5,10,10,5,0,0,
        0,0,5,10,10,5,0,0,
        0,0,5,10,10,5,0,0
    };

    private static final int[] queenTable = {
        -10,-5,0,5,5,0,-5,-10,
        -5,0,5,10,10,5,0,-5,
        0,5,10,15,15,10,5,0,
        5,10,15,20,20,15,10,5,
        5,10,15,20,20,15,10,5,
        0,5,10,15,15,10,5,0,
        -5,0,5,10,10,5,0,-5,
        -10,-5,0,5,5,0,-5,-10
    };
    private static final int[] kingMid = {
        -30,-40,-40,-50,-50,-40,-40,-30,
        -30,-40,-40,-50,-50,-40,-40,-30,
        -30,-40,-40,-50,-50,-40,-40,-30,
        -30,-40,-40,-50,-50,-40,-40,-30,
        -20,-30,-30,-40,-40,-30,-30,-20,
        -10,-20,-20,-20,-20,-20,-20,-10,
        20,20,0,0,0,0,20,20,
        20,30,10,0,0,10,30,20
    };
    private static final int[] kingEnd = {
        -50,-30,-30,-30,-30,-30,-30,-50,
        -30,-10,0,0,0,0,-10,-30,
        -30,0,20,30,30,20,0,-30,
        -30,0,30,40,40,30,0,-30,
        -30,0,30,40,40,30,0,-30,
        -30,0,20,30,30,20,0,-30,
        -30,-10,0,0,0,0,-10,-30,
        -50,-30,-30,-30,-30,-30,-30,-50
    };
    public static int evaluate(Board b) {
        int mid = 0;
        int end = 0;

        mid += tallyPieces(b.pieceBitboards[Board.WP], pawnVal, pawnTable, true);
        mid += tallyPieces(b.pieceBitboards[Board.WN], knightVal, knightTable, true);
        mid += tallyPieces(b.pieceBitboards[Board.WB], bishopVal, bishopTable, true);
        mid += tallyPieces(b.pieceBitboards[Board.WR], rookVal, rookTable, true);
        mid += tallyPieces(b.pieceBitboards[Board.WQ], queenVal, queenTable, true);
        mid -= tallyPieces(b.pieceBitboards[Board.BP], pawnVal, pawnTable, false);
        mid -= tallyPieces(b.pieceBitboards[Board.BN], knightVal, knightTable, false);
        mid -= tallyPieces(b.pieceBitboards[Board.BB], bishopVal, bishopTable, false);
        mid -= tallyPieces(b.pieceBitboards[Board.BR], rookVal, rookTable, false);
        mid -= tallyPieces(b.pieceBitboards[Board.BQ], queenVal, queenTable, false);

        end += tallyPieces(b.pieceBitboards[Board.WP], pawnVal, pawnTable, true);
        end += tallyPieces(b.pieceBitboards[Board.WN], knightVal, knightTable, true);
        end += tallyPieces(b.pieceBitboards[Board.WB], bishopVal, bishopTable, true);

        // probably should split this better but it works
                end += tallyPieces(b.pieceBitboards[Board.WR], rookVal, rookTable, true);
        end += tallyPieces(b.pieceBitboards[Board.WQ], queenVal, queenTable, true);

        end -= tallyPieces(b.pieceBitboards[Board.BP], pawnVal, pawnTable, false);
        end -= tallyPieces(b.pieceBitboards[Board.BN], knightVal, knightTable, false);
        end -= tallyPieces(b.pieceBitboards[Board.BB], bishopVal, bishopTable, false);
        end -= tallyPieces(b.pieceBitboards[Board.BR], rookVal, rookTable, false);
        end -= tallyPieces(b.pieceBitboards[Board.BQ], queenVal, queenTable, false);


        int pieces = 
            Long.bitCount(b.pieceBitboards[Board.WN] | b.pieceBitboards[Board.BN]) +
            Long.bitCount(b.pieceBitboards[Board.WB] | b.pieceBitboards[Board.BB]) +
            Long.bitCount(b.pieceBitboards[Board.WR] | b.pieceBitboards[Board.BR]) * 2 +
            Long.bitCount(b.pieceBitboards[Board.WQ] | b.pieceBitboards[Board.BQ]) * 4;

        int max = 24;
        int phase = Math.min(pieces, max);
        long wk = b.pieceBitboards[Board.WK];
        long bk = b.pieceBitboards[Board.BK];
        if(wk != 0) {
                int sq = Long.numberOfTrailingZeros(wk);
                mid += kingMid[sq];
                end += kingEnd[sq];
        }
        if(bk != 0) {
            int sq = Long.numberOfTrailingZeros(bk);
            mid -= kingMid[sq ^ 56];
            end -= kingEnd[sq ^ 56];
        }

        boolean save = b.whiteToMove;

        b.whiteToMove = true;
        int whiteMoves = MoveGenerator.generatePseudoLegalMoves(b).size();
        b.whiteToMove = false;
        int blackMoves = MoveGenerator.generatePseudoLegalMoves(b).size();

        b.whiteToMove = save;
        mid += mobWeight * (whiteMoves - blackMoves);


        long wp = b.pieceBitboards[Board.WP];
        long bp = b.pieceBitboards[Board.BP];
        for(int file = 0; file < 8; file++) {
            long mask = 0x0101010101010101L << file;
            int w = Long.bitCount(wp & mask);
            int bl = Long.bitCount(bp & mask);

            if(w > 1)
                mid -= doubledPen * (w - 1);

            if(bl > 1)
                mid += doubledPen * (bl - 1);
            long near = (file > 0 ? mask >> 1 : 0) |
                        (file < 7 ? mask << 1 : 0);

            if(w > 0 && (wp & near) == 0)
                mid -= isoPen * w;

            if(bl > 0 && (bp & near) == 0)
                mid += isoPen * bl;
        }
        // passed pawns
        long pawns = wp;

        while(pawns != 0) {
   int sq = Long.numberOfTrailingZeros(pawns);

            int file = sq % 8;
            int rank = sq / 8;

            long ahead = 0;

            for(int r = rank + 1; r < 8; r++) {
                for(int f = Math.max(0,file-1); f <= Math.min(7,file+1); f++) {
                    ahead |= 1L << (r * 8 + f);
                }
            }

            if((bp & ahead) == 0)
                mid += passedBonus;

            pawns &= pawns - 1;
        }
        pawns = bp;

        while(pawns != 0) {

            int sq = Long.numberOfTrailingZeros(pawns);

            int file = sq % 8;
            int rank = sq / 8;

            long ahead = 0;

            for(int r = rank - 1; r >= 0; r--) {
                for(int f = Math.max(0,file-1); f <= Math.min(7,file+1); f++) {
                    ahead |= 1L << (r * 8 + f);
                }
            }

            if((wp & ahead) == 0)
                mid -= passedBonus;

            pawns &= pawns - 1;
        }
        // TODO bishop pair someday maybe

        return ((mid * phase) + (end * (max - phase))) / max;
    }

    static int tallyPieces(long bits, int val, int[] table, boolean white) {
        int total = 0;
        while(bits != 0) {
            int sq = Long.numberOfTrailingZeros(bits);

            total += val;

            if(table != null)
                total += table[white ? sq : sq ^ 56];
            bits &= bits - 1;
        }

        return total;
    }


    public static int getPieceValue(int sq, Board b) {

        long bit = 1L << sq;
        if((b.pieceBitboards[Board.WP] & bit) != 0) return pawnVal;
        if((b.pieceBitboards[Board.WN] & bit) != 0) return knightVal;
        if((b.pieceBitboards[Board.WB] & bit) != 0) return bishopVal;
        if((b.pieceBitboards[Board.WR] & bit) != 0) return rookVal;
        if((b.pieceBitboards[Board.WQ] & bit) != 0) return queenVal;
        if((b.pieceBitboards[Board.WK] & bit) != 0) return kingVal;

        if((b.pieceBitboards[Board.BP] & bit) != 0) return pawnVal;
        if((b.pieceBitboards[Board.BN] & bit) != 0) return knightVal;
        if((b.pieceBitboards[Board.BB] & bit) != 0) return bishopVal;
        if((b.pieceBitboards[Board.BR] & bit) != 0) return rookVal;
        if((b.pieceBitboards[Board.BQ] & bit) != 0) return queenVal;
        if((b.pieceBitboards[Board.BK] & bit) != 0) return kingVal;

        return 0;
    }
}