import java.io.*;

/* Implements a recursive descent parser by extending AbstractSyntaxAnalyser.
Parses a simple Ada-like language per the provided grammar, tracing parsing actions with Generate
and reporting the first syntax error with detailed context via CompilationException. */

public class SyntaxAnalyser extends AbstractSyntaxAnalyser {

/* Parses the whole program starting with "begin" and ending with "end".
This is the entry point to check the program’s structure using a try-catch to handle errors
and trace the process. */

    public SyntaxAnalyser(String filename) throws IOException {
        this.lex = new LexicalAnalyser(filename);
        this.myGenerate = new Generate();
    }

    @Override
    public void _statementPart_() throws IOException, CompilationException {

/* Handles a list of statements separated by semicolons if present.
This processes multiple statements in the program by calling itself recursively after
a semicolon to continue parsing. */

        myGenerate.commenceNonterminal("StatementPart");
        try {
            acceptTerminal(Token.beginSymbol);
            _statementList_();
            acceptTerminal(Token.endSymbol);
        } catch (CompilationException e) {
            throw new CompilationException("in StatementPart on line: " + nextToken.lineNumber, e);
        }
        myGenerate.finishNonterminal("StatementPart");
    }

    private void _statementList_() throws IOException, CompilationException {

/* Decides which type of statement to parse based on the next token.
This is essential to support different statement types like "if" or "while".
It uses a switch to pick the right method or report an error. */

        myGenerate.commenceNonterminal("StatementList");
        try {
            _statement_();
            if (nextToken.symbol == Token.semicolonSymbol) {
                acceptTerminal(Token.semicolonSymbol);
                _statementList_();
            }
        } catch (CompilationException e) {
            throw new CompilationException("in StatementList on line: " + nextToken.lineNumber, e);
        }
        myGenerate.finishNonterminal("StatementList");
    }

    private void _statement_() throws IOException, CompilationException {

/* Decides which type of statement to parse based on the next token to support different 
statement types like "if" or "while". */

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
                    myGenerate.reportError(nextToken, "Expected a statement on line: " + nextToken.lineNumber);
            }
        } catch (CompilationException e) {
            throw new CompilationException("in Statement on line: " + nextToken.lineNumber, e);
        }
        myGenerate.finishNonterminal("Statement");
    }

    private void _assignmentStatement_() throws IOException, CompilationException {

/* Parses an assignment to check if a variable is set to a value or string as per the grammar.
It chooses between an expression or string constant based on the token. */

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
            throw new CompilationException("in AssignmentStatement on line: " + nextToken.lineNumber, e);
        }
        myGenerate.finishNonterminal("AssignmentStatement");
    }

    private void _ifStatement_() throws IOException, CompilationException {

/* Handles conditional logic in the language by checking tokens step-by-step 
and calling other methods for parts.*/

        int lineNumber = nextToken.lineNumber;
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
            throw new CompilationException("in IfStatement on line: " + nextToken.lineNumber, e);
        }
        myGenerate.finishNonterminal("IfStatement");
    }

    private void _whileStatement_() throws IOException, CompilationException {

/* Parses a "while" loop to support repeated execution based on a condition.
It tracks the line number for accurate error reporting. */

        int lineNumber = nextToken.lineNumber; 
        myGenerate.commenceNonterminal("WhileStatement");
        try {
            acceptTerminal(Token.whileSymbol);
            lineNumber = nextToken.lineNumber;
            _condition_();
            acceptTerminal(Token.loopSymbol);
            _statementList_();
            acceptTerminal(Token.endSymbol);
            acceptTerminal(Token.loopSymbol);
        } catch (CompilationException e) {
            throw new CompilationException("in WhileStatement on line: " + lineNumber, e); 
        }
        myGenerate.finishNonterminal("WhileStatement");
    }

    private void _procedureStatement_() throws IOException, CompilationException {

 /* Parses a procedure call. This handles function-like calls in the language.
It ensures the correct sequence of tokens is followed. */

        myGenerate.commenceNonterminal("ProcedureStatement");
        try {
            acceptTerminal(Token.callSymbol);
            acceptTerminal(Token.identifier);
            acceptTerminal(Token.leftParenthesis);
            _argumentList_();
            acceptTerminal(Token.rightParenthesis);
        } catch (CompilationException e) {
            throw new CompilationException("in ProcedureStatement on line: " + nextToken.lineNumber, e);
        }
        myGenerate.finishNonterminal("ProcedureStatement");
    }

    private void _untilStatement_() throws IOException, CompilationException {

/* Parses a "do-until" loop with statements and a condition. This allows execution until a condition is met.
It processes the body first, then checks the condition. */

        myGenerate.commenceNonterminal("UntilStatement");
        try {
            acceptTerminal(Token.doSymbol);
            _statementList_();
            acceptTerminal(Token.untilSymbol);
            _condition_();
        } catch (CompilationException e) {
            throw new CompilationException("in UntilStatement on line: " + nextToken.lineNumber, e);
        }
        myGenerate.finishNonterminal("UntilStatement");
    }

    private void _forStatement_() throws IOException, CompilationException {

/* Parses a "for" loop to support controlled iteration in the program.
This follows a strict token order to match the grammar. */

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
            throw new CompilationException("in ForStatement on line: " + nextToken.lineNumber, e);
        }
        myGenerate.finishNonterminal("ForStatement");
    }

    private void _argumentList_() throws IOException, CompilationException {

/* Parses a list of arguments for a procedure call.
This handles one or more identifiers separated by commas recursively. */

        myGenerate.commenceNonterminal("ArgumentList");
        try {
            acceptTerminal(Token.identifier);
            if (nextToken.symbol == Token.commaSymbol) {
                acceptTerminal(Token.commaSymbol);
                _argumentList_();
            }
        } catch (CompilationException e) {
            throw new CompilationException("in ArgumentList on line: " + nextToken.lineNumber, e);
        }
        myGenerate.finishNonterminal("ArgumentList");
    }

    private void _condition_() throws IOException, CompilationException {

/* Parses a condition for control statements for loops and if statements to work by checking
an identifier, operator, and a value. */

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
            throw new CompilationException("in Condition on line: " + nextToken.lineNumber, e);
        }
        myGenerate.finishNonterminal("Condition");
    }

    private void _conditionalOperator_() throws IOException, CompilationException {

/* Parses operators like ">" or "=" for conditions.
This ensures valid comparisons in the language.*/

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

    private void _expression_() throws IOException, CompilationException {

/* Parses expressions like addition or subtraction to support basic math operations in assignments. */

        myGenerate.commenceNonterminal("Expression");
        try {
            _term_();
            while (nextToken.symbol == Token.plusSymbol || nextToken.symbol == Token.minusSymbol) {
                acceptTerminal(nextToken.symbol);
                _term_();
            }
        } catch (CompilationException e) {
            throw new CompilationException("in Expression on line: " + nextToken.lineNumber, e);
        }
        myGenerate.finishNonterminal("Expression");
    }

    private void _term_() throws IOException, CompilationException {

/* Parses terms with multiplication or division. This breaks expressions into smaller parts for clarity. */

        myGenerate.commenceNonterminal("Term");
        try {
            _factor_();
            while (nextToken.symbol == Token.timesSymbol ||
                   nextToken.symbol == Token.divideSymbol ||
                   nextToken.symbol == Token.modSymbol) {
                acceptTerminal(nextToken.symbol);
                _factor_();
            }
        } catch (CompilationException e) {
            throw new CompilationException("in Term on line: " + nextToken.lineNumber, e);
        }
        myGenerate.finishNonterminal("Term");
    }

    private void _factor_() throws IOException, CompilationException {

/* Parses factors and handles the basic building blocks of expressions.
It checks the token type to decide what to parse. */

        myGenerate.commenceNonterminal("Factor");
        try {
            if (nextToken.symbol == Token.identifier) {
                acceptTerminal(Token.identifier);
            } else if (nextToken.symbol == Token.numberConstant) {
                acceptTerminal(Token.numberConstant);
            } else if (nextToken.symbol == Token.leftParenthesis) {
                acceptTerminal(Token.leftParenthesis);
                _expression_();
                acceptTerminal(Token.rightParenthesis);
            } else {
                myGenerate.reportError(nextToken, "Expected identifier, number, or ( on line: " + nextToken.lineNumber);
            }
        } catch (CompilationException e) {
            throw new CompilationException("in Factor on line: " + nextToken.lineNumber, e);
        }
        myGenerate.finishNonterminal("Factor");
    }

    @Override
    public void acceptTerminal(int symbol) throws IOException, CompilationException {

/* Checks if the next token matches the expected one and moves forward.
This validates syntax and advances the token stream.
It reports an error with token names if there’s a mismatch. */

        if (nextToken.symbol == symbol) {
            myGenerate.insertTerminal(nextToken);
            nextToken = lex.getNextToken();
        } else {
            myGenerate.reportError(nextToken, "Expected '" + Token.getName(symbol) + "' but found '" + Token.getName(nextToken.symbol) + "' on line: " + nextToken.lineNumber);
        }
    }
}
