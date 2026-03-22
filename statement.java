import java.util.ArrayList;

public class Statement {

    // Stores the predicate name
    private String predicate;

    // Stores the parsed arguments for the statement
    private ArrayList<String> arguments;

    // True if this statement is a cmavo definition
    private boolean definition;

    // Creates a normal statement
    public Statement(String predicate, ArrayList<String> arguments) {
        this.predicate = predicate;
        this.arguments = arguments;
        this.definition = false;
    }

    // Creates a statement and lets us say if it is a definition
    public Statement(String predicate, ArrayList<String> arguments, boolean definition) {
        this.predicate = predicate;
        this.arguments = arguments;
        this.definition = definition;
    }

    // Returns the predicate name
    public String getPredicate() {
        return predicate;
    }

    // Returns the list of arguments
    public ArrayList<String> getArguments() {
        return arguments;
    }

    // Returns true if this statement is a definition
    public boolean isDefinition() {
        return definition;
    }

    @Override
    public String toString() {
        return "Predicate: " + predicate
                + ", Arguments: " + arguments
                + ", Definition: " + definition;
    }
}