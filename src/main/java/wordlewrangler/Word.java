package wordlewrangler;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public record Word(String letters) implements Comparable<Word> {

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
            .flatMap(ls ->
                filter && invalid(ls)
                    ? Stream.empty()
                    : Stream.of(new Word(ls)))
            .sorted();
    }

    public Word {
        if (invalid(letters)) {
            throw new IllegalArgumentException("Not a five-letter word: " + letters);
        }
    }

    public int length() {
        return letters.length();
    }

    @Override
    public int compareTo(Word o) {
        return toString().compareTo(o.toString());
    }

    public boolean contains(char c) {
        return letters.indexOf(c) >= 0;
    }

    public boolean containsAll(char c, List<Integer> excluded) {
        return IntStream.range(0, letters.length())
            .filter(index -> charAt(index) == c)
            .allMatch(excluded::contains);
    }

    public Stream<IndexedChar> indexedChars() {
        return IntStream.range(0, letters.length())
            .mapToObj(i -> new IndexedChar(i, letters.charAt(i)));
    }

    public char charAt(int index) {
        return letters().charAt(index);
    }

    public Constraint constraintFor(IndexedChar indexedChar) {
        for (var index = 0; index < letters.length(); index++) {
            if (charAt(index) == indexedChar.c()) {
                if (index == indexedChar.index()) {
                    return new Constraint.Fixed(indexedChar.c(), indexedChar.index());
                }
                return new Constraint.Present(indexedChar.c(), indexedChar.index());
            }
        }
        return new Constraint.Unused(indexedChar.c());
    }

    Stream<Character> chars() {
        return indexedChars().map(IndexedChar::c);
    }

    static final int SIZE = 5;

    private static boolean invalid(String letters) {
        var str = Objects.requireNonNull(letters, "letters");
        return str.length() != SIZE || !str.chars().allMatch(Character::isAlphabetic);
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public String toString() {
        return chars().map(Object::toString)
            .collect(Collectors.joining(""));
    }
}
