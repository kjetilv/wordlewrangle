package connectivizier;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Gatherers;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Collections.emptySet;

public record Connections(List<Word> words, List<Attempt> attempts) {

    public static Connections create(String words) {
        var splits =
            Arrays.stream(words.split("\\s+"))
                .map(String::trim)
                .distinct()
                .toList();
        if (splits.size() == 16) {
            return new Connections(
                IntStream.range(0, 16)
                    .mapToObj(i ->
                        new Word(KEYS[i], splits.get(i)))
                    .toList(),
                List.of()
            );
        }
        throw new IllegalArgumentException("Must have 16 words, got " + splits.size() + ": " + words);
    }

    public Connections {
        if (words.size() != 16) {
            throw new IllegalArgumentException("Must have 16 words, got " + words.size() + ": " + words);
        }
    }

    public List<Attempt> blockers(Guess guess) {
        var overlap = guess.overlap(found());
        if (overlap.isEmpty()) {
            return attempts.stream()
                .filter(attempt ->
                    attempt.blocks(guess))
                .toList();
        }
        var overlapString = overlap.stream()
            .map(Word::word)
            .collect(Collectors.joining(", "));
        throw new IllegalArgumentException("Guess " + guess + " contains already found word: " + overlapString);
    }

    public Collection<Word> found() {
        return attempts.stream()
            .map(Attempt::found)
            .flatMap(Collection::stream)
            .collect(Collectors.toSet());
    }

    public Connections add(String codedGuess) {
        return this;
    }

    public Connections add(Guess guess, Attempt attempt) {
        return new Connections(
            words,
            Stream.concat(
                    attempts.stream(),
                    Stream.of(attempt)
                )
                .toList()
        );
    }

    public String string() {
        int width = words.stream().mapToInt(Word::size).max().orElseThrow();
        var wordStream = unguessed();
        var guessedRows = attempts.stream()
            .filter(Attempt::isCorrect)
            .map(Attempt::found)
            .map(words ->
                Word.printRow(words, width))
            .toList();
        var unguessedRows = wordStream.stream()
            .gather(Gatherers.windowFixed(4))
            .map(row ->
                Word.printRow(row, width))
            .toList();
        return Stream.of(guessedRows, unguessedRows)
            .flatMap(Collection::stream)
            .collect(Collectors.joining("\n"));
    }

    public Stream<Guess> guesses() {
        var unguessed = unguessed();
        return unguessed.stream().flatMap(word ->
            guesses(
                copyAndAdd(emptySet(), word),
                copyAndRemove(unguessed, word)
            ));
    }

    private Collection<Word> unguessed() {
        var guessed = attempts.stream()
            .filter(Attempt::isCorrect)
            .map(Attempt::found)
            .flatMap(Collection::stream)
            .collect(Collectors.toSet());
        return words.stream()
            .filter(word -> !guessed.contains(word))
            .collect(Collectors.toSet());
    }

    private static final char[] KEYS = {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
    };

    private static Stream<Guess> guesses(
        Collection<Word> collector,
        Collection<Word> remaining
    ) {
        if (collector.size() == 4) {
            return Stream.of(new Guess(collector));
        }
        return remaining.stream().flatMap(rem ->
            guesses(
                copyAndAdd(collector, rem),
                copyAndRemove(remaining, rem)
            ));
    }

    private static Collection<Word> copyAndRemove(
        Collection<Word> collector,
        Word word
    ) {
        var copy = new HashSet<>(collector);
        var remove = copy.remove(word);
        assert remove : "Not removed: " + word + " from " + collector;
        return Collections.unmodifiableSet(copy);
    }

    private static Collection<Word> copyAndAdd(
        Collection<Word> collector,
        Word rem
    ) {
        var copy = new HashSet<>(collector);
        var add = copy.add(rem);
        assert add : "Not added: " + rem + " to " + collector;
        return Collections.unmodifiableSet(copy);
    }
}
