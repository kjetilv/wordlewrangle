package wordlewrangler;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public record Word(char[] letters) implements Comparable<Word> {

    public static List<Word> fromFile(String path) {
        return fromFile(Path.of(path));
    }

    public static List<Word> fromFile(Path path) {
        return fromFile(path, false);
    }

    public static List<Word> fromFile(Path path, boolean filter) {
        try (
            var lines = Files.lines(path);
            var words = words(lines, filter)
        ) {
            return Stream.concat(
                    Stream.of(
                        new Word("SLATE"),
                        new Word("CLASP")
                    ), words
                )
                .toList();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to read " + path, e);
        }
    }

    public static List<Word> words(String string) {
        return words(Arrays.stream(string.split("\\s+")))
            .toList();
    }

    public static Stream<Word> words(Stream<String> lines) {
        return words(lines, false);
    }

    public static Stream<Word> words(Stream<String> lines, boolean filter) {
        return lines
            .map(line -> line.split("\\s+"))
            .flatMap(Arrays::stream)
            .filter(s -> !s.isBlank())
            .map(String::trim)
            .map(String::toUpperCase)
            .distinct()
            .map(String::toCharArray)
            .flatMap(ls ->
                filter && invalid(ls)
                    ? Stream.empty()
                    : Stream.of(new Word(ls)))
            .sorted();
    }

    public Word(String word) {
        this(word.toUpperCase(Locale.ROOT).toCharArray());
    }

    public Word {
        if (invalid(letters)) {
            throw new IllegalArgumentException("Not a five-letter word: " + new String(letters));
        }
    }

    public int length() {
        return letters.length;
    }

    @Override
    public int compareTo(Word o) {
        return toString().compareTo(o.toString());
    }

    public boolean contains(char c) {
        for (char letter : letters) {
            if (letter == c) {
                return true;
            }
        }
        return false;
    }

    public boolean containsAt(char c, int[] positions) {
        if (positions.length == 1) {
            return letters[positions[0]] == c;
        }
        boolean[] matches = new boolean[5];
        var found = false;
        for (int i = 0; i < matches.length; i++) {
            if (letters[i] == c) {
                matches[i] = true;
                found = true;
            }
        }
        if (!found) {
            return false;
        }
        for (int position : positions) {
            if (matches[position]) {
                return true;
            }
        }
        return false;
    }

    public Stream<IndexedChar> indexedChars() {
        return IntStream.range(0, letters.length)
            .mapToObj(i -> new IndexedChar(i, letters[i]));
    }

    public char charAt(int index) {
        return letters[index];
    }

    public Constraint constraintFor(char c, int index) {
        for (var i = 0; i < letters.length; i++) {
            if (charAt(i) == c) {
                if (i == index) {
                    return new Constraint.Found(c, index);
                }
                return new Constraint.Present(c, index);
            }
        }
        return new Constraint.Unused(c, index);
    }

    static final int SIZE = 5;

    private static boolean invalid(char[] letters) {
        var str = Objects.requireNonNull(letters, "letters");
        if (str.length != SIZE) {
            return true;
        }
        for (char c : letters) {
            if (!Character.isAlphabetic(c)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Word(var other) && Arrays.equals(this.letters, other);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(letters);
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public String toString() {
        return new String(letters);
    }
}
