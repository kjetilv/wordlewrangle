import wordlewrangler.Constraint;
import wordlewrangler.Game;
import wordlewrangler.Word;

public static final Random RND = new Random();

static Word random(List<Word> words) {
    return words.get(RND.nextInt(words.size()));
}

@SuppressWarnings("MethodMayBeStatic")
void main() {
    var words = Word.fromFile(Path.of("words.txt"));
    var game = new Game(words).random().guessWord();

    System.out.println("Word   : " + game.solution());

    while (true) {
        if (game.done()) {
            System.out.println("\nFound it! " + game.lastGuess() + "!\n\nGuesses: " + game.guesses()
                .stream()
                .map(Word::toString)
                .collect(Collectors.joining(" ")));
            return;
        }

        System.out.println("\nGuess " + game.guesses().size() + ": " + game.lastGuess());

        System.out.println("Constraints:");
        printConstraints(game.constraints());

        if (game.candidates().size() > 20) {
            System.out.println("Still viable: " + game.candidates().size());
        } else {
            System.out.println("Still viable:");
            printWords(game.candidates());
        }

        System.out.println("Hot candidates:");
        var elims = game.hottestCandidates();
        printWords(elims);

        var hotCandidate = game.someHotCandidate();
        var hotWord = hotCandidate.word();
        System.out.println("Trying hot word: " + hotWord);

        game = game.tryWord(hotWord);
    }
}

private static void printWords(List<?> l) {
    l.stream().gather(Gatherers.windowFixed(5))
        .forEach(window -> {
            window.forEach(i -> System.out.print(" " + i));
            System.out.println();
        });
}

private static void printConstraints(Collection<Constraint> constraints) {
    System.out.println(" " + constraints.stream().sorted()
        .map(Object::toString)
        .collect(Collectors.joining(" ")));
}

private static List<Constraint> findConstraints(Word guess, Word selected) {
    return guess.indexedChars()
        .map(selected::constraintFor)
        .toList();
}

