package wordlewrangler;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("NullableProblems")
public record Game(
    Word solution,
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
        Objects.requireNonNull(candidates, "candidates");
        Objects.requireNonNull(constraints, "constraints");
        Objects.requireNonNull(guesses, "guesses");
    }

    public Game set(String word) {
        return set(new Word(word));
    }

    public Game set(Word solution) {
        if (guesses.isEmpty()) {
            if (candidates.contains(solution)) {
                return new Game(solution, candidates, constraints, guesses);
            }
            throw new IllegalArgumentException("No such candidate: " + solution);
        }
        throw new IllegalStateException(this + " is already in progress!");
    }

    public Game guessWord() {
        return tryWord(randomElement(candidates));
    }

    public Game tryWord(String guess) {
        return tryWord(new Word(guess));
    }

    public Game tryWord(Word guess) {
        if (solution != null) {
            var guessConstraints = constraintsAgainst(solution, guess);
            return apply(guess, guessConstraints);
        }
        throw new IllegalStateException(this + " is a secret game, constraints must be supplied with new guess");
    }

    public Game tried(String guess, String spec) {
        return apply(
            new Word(guess),
            Constraint.parse(new Word(guess), spec)
        );
    }

    public WordElim someHotCandidate() {
        return randomElement(hottestCandidates());
    }

    public List<WordElim> hotCandidates() {
        return solution == null
            ? averageHotCandidates()
            : hotCandidates(solution);
    }

    public List<WordElim> hottestCandidates() {
        return hottestOf(hotCandidates());
    }

    public boolean done() {
        return candidates.isEmpty() ||
               !guesses.isEmpty() && guesses.getLast().equals(solution);
    }

    public Word lastGuess() {
        return guesses.getLast();
    }

    public Game random() {
        return new Game(randomElement(candidates), candidates, constraints, guesses);
    }

    private List<WordElim> averageHotCandidates() {
        return candidates.stream()
            .parallel()
            .map(this::hotCandidates)
            .map(list ->
                list.stream()
                    .collect(Collectors.toMap(WordElim::word, Function.identity())))
            .reduce(this::merged)
            .map(Game::average)
            .orElseGet(Collections::emptyMap)
            .values()
            .stream()
            .sorted(BY_ELIMINATION.reversed())
            .toList();
    }

    private Map<Word, WordElim> merged(Map<Word, WordElim> m, Map<Word, WordElim> other) {
        candidates.forEach(candidate ->
            m.merge(candidate, other.get(candidate), WordElim::add));
        return m;
    }

    private List<WordElim> hotCandidates(Word assumingSolution) {
        return candidates.stream()
            .map(guess ->
                elimination(guess, assumingSolution))
            .sorted(BY_ELIMINATION.reversed())
            .toList();
    }

    private Game apply(Word guess, List<Constraint> guessConstraints) {
        var newConstraints = mergeConstraints(this.constraints, guessConstraints);
        var trimmedCandidates = viable(candidates, newConstraints);
        return new Game(solution, trimmedCandidates, newConstraints, add(guess));
    }

    private WordElim elimination(Word guess, Word assumingSolution) {
        var wordConstraints = constraintsAgainst(assumingSolution, guess);
        var combinedConstraints = mergeConstraints(this.constraints, wordConstraints);
        var remaining = viable(candidates, combinedConstraints).size();
        var eliminated = candidates.size() - remaining;
        return new WordElim(guess, eliminated);
    }

    private List<Word> add(Word guess) {
        return Stream.concat(guesses.stream(), Stream.of(guess))
            .toList();
    }

    private static final Random RND = new Random();

    private static final Comparator<WordElim> BY_ELIMINATION =
        Comparator.comparing(WordElim::eliminated);

    private static List<WordElim> hottestOf(List<WordElim> hotCandidates) {
        var maxElimination = maxEliminations(hotCandidates);
        return hotCandidates.stream()
            .filter(eliminates(maxElimination))
            .toList();
    }

    private static Map<Word, WordElim> average(Map<Word, WordElim> result) {
        return result.entrySet()
            .stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> entry.getValue().avg()
            ));
    }

    private static List<Constraint> constraintsAgainst(Word assumed, Word guess) {
        return guess.indexedChars()
            .map(Objects.requireNonNull(assumed, "assumed")::constraintFor)
            .toList();
    }

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
            .distinct()
            .toList();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" +
               (solution == null ? "<secret>" : solution.toString()) +
               ", guesses:" + guesses.stream()
                   .map(Word::toString)
                   .collect(Collectors.joining(" ")) +
               ", candidates:" + candidates.size() +
               "]";
    }
}
