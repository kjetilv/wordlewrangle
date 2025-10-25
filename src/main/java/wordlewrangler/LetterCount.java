package wordlewrangler;

@SuppressWarnings("NullableProblems")
public record LetterCount(char c, int count) implements Comparable<LetterCount> {
    public LetterCount add(LetterCount other) {
        return new LetterCount(c, count + other.count());
    }

    @Override
    public int compareTo(LetterCount o) {
        return Integer.compare(count, o.count);
    }

    @Override
    public String toString() {
        return c + ":" + count;
    }
}
