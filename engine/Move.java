package engine;

public class Move {
    public final int fromSq;
    public final int toSq;
    public final char promotion;

    public Move(int from, int to) {
        fromSq = from; toSq = to; promotion = ' ';
    }

    public Move(int from, int to, char promo) {
        fromSq = from; toSq = to; promotion = promo;
    }

    public static Move fromString(String uci) {
        if (uci == null || uci.length() < 4) return null;
        int from = (uci.charAt(0) - 'a') + (uci.charAt(1) - '1') * 8;
        int to   = (uci.charAt(2) - 'a') + (uci.charAt(3) - '1') * 8;
        char p   = uci.length() == 5 ? Character.toLowerCase(uci.charAt(4)) : ' ';
        return new Move(from, to, p);
    }

    @Override
    public String toString() {
        String s = coord(fromSq) + coord(toSq);
        if (promotion != ' ') s += promotion;
        return s;
    }

    private static String coord(int sq) {
        return "" + (char)('a' + sq % 8) + (char)('1' + sq / 8);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Move)) return false;
        Move o = (Move) obj;
        return fromSq == o.fromSq && toSq == o.toSq && promotion == o.promotion;
    }

    @Override
    public int hashCode() { return 31 * (31 * fromSq + toSq) + promotion; }
}