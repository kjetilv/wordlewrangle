package connectivizier;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@SuppressWarnings("NullableProblems")
public record Word(char key, String word) implements Comparable<Word> {

    public static String printRow(Collection<Word> row) {
        return printRow(row, 0);
    }

    public static String printRow(Collection<Word> row, int width) {
        return row.stream()
            .map(word -> word.string(width))
            .collect(Collectors.joining(" "));
    }

    @Override
    public int compareTo(Word o) {
        return Character.compare(key, o.key);
    }

    String string(int width) {
        var mySize = size();
        int missing = Math.max(0, width - mySize);
        return "[" + key + " " + spaces(missing) + word + "]";
    }

    int size() {
        return word.length() + "[0 ]".length();
    }

    private static String spaces(int missing) {
        return missing == 0
            ? ""
            : IntStream.range(0, missing).mapToObj(_ -> " ")
                .collect(Collectors.joining());
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Word w && Objects.equals(key, w.key);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(key);
    }

    @Override
    public String toString() {
        return string(0);
    }
}
