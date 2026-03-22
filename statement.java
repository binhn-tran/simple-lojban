import java.util.ArrayList;

public class Statement {

    // stores the predicate name
    private String predicate;

    // stores the arguments for the statement
    private ArrayList<String> arguments;

    // creates a new statement with a predicate and its arguments
    public Statement(String predicate, ArrayList<String> arguments) {
        this.predicate = predicate;
        this.argumenets = arguments;
    }

    // returns the predicate name 
    public String getPredicate() {
        return predicate;
    }

    // returns the list of arguments
    public ArrayList<String> getArguments() {
        return arguments;
    }

    @Override
    public String toString() {
        return "Predicate: " + predicate + ", Arguments: " + arguments;

}