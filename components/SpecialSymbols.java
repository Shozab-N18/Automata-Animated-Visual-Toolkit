package components;

public enum SpecialSymbols {
    EPSILON("ε"),
    EMPTY_SET("∅"),
    ALPHABET("Σ"),
    TRANSITION("δ");

    private final String symbol;

    SpecialSymbols(String symbol) {
        this.symbol = symbol;
    }

    @Override
    public String toString() {
        return symbol;
    }
}
