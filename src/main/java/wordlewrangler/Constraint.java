package wordlewrangler;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@SuppressWarnings("NullableProblems")
public sealed interface Constraint extends Comparable<Constraint> {

    @Override
    default int compareTo(Constraint o) {
        return Character.compare(c(), o.c());
    }

    default int[] foundPositions() {
        return Constraints.NOWHERE;
    }

    default Constraint clearFound(Set<Integer> found) {
        return this;
    }

    Constraint merge(Constraint constraint);

    char c();

    int[] positions();

    boolean eliminates(char[] letters);

    private static String toStrings(int[] ints) {
        return ints == null || ints.length == 0 ? "[]" : IntStream.of(ints)
                                                         .mapToObj(String::valueOf)
                                                         .collect(Collectors.joining(","));
    }

    private static int[] combine(int[] ps1, int[] ps2) {
        int[] ps = new int[ps1.length + ps2.length];
        System.arraycopy(ps1, 0, ps, 0, ps1.length);
        System.arraycopy(ps2, 0, ps, ps1.length, ps2.length);
        return ps;
    }

    private static int[] remove(int[] positions, Set<Integer> removals) {
        if (removals.isEmpty()) {
            return positions;
        }
        int[] remaining = new int[positions.length];
        int r = 0;
        for (int p = 0; p < positions.length; p++) {
            if (!removals.contains(positions[p])) {
                remaining[r++] = positions[p];
            }
        }
        if (r == positions.length) {
            return remaining;
        }
        if (r == 0) {
            return null;

        }
        int[] subRemaining = new int[r];
        System.arraycopy(remaining, 0, subRemaining, 0, r);
        return subRemaining;
    }

    record Green(char c, int[] positions) implements Constraint {

        public Green(char c, int position) {
            this(c, new int[] {position});
        }

        @Override
        public Constraint merge(Constraint constraint) {
            if (constraint instanceof Green(var fc, var pos) && fc == c) {
                return new Green(c, combine(positions, pos));
            }
            throw new IllegalStateException(this + " cannot merge with  " + constraint);
        }

        @Override
        public boolean eliminates(char[] letters) {
            for (int position : positions) {
                if (letters[position] != c) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public int[] foundPositions() {
            return positions;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof Green(var fc, var ps) && c == fc &&
                   Arrays.equals(positions, ps);
        }

        @Override
        public int hashCode() {
            return Objects.hash(c, Arrays.hashCode(positions));
        }

        @Override
        public String toString() {
            return "[✅" + c + " " + toStrings(positions) + "]";
        }
    }

    record Yellow(char c, int[] positions) implements Constraint {

        public Yellow(char c, int excluded) {
            this(c, new int[] {excluded});
        }

        public Yellow(char c, int excluded1, int excluded2) {
            this(c, new int[] {excluded1, excluded2});
        }

        @Override
        public Constraint merge(Constraint constraint) {
            if (constraint instanceof Yellow(var pc, var pos) && pc == c) {
                return new Yellow(c, Constraint.combine(positions, pos));
            }
            if (constraint instanceof Grey(var pc, var pos) && pc == c) {
                return new Yellow(c, Constraint.combine(positions, pos));
            }
            throw new IllegalStateException(this + " cannot merge with  " + constraint);
        }

        @Override
        public Constraint clearFound(Set<Integer> found) {
            var removed = remove(positions, found);
            return removed == null ? null : new Yellow(c, removed);
        }

        @Override
        public boolean eliminates(char[] letters) {
            if (positions.length == 1) {
                return letters[positions[0]] == c;
            }
            boolean[] foundPositions = new boolean[5];
            var found = false;
            for (int i = 0; i < foundPositions.length; i++) {
                if (letters[i] == c) {
                    foundPositions[i] = true;
                    found = true;
                }
            }
            if (!found) {
                return true;
            }
            for (int position : positions) {
                if (foundPositions[position]) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof Yellow(var pc, var ps) && c == pc && Arrays.equals(positions, ps);
        }

        @Override
        public int hashCode() {
            return Objects.hash(c, Arrays.hashCode(positions));
        }

        @Override
        public String toString() {
            return "[🟨" + c + "🚫 " + toStrings(positions) + "]";
        }

    }

    record Grey(char c, int[] positions) implements Constraint {

        public Grey(char c) {
            this(c, Constraints.ALL_POSITIONS);
        }

        public Grey(char c, int index) {
            this(c, new int[] {index});
        }

        @Override
        public Constraint merge(Constraint constraint) {
            if (constraint instanceof Grey(var uc, var pos) && uc == c) {
                if (positions.length == 5) {
                    return this;
                }
                if (pos.length == 5) {
                    return new Grey(c);
                }
                return new Grey(c, Constraint.combine(this.positions, pos));
            }
            throw new IllegalStateException(this + " cannot merge with  " + constraint);
        }

        @Override
        public Constraint clearFound(Set<Integer> found) {
            var removed = remove(positions, found);
            return removed == null ? null : new Grey(c, removed);
        }

        @Override
        public boolean eliminates(char[] letters) {
            for (int position : positions) {
                if (letters[position] == c) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof Grey(var uc, var ps) && c == uc && Arrays.equals(positions, ps);
        }

        @Override
        public int hashCode() {
            return Objects.hash(c, Arrays.hashCode(positions));
        }

        @Override
        public String toString() {
            var length = positions.length;
            return "[️🔘️" + c + (length == 0 || length == 5
                ? ""
                : " " + toStrings(positions)) + "]";
        }
    }
}
