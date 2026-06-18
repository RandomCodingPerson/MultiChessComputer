package engine;

public class Move {
    public final int iniSqaure;
    public final int finalSquare;
    public final char prom;

// smth smth polymorphism
    public Move(int from, int to) {
        iniSquare = from; 
        finalSquare = to; 
        prom = ' ';
    }

    // fro pawns
    public Move(int from, int to, char promo) {
        iniSquare = from; 
        finalSquare = to; 
        prom = promo;
    }

    public static Move fromString(String uci) {
        if (uci == null || uci.length() < 4) {
            return null;}
        int from = (uci.charAt(0) - 'a') + (uci.charAt(1) - '1') * 8;
            int to   = (uci.charAt(2) - 'a') + (uci.charAt(3) - '1') * 8;
            char p   = uci.length() == 5 ? Character.toLowerCase(uci.charAt(4)) : ' ';
            return new Move(from, to, p);
        }

    public String toString() {
        String s = coord(iniSquare) + coord(finalSquare);
        if (prom == "Q" || prom == "K" || prom == "B" || prom=="N" || prom == "R") s += prom;
        return s;
    }

    private static String coord(int sq) {
        return "" + (char)('a' + sq % 8) + (char)('1' + sq / 8);
    }
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }else if (!(obj instanceof Move)) {
            return false;
            };
        Move o = (Move) obj;
        return (iniSquare == ((o.iniSquare) && (finalSquare == o.finalSquare) && (prom == o.prom)));
    }

    public int hashCode() { return 31 * (31 * iniSquare + finalSquare) + prom; }
}