package wordlewrangler;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public record Word(char[] letters) implements Comparable<Word> {

    public static List<Word> list(String file) {
        try (
            var lines = Files.lines(Path.of(file));
            var words = lines
                .map(line -> line.split("\\s+"))
                .flatMap(Arrays::stream)
                .filter(s -> !s.isBlank())
                .map(String::trim)
                .map(String::toUpperCase)
                .distinct()
                .map(Word::of)
                .sorted()
        ) {
            return Stream.concat(Stream.of(Word.of("SLATE")), words)
                .toList();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to read " + file);
        }
    }

    static Word of(String string) {
        if (string.length() != 5) {
            throw new IllegalArgumentException("Not a five-letter word: " + string);
        }
        return new Word(string.toCharArray());
    }

    public Word(String string) {
        this(string.toCharArray());
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

    public boolean contains(char c, List<Integer> excluded) {
        for (Integer exc : excluded) {
            if (letters[exc] == c) {
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
        return letters()[index];
    }

    public Constraint indexOf(IndexedChar indexedChar) {
        for (var index = 0; index < letters.length; index++) {
            if (letters[index] == indexedChar.c()) {
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

    private boolean matchAt(IndexedChar ic, int i) {
        return letters[i] == ic.c();
    }

    @Override
    public String toString() {
        return chars().map(Object::toString)
            .collect(Collectors.joining(""));
    }
}
