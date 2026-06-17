package ai;

public class TranspositionTable {

    public static final byte EXACT = 0;
    public static final byte ALPHA = 1;
    public static final byte BETA = 2;

    private int storeCount = 0;

    public static class TTEntry {
        public long zobristKey;
        public int score;
        public int depth;
        public byte flags;
        public String bestMove;

        public TTEntry(long key, int score, int depth, byte flags, String bestMove) {
            this.zobristKey = key;
            this.score = score;
            this.depth = depth;
            this.flags = flags;
            this.bestMove = bestMove;
        }
    }

    private final TTEntry[] table;
    private final int mask;

    public TranspositionTable(int sizePow) {
        int sz = 1 << sizePow;
        table = new TTEntry[sz];
        mask = sz - 1;
    }

    public void put(long key, int score, int depth, byte flags, String bestMove) {
        int idx = (int)(key & mask);
        if (table[idx] == null || depth >= table[idx].depth) {
            table[idx] = new TTEntry(key, score, depth, flags, bestMove);
            storeCount++;
        }
    }

    public TTEntry get(long key) {
        int idx = (int)(key & mask);
        TTEntry e = table[idx];
        if (e != null && e.zobristKey == key) return e;
        return null;
    }
}