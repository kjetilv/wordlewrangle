import wordlewrangler.Constraint;
import wordlewrangler.Game;
import wordlewrangler.Word;
import wordlewrangler.WordElim;

public static final Random RND = new Random();

static Word random(List<Word> words) {
    return words.get(RND.nextInt(words.size()));
}

@SuppressWarnings("MethodMayBeStatic")
void main() {
    var words = Word.fromFile(Path.of("words.txt"));
    var game = new Game(words).guessWord();

    System.out.println("Word   : " + game.word());

    while (true) {
        if (game.done()) {
            System.out.println("Found it! " + game.lastGuess() + "! Guesses: " + game.guesses()
                .stream()
                .map(Word::toString)
                .collect(Collectors.joining(" ")));
            return;
        }

        System.out.println("\nGuess " + game.guesses().size() + ": " + game.lastGuess());

        System.out.println("Constraints:");
        printConstraints(game.constraints());

        System.out.println("Still viable:");
        printWords(game.candidates(), 16);

        System.out.println("Hot candidates:");
        var elims = game.hotCandidates();
        printWords(elims, 10);

        var hotCandidate = game.hotCandidate();
        var hotWord = hotCandidate.word();
        System.out.println("Trying hot word: " + hotWord);

        game = game.tryWord(hotWord);
    }
}

private static void printWords(List<?> l, int windowSize) {
    l.stream().gather(Gatherers.windowFixed(windowSize))
        .forEach(window -> {
            window.forEach(i -> System.out.print("  " + i));
            System.out.println();
        });
}

private static void printConstraints(Collection<Constraint> constraints) {
    System.out.println(
        constraints.stream().sorted()
            .map(Object::toString)
            .collect(Collectors.joining("  ")
            ));
}

private static List<Constraint> findConstraints(Word guess, Word selected) {
    return guess.indexedChars()
        .map(selected::constraintFor)
        .toList();
}

