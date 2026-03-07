import wordlewrangler.Word;

@SuppressWarnings("MethodMayBeStatic")
void main() {
    Stream.of("words.txt", "wordsish.txt", "words-expanded.txt")
        .parallel()
        .map(Path::of)
        .forEach(path -> {
            List<Word> all = Word.fromFile(path, true)
                .stream()
                .distinct()
                .toList();
            String newFile = contents(all);
            System.out.println(path + ": Found " + all.size() + " unique words.");

            try {
                Files.write(path, newFile.getBytes());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
}

private static String contents(List<Word> all) {
    return all.stream()
        .gather(Gatherers.windowFixed(20))
        .map(words ->
            words.stream()
                .map(Word::toString))
        .map(strings ->
            strings.collect(Collectors.joining(" ")))
        .collect(Collectors.joining("\n"));
}