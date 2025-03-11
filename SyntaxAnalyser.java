import java.io.*;

public class SyntaxAnalyser extends AbstractSyntaxAnalyser {

    public SyntaxAnalyser(String filename) throws IOException {
        this.lex = new LexicalAnalyser(filename);
        this.myGenerate = new Generate();
        // Do not fetch nextToken here; let parse() and acceptTerminal() handle it
    }

    @Override
    public void _statementPart_() throws IOException, CompilationException {
        myGenerate.commenceNonterminal("StatementPart");
        try {
            acceptTerminal(Token.beginSymbol);
            _statementList_();
            acceptTerminal(Token.endSymbol);
        } catch (CompilationException e) {
            throw new CompilationException("in StatementPart", e);
        }
        myGenerate.finishNonterminal("StatementPart");
    }

    private void _statementList_() throws IOException, CompilationException {
        myGenerate.commenceNonterminal("StatementList");
        try {
            _statement_();
            if (nextToken.symbol == Token.semicolonSymbol) {
                acceptTerminal(Token.semicolonSymbol);
                _statementList_();
            }
        } catch (CompilationException e) {
            throw new CompilationException("in StatementList", e);
        }
        myGenerate.finishNonterminal("StatementList");
    }

    private void _statement_() throws IOException, CompilationException {
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
                    myGenerate.reportError(nextToken, "Expected a statement");
            }
        } catch (CompilationException e) {
            throw new CompilationException("in Statement", e);
        }
        myGenerate.finishNonterminal("Statement");
    }

    private void _assignmentStatement_() throws IOException, CompilationException {
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
            throw new CompilationException("in AssignmentStatement", e);
        }
        myGenerate.finishNonterminal("AssignmentStatement");
    }

    private void _ifStatement_() throws IOException, CompilationException {
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
            throw new CompilationException("in IfStatement", e);
        }
        myGenerate.finishNonterminal("IfStatement");
    }

    private void _whileStatement_() throws IOException, CompilationException {
        myGenerate.commenceNonterminal("WhileStatement");
        try {
            acceptTerminal(Token.whileSymbol);
            _condition_();
            acceptTerminal(Token.loopSymbol);
            _statementList_();
            acceptTerminal(Token.endSymbol);
            acceptTerminal(Token.loopSymbol);
        } catch (CompilationException e) {
            throw new CompilationException("in WhileStatement", e);
        }
        myGenerate.finishNonterminal("WhileStatement");
    }

    private void _procedureStatement_() throws IOException, CompilationException {
        myGenerate.commenceNonterminal("ProcedureStatement");
        try {
            acceptTerminal(Token.callSymbol);
            acceptTerminal(Token.identifier);
            acceptTerminal(Token.leftParenthesis);
            _argumentList_();
            acceptTerminal(Token.rightParenthesis);
        } catch (CompilationException e) {
            throw new CompilationException("in ProcedureStatement", e);
        }
        myGenerate.finishNonterminal("ProcedureStatement");
    }

    private void _untilStatement_() throws IOException, CompilationException {
        myGenerate.commenceNonterminal("UntilStatement");
        try {
            acceptTerminal(Token.doSymbol);
            _statementList_();
            acceptTerminal(Token.untilSymbol);
            _condition_();
        } catch (CompilationException e) {
            throw new CompilationException("in UntilStatement", e);
        }
        myGenerate.finishNonterminal("UntilStatement");
    }

    private void _forStatement_() throws IOException, CompilationException {
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
            throw new CompilationException("in ForStatement", e);
        }
        myGenerate.finishNonterminal("ForStatement");
    }

    private void _argumentList_() throws IOException, CompilationException {
        myGenerate.commenceNonterminal("ArgumentList");
        try {
            acceptTerminal(Token.identifier);
            if (nextToken.symbol == Token.commaSymbol) {
                acceptTerminal(Token.commaSymbol);
                _argumentList_();
            }
        } catch (CompilationException e) {
            throw new CompilationException("in ArgumentList", e);
        }
        myGenerate.finishNonterminal("ArgumentList");
    }

    private void _condition_() throws IOException, CompilationException {
        myGenerate.commenceNonterminal("Condition");
        try {
            acceptTerminal(Token.identifier);
            _conditionalOperator_();
            if (nextToken.symbol == Token.identifier ||
                nextToken.symbol == Token.numberConstant ||
                nextToken.symbol == Token.stringConstant) {
                acceptTerminal(nextToken.symbol);
            } else {
                myGenerate.reportError(nextToken, "Expected identifier, number, or string after conditional operator");
            }
        } catch (CompilationException e) {
            throw new CompilationException("in Condition", e);
        }
        myGenerate.finishNonterminal("Condition");
    }

    private void _conditionalOperator_() throws IOException, CompilationException {
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
                    myGenerate.reportError(nextToken, "Expected a conditional operator");
            }
        } catch (CompilationException e) {
            throw new CompilationException("in ConditionalOperator", e);
        }
        myGenerate.finishNonterminal("ConditionalOperator");
    }

    private void _expression_() throws IOException, CompilationException {
        myGenerate.commenceNonterminal("Expression");
        try {
            _term_();
            while (nextToken.symbol == Token.plusSymbol || nextToken.symbol == Token.minusSymbol) {
                acceptTerminal(nextToken.symbol);
                _term_();
            }
        } catch (CompilationException e) {
            throw new CompilationException("in Expression", e);
        }
        myGenerate.finishNonterminal("Expression");
    }

    private void _term_() throws IOException, CompilationException {
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
            throw new CompilationException("in Term", e);
        }
        myGenerate.finishNonterminal("Term");
    }

    private void _factor_() throws IOException, CompilationException {
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
                myGenerate.reportError(nextToken, "Expected identifier, number, or (");
            }
        } catch (CompilationException e) {
            throw new CompilationException("in Factor", e);
        }
        myGenerate.finishNonterminal("Factor");
    }

    @Override
    public void acceptTerminal(int symbol) throws IOException, CompilationException {
        if (nextToken.symbol == symbol) {
            myGenerate.insertTerminal(nextToken);
            nextToken = lex.getNextToken();
        } else {
            myGenerate.reportError(nextToken, "Expected '" + Token.getName(symbol) + "' but found '" + Token.getName(nextToken.symbol) + "'");
        }
    }
}
