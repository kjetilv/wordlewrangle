package wordlewrangler;

@SuppressWarnings("NullableProblems")
public record WordElim(Word word, int eliminated, int count) {

    public WordElim(Word word, int eliminated) {
        this(word, eliminated, 1);
    }

    WordElim add(WordElim other) {
        return new WordElim(word, eliminated + other.eliminated(), count + other.count());
    }

    WordElim avg() {
        return new WordElim(word, Math.toIntExact(Math.round(1d * eliminated / count)), 1);
    }

    @Override
    public String toString() {
        return "[" + word + ":" + eliminated + (count > 1 ? " [x" + (count - 1) : "]") + "]";
    }
}
