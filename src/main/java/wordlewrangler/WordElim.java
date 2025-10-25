package wordlewrangler;

@SuppressWarnings("NullableProblems")
public record WordElim(Word word, int eliminated, int count) implements Comparable<WordElim> {

    public WordElim(Word word, int eliminated) {
        this(word, eliminated, 1);
    }

    @Override
    public int compareTo(WordElim o) {
        return word.compareTo(o.word);
    }

    WordElim add(WordElim other) {
        return new WordElim(word, eliminated + other.eliminated(), count + other.count());
    }

    WordElim avg() {
        return new WordElim(word, Math.toIntExact(Math.round(1d * eliminated / count)), 1);
    }

    @Override
    public String toString() {
        return word + ":" + eliminated + (count > 1 ? " [x" + (count - 1) : "");
    }
}
