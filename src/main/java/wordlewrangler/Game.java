package wordlewrangler;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@SuppressWarnings("NullableProblems")
public record Game(
    Word solution,
    int unitaryLength,
    Collection<Word> dictionary,
    Collection<Word> candidates,
    Collection<Word> past,
    List<Constraint> constraints,
    List<Word> guesses
) {

    public Game(
        Word solution,
        int unitaryLength,
        Collection<Word> dictionary,
        Collection<Word> candidates,
        Collection<Word> past,
        List<Constraint> constraints,
        List<Word> guesses
    ) {
        this.solution = solution;
        this.unitaryLength = unitaryLength;
        this.dictionary = Set.copyOf(dictionary);
        this.candidates = Set.copyOf(candidates);
        this.past = Set.copyOf(past);
        this.constraints = constraints;
        this.guesses = guesses;
    }

    public Game(List<Word> candidates) {
        this(null, candidates);
    }

    public Game(Word solution, List<Word> candidates) {
        if (candidates.isEmpty()) {
            throw new IllegalStateException("Empty candidates");
        }
        var length = unitaryLength(candidates);
        if (solution != null) {
            if (!candidates.contains(solution)) {
                throw new IllegalArgumentException("Invalid solution, not contained in candidates: " + solution);
            }
            if (solution.length() != length) {
                throw new IllegalStateException("Invalid length for solution, should be " + length + ": " + solution);
            }
        }
        this(
            solution,
            length,
            candidates,
            candidates,
            List.of(),
            Collections.emptyList(),
            Collections.emptyList()
        );
    }

    public Game past(Collection<Word> past) {
        return new Game(
            solution,
            unitaryLength,
            dictionary,
            candidates.stream()
                .filter(word -> !past.contains(word))
                .toList(),
            past,
            constraints,
            guesses
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
                                word.letters()[position])
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
            past,
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
        return apply(new Word(guess), providedConstraints(guess, spec));
    }

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
            past,
            constraints,
            guesses
        );
    }

    public WordScores wordScores() {
        var wordElims = hotCandidatesDescending();
        if (wordElims.isEmpty()) {
            return WordScores.EMPTY;
        }
        var distribution = distribution();
        var wordScores = wordElims.stream()
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
        var trimmedCandidates = viable(candidates, newConstraints.toArray(Constraint[]::new));
        return new Game(solution, unitaryLength, dictionary, trimmedCandidates, past, newConstraints, add(guess));
    }

    private Stream<WordElim> averageHotCandidates(Collection<Word> words) {
        return words.stream().parallel()
            .map(candidate ->
                hotCandidates(candidate, words).collect(
                    Collectors.toMap(
                        WordElim::word,
                        Function.identity()
                    ))
            )
            .reduce(Game::merge)
            .map(result ->
                average(result).values())
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
        var remaining = viable(candidates, combinedConstraints.toArray(Constraint[]::new)).size();
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

    private static List<Word> viable(Collection<Word> candidates, Constraint[] constraints) {
        List<Word> viable = new ArrayList<>();
        for (Word word : candidates) {
            if (viable(constraints, word)) {
                viable.add(word);
            }
        }
        return viable;
    }

    private static boolean viable(Constraint[] constraints, Word word) {
        for (Constraint constraint : constraints) {
            if (constraint.excludes(word)) {
                return false;
            }
        }
        return true;
    }

    private static List<Constraint> providedConstraints(String guess, String spec) {
        return Constraint.parse(new Word(guess), spec);
    }

    @SafeVarargs
    private static List<Constraint> mergeConstraints(Collection<Constraint>... collections) {
        Set<Constraint> constraints = new HashSet<>();
        for (Collection<Constraint> collection : collections) {
            constraints.addAll(collection);
        }
        Set<Integer> foundPositions = new HashSet<>();
        for (Constraint constraint : constraints) {
            for (Integer position : constraint.foundPositions()) {
                foundPositions.add(position);
            }
        }
        var remaining = constraints.stream()
            .map(constraint ->
                constraint.clearFound(foundPositions))
            .filter(Objects::nonNull)
            .toList();

        Map<Character, Constraint> presentMap = new HashMap<>();
        Map<Character, Constraint> unusedMap = new HashMap<>();
        Map<Character, Constraint> foundMap = new HashMap<>();

        for (Constraint constraint : remaining) {
            switch (constraint) {
                case Constraint.Present present -> presentMap.compute(
                    present.c(),
                    (_, p) ->
                        p == null ? present : p.merge(present)
                );
                case Constraint.Unused unused -> unusedMap.compute(
                    unused.c(),
                    (_, u) ->
                        u == null ? unused : unused.merge(u)
                );
                case Constraint.Found found -> foundMap.compute(
                    found.c(),
                    (_, f) ->
                        f == null ? found : f.merge(found)
                );
            }
        }

        return Stream.of(presentMap.values(), foundMap.values(), unusedMap.values())
            .flatMap(Collection::stream)
            .toList();
    }

    private static List<Constraint> combine(
        List<Constraint> constraints,
        Class<? extends Constraint> type
    ) {
        return constraints.stream()
            .filter(type::isInstance)
            .collect(Collectors.groupingBy(Constraint::c))
            .values()
            .stream()
            .map(Game::combined)
            .toList();
    }

    private static Constraint combined(List<Constraint> values) {
        return values.stream()
            .reduce(Constraint::merge)
            .orElseThrow(() ->
                new IllegalStateException("Cannot merge constraints"));
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
        List<Constraint> constraints = new ArrayList<>(guess.length());
        for (int i = 0; i < guess.length(); i++) {
            constraints.add(solution.constraintFor(guess.letters()[i], i));
        }
        return constraints;
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
