public static final Random RND = new Random();

static Word random(List<Word> words) {
    return words.get(RND.nextInt(words.size()));
}

@SuppressWarnings("MethodMayBeStatic")
void main() {

    var words = Word.list("words.txt");

    var selected = random(words);

    var guess = random(words);

    List<Constraint> constraints = constraints(guess, selected);

    System.out.println("Word   : " + selected);
    System.out.println("Guess 1: " + guess);
    constraints.forEach(
        constraint -> System.out.println("  " + constraint)
    );

    System.out.println("Still viable:");
    List<Word> viable = words.stream()
        .filter(word ->
            constraints.stream().noneMatch(constraint -> constraint.excludes(word)))
        .toList();
    viable.forEach(System.out::println);

    var guess2 = random(viable);

    System.out.println("Word   : " + selected);
    System.out.println("Guess 1: " + guess);
    System.out.println("Guess 2: " + guess2);

    List<Constraint> constraints2 = constraints(guess2, selected);

    Map<Character, Constraint> collect = mergeConstraints(constraints, constraints2);

    System.out.println(collect.values()
        .stream()
        .toList());
}

private static Map<Character, Constraint> mergeConstraints(
    List<Constraint> constraints,
    List<Constraint> constraints2
) {
    return Stream.of(constraints, constraints2).flatMap(List::stream)
        .collect(Collectors.groupingBy(Constraint::c))
        .entrySet()
        .stream()
        .collect(Collectors.toMap(
            Map.Entry::getKey,
            entry -> entry.getValue()
                .stream().reduce(Constraint::merge).orElseThrow()
        ));
}

private static List<Constraint> constraints(Word guess, Word selected) {
    return guess.indexedChars()
        .map(selected::indexOf)
        .toList();
}

public record Word(char[] letters) implements Comparable<Word> {

    static List<Word> list(String file) {
        try (
            Stream<String> lines = Files.lines(Path.of(file));
            Stream<Word> words = lines
                .map(line -> line.split("\\s+"))
                .flatMap(Arrays::stream)
                .filter(s -> !s.isBlank())
                .map(String::trim)
                .map(String::toUpperCase)
                .map(Word::of)
                .sorted()
        ) {
            return Stream.concat(Stream.of(Word.of("SLATE")), words)
                .toList();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to read " + file);
        }
    }

    static Word of(String string) {
        if (string.length() != 5) {
            throw new IllegalArgumentException("Not a five-letter word: " + string);
        }
        return new Word(string.toCharArray());
    }

    @Override
    public int compareTo(Word o) {
        return toString().compareTo(o.toString());
    }

    Stream<IndexedChar> indexedChars() {
        return IntStream.range(0, letters.length)
            .mapToObj(i -> new IndexedChar(i, letters[i]));
    }

    Stream<Character> chars() {
        return indexedChars().map(IndexedChar::c);
    }

    Constraint indexOf(IndexedChar indexedChar) {
        for (int index = 0; index < letters.length; index++) {
            if (letters[index] == indexedChar.c()) {
                if (index == indexedChar.index()) {
                    return new Constraint.Found(indexedChar.c(), indexedChar.index());
                }
                return new Constraint.Present(indexedChar.c(), indexedChar.index());
            }
        }
        return new Constraint.Unused(indexedChar.c());
    }

    private boolean matchAt(IndexedChar ic, int i) {
        return letters[i] == ic.c();
    }

    @Override
    public String toString() {
        return chars().map(Object::toString)
            .collect(Collectors.joining(""));
    }
}

record IndexedChar(int index, char c) {
}

public sealed interface Constraint {

    char c();

    boolean excludes(Word word);

    Constraint merge(Constraint other);

    record Found(char c, int index) implements Constraint {

        @Override
        public Constraint merge(Constraint other) {
            return this;
        }

        @Override
        public boolean excludes(Word word) {
            return word.letters[index] != c;
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + "[" + c + " @  " + index + "]";
        }
    }

    record Present(char c, List<Integer> excluded) implements Constraint {

        public Present(char c, int excluded) {
            this(c, List.of(excluded));
        }

        @Override
        public boolean excludes(Word word) {

            return word.chars().noneMatch(wordChar -> wordChar == c);
        }

        @Override
        public Constraint merge(Constraint other) {
            return switch (other) {
                case Found found -> found;
                case Present present -> new Present(
                    c,
                    merge(present)
                );
                default -> throw new IllegalStateException("Unexpected value: " + other);
            };
        }

        private List<Integer> merge(Present present) {
            return Stream.concat(
                    excluded.stream(),
                    present.excluded.stream()
                )
                .distinct()
                .toList();
        }

        @Override
        public String toString() {
            String notAt = excluded.stream()
                .map(Object::toString)
                .collect(Collectors.joining(" "));
            return getClass().getSimpleName() + "[" + c + " !@ " + notAt + "]";
        }
    }

    record Unused(char c) implements Constraint {

        @Override
        public Constraint merge(Constraint other) {
            return this;
        }

        @Override
        public boolean excludes(Word word) {
            return word.chars().anyMatch(wordChar -> wordChar == c);
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + "[" + c + "]";
        }
    }
}