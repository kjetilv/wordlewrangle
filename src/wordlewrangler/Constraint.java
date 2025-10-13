package wordlewrangler;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("NullableProblems")
public sealed interface Constraint extends Comparable<Constraint> {

    @Override
    default int compareTo(Constraint o) {
        return Character.compare(c(), o.c());
    }

    char c();

    boolean excludes(Word word);

    Constraint merge(Constraint other);

    record Fixed(char c, int index) implements Constraint {

        @Override
        public Constraint merge(Constraint other) {
            return this;
        }

        @Override
        public boolean excludes(Word word) {
            return word.charAt(index) != c;
        }

        @Override
        public String toString() {
            return "[✅" + c + "=" + index + "]";
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
            if (word.contains(c, excluded)) {
                return true;
            }
            return word.chars().noneMatch(wordChar -> wordChar == c);
        }

        @Override
        public Constraint merge(Constraint other) {
            return switch (other) {
                case Fixed fixed -> fixed;
                case Present present -> new Present(
                    c,
                    merge(present)
                );
                default -> throw new IllegalStateException("Unexpected value: " + other);
            };
        }

        private List<Integer> merge(Present present) {
            return Stream.concat(
                    excluded.stream(),
                    present.excluded.stream()
                )
                .distinct()
                .sorted()
                .toList();
        }

        @Override
        public String toString() {
            var notAt = excluded.stream()
                .map(Object::toString)
                .collect(Collectors.joining(","));
            return "[🟨" + c + " 🚫 " + notAt + "]";
        }
    }

    record Unused(char c) implements Constraint {

        @Override
        public Constraint merge(Constraint other) {
            return this;
        }

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
