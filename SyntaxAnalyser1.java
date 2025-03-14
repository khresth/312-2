import java.io.*;

/* This class implements a recursive descent parser by extending AbstractSyntaxAnalyser.
   It parses a simple Ada-like language based on a provided grammar, using a top-down approach.
   The parser traces its actions via the Generate class for debugging and reports the first syntax
   error with detailed context (line number and token) through CompilationException. */

public class SyntaxAnalyser extends AbstractSyntaxAnalyser {

/*  The constructor initializes the lexical analyzer with a source file and sets up the Generate
    object for tracing. This prepares the parser to process tokens from the input file. */

    public SyntaxAnalyser(String filename) throws IOException {
        this.lex = new LexicalAnalyser(filename);
        this.myGenerate = new Generate();
    }

    @Override
    public void _statementPart_() throws IOException, CompilationException {
        int lineNum = nextToken.lineNumber;

/*  Parsing the program's main structure, expecting "begin" followed by a statement list
    and "end". This method is the entry point for parsing, using try-catch to
    capture and rethrow errors with context for debugging. */

        myGenerate.commenceNonterminal("StatementPart");
        try {
            acceptTerminal(Token.beginSymbol);
            _statementList_();
            acceptTerminal(Token.endSymbol);
        } catch (CompilationException e) {
            throw new CompilationException("in StatementPart on line: " + lineNum, e);
        }
        myGenerate.finishNonterminal("StatementPart");
    }

    private void _statementList_() throws IOException, CompilationException {
        int lineNum = nextToken.lineNumber;

/*  Recursively processesing a list of statements separated by semicolons. This method
    handles multiple statements by parsing one, checking for a semicolon, and then
    calling itself to continue if more statements follow. */
 
        myGenerate.commenceNonterminal("StatementList");
        try {
            _statement_();
            if (nextToken.symbol == Token.semicolonSymbol) {
                acceptTerminal(Token.semicolonSymbol);
                _statementList_();
            }
        } catch (CompilationException e) {
            throw new CompilationException("in StatementList on line: " + lineNum, e);
        }
        myGenerate.finishNonterminal("StatementList");
    }

    private void _statement_() throws IOException, CompilationException {
        int lineNum = nextToken.lineNumber;

/*  Determining the type of statement to parse based on the next token.
    Delegating to the appropriate parsing method, ensuring the
    grammar's statement types are supported and reports errors for invalid tokens. */

        myGenerate.commenceNonterminal("Statement");
        try {
            switch (nextToken.symbol) {
                case Token.identifier:
                    _assignmentStatement_();
                    break;
                case Token.callSymbol:
                    _procedureStatement_();
                    break;
                case Token.ifSymbol:
                    _ifStatement_();
                    break;
                case Token.whileSymbol:
                    _whileStatement_();
                    break;
                case Token.doSymbol:
                    _untilStatement_();
                    break;
                case Token.forSymbol:
                    _forStatement_();
                    break;
                default:
                    myGenerate.reportError(nextToken, "Expected a statement on line: " + lineNum);
            }
        } catch (CompilationException e) {
            throw new CompilationException("in Statement on line: " + lineNum, e);
        }
        myGenerate.finishNonterminal("Statement");
    }

    private void _assignmentStatement_() throws IOException, CompilationException {
        int lineNum = nextToken.lineNumber;

/*  Parsing an assignment statement. It ensures an identifier
    is followed by ":=" and then either an expression or a string constant to support the
    grammar's flexibility. */

        myGenerate.commenceNonterminal("AssignmentStatement");
        try {
            acceptTerminal(Token.identifier);
            acceptTerminal(Token.becomesSymbol);
            if (nextToken.symbol == Token.stringConstant) {
                acceptTerminal(Token.stringConstant);
            } else {
                _expression_();
            }
        } catch (CompilationException e) {
            throw new CompilationException("in AssignmentStatement on line: " + lineNum, e);
        }
        myGenerate.finishNonterminal("AssignmentStatement");
    }

    private void _ifStatement_() throws IOException, CompilationException {
        int lineNum = nextToken.lineNumber;

/*  Parsing an "if" statement and optional "else" branch. It ensures the sequence
    "if condition then else statements end if" is followed, delegating to
    _condition_ and _statementList_ for subcomponents. */

        myGenerate.commenceNonterminal("IfStatement");
        try {
            acceptTerminal(Token.ifSymbol);
            _condition_();
            acceptTerminal(Token.thenSymbol);
            _statementList_();
            if (nextToken.symbol == Token.elseSymbol) {
                acceptTerminal(Token.elseSymbol);
                _statementList_();
            }
            acceptTerminal(Token.endSymbol);
            acceptTerminal(Token.ifSymbol);
        } catch (CompilationException e) {
            throw new CompilationException("in IfStatement on line: " + lineNum, e);
        }
        myGenerate.finishNonterminal("IfStatement");
    }

    private void _whileStatement_() throws IOException, CompilationException {
        int lineNum = nextToken.lineNumber;

/*  Parsesing a "while" loop to supports
    repeated execution based on conditionals, with error reporting tied to the current line. */
 
        myGenerate.commenceNonterminal("WhileStatement");
        try {
            acceptTerminal(Token.whileSymbol);
            _condition_();
            acceptTerminal(Token.loopSymbol);
            _statementList_();
            acceptTerminal(Token.endSymbol);
            acceptTerminal(Token.loopSymbol);
        } catch (CompilationException e) {
            throw new CompilationException("in WhileStatement on line: " + lineNum, e);
        }
        myGenerate.finishNonterminal("WhileStatement");
    }

    private void _procedureStatement_() throws IOException, CompilationException {
        int lineNum = nextToken.lineNumber;

/*  Parsing a procedure call to ensure the syntax is followed, delegating argument parsing 
    to _argumentList_ for comma-separated identifiers. */

        myGenerate.commenceNonterminal("ProcedureStatement");
        try {
            acceptTerminal(Token.callSymbol);
            acceptTerminal(Token.identifier);
            acceptTerminal(Token.leftParenthesis);
            _argumentList_();
            acceptTerminal(Token.rightParenthesis);
        } catch (CompilationException e) {
            throw new CompilationException("in ProcedureStatement on line: " + lineNum, e);
        }
        myGenerate.finishNonterminal("ProcedureStatement");
    }

    private void _untilStatement_() throws IOException, CompilationException {
        int lineNum = nextToken.lineNumber;

/*  Do-until" loop to checks the condition, matching the grammar's post-test loop structure. */

        myGenerate.commenceNonterminal("UntilStatement");
        try {
            acceptTerminal(Token.doSymbol);
            _statementList_();
            acceptTerminal(Token.untilSymbol);
            _condition_();
        } catch (CompilationException e) {
            throw new CompilationException("in UntilStatement on line: " + lineNum, e);
        }
        myGenerate.finishNonterminal("UntilStatement");
    }

    private void _forStatement_() throws IOException, CompilationException {
        int lineNum = nextToken.lineNumber;

/*  Parsing a "for" loop with initialization, condition, and update.
    Following a strict token sequence to enforce the grammar’s controlled iteration structure. */

        myGenerate.commenceNonterminal("ForStatement");
        try {
            acceptTerminal(Token.forSymbol);
            acceptTerminal(Token.leftParenthesis);
            _assignmentStatement_();
            acceptTerminal(Token.semicolonSymbol);
            _condition_();
            acceptTerminal(Token.semicolonSymbol);
            _assignmentStatement_();
            acceptTerminal(Token.rightParenthesis);
            acceptTerminal(Token.doSymbol);
            _statementList_();
            acceptTerminal(Token.endSymbol);
            acceptTerminal(Token.loopSymbol);
        } catch (CompilationException e) {
            throw new CompilationException("in ForStatement on line: " + lineNum, e);
        }
        myGenerate.finishNonterminal("ForStatement");
    }

    private void _argumentList_() throws IOException, CompilationException {
        int lineNum = nextToken.lineNumber;

/*  Recursively parsing a comma separated list of identifiers for procedure arguments.
    Handling one or more arguments by checking for commas and calling itself as needed. */

        myGenerate.commenceNonterminal("ArgumentList");
        try {
            acceptTerminal(Token.identifier);
            if (nextToken.symbol == Token.commaSymbol) {
                acceptTerminal(Token.commaSymbol);
                _argumentList_();
            }
        } catch (CompilationException e) {
            throw new CompilationException("in ArgumentList on line: " + lineNum, e);
        }
        myGenerate.finishNonterminal("ArgumentList");
    }

    private void _condition_() throws IOException, CompilationException {
        int lineNum = nextToken.lineNumber;

/*  Parsing a condition used in control structures. Ensuring an identifier,
    a identifier, number, or string are present. */

        myGenerate.commenceNonterminal("Condition");
        try {
            acceptTerminal(Token.identifier);
            _conditionalOperator_();
            if (nextToken.symbol == Token.identifier ||
                nextToken.symbol == Token.numberConstant ||
                nextToken.symbol == Token.stringConstant) {
                acceptTerminal(nextToken.symbol);
            } else {
                myGenerate.reportError(nextToken, "Expected identifier, number, or string after conditional operator on line: " + nextToken.lineNumber);
            }
        } catch (CompilationException e) {
            throw new CompilationException("in Condition on line: " + lineNum, e);
        }
        myGenerate.finishNonterminal("Condition");
    }

    private void _conditionalOperator_() throws IOException, CompilationException {

/*  Parsing comparison operators for conditions. Validates that a
    valid operator is present then reporting an error for unexpected tokens. */

        myGenerate.commenceNonterminal("ConditionalOperator");
        try {
            switch (nextToken.symbol) {
                case Token.lessThanSymbol:
                case Token.greaterThanSymbol:
                case Token.greaterEqualSymbol:
                case Token.equalSymbol:
                case Token.notEqualSymbol:
                case Token.lessEqualSymbol:
                    acceptTerminal(nextToken.symbol);
                    break;
                default:
                    myGenerate.reportError(nextToken, "Expected a conditional operator on line: " + nextToken.lineNumber);
            }
        } catch (CompilationException e) {
            throw new CompilationException("in ConditionalOperator on line: " + nextToken.lineNumber, e);
        }
        myGenerate.finishNonterminal("ConditionalOperator");
    }

    public void _expression_() throws IOException, CompilationException {
        int lineNum = nextToken.lineNumber;

/*  Parsing arithmetic expressions recursively. Handling addition and
    subtraction by parsing a term and checking for further operators. */

        myGenerate.commenceNonterminal("Expression");
        try {
            _term_();
            if (nextToken.symbol == Token.plusSymbol || nextToken.symbol == Token.minusSymbol) {
                acceptTerminal(nextToken.symbol);
                _expression_();
            }
        } catch (CompilationException e) {
            throw new CompilationException("Error in Expression line " + lineNum, e);
        }
        myGenerate.finishNonterminal("Expression");
    }

    public void _term_() throws IOException, CompilationException {
        int lineNum = nextToken.lineNumber;

/*  Parsing terms in expressions to supports multiplication, division,
    and modulus by breaking expressions into smaller factors recursively. */

        try {
            myGenerate.commenceNonterminal("Term");
            _factor_();
            switch (nextToken.symbol) {
                case Token.timesSymbol:
                case Token.divideSymbol:
                case Token.modSymbol:
                    acceptTerminal(nextToken.symbol);
                    _term_();
                    break;
                default:
                    break;
            }
        } catch (CompilationException e) {
            throw new CompilationException("Error in Term line " + lineNum, e);
        }
        myGenerate.finishNonterminal("Term");
    }

    private void _factor_() throws IOException, CompilationException {
        int lineNum = nextToken.lineNumber;
/*  Parsing the basic units of expressions then using a switch to handle different token 
    types and ensures proper syntax, like matching parentheses. */

        myGenerate.commenceNonterminal("Factor");
        switch (nextToken.symbol) {
            case Token.identifier:
                acceptTerminal(Token.identifier);
                break;
            case Token.numberConstant:
                acceptTerminal(Token.numberConstant);
                break;
            case Token.leftParenthesis:
                acceptTerminal(Token.leftParenthesis);
                _expression_();
                if (nextToken.symbol == Token.rightParenthesis) {
                    acceptTerminal(Token.rightParenthesis);
                } else {
                    myGenerate.reportError(nextToken, "Closing parenthesis missing ')' on line: " + lineNum);
                }
                break;
            default:
                myGenerate.reportError(nextToken, "Expected an identifier, number constant, or '('expression')' on line: " + lineNum);
        }
        myGenerate.finishNonterminal("Factor");
    }

    @Override
    public void acceptTerminal(int symbol) throws IOException, CompilationException {

/*  Validating that the next token matches the expected symbol and advances the token stream.
    Logs the token via Generate and reports mismatches with token names for clear error messages. */

        if (nextToken.symbol == symbol) {
            myGenerate.insertTerminal(nextToken);
            nextToken = lex.getNextToken();
        } else {
            myGenerate.reportError(nextToken, "Expected '" + Token.getName(symbol) + "' but found '" + Token.getName(nextToken.symbol) + "' on line: " + nextToken.lineNumber);
        }
    }
}
