package ai;

public class TranspositionTable {

    public static final byte EXACT = 0;
    private int storeCount = 0;
    
public static final byte ALPHA = 1;
    public static final byte BETA = 2;
    private final TTEntry[] table;
    private final int mask;

    // each thingy in the table
    public static class TTEntry {

        public long zobristKey;
        public int score;;
        public int depth;
        public byte flags;
        public String bestMove;

        public TTEntry(long k, int sc, int d, byte fl, String bM) {

            zobristKey = key;
            score = sc;
            depth = d;
            bestMove = bM;
            flags = fl;
        }
    }


    public TranspositionTable(int sizePow) {

        int size = 1 << sizePow;

        mask = size - 1;
        table = new TTEntry[size];
    }
    public void put(long key, int score, int depth, byte flags, String bestMove) {
        // using one & cuz its bit stuff :P
        int idx = (int) (key & mask);

        TTEntry e = table[idx];

        // keep deeper one
        if (e != null) {
            if (depth < e.depth)
                return;
        }

        table[idx] = new TTEntry(
            key,
            score,
            depth,
            flags,
            bestMove
        );

        // basically trkacing  depth
        storeCount++;
    }

    public TTEntry get(long key) {

        // tt lookup
        TTEntry e = table[(int)(key & mask)];

        if (e == null) return null;

        // collision
        if (e.zobristKey != key)
            return null;

        return e;
    }

}