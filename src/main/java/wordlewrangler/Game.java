package wordlewrangler;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
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
        this(
            word,
            candidates,
            Collections.emptyList(),
            Collections.emptyList()
        );
    }

    public Game {
        Objects.requireNonNull(candidates, "candidates");
        Objects.requireNonNull(constraints, "constraints");
        Objects.requireNonNull(guesses, "guesses");
    }

    public Game set(String word) {
        return set(new Word(word));
    }

    public LetterDistributions distribution() {
        return new LetterDistributions(IntStream.range(0, candidates.getFirst().letters().length())
            .mapToObj(position ->
                new LetterDistribution(
                    position,
                    candidates.stream()
                        .map(word ->
                            word.charAt(position))
                        .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                        .entrySet()
                        .stream()
                        .map(entry ->
                            new LetterCount(entry.getKey(), Math.toIntExact(entry.getValue())))
                        .sorted(Comparator.<LetterCount>naturalOrder().reversed())
                        .toList()
                ))
            .toList());
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
        if (solution == null) {
            throw new IllegalStateException(this + " is a secret game, constraints must be supplied with new guess");
        }
        return apply(
            guess,
            constraintsAgainst(solution, guess)
        );
    }

    public Game tried(String guess, String spec) {
        return apply(
            new Word(guess),
            providedConstraints(guess, spec)
        );
    }

    public WordElim someHotCandidate() {
        return randomElement(hottestCandidates());
    }

    public List<WordElim> hottestCandidates() {
        List<WordElim> desc = hotCandidatesDescending();
        if (desc.isEmpty()) {
            return desc;
        }
        int max = desc.getFirst().eliminated();
        return desc.stream()
            .takeWhile(wordElim ->
                wordElim.eliminated() == max)
            .toList();
    }

    public List<WordElim> hotCandidatesDescending() {
        return (solution == null
            ? averageHotCandidates()
            : candidates.stream()
                .map(guess ->
                    new WordElim(guess, eliminated(guess, solution)))
        ).sorted(DESCENDING_ELIMINATION)
            .toList();
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

    private Game apply(Word guess, List<Constraint> guessConstraints) {
        var newConstraints = mergeConstraints(this.constraints, guessConstraints);
        var trimmedCandidates = viable(candidates, newConstraints);
        return new Game(solution, trimmedCandidates, newConstraints, add(guess));
    }

    private Stream<WordElim> averageHotCandidates() {
        return candidates.stream().parallel()
            .map(this::hotCandidates)
            .map(Game::mapByWord)
            .reduce(Game::merge)
            .map(Game::average)
            .map(Map::values)
            .orElseGet(Collections::emptyList)
            .stream()
            .sorted(DESCENDING_ELIMINATION);
    }

    private Stream<WordElim> hotCandidates(Word solution) {
        return candidates.stream()
            .map(guess ->
                new WordElim(guess, eliminated(guess, solution)));
    }

    private int eliminated(Word guess, Word assumingSolution) {
        Objects.requireNonNull(assumingSolution, "assumingSolution");
        var wordConstraints = constraintsAgainst(assumingSolution, guess);
        var combinedConstraints = mergeConstraints(this.constraints, wordConstraints);
        var remaining = viable(candidates, combinedConstraints).size();
        return candidates.size() - remaining;
    }

    private List<Word> add(Word guess) {
        return Stream.concat(guesses.stream(), Stream.of(guess))
            .toList();
    }

    private static final Random RND = new Random();

    private static final Comparator<WordElim> DESCENDING_ELIMINATION =
        Comparator.comparing(WordElim::eliminated).reversed();

    private static List<Word> viable(List<Word> candidates, List<Constraint> constraints) {
        return candidates.stream()
            .filter(satisfiesConstraints(constraints))
            .toList();
    }

    private static List<Constraint> providedConstraints(String guess, String spec) {
        return Constraint.parse(new Word(guess), spec);
    }

    @SafeVarargs
    private static List<Constraint> mergeConstraints(Collection<Constraint>... collections) {
        return Arrays.stream(collections)
            .flatMap(Collection::stream)
            .distinct()
            .toList();
    }

    private static Map<Word, WordElim> mapByWord(Stream<WordElim> list) {
        return list.collect(Collectors.toMap(
                WordElim::word,
                Function.identity()
            )
        );
    }

    private static Map<Word, WordElim> merge(Map<Word, WordElim> m, Map<Word, WordElim> other) {
        m.keySet()
            .stream().parallel()
            .forEach(candidate ->
                m.merge(
                    candidate,
                    other.get(candidate),
                    WordElim::add
                ));
        return m;
    }

    private static Map<Word, WordElim> average(Map<Word, WordElim> result) {
        return result.entrySet()
            .stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> entry.getValue().avg()
            ));
    }

    private static List<Constraint> constraintsAgainst(Word solution, Word guess) {
        return guess.indexedChars()
            .map(Objects.requireNonNull(solution, "assumed")::constraintFor)
            .toList();
    }

    private static Predicate<Word> satisfiesConstraints(List<Constraint> constraints) {
        return word ->
            constraints.stream()
                .noneMatch(constraint ->
                    constraint.excludes(word));
    }

    private static <T> T randomElement(List<T> list) {
        return list.get(RND.nextInt(list.size()));
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
