package wordlewrangler;

@SuppressWarnings("NullableProblems")
public record WordElim(Word word, int eliminated) {

    @Override
    public String toString() {
        return "[" + word + ":" + eliminated + "]";
    }
}
