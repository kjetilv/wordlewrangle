package wordlewrangler;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("NullableProblems")
public record Game(
    Word word,
    List<Word> candidates,
    List<Constraint> constraints,
    List<Word> guesses
) {

    public Game(String data) {
        this(Word.words(data));
    }

    public Game(List<Word> candidates) {
        this(null, candidates);
    }

    public Game(Word word, List<Word> candidates) {
        this(word, candidates, Collections.emptyList(), Collections.emptyList());
    }

    public Game {
        if (Objects.requireNonNull(candidates, "candidates").isEmpty()) {
            throw new IllegalArgumentException("No candidates");
        }
        Objects.requireNonNull(constraints, "constraints");
        Objects.requireNonNull(guesses, "guesses");
    }

    public Game set(String word) {
        return set(new Word(word));
    }

    public Game set(Word word) {
        if (candidates.contains(word)) {
            return new Game(word, candidates, constraints, guesses);
        }
        if (!guesses.isEmpty()) {
            throw new IllegalStateException(this + " is already in progress!");
        }
        throw new IllegalArgumentException("No such candidate: " + word);
    }

    public Game guessWord() {
        return tryWord(randomElement(candidates));
    }

    public Game tryWord(String guess) {
        return tryWord(new Word(guess));
    }

    public Game tryWord(Word guess) {
        var guessConstraints = newConstraints(guess);
        return apply(guess, guessConstraints);
    }

    public WordElim hotCandidate() {
        var hotCandidates = hotCandidates();
        return hotCandidates.size() == 1
            ? hotCandidates.getFirst()
            : randomElement(hottestCandiadates());
    }

    public List<WordElim> hottestCandiadates() {
        List<WordElim> hotCandidates = hotCandidates();
        var maxElimination = maxEliminations(hotCandidates);
        return hotCandidates.stream()
            .filter(eliminates(maxElimination))
            .toList();
    }

    public List<WordElim> hotCandidates() {
        return candidates.stream()
            .map(this::elimination)
            .sorted(BY_ELIMINATION.reversed())
            .toList();
    }

    public boolean done() {
        return !guesses.isEmpty() && guesses.getLast().equals(word);
    }

    public Word lastGuess() {
        return guesses.getLast();
    }

    private Game apply(Word guess, List<Constraint> guessConstraints) {
        var newConstraints = mergeConstraints(this.constraints, guessConstraints);
        var trimmedCandidates = viable(candidates, newConstraints);
        return new Game(word, trimmedCandidates, newConstraints, add(guess));
    }

    private WordElim elimination(Word word) {
        var wordConstraints = newConstraints(word);
        var combinedConstraints = mergeConstraints(this.constraints, wordConstraints);
        var remaining = viable(candidates, combinedConstraints).size();
        var eliminated = candidates.size() - remaining;
        return new WordElim(word, eliminated);
    }

    private List<Word> add(Word guess) {
        return Stream.concat(guesses.stream(), Stream.of(guess))
            .toList();
    }

    private List<Constraint> newConstraints(Word guess) {
        if (word == null) {
            throw new IllegalStateException(this + " is a secret game, constraints must be supplied with new guess");
        }
        return guess.indexedChars()
            .map(word::constraintFor)
            .toList();
    }

    private static final Random RND = new Random();

    private static final Comparator<WordElim> BY_ELIMINATION =
        Comparator.comparing(WordElim::eliminated);

    private static List<Word> viable(List<Word> candidates, List<Constraint> constraints) {
        return candidates.stream()
            .filter(word ->
                includes(constraints, word))
            .toList();
    }

    private static Predicate<WordElim> eliminates(int max) {
        return wordElim -> wordElim.eliminated() == max;
    }

    private static int maxEliminations(List<WordElim> hotCandidates) {
        return hotCandidates.stream()
            .mapToInt(WordElim::eliminated)
            .max()
            .orElse(0);
    }

    private static boolean includes(List<Constraint> constraints, Word word) {
        return constraints.stream()
            .noneMatch(constraint ->
                constraint.excludes(word));
    }

    private static <T> T randomElement(List<T> list) {
        return list.get(RND.nextInt(list.size()));
    }

    @SafeVarargs
    private static List<Constraint> mergeConstraints(Collection<Constraint>... collections) {
        return Arrays.stream(collections)
            .flatMap(Collection::stream)
            .toList();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" +
               (word == null ? "<secret>" : word.toString()) +
               ", guesses:" + guesses.stream()
                   .map(Word::toString)
                   .collect(Collectors.joining(" ")) +
               ", candidates:" + candidates.size() +
               "]";
    }
}
