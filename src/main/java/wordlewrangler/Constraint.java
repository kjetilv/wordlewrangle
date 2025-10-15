package wordlewrangler;

import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("NullableProblems")
public sealed interface Constraint extends Comparable<Constraint> {

    @Override
    default int compareTo(Constraint o) {
        return Character.compare(c(), o.c());
    }

    char c();

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
