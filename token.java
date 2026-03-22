private class Token {

    // stores what kind of token it is and its value
    private String type;

    // stores the text from the input that this token represents


    // makes a new token object
    public Token(String type, String value) {
        this.type = type;
        this.value = value;
    }

    // gets the token type
    public String getType() {
        return type;
    }

    // gets the token value
    public String getValue() {
        return value;
    }

    // returns the token in a readable format 
    @Override
    public String toString() {
        return "(" + type + ", " + value + ")";
    }
}