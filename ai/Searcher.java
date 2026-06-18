package ai;

import engine.Board;
import engine.MoveGenerator;
import engine.Move;

import java.util.List;
import java.util.ArrayList;

public class Searcher {
    private static TranspositionTable tt = new TranspositionTable(20);
    private static final int INF = 20_000_000;

    public static String findBestMove(Board board, int maxDepth) {
        String bestMove = null;

        for (int depth = 1; depth <= maxDepth; depth++) {
           int alpha = -INF;
            int beta = INF;
            List<String> moves = MoveGenerator.generatePseudoLegalMoves(board);
            if (moves.isEmpty()) break;

            String bestSoFar = moves.get(0);
            int bestScore = board.whiteToMove ? -INF : INF;

            for (String mv : moves) {
                Board next = dupBoard(board);
                if (!next.makeMove(mv)) continue;
                int score = search(next, depth - 1, alpha, beta);
               if (board.whiteToMove) {
                    if (score > bestScore) { bestScore = score; bestSoFar = mv; }
                    alpha = Math.max(alpha, score);
                } else {
                    if (score < bestScore) { bestScore = score; bestSoFar = mv; }
                    beta = Math.min(beta, score);
                }
                if (beta <= alpha) break;
            }
            // System.err.println("d" + depth + " best=" + bestSoFar + " score=" + bestScore);
            bestMove = bestSoFar;
        }
        return bestMove;
    }

    private static int search(Board board, int depth, int alpha, int beta) {
        TranspositionTable.TTEntry cached = tt.get(board.currentHash);
        if (cached != null && cached.depth >= depth) {
            if (cached.flags == TranspositionTable.EXACT) return cached.score;
            if (cached.flags == TranspositionTable.ALPHA && cached.score <= alpha) return cached.score;
            if (cached.flags == TranspositionTable.BETA && cached.score >= beta) return cached.score;
        }

        if (depth == 0) return quiesce(board, alpha, beta);

        List<String> moves = MoveGenerator.generatePseudoLegalMoves(board);
        if (moves.isEmpty()) return Evaluator.evaluate(board);

        sortMoves(moves, board);

        int origAlpha = alpha;
        String ttMove = null;

        if (board.whiteToMove) {
            int best = -INF;
            for (String mv : moves) {
                Board next = dupBoard(board);
                if (!next.makeMove(mv)) continue;
                int score = search(next, depth - 1, alpha, beta);
                if (score > best) { best = score; ttMove = mv; }
                alpha = Math.max(alpha, score);
                if (beta <= alpha) {
                    tt.put(board.currentHash, best, depth, TranspositionTable.BETA, ttMove);
                    return best;
                }
            }
            byte flag = (best <= origAlpha) ? TranspositionTable.ALPHA : TranspositionTable.EXACT;
            tt.put(board.currentHash, best, depth, flag, ttMove);
            return best;
        } else {
            int best = INF;
            for (String mv : moves) {
                Board next = dupBoard(board);
                if (!next.makeMove(mv)) continue;
                int score = search(next, depth - 1, alpha, beta);
                if (score < best) { best = score; ttMove = mv; }
                beta = Math.min(beta, score);
                if (beta <= alpha) {
                    tt.put(board.currentHash, best, depth, TranspositionTable.ALPHA, ttMove);
                    return best;
                }
            }
            byte flag = (best >= beta) ? TranspositionTable.BETA : TranspositionTable.EXACT;
            tt.put(board.currentHash, best, depth, flag, ttMove);
            return best;
        }
    }


    private static int quiesce(Board board, int alpha, int beta) {
        int standPat = Evaluator.evaluate(board);
          if (board.whiteToMove) {
            if (standPat >= beta) return standPat;
            if (standPat > alpha) alpha = standPat;
        } else {
            if (standPat <= alpha) return standPat;
            if (standPat < beta) beta = standPat;
        }

        List<String> caps = getCaptures(board);

        if (board.whiteToMove) {
            int best = standPat;
            for (String mv : caps) {
                Board next = dupBoard(board);
                if (!next.makeMove(mv)) continue;
                int score = quiesce(next, alpha, beta);
                best = Math.max(best, score);
                alpha = Math.max(alpha, score);
                if (beta <= alpha) return best;
            }
            return best;
        } else {
            int best = standPat;

            for (String mv : caps) {
                    Board next = dupBoard(board);
                if (!next.makeMove(mv)) continue;
                int score = quiesce(next, alpha, beta);
                best = Math.min(best, score);
                beta = Math.min(beta, score);
                if (beta <= alpha) return best;
             }
            return best;
        }
    }

    private static List<String> getCaptures(Board board) {
        List<String> all = MoveGenerator.generatePseudoLegalMoves(board);
        List<String> caps = new ArrayList<>();
        long enemy = board.whiteToMove ? board.blackPieces : board.whitePieces;
        for (String mv : all) {
            int toSq = (mv.charAt(2) - 'a') + (mv.charAt(3) - '1') * 8;
            if (((1L << toSq) & enemy) != 0) caps.add(mv);
        }
        return caps;
    }

    private static void sortMoves(List<String> moves, Board board) {
        moves.sort((a, b) -> mvvLva(b, board) - mvvLva(a, board));
    }

    private static int mvvLva(String mv, Board board) {
    int from = (mv.charAt(0) - 'a') + (mv.charAt(1) - '1') * 8;
    int to = (mv.charAt(2) - 'a') + (mv.charAt(3) - '1') * 8;
    return Evaluator.getPieceValue(to, board) * 10 - Evaluator.getPieceValue(from, board);
    }

    private static Board dupBoard(Board src) {
        Board dst = new Board();
        System.arraycopy(src.pieceBitboards, 0, dst.pieceBitboards, 0, 12);
        dst.whitePieces = src.whitePieces;
        dst.blackPieces = src.blackPieces;
        dst.allPieces = src.allPieces;
        dst.whiteToMove = src.whiteToMove;

        dst.currentHash = src.currentHash;
        dst.castlingRights = src.castlingRights;
        dst.enPassantSquare = src.enPassantSquare;
        return dst;
    }
}