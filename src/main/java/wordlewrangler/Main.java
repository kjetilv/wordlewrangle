import module java.base;
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
    var words = Word.fromFile(Path.of("words.txt"), false);
    var game = new Game(words).random().guessWord();

    IO.println("Word   : " + game.solution());

    while (true) {
        if (game.done()) {
            IO.println("\nFound it! " + game.lastGuess() + "!\n\nGuesses: " + game.guesses()
                .stream()
                .map(Word::toString)
                .collect(Collectors.joining(" ")));
            return;
        }

        IO.println("\nGuess " + game.guesses().size() + ": " + game.lastGuess());

        IO.println("Constraints:");
        printConstraints(game.constraints());

        if (game.candidates().size() > 20) {
            IO.println("Still viable: " + game.candidates().size());
        } else {
            IO.println("Still viable:");
            printWords(game.candidates());
        }

        var wordElims = game.hottestCandidates()
            .stream()
            .toList();
        IO.println("Hot candidates: " + wordElims.size());
        printWords(wordElims);

        var hotCandidate = game.someHotCandidate();
        var hotWord = hotCandidate.word();
        IO.println("Trying hot word: " + hotWord);

        game = game.tryWord(hotWord);
    }
}

private static void printWords(Collection<?> l) {
    l.stream()
        .gather(Gatherers.windowFixed(5))
        .forEach(window -> {
            window.forEach(i -> System.out.print(" " + i));
            IO.println();
        });
}

private static void printConstraints(Collection<Constraint> constraints) {
    IO.println(" " + constraints.stream().sorted()
        .map(Object::toString)
        .gather(Gatherers.windowFixed(5))
        .map(list ->
            String.join(" ", list))
        .collect(Collectors.joining("\n ")));
}

private static List<Constraint> findConstraints(Word guess, Word selected) {
    return guess.indexedChars()
        .map(selected::constraintFor)
        .toList();
}

