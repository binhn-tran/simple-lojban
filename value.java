public class Value {
    
    // stores what kind of value 
    private String type;

    // stores the value as text
    private String value;

    // creates a new value object
    public Value(String type, String value) {
        this.type = type;
        this.value = value;
    }

    // returns the type of the value
    public String getType() {
        return type;
    }

    // returns the value
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "(" + type + ", " + value + ")";
    }
}