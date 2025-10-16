package wordlewrangler;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@SuppressWarnings("NullableProblems")
public sealed interface Constraint extends Comparable<Constraint> {

    static List<Constraint> parse(Word guess, String spec) {
        return IntStream.range(0, spec.length()).<Constraint>mapToObj(i -> {
                char type = spec.charAt(i);
                char c = guess.charAt(i);
                return switch (type) {
                    case 'F' -> new Fixed(c, i);
                    case 'P' -> new Present(c, i);
                    case 'U' -> new Unused(c);
                    default -> throw new IllegalArgumentException("Invalid constraint spec: " + spec);
                };
            })
            .distinct()
            .toList();
    }

    @Override
    default int compareTo(Constraint o) {
        return Character.compare(c(), o.c());
    }

    char c();

    default IntStream resolved() {
        return IntStream.empty();
    }

    boolean excludes(Word word);

    private static String toStrings(List<?> excluded1) {
        return excluded1.stream()
            .map(Object::toString)
            .collect(Collectors.joining(","));
    }

    record Fixed(char c, List<Integer> positions) implements Constraint {

        public Fixed(char c, Integer position) {
            this(c, List.of(position));
        }

        @Override
        public boolean excludes(Word word) {
            return positions.stream()
                .anyMatch(position ->
                    word.charAt(position) != c);
        }

        @Override
        public IntStream resolved() {
            return positions.stream().mapToInt(i -> i);
        }

        @Override
        public String toString() {
            return "[✅" + c + " " + toStrings(positions) + "]";
        }
    }

    record Present(char c, List<Integer> excluded) implements Constraint {

        public Present(char c, int excluded) {
            this(c, List.of(excluded));
        }

        @Override
        public boolean excludes(Word word) {
            if (!word.contains(c)) {
                return true;
            }
            if (word.containsAll(c, excluded)) {
                return true;
            }
            return word.chars().noneMatch(wordChar -> c == wordChar);
        }

        @Override
        public String toString() {
            return "[🟨" + c + "🚫 " + toStrings(excluded) + "]";
        }
    }

    record Unused(char c) implements Constraint {

        @Override
        public boolean excludes(Word word) {
            return word.chars().anyMatch(wordChar -> wordChar == c);
        }

        @Override
        public String toString() {
            return "[❌" + c + "]";
        }
    }
}
