package wordlewrangler;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@SuppressWarnings("NullableProblems")
public sealed interface Constraint extends Comparable<Constraint> {

    int[] ALL_POSITIONS = {0, 1, 2, 3, 4};

    static List<Constraint> parse(Word guess, String spec) {
        var list = IntStream.range(0, spec.length()).<Constraint>mapToObj(i -> {
                var type = spec.charAt(i);
                var c = guess.charAt(i);
                return switch (type) {
                    case 'F' -> new Found(c, i);
                    case 'P' -> new Present(c, i);
                    case 'U' -> new Unused(c, i);
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

    default IntStream foundPositions() {
        return IntStream.empty();
    }

    default OptionalInt foundChar() {
        return OptionalInt.empty();
    }

    default Optional<Constraint> clearFound(Set<Integer> found) {
        return Optional.of(this);
    }

    Constraint merge(Constraint constraint);

    char c();

    int[] positions();

    boolean excludes(Word word);

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

    private static Optional<int[]> remove(int[] positions, Set<Integer> found) {
        return Optional.of(IntStream.of(positions)
                .filter(pos -> !found.contains(pos))
                .toArray()
            )
            .filter(array -> array.length > 0);
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
                if (word.charAt(position) != c) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public IntStream foundPositions() {
            return IntStream.of(positions);
        }

        @Override
        public OptionalInt foundChar() {
            return OptionalInt.of(c);
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
        public Optional<Constraint> clearFound(Set<Integer> found) {
            return remove(positions, found)
                .map(remaining ->
                    new Present(c, remaining));
        }

        @Override
        public boolean excludes(Word word) {
            return !word.containsAt(positions, c);
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
                return new Unused(c, Constraint.combine(this.positions, pos));
            }
            throw new IllegalStateException(this + " cannot merge with  " + constraint);
        }

        @Override
        public Optional<Constraint> clearFound(Set<Integer> found) {
            return remove(positions, found)
                .map(remaining ->
                    new Unused(c, remaining));
        }

        @Override
        public boolean excludes(Word word) {
            for (int position : positions) {
                if (word.charAt(position) == c) {
                    return true;
                }
            }
            return false;
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
