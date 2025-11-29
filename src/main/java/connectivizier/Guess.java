package connectivizier;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public record Guess(Collection<Word> words) {

    public Guess(Collection<Word> words) {
        if (Objects.requireNonNull(words, "words").size() != 4) {
            throw new IllegalArgumentException("Must be four words: " + words);
        }
        if (words.stream()
                .map(Word::key)
                .distinct()
                .count() != 4
        ) {
            throw new IllegalArgumentException("Must be four distinct words: " + words);
        }
        this.words = words.stream().sorted().toList();
    }

    public boolean containsAny(Guess attempt) {
        return !overlap(attempt).isEmpty();
    }

    public List<Word> overlap(
        Collection<Word> words
    ) {
        return words.stream()
            .filter(this::contains)
            .toList();
    }

    public String toShortString() {
        return words.stream()
            .map(Word::key)
            .map(c -> Character.toString(c))
            .sorted()
            .collect(Collectors.joining());
    }

    List<Word> overlap(Guess attempt) {
        return overlap(attempt.words);
    }

    private boolean contains(Word word) {
        return words.contains(word);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Guess guess && guess.toShortString().equals(toShortString());
    }

    @Override
    public int hashCode() {
        return toShortString().hashCode();
    }
}
