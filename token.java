public class Token {

    // Stores what kind of token it is
    private String type;

    // Stores the text from the input that this token represents
    private String value;

    // Makes a new token object
    public Token(String type, String value) {
        this.type = type;
        this.value = value;
    }

    // Gets the token type
    public String getType() {
        return type;
    }

    // Gets the token value
    public String getValue() {
        return value;
    }

    // Returns the token in a readable format
    @Override
    public String toString() {
        return "(" + type + ", " + value + ")";
    }
}