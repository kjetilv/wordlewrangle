package wordlewrangler;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record Game(
    Word word,
    List<Word> candidates,
    List<Constraint> constraints,
    List<Word> guesses
) {

    public Game(List<Word> candidates) {
        this(randomElement(candidates), candidates);
    }

    public Game(Word word, List<Word> candidates) {
        this(word, candidates, Collections.emptyList(), Collections.emptyList());
    }

    public Game guessWord() {
        return tryWord(randomElement(candidates));
    }

    public Game tryWord(Word guess) {
        return guessed(guess, constraintsGiven(guess));
    }

    public Optional<WordElim> hotCandidate() {
        List<WordElim> hotties = hotCandidates().toList();
        if (hotties.isEmpty()) {
            return Optional.empty();
        }
        int max = hotties.stream()
            .mapToInt(WordElim::eliminated).max()
            .orElse(0);
        List<WordElim> hottest = hotties.stream()
            .filter(wordElim -> wordElim.eliminated() == max)
            .toList();
        return Optional.of(randomElement(hottest));
    }

    public Stream<WordElim> hotCandidates() {
        return candidates.stream()
            .map(this::elimination)
            .sorted(
                Comparator.comparing(WordElim::eliminated).reversed()
            );
    }

    public boolean done() {
        return !guesses.isEmpty() && guesses.getLast().equals(word);
    }

    public Word lastGuess() {
        return guesses.getLast();
    }

    public WordElim elimination(Word word) {
        List<Constraint> resultConstraints = constraintsGiven(word);
        return new WordElim(word, stillViable(resultConstraints).size());
    }

    public Game guessed(Word guess, List<Constraint> newConstraints) {
        var newCandidates = stillViable(newConstraints);
        return new Game(
            word,
            newCandidates,
            newConstraints,
            Stream.concat(guesses.stream(), Stream.of(guess))
                .toList()
        );
    }

    private List<Constraint> constraintsGiven(Word guess) {
        return mergeConstraints(
            this.constraints,
            findConstraints(guess, word)
        );
    }

    private List<Word> stillViable(List<Constraint> constraints) {
        return candidates.stream()
            .filter(word ->
                constraints.stream().noneMatch(constraint ->
                    constraint.excludes(word)))
            .toList();
    }

    private static final Random RND = new Random();

    private static <T> T randomElement(List<T> list) {
        return list.get(RND.nextInt(list.size()));
    }

    @SafeVarargs
    private static List<Constraint> mergeConstraints(Collection<Constraint>... collections) {
        return mergeConstraints(Stream.of(collections).flatMap(Collection::stream));
    }

    private static List<Constraint> mergeConstraints(Stream<Constraint> constraintStream) {
        return constraintStream
            .collect(Collectors.groupingBy(Constraint::c))
            .entrySet()
            .stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> entry.getValue()
                    .stream().reduce(Constraint::merge).orElseThrow()
            ))
            .values()
            .stream()
            .sorted()
            .toList();
    }

    private static List<Constraint> findConstraints(Word guess, Word selected) {
        return guess.indexedChars()
            .map(selected::indexOf)
            .toList();
    }
}
