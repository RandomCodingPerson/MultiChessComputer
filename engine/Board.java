package engine;
import java.util.Arrays;

public class Board {
    public long[] pieceBitboards = new long[12];
    public long whitePieces = 0L;
    public long blackPieces = 0L;
    public long allPieces = 0L;
    public int castlingRights = 15;
    public int enPassantSquare = -1;
    public boolean whiteToMove = true;
    public long currentHash = 0L;

    public static final int WP=0, WN=1, WB=2, WR=3, WQ=4, WK=5;
    public static final int BP=6, BN=7, BB=8, BR=9, BQ=10, BK=11;

    public Board() {
        importFEN("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
    }

    public void updateOccupancy() {
        whitePieces = 0L;
        blackPieces = 0L;
        for (int i = WP; i <= WK; i++) whitePieces |= pieceBitboards[i];
        for (int i = BP; i <= BK; i++) blackPieces |= pieceBitboards[i];
        allPieces = whitePieces | blackPieces;
    }

    public boolean makeMove(String uciMove) {
        if (uciMove == null) return false;
        String raw = uciMove.trim();
        String found = null;
        for (int i = 0; i + 4 <= raw.length(); i++) {
            String sub = raw.substring(i, i + 4).toLowerCase();
            boolean ok = sub.charAt(0) >= 'a' && sub.charAt(0) <= 'h'
                && sub.charAt(1) >= '1' && sub.charAt(1) <= '8'
                && sub.charAt(2) >= 'a' && sub.charAt(2) <= 'h'
                && sub.charAt(3) >= '1' && sub.charAt(3) <= '8';
            if (ok) {
                if (i + 5 <= raw.length()) {
                    char p = Character.toLowerCase(raw.charAt(i + 4));
                    if (p == 'q' || p == 'r' || p == 'b' || p == 'n') {
                        found = raw.substring(i, i + 5).toLowerCase(); break;
                    }
                }
                found = sub; break;
            }
        }
        if (found == null) return false;
        uciMove = found;

        int from = (uciMove.charAt(0) - 'a') + (uciMove.charAt(1) - '1') * 8;
        int to   = (uciMove.charAt(2) - 'a') + (uciMove.charAt(3) - '1') * 8;
        long fromMask = 1L << from;
        long toMask   = 1L << to;

        int mover = -1;
        int start = whiteToMove ? WP : BP;
        int end   = whiteToMove ? WK : BK;
        for (int i = start; i <= end; i++) {
            if ((pieceBitboards[i] & fromMask) != 0) { mover = i; break; }
        }
        if (mover == -1) return false;

        int victim = -1;
        int oppStart = whiteToMove ? BP : WP;
        int oppEnd   = whiteToMove ? BK : WK;
        for (int i = oppStart; i <= oppEnd; i++) {
            if ((pieceBitboards[i] & toMask) != 0) { victim = i; break; }
        }

        long oldHash   = this.currentHash;
        int oldRights  = this.castlingRights;
        int oldEpSq    = this.enPassantSquare;
        int oldEpFile  = (this.enPassantSquare == -1) ? 8 : (this.enPassantSquare % 8);

        int epSq = -1;
        int epPiece = -1;
        if ((mover == WP || mover == BP) && to == enPassantSquare) {
            epSq    = whiteToMove ? to - 8 : to + 8;
            epPiece = whiteToMove ? BP : WP;
            pieceBitboards[epPiece] &= ~(1L << epSq);
            currentHash = Zobrist.togglePiece(currentHash, epPiece, epSq);
        }

        boolean didCastle = false;
        int rookFrom = -1, rookTo = -1;
        if      (mover == WK && from == 4 && to == 6) { rookFrom = 7;  rookTo = 5;  didCastle = true; }
        else if (mover == WK && from == 4 && to == 2) { rookFrom = 0;  rookTo = 3;  didCastle = true; }
        else if (mover == BK && from == 60 && to == 62) { rookFrom = 63; rookTo = 61; didCastle = true; }
        else if (mover == BK && from == 60 && to == 58) { rookFrom = 56; rookTo = 59; didCastle = true; }

        if (didCastle) {
            int rook = whiteToMove ? WR : BR;
            pieceBitboards[rook] &= ~(1L << rookFrom);
            pieceBitboards[rook] |=  (1L << rookTo);
            currentHash = Zobrist.togglePiece(currentHash, rook, rookFrom);
            currentHash = Zobrist.togglePiece(currentHash, rook, rookTo);
        }

        currentHash = Zobrist.togglePiece(currentHash, mover, from);
        if (victim != -1) currentHash = Zobrist.togglePiece(currentHash, victim, to);
        pieceBitboards[mover] &= ~fromMask;
        if (victim != -1) pieceBitboards[victim] &= ~toMask;

        boolean hasPromo = uciMove.length() == 5 && (mover == WP || mover == BP);
        int promo = -1;
        if (hasPromo) {
            char pc = uciMove.charAt(4);
            if (whiteToMove) {
                if (pc == 'q') promo = WQ; else if (pc == 'r') promo = WR;
                else if (pc == 'b') promo = WB; else if (pc == 'n') promo = WN;
            } else {
                if (pc == 'q') promo = BQ; else if (pc == 'r') promo = BR;
                else if (pc == 'b') promo = BB; else if (pc == 'n') promo = BN;
            }
            pieceBitboards[promo] |= toMask;
            currentHash = Zobrist.togglePiece(currentHash, promo, to);
        } else {
            pieceBitboards[mover] |= toMask;
            currentHash = Zobrist.togglePiece(currentHash, mover, to);
        }

        if (from == 4  || to == 4)  castlingRights &= ~3;
        if (from == 60 || to == 60) castlingRights &= ~12;
        if (from == 7  || to == 7)  castlingRights &= ~1;
        if (from == 0  || to == 0)  castlingRights &= ~2;
        if (from == 63 || to == 63) castlingRights &= ~4;
        if (from == 56 || to == 56) castlingRights &= ~8;

        int newEpSq = -1;
        if (mover == WP && (to - from == 16)) newEpSq = from + 8;
        if (mover == BP && (from - to == 16)) newEpSq = from - 8;
        this.enPassantSquare = newEpSq;
        int newEpFile = (newEpSq == -1) ? 8 : (newEpSq % 8);

        currentHash = Zobrist.toggleCastling(currentHash, oldRights, this.castlingRights);
        currentHash = Zobrist.toggleEnPassant(currentHash, oldEpFile, newEpFile);
        currentHash = Zobrist.toggleTurn(currentHash);
        updateOccupancy();

        long kingBB = whiteToMove ? pieceBitboards[WK] : pieceBitboards[BK];
        if (kingBB == 0L) {
            System.err.println("warn: no king found after " + uciMove + ", rolling back");
            // undo everything
            pieceBitboards[mover] |= fromMask;
            if (hasPromo) pieceBitboards[promo] &= ~toMask;
            else pieceBitboards[mover] &= ~toMask;
            if (victim != -1) pieceBitboards[victim] |= toMask;
            if (epSq != -1) pieceBitboards[epPiece] |= (1L << epSq);
            if (didCastle) {
                int rook = whiteToMove ? WR : BR;
                pieceBitboards[rook] |=  (1L << rookFrom);
                pieceBitboards[rook] &= ~(1L << rookTo);
            }
            this.currentHash    = oldHash;
            this.castlingRights = oldRights;
            this.enPassantSquare = oldEpSq;
            updateOccupancy();
            return false;
        }

        int kingSq = Long.numberOfTrailingZeros(kingBB);
        if (MoveGenerator.isSquareAttacked(this, kingSq, !whiteToMove)) {
            // undo everything
            pieceBitboards[mover] |= fromMask;
            if (hasPromo) pieceBitboards[promo] &= ~toMask;
            else pieceBitboards[mover] &= ~toMask;
            if (victim != -1) pieceBitboards[victim] |= toMask;
            if (epSq != -1) pieceBitboards[epPiece] |= (1L << epSq);
            if (didCastle) {
                int rook = whiteToMove ? WR : BR;
                pieceBitboards[rook] |=  (1L << rookFrom);
                pieceBitboards[rook] &= ~(1L << rookTo);
            }
            this.currentHash    = oldHash;
            this.castlingRights = oldRights;
            this.enPassantSquare = oldEpSq;
            updateOccupancy();
            return false;
        }

        whiteToMove = !whiteToMove;
        this.currentHash = Zobrist.calculateHash(this);
        return true;
    }

    public void importFEN(String fen) {
        Arrays.fill(pieceBitboards, 0L);
        String[] parts = fen.split(" ");
        String[] rows  = parts[0].split("/");
        for (int r = 0; r < 8; r++) {
            int rank = 7 - r, file = 0;
            for (char c : rows[r].toCharArray()) {
                if (Character.isDigit(c)) { file += Character.getNumericValue(c); }
                else {
                    int pType = charToPiece(c);
                    if (pType != -1) pieceBitboards[pType] |= (1L << (rank * 8 + file));
                    file++;
                }
            }
        }
        whiteToMove = parts[1].equals("w");
        updateOccupancy();
        this.castlingRights  = 15;
        this.enPassantSquare = -1;
        this.currentHash     = Zobrist.calculateHash(this);
    }

    private int charToPiece(char c) {
        switch (c) {
            case 'P': return WP; case 'N': return WN; case 'B': return WB;
            case 'R': return WR; case 'Q': return WQ; case 'K': return WK;
            case 'p': return BP; case 'n': return BN; case 'b': return BB;
            case 'r': return BR; case 'q': return BQ; case 'k': return BK;
            default:  return -1;
        }
    }

    public void printBoard() {
        System.out.println("\n  +---+---+---+---+---+---+---+---+");
        for (int r = 7; r >= 0; r--) {
            System.out.print((r + 1) + " | ");
            for (int f = 0; f < 8; f++) {
                int sq = r * 8 + f;
                char sym = '.';
                for (int i = 0; i < 12; i++) {
                    if ((pieceBitboards[i] & (1L << sq)) != 0) { sym = pieceChar(i); break; }
                }
                System.out.print(sym + " | ");
            }
            System.out.println();
            System.out.println("  +---+---+---+---+---+---+---+---+");
        }
        System.out.println("    a   b   c   d   e   f   g   h\n");
        System.out.println("Turn: " + (whiteToMove ? "White" : "Black"));
        System.out.println("Zobrist: 0x" + Long.toHexString(currentHash).toUpperCase());
    }

    public boolean isPseudoLegal(String input) {
        if (input == null) return false;
        String raw = input.trim();
        String found = null;
        for (int i = 0; i + 4 <= raw.length(); i++) {
            String sub = raw.substring(i, i + 4).toLowerCase();
            boolean ok = sub.charAt(0) >= 'a' && sub.charAt(0) <= 'h'
                && sub.charAt(1) >= '1' && sub.charAt(1) <= '8'
                && sub.charAt(2) >= 'a' && sub.charAt(2) <= 'h'
                && sub.charAt(3) >= '1' && sub.charAt(3) <= '8';
            if (ok) {
                if (i + 5 <= raw.length()) {
                    char p = Character.toLowerCase(raw.charAt(i + 4));
                    if (p == 'q' || p == 'r' || p == 'b' || p == 'n') {
                        found = raw.substring(i, i + 5).toLowerCase(); break;
                    }
                }
                found = sub; break;
            }
        }
        if (found == null) return false;
        return engine.MoveGenerator.generatePseudoLegalMoves(this).contains(found);
    }

    private char pieceChar(int type) {
        char[] s = {'P','N','B','R','Q','K','p','n','b','r','q','k'};
        return s[type];
    }
}