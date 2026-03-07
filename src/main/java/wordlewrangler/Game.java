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
    int unitaryLength,
    Collection<Word> dictionary,
    Collection<Word> candidates,
    List<Constraint> constraints,
    List<Word> guesses
) {

    public Game(List<Word> candidates) {
        this(null, candidates);
    }

    public Game(Word solution, List<Word> dictionary) {
        if (dictionary.isEmpty()) {
            throw new IllegalStateException("Empty dictionary");
        }
        var length = unitaryLength(dictionary);
        if (solution != null) {
            if (!dictionary.contains(solution)) {
                throw new IllegalArgumentException("Invalid solution, not contained in dictionary: " + solution);
            }
            if (solution.length() != length) {
                throw new IllegalStateException("Invalid length for solution, should be " + length + ": " + solution);
            }
        }
        this(
            solution,
            length,
            dictionary,
            dictionary,
            Collections.emptyList(),
            Collections.emptyList()
        );
    }

    public Game set(String word) {
        return set(new Word(word));
    }

    public LetterDistributions distribution() {
        return new LetterDistributions(
            IntStream.range(0, unitaryLength)
                .mapToObj(position ->
                    new LetterDistribution(
                        position,
                        candidates.stream()
                            .map(word ->
                                word.charAt(position))
                            .collect(
                                Collectors.groupingBy(
                                    Function.identity(),
                                    Collectors.counting()
                                ))
                            .entrySet()
                            .stream()
                            .map(entry ->
                                new LetterCount(
                                    entry.getKey(),
                                    Math.toIntExact(entry.getValue())
                                ))
                            .sorted(Comparator.<LetterCount>naturalOrder().reversed())
                            .toList()
                    ))
                .toList());
    }

    public Game set(Word solution) {
        if (!guesses.isEmpty()) {
            throw new IllegalStateException(this + " is already in progress!");
        }
        if (!dictionary.contains(solution)) {
            throw new IllegalArgumentException("No such candidate: " + solution);
        }
        return new Game(
            solution,
            unitaryLength,
            dictionary,
            candidates,
            constraints,
            guesses
        );
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
        return apply(guess, constraintsAgainst(solution, guess));
    }

    public Game tried(String guess, String spec) {
        return apply(new Word(guess), providedConstraints(guess, spec));}

    public WordElim someHotCandidate() {
        return randomElement(hottestCandidates());
    }

    public List<WordElim> hottestCandidates() {
        var desc = hotCandidatesDescending();
        if (desc.isEmpty()) {
            return desc;
        }
        var max = desc.getFirst().eliminated();
        return desc.stream()
            .takeWhile(wordElim ->
                wordElim.eliminated() == max)
            .toList();
    }

    public List<WordElim> hotCandidatesDescending() {
        return byElimination(solution != null
            ? hotCandidates(solution, candidates)
            : averageHotCandidates(candidates)
        );
    }

    public List<WordElim> hotEliminatorsDescending() {
        return byElimination(averageHotCandidates(dictionary));
    }

    public boolean done() {
        return candidates.isEmpty() || !guesses.isEmpty() && guesses.getLast().equals(solution);
    }

    public Word lastGuess() {
        return guesses.getLast();
    }

    public Game random() {
        return new Game(
            randomElement(candidates),
            unitaryLength,
            dictionary,
            candidates,
            constraints,
            guesses
        );
    }

    public WordScores wordScores() {
        List<WordElim> wordElims = hotCandidatesDescending();
        if (wordElims.isEmpty()) {
            return WordScores.EMPTY;
        }
        LetterDistributions distribution = distribution();
        List<WordScore> wordScores = wordElims.stream()
            .map(elim ->
                new WordScore(
                    elim.word(),
                    distribution.score(elim.word()),
                    elim.eliminated()
                ))
            .toList();
        return new WordScores(
            wordScores,
            wordElims.stream()
                .mapToInt(WordElim::eliminated)
                .max()
                .orElseThrow(),
            wordScores.stream()
                .mapToDouble(WordScore::distribution)
                .max()
                .orElseThrow()
        );
    }

    private Game apply(Word guess, List<Constraint> guessConstraints) {
        if (guess.length() != unitaryLength) {
            throw new IllegalArgumentException("Guess length must be " + unitaryLength + ": " + guess);
        }
        var newConstraints = mergeConstraints(constraints, guessConstraints);
        var trimmedCandidates = viable(candidates, newConstraints);
        return new Game(solution, unitaryLength, dictionary, trimmedCandidates, newConstraints, add(guess));
    }

    private Stream<WordElim> averageHotCandidates(Collection<Word> words) {
        return words.stream().parallel()
            .map(candidate ->
                hotCandidates(candidate, words))
            .map(Game::mapByWord)
            .reduce(Game::merge)
            .map(Game::average)
            .map(Map::values)
            .orElseGet(Collections::emptyList)
            .stream()
            .sorted(DESCENDING_ELIMINATION);
    }

    private Stream<WordElim> hotCandidates(Word solution, Collection<Word> words) {
        return words.stream()
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

    private static List<WordElim> byElimination(Stream<WordElim> wordElimStream) {
        return wordElimStream.sorted(DESCENDING_ELIMINATION)
            .toList();
    }

    @SafeVarargs
    private static int unitaryLength(Collection<Word>... wordses) {
        return Arrays.stream(wordses)
            .flatMap(Collection::stream)
            .distinct()
            .mapToInt(Word::length)
            .reduce((l1, l2) -> {
                if (l1 != l2) {
                    throw new IllegalStateException("Different lengths detected: " + l1 + " != " + l2);
                }
                return l1;
            }).orElseThrow(() ->
                new IllegalStateException("No words in collection")
            );
    }

    private static List<Word> viable(Collection<Word> candidates, List<Constraint> constraints) {
        return candidates.stream()
            .filter(satisfiesConstraints(constraints))
            .toList();
    }

    private static List<Constraint> providedConstraints(String guess, String spec) {
        return Constraint.parse(new Word(guess), spec);
    }

    @SafeVarargs
    private static List<Constraint> mergeConstraints(Collection<Constraint>... collections) {
        List<Constraint> constraints = Arrays.stream(collections)
            .flatMap(Collection::stream)
            .distinct()
            .toList();
        Set<Character> fixes = constraints.stream()
            .filter(Constraint.Fixed.class::isInstance)
            .map(Constraint::c)
            .collect(Collectors.toSet());
        return constraints.stream()
            .filter(constraint ->
                !(constraint instanceof Constraint.Unused(char c) && fixes.contains(c)))
            .toList();
    }

    private static Map<Word, WordElim> mapByWord(Stream<WordElim> list) {
        return list.collect(
            Collectors.toMap(
                WordElim::word,
                Function.identity()
            ));
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

    private static <T> T randomElement(Collection<T> coll) {
        var index = RND.nextInt(coll.size());
        return (coll instanceof List<T> l ? l : new ArrayList<>(coll)).get(index);
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
