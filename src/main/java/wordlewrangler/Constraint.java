package wordlewrangler;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@SuppressWarnings("NullableProblems")
public sealed interface Constraint extends Comparable<Constraint> {

    int[] ALL_POSITIONS = {0, 1, 2, 3, 4};

    static List<Constraint> parse(Word guess, String spec) {
        var list = IntStream.range(0, spec.length()).<Constraint>mapToObj(i -> {
                var type = spec.charAt(i);
                var c = guess.letters()[i];
                return switch (type) {
                    case 'F' -> new Found(c, i);
                    case 'P' -> new Present(c, i);
                    case 'U' -> new Unused(c);
                    default -> throw new IllegalArgumentException("Invalid constraint spec: " + spec);
                };
            })
            .toList();
        return list.stream().distinct()
            .toList();
    }

    @Override
    default int compareTo(Constraint o) {
        return Character.compare(c(), o.c());
    }

    default int[] foundPositions() {
        return NONE;
    }

    default OptionalInt foundChar() {
        return OptionalInt.empty();
    }

    default Constraint clearFound(Set<Integer> found) {
        return this;
    }

    Constraint merge(Constraint constraint);

    char c();

    int[] positions();

    boolean excludes(Word word);

    int[] NONE = new int[0];

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

    private static int[] remove(int[] positions, Set<Integer> found) {
        var removed = IntStream.of(positions)
            .filter(pos -> !found.contains(pos))
            .toArray();
        return removed.length == 0 ? null : removed;
    }

    record Found(char c, int[] positions) implements Constraint {

        public Found(char c, int position) {
            this(c, new int[] {position});
        }

        @Override
        public Constraint merge(Constraint constraint) {
            if (constraint instanceof Found(var fc, var pos) && fc == c) {
                return new Found(c, combine(positions, pos));
            }
            throw new IllegalStateException(this + " cannot merge with  " + constraint);
        }

        @Override
        public boolean excludes(Word word) {
            for (int position : positions) {
                if (word.letters()[position] != c) {
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
        public OptionalInt foundChar() {
            return OptionalInt.of(c);
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof Found(var fc, var ps) && c == fc &&
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

    record Present(char c, int[] positions) implements Constraint {

        public Present(char c, int excluded) {
            this(c, new int[] {excluded});
        }

        @Override
        public Constraint merge(Constraint constraint) {
            if (constraint instanceof Present(var pc, var pos) && pc == c) {
                return new Present(c, Constraint.combine(positions, pos));
            }
            throw new IllegalStateException(this + " cannot merge with  " + constraint);
        }

        @Override
        public Constraint clearFound(Set<Integer> found) {
            var removed = remove(positions, found);
            return removed == null ? null : new Present(c, removed);
        }

        @Override
        public boolean excludes(Word word) {
            return word.containsAt(c, positions);
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof Present(var pc, var ps) && c == pc && Arrays.equals(positions, ps);
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

    record Unused(char c, int[] positions) implements Constraint {

        public Unused(char c) {
            this(c, ALL_POSITIONS);
        }

        public Unused(char c, int index) {
            this(c, new int[] {index});
        }

        @Override
        public Constraint merge(Constraint constraint) {
            if (constraint instanceof Unused(var uc, var pos) && uc == c) {
                if (positions.length == 5) {
                    return this;
                }
                if (pos.length == 5) {
                    return new Unused(c);
                }
                return new Unused(c, Constraint.combine(this.positions, pos));
            }
            throw new IllegalStateException(this + " cannot merge with  " + constraint);
        }

        @Override
        public Constraint clearFound(Set<Integer> found) {
            var removed = remove(positions, found);
            return removed == null ? null : new Unused(c, removed);
        }

        @Override
        public boolean excludes(Word word) {
            var letters = word.letters();
            for (int position : positions) {
                if (letters[position] == c) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof Unused(var uc, var ps) && c == uc && Arrays.equals(positions, ps);
        }

        @Override
        public int hashCode() {
            return Objects.hash(c, Arrays.hashCode(positions));
        }

        @Override
        public String toString() {
            var length = positions.length;
            return "[❌" + c + (length == 0 || length == 5
                ? ""
                : " " + toStrings(positions)) + "]";
        }
    }
}
