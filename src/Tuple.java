public class Tuple<X, Y> {
    public final X x;
    public final Y y;

    public Tuple(X x, Y y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return "(" + x + "," + y + ")";
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof Tuple)) {
            return false;
        }

        Tuple<X, Y> other_ = (Tuple<X, Y>) other;

        // this may cause NPE if nulls are valid values for x or y. The logic may be improved to handle nulls properly, if needed.
        return other_.x.equals(this.x) && other_.y.equals(this.y);
    }
}