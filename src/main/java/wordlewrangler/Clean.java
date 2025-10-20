import wordlewrangler.Word;

void main() throws Exception {
    Path path = Path.of("words.txt");
    List<Word> all = Word.fromFile(path).stream().distinct().toList();
    String newFile = contents(all);
    System.out.println("Found " + all.size() + " unique words.");

    Files.write(path, newFile.getBytes());
}

private static String contents(List<Word> all) {
    return all.stream()
        .gather(Gatherers.windowFixed(20))
        .map(words ->
            words.stream()
                .map(Word::toString))
        .map(strings -> strings.collect(Collectors.joining(" ")))
        .collect(Collectors.joining("\n"));
}