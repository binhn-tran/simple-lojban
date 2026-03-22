public class Value {

    // Stores what kind of value this is
    // Example: NUMBER, NAME, EMPTY_LIST, or LIST
    private String type;

    // Stores the text value for simple types like NUMBER or NAME
    private String value;

    // Used for list structures like steko
    private Value head;
    private Value tail;

    // Constructor for simple values like numbers and names
    public Value(String type, String value) {
        this.type = type;
        this.value = value;
        this.head = null;
        this.tail = null;
    }

    // Constructor for list values
    public Value(String type, Value head, Value tail) {
        this.type = type;
        this.value = null;
        this.head = head;
        this.tail = tail;
    }

    // Returns the type of the value
    public String getType() {
        return type;
    }

    // Returns the text value
    public String getValue() {
        return value;
    }

    // Returns the head of a list
    public Value getHead() {
        return head;
    }

    // Returns the tail of a list
    public Value getTail() {
        return tail;
    }

    // Checks if this value is an empty list
    public boolean isEmptyList() {
        return type.equals("EMPTY_LIST");
    }

    @Override
    public String toString() {
        if (type.equals("LIST")) {
            return "[" + head + " | " + tail + "]";
        } else if (type.equals("EMPTY_LIST")) {
            return "[]";
        } else {
            return "(" + type + ", " + value + ")";
        }
    }
}