# simple-lojban

# Simple Lojban Predicate Calculus Interpreter

## Overview

This project is a Java implementation of a simple predicate calculus interpreter using a simplified form of Lojban. The program takes one input string, checks whether it follows the required rules, breaks it into tokens, parses those tokens into statements, and then interprets the statements using predefined predicates and stored facts. The main idea of the program is that all statements except the last one are used to build up the database, while the final statement is treated as the query. The program then evaluates that final statement and returns whether it succeeds, along with any variable values that are found.

## Files

The project is divided into several Java files, each with a specific role. `main.java` handles user input and runs the program by calling the tokenizer, parser, and interpreter. `token.java` defines the `Token` class, which stores the type and value of each token. `tokenizer.java` scans the input string, checks that the words and characters are valid, and converts the input into a list of tokens. `statement.java` represents one parsed statement, including its predicate, arguments, and whether it is a `cmavo` definition. `value.java` represents the values used in statements, such as numbers, names, and list structures. `parser.java` takes the list of tokens and turns them into complete statements that the interpreter can use. `interpreter.java` processes the parsed statements, stores facts and definitions, and evaluates the final query.

## How the Program Works

The program works in three main stages. First, the tokenizer reads the input string and separates it into valid tokens such as statement markers, names, numbers, predicates, and special words. Next, the parser takes those tokens and groups them into complete statements in a structured form. Finally, the interpreter uses the earlier statements to build a database of facts and definitions, then evaluates the last statement as the final query. This design made the program easier to organize because each part has a clear job.

## Features

The program reads one full input string, checks whether the input follows the required format, tokenizes the input into valid word types, parses the tokens into statements, stores facts and predicate definitions, and evaluates the final query. It can also return variable values when variables appear in the final statement. If the input is invalid, the program displays an error message instead of crashing.

## Built-In Predicate Support

The interpreter includes support for built-in predicates such as `fatci`, `sumji`, `vujni`, `dunli`, `steni`, and `steko`. It also supports predicate definitions using `cmavo`, which allows the program to handle both predefined logic and user-defined predicate behavior based on the assignment rules.

## How to Compile and Run

To run the program, Java must first be installed on the computer. All of the `.java` files should be placed in the same folder. After that, a terminal or command prompt should be opened in that folder. The program can be compiled by running `javac *.java`. Once the files compile successfully, the program can be started by running `java Main`. After the program starts, the user enters one full input string. The program will then process that input and either print the result of the final query along with any variable values found, or display an error message if the input does not follow the correct format.

## Example

An example of compiling and running the program is to first enter `javac *.java` in the terminal and then enter `java main`. After that, the program will wait for the user to type one full input string. Once the input is entered, the program will evaluate it and produce either a result or an error message.

## Design Choices

One of the main design choices in this assignment was to separate the program into multiple files so that each part of the process could be handled independently. The tokenizer is responsible for scanning, the parser is responsible for structure, and the interpreter is responsible for logic and evaluation. This made the program easier to debug, test, and understand. It also helped keep the code more organized instead of putting everything into one large file.

