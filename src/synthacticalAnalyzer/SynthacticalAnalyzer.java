package synthacticalAnalyzer;
import AST.ASTNode;
import AST.SemanticActions;
import lexicalAnalyzer.LexicalAnalyzer;
import lexicalAnalyzer.Token;

import java.io.*;
import java.util.ArrayList;
import java.util.Stack;

public class SynthacticalAnalyzer {
    private Token lookahead;
    private LexicalAnalyzer lexer;
    private PrintWriter pwDerivation = new PrintWriter(new FileWriter("parser/example-polynomial.outderivation.src", true));
    private PrintWriter pwError = new PrintWriter(new FileWriter("parser/example-polynomial.outsyntaxerror.src", true));
    private String currentLHS = "";
    private ArrayList<String> LHSInError = new ArrayList<String>();
    private String missingStatementMessage = "Missing Statement expected";
    private boolean inErrorRecovery = false;
    private SemanticActions semanticActions = new SemanticActions(new Stack<ASTNode>());
    public SynthacticalAnalyzer(LexicalAnalyzer lexer) throws IOException {
        this.lexer = lexer;
        this.lookahead = lexer.nextToken();

    }

    private boolean match(Token.TokenType terminal) throws IOException {
        String signature = Thread.currentThread().getStackTrace()[2].getMethodName();
        if (!LHSInError.contains(signature))
            inErrorRecovery = false;
        else
            inErrorRecovery = true;

        if(getType() == terminal){
            if(signature.equals("classDecl") && getType() == Token.TokenType.ID) {
                semanticActions.createId(lookahead);
                semanticActions.createEpsilon(null);
            } else if(signature.equals("visibility")){
              semanticActions.createVisibility(lookahead);
            } else {
                semanticActions.createId(lookahead);
            }
            write(signature, terminal.getValue());
            skip();
        } else if (inErrorRecovery && LHSInError.contains(signature)) {
            errorRecovery(signature, terminal.getValue());
        } else {
            //first error detected
            LHSInError.add(signature);
            errorRecovery(signature, terminal.getValue());
        }
        return true;
    }

    private void write(String LHS, String RHS) {
        if(LHS.equals(currentLHS)){
            pwDerivation.print(" " + RHS);
        } else {
            pwDerivation.print("\n" + LHS + " -> " + RHS);
            currentLHS = LHS;
        }

    }

    private void skip() throws IOException {
        lookahead = lexer.nextToken();
    }

    private void error(String LHS, String RHS) {
        pwError.println(LHS + " -> " + RHS);

    }
    private void errorRecovery(String signature, String terminal) throws IOException {
        if(!inErrorRecovery){
            //Executed the first time an error is found
            inErrorRecovery = true;
            error(currentLHS, "Expecting: " + terminal + " at: " + getLocation().split(" ")[0]);
        }

    }
    private String funcName(){
        return Thread.currentThread().getStackTrace()[2].getMethodName();
    }

    public boolean parse() throws IOException {

        boolean start = start();
        pwDerivation.close();
        pwError.close();
        return start;

    }

    private Token.TokenType getType() {
        return lookahead.getType();
    }
    private String getLocation(){
        return lookahead.getLocation();
    }
    private String getLexeme(){
        return lookahead.getLexeme();
    }
    //START                      -> PROG eof
    private boolean start() throws IOException {
        if(prog() && match(Token.TokenType.EOF)){
            return true;
        } else {
            return false;
        }
    }

    //CLASSDECLORFUNCDEF         -> FUNCDEF | CLASSDECL
    private boolean classDeclOrFuncDef() throws IOException {
        if (getType() == Token.TokenType.RESERVED_CLASS) {
            if(classDecl() || inErrorRecovery){
                inErrorRecovery = false;
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.RESERVED_FUNCTION) {
            if(funcDef() || inErrorRecovery){
                inErrorRecovery = false;
                return true;
            } else {
                return false;
            }
        } else {
            error(funcName(), missingStatementMessage);
            return true;
        }
    }

    //CLASSDECL                  -> class id OPTINHERITS lcurbr REPTMEMBERDECL rcurbr semi
    private boolean classDecl() throws IOException {
        if (getType() == Token.TokenType.RESERVED_CLASS) {
            if (match(Token.TokenType.RESERVED_CLASS) && match(Token.TokenType.ID) && optInherits() && match(Token.TokenType.PUNCTUATION_LCURLY)
                    && reptMemberDecl() && match(Token.TokenType.PUNCTUATION_RCURLY) && match(Token.TokenType.PUNCTUATION_SEMICOLON) || inErrorRecovery) {
                inErrorRecovery = false;
                return true;
            } else {
                return false;
            }
        } else {
            error(funcName(), missingStatementMessage);
            return true;
        }
    }

    //FUNCDEF                    -> FUNCHEAD FUNCBODY
    private boolean funcDef() throws IOException {
        if (getType() == Token.TokenType.RESERVED_FUNCTION) {
            if (funcHead() && funcBody() || inErrorRecovery) {
                inErrorRecovery = false;
                return true;
            } else {
                return false;
            }
        } else {
            error(funcName(), missingStatementMessage);
            return true;
        }
    }

    //FUNCHEAD                   -> function id FUNCHEADTAIL
    private boolean funcHead() throws IOException {
        if (getType() == Token.TokenType.RESERVED_FUNCTION) {
            if (match(Token.TokenType.RESERVED_FUNCTION) && match(Token.TokenType.ID) && funcHeadTail() || inErrorRecovery) {
                inErrorRecovery = false;
                return true;
            } else {
                return false;
            }
        } else {
            error(funcName(), missingStatementMessage);
            return true;
        }
    }

    //FUNCBODY                   -> lcurbr REPTLOCALVARORSTAT rcurbr
    private boolean funcBody() throws IOException {
        if (getType() == Token.TokenType.PUNCTUATION_LCURLY) {
            if (match(Token.TokenType.PUNCTUATION_LCURLY) && reptLocalVarOrStat() && match(Token.TokenType.PUNCTUATION_RCURLY) || inErrorRecovery) {
                inErrorRecovery = false;
                return true;
            } else {
                return false;
            }
        } else {
            error(funcName(), missingStatementMessage);
            return true;
        }
    }

    //REPTLOCALVARORSTAT         -> LOCALVARORSTAT REPTLOCALVARORSTAT | EPSILON
    private boolean reptLocalVarOrStat() throws IOException {
        if (getType() == Token.TokenType.RESERVED_IF || getType() == Token.TokenType.RESERVED_READ || getType() == Token.TokenType.RESERVED_RETURN
                || getType() == Token.TokenType.RESERVED_WHILE || getType() == Token.TokenType.RESERVED_WRITE || getType() == Token.TokenType.ID
                || getType() == Token.TokenType.RESERVED_LOCALVAR) {
            if (localVarOrStat() && reptLocalVarOrStat() || inErrorRecovery) {
                inErrorRecovery = false;
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.PUNCTUATION_RCURLY) {
            return true;
        } else {
            error(funcName(), missingStatementMessage);
            return true;
        }
    }
    //LOCALVARORSTAT             -> STATEMENT | LOCALVARDECL
    private boolean localVarOrStat() throws IOException {
        if (getType() == Token.TokenType.RESERVED_LOCALVAR) {
            if (localVarDecl() || inErrorRecovery) {
                inErrorRecovery = false;
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.RESERVED_IF || getType() == Token.TokenType.RESERVED_READ || getType() == Token.TokenType.RESERVED_RETURN
                || getType() == Token.TokenType.RESERVED_WHILE || getType() == Token.TokenType.RESERVED_WRITE || getType() == Token.TokenType.ID) {
            if (statement() || inErrorRecovery) {
                inErrorRecovery = false;
                return true;
            } else {
                return false;
            }
        } else {
            error(funcName(), missingStatementMessage);
            return true;
        }
    }

    //STATEMENT                  -> if lpar RELEXPR rpar then STATBLOCK else STATBLOCK semi
    //                               | read lpar VARIABLE rpar semi
    //                               | return lpar EXPR rpar semi
    //                               | while lpar RELEXPR rpar STATBLOCK semi
    //                               | write lpar EXPR rpar semi
    //                               | id STATEMENTIDNEST semi
    private boolean statement() throws IOException {
        if (getType() == Token.TokenType.ID) {
            if (match(Token.TokenType.ID) && statementIdnest() && match(Token.TokenType.PUNCTUATION_SEMICOLON) || inErrorRecovery) {
                inErrorRecovery = false;
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.RESERVED_WRITE) {
            if (match(Token.TokenType.RESERVED_WRITE) && match(Token.TokenType.PUNCTUATION_LPAREN) && expr() && match(Token.TokenType.PUNCTUATION_RPAREN)
                    && match(Token.TokenType.PUNCTUATION_SEMICOLON) || inErrorRecovery) {
                inErrorRecovery = false;
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.RESERVED_WHILE) {
            if (match(Token.TokenType.RESERVED_WHILE) && match(Token.TokenType.PUNCTUATION_LPAREN) && relExpr() && match(Token.TokenType.PUNCTUATION_RPAREN)
                    && statBlock() && match(Token.TokenType.PUNCTUATION_SEMICOLON) || inErrorRecovery) {
                inErrorRecovery = false;
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.RESERVED_RETURN) {
            if (match(Token.TokenType.RESERVED_RETURN) && match(Token.TokenType.PUNCTUATION_LPAREN) && expr() && match(Token.TokenType.PUNCTUATION_RPAREN)
                  && statBlock() && match(Token.TokenType.PUNCTUATION_SEMICOLON) || inErrorRecovery) {
                inErrorRecovery = false;
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.RESERVED_READ) {
            if (match(Token.TokenType.RESERVED_READ) && match(Token.TokenType.PUNCTUATION_LPAREN) && variable() && match(Token.TokenType.PUNCTUATION_RPAREN)
                    && match(Token.TokenType.PUNCTUATION_SEMICOLON) || inErrorRecovery) {
                inErrorRecovery = false;
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.RESERVED_IF) {
            if (match(Token.TokenType.RESERVED_IF) && match(Token.TokenType.PUNCTUATION_LPAREN) && relExpr() && match(Token.TokenType.PUNCTUATION_RPAREN)
                    && match(Token.TokenType.RESERVED_THEN) && statBlock() && match(Token.TokenType.RESERVED_ELSE) && statBlock() && match(Token.TokenType.PUNCTUATION_SEMICOLON) || inErrorRecovery) {
                inErrorRecovery = false;
                return true;
            } else {
                return false;
            }
        } else {
            error(funcName(), missingStatementMessage);
            return true;
        }
    }

    // STATEMENTIDNEST            -> lpar APARAMS rpar STATEMENTIDNEST2
    //                               | dot id STATEMENTIDNEST
    //                               | ASSIGNOP EXPR
    //                               | INDICE REPTIDNEST1 STATEMENTIDNEST3
    private boolean statementIdnest() throws IOException {
        if(getType() == Token.TokenType.PUNCTUATION_LBRACKET){
            if(indice() && reptIdnest1() && statementIdnest3() || inErrorRecovery){
                inErrorRecovery = false;
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.OPERATOR_ASSIGN) {
            if(assignOp() && expr() || inErrorRecovery){
                inErrorRecovery = false;
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.PUNCTUATION_DOT) {
            if (match(Token.TokenType.PUNCTUATION_DOT) && match(Token.TokenType.ID) && statementIdnest() || inErrorRecovery){
                inErrorRecovery = false;
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.PUNCTUATION_LPAREN) {
            if(match(Token.TokenType.PUNCTUATION_LPAREN) && aParams() && match(Token.TokenType.PUNCTUATION_RPAREN) && statementIdnest2() || inErrorRecovery){
                inErrorRecovery = false;
                return true;
            } else {
                return false;
            }
        } else {
            error(funcName(), missingStatementMessage);
            return true;
        }
    }

    //STATEMENTIDNEST2           -> dot id STATEMENTIDNEST
    //                               | EPSILON
    private boolean statementIdnest2() throws IOException {
        if(getType() == Token.TokenType.PUNCTUATION_DOT){
            if(match(Token.TokenType.PUNCTUATION_DOT) && match(Token.TokenType.ID) && statementIdnest() || inErrorRecovery){
                inErrorRecovery = false;
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.PUNCTUATION_SEMICOLON) {
            return true;
        } else {
            error(funcName(), missingStatementMessage);
            return true;
        }
    }
    
    //STATEMENTIDNEST3           -> dot id STATEMENTIDNEST
    //                               | ASSIGNOP EXPR
    private boolean statementIdnest3() throws IOException {
        if(getType() == Token.TokenType.PUNCTUATION_DOT){
            if(match(Token.TokenType.PUNCTUATION_DOT) && match(Token.TokenType.ID) && statementIdnest() || inErrorRecovery){
                inErrorRecovery = false;
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.OPERATOR_ASSIGN) {
            if(assignOp() && expr() || inErrorRecovery){
                inErrorRecovery = false;
                return true;
            } else {
                return false;
            }
        } else {
            error(funcName(), missingStatementMessage);
            return true;
        }
    }

    // IDNEST                     -> dot id IDNEST2
    private boolean idnest() throws IOException {
        if (getType() == Token.TokenType.PUNCTUATION_DOT) {
            if (match(Token.TokenType.PUNCTUATION_DOT) && match(Token.TokenType.ID) && idnest2() || inErrorRecovery) {
                inErrorRecovery = false;
                return true;
            } else {
                return false;
            }
        } else {
            error(funcName(), missingStatementMessage);
            return true;
        }
    }
    //IDNEST2                    -> lpar APARAMS rpar | REPTIDNEST1
    //follow: rpar, comma, semi, eq, geq, gt, leq, lt, neq, rsqbr, minus, or, plus, and, div, mult, dot
    private boolean idnest2() throws IOException {
        if (getType() == Token.TokenType.PUNCTUATION_LBRACKET || getType() == Token.TokenType.PUNCTUATION_RPAREN || getType() == Token.TokenType.PUNCTUATION_COMMA || getType() == Token.TokenType.PUNCTUATION_SEMICOLON
        || getType() == Token.TokenType.OPERATOR_EQ || getType() == Token.TokenType.OPERATOR_GE || getType() == Token.TokenType.OPERATOR_GT || getType() == Token.TokenType.OPERATOR_LE
            || getType() == Token.TokenType.OPERATOR_LT || getType() == Token.TokenType.OPERATOR_NE || getType() == Token.TokenType.PUNCTUATION_RBRACKET || getType() == Token.TokenType.OPERATOR_MINUS
            || getType() == Token.TokenType.RESERVED_OR || getType() == Token.TokenType.OPERATOR_PLUS || getType() == Token.TokenType.RESERVED_AND || getType() == Token.TokenType.OPERATOR_DIV
            || getType() == Token.TokenType.OPERATOR_MULT || getType() == Token.TokenType.PUNCTUATION_DOT) {
            if (reptIdnest1() || inErrorRecovery) {
                inErrorRecovery = false;
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.PUNCTUATION_LPAREN) {
            if (match(Token.TokenType.PUNCTUATION_LPAREN) && aParams() && match(Token.TokenType.PUNCTUATION_RPAREN) || inErrorRecovery) {
                inErrorRecovery = false;
                return true;
            } else {
                return false;
            }
        } else {
            error(funcName(), missingStatementMessage);
            return true;
        }
    }
    // REPTIDNEST1                -> INDICE REPTIDNEST1 | EPSILON
    //follow: rpar, comma, semi, eq, geq, gt, leq, lt, neq, rsqbr, minus, or, plus, and, div, mult, dot, equal
    private boolean reptIdnest1() throws IOException {
        if (getType() == Token.TokenType.PUNCTUATION_LBRACKET) {
            if (indice() && reptIdnest1() || inErrorRecovery) {
                inErrorRecovery = false;
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.PUNCTUATION_RPAREN || getType() == Token.TokenType.PUNCTUATION_COMMA || getType() == Token.TokenType.PUNCTUATION_SEMICOLON
        || getType() == Token.TokenType.OPERATOR_EQ || getType() == Token.TokenType.OPERATOR_GE || getType() == Token.TokenType.OPERATOR_GT
        || getType() == Token.TokenType.OPERATOR_LE || getType() == Token.TokenType.OPERATOR_LT || getType() == Token.TokenType.OPERATOR_NE
        || getType() == Token.TokenType.PUNCTUATION_RBRACKET || getType() == Token.TokenType.OPERATOR_MINUS || getType() == Token.TokenType.RESERVED_OR
        || getType() == Token.TokenType.OPERATOR_PLUS || getType() == Token.TokenType.OPERATOR_DIV || getType() == Token.TokenType.RESERVED_AND
        || getType() == Token.TokenType.OPERATOR_MULT || getType() == Token.TokenType.PUNCTUATION_DOT || getType() == Token.TokenType.OPERATOR_ASSIGN) {
            return true;
        } else {
            error(funcName(), missingStatementMessage);
            return true;
        }
    }



    //ASSIGNOP                   -> equal
    private boolean assignOp() throws IOException {
        if (getType() == Token.TokenType.OPERATOR_ASSIGN) {
            if (match(Token.TokenType.OPERATOR_ASSIGN) || inErrorRecovery) {
                inErrorRecovery = false;
                return true;
            } else {
                return false;
            }
        } else {
            error(funcName(), missingStatementMessage);
            return true;
        }
    }

    //VARIABLE                   -> id VARIABLE2
    private boolean variable() throws IOException {
        if (getType() == Token.TokenType.ID) {
            if (match(Token.TokenType.ID) && variable2() || inErrorRecovery) {
                inErrorRecovery = false;
                return true;
            } else {
                return false;
            }
        } else {
            error(funcName(), missingStatementMessage);
            return true;
        }
    }

    //VARIABLE2                  -> lpar APARAMS rpar VARIDNEST
    //                               | REPTIDNEST1 REPTVARIABLE
    private boolean variable2() throws IOException {
        if(getType() == Token.TokenType.PUNCTUATION_LPAREN){
            if (match(Token.TokenType.PUNCTUATION_LPAREN) && aParams() && match(Token.TokenType.PUNCTUATION_RPAREN) && varIdnest() || inErrorRecovery) {
                inErrorRecovery = false;
                return true;
            } else{
                return false;
            }
        } else if (getType() == Token.TokenType.PUNCTUATION_LBRACKET || getType() == Token.TokenType.PUNCTUATION_DOT || getType() == Token.TokenType.PUNCTUATION_RPAREN) {
             if(reptIdnest1() && reptVariable() || inErrorRecovery){
                 inErrorRecovery = false;
                 return true;
             } else {
                 return false;
             }
        } else {
            return true;
        }
    }
    //INDICE                     -> lsqbr ARITHEXPR rsqbr
    private boolean indice() throws IOException {
        if (getType() == Token.TokenType.PUNCTUATION_LBRACKET) {
            if (match(Token.TokenType.PUNCTUATION_LBRACKET) && arithExpr() && match(Token.TokenType.PUNCTUATION_RBRACKET) || inErrorRecovery) {
                inErrorRecovery = false;
                return true;
            } else {
                return false;
            }
        } else {
            error(funcName(), missingStatementMessage);
            return true;
        }
    }

    //RELEXPR                    -> ARITHEXPR RELOP ARITHEXPR
    private boolean relExpr() throws IOException {
        if (getType() == Token.TokenType.PUNCTUATION_LPAREN || getType() == Token.TokenType.FLOAT || getType() == Token.TokenType.INTEGER
                || getType() == Token.TokenType.RESERVED_NOT || getType() == Token.TokenType.ID || getType() == Token.TokenType.OPERATOR_PLUS
                || getType() == Token.TokenType.OPERATOR_MINUS) {
            if (arithExpr() && relOp() && arithExpr() || inErrorRecovery) {
                inErrorRecovery = false;
                return true;
            } else {
                return false;
            }
        } else {
            error(funcName(), missingStatementMessage);
            return true;
        }
    }

    // STATBLOCK                  -> lcurbr REPTSTATBLOCK1 rcurbr
    //                               | STATEMENT
    //                               | EPSILON
    private boolean statBlock() throws IOException {
        if (getType() == Token.TokenType.RESERVED_IF || getType() == Token.TokenType.RESERVED_READ || getType() == Token.TokenType.RESERVED_RETURN
                || getType() == Token.TokenType.RESERVED_WHILE || getType() == Token.TokenType.RESERVED_WRITE || getType() == Token.TokenType.ID) {
            if (statement() || inErrorRecovery) {
                inErrorRecovery = false;
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.PUNCTUATION_LCURLY) {
            if (match(Token.TokenType.PUNCTUATION_LCURLY) && reptStatBlock1() && match(Token.TokenType.PUNCTUATION_RCURLY) || inErrorRecovery) {
                inErrorRecovery = false;
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.RESERVED_ELSE || getType() == Token.TokenType.PUNCTUATION_SEMICOLON) {
            return true;
        } else {
            error(funcName(), missingStatementMessage);
            return true;
        }
    }

    // REPTSTATBLOCK1             -> STATEMENT REPTSTATBLOCK1 | EPSILON
    private boolean reptStatBlock1() throws IOException {
        if (getType() == Token.TokenType.RESERVED_IF || getType() == Token.TokenType.RESERVED_READ || getType() == Token.TokenType.RESERVED_RETURN
                || getType() == Token.TokenType.RESERVED_WHILE || getType() == Token.TokenType.RESERVED_WRITE || getType() == Token.TokenType.ID) {
            if (statement() && reptStatBlock1() || inErrorRecovery) {
                inErrorRecovery = false;
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.PUNCTUATION_RCURLY) {
            return true;
        } else {
            error(funcName(), missingStatementMessage);
            return true;
        }
    }
    //LOCALVARDECL               -> localvar id colon TYPE ARRAYOROBJECT semi
    private boolean localVarDecl() throws IOException {
        if (getType() == Token.TokenType.RESERVED_LOCALVAR) {
            if (match(Token.TokenType.RESERVED_LOCALVAR) && match(Token.TokenType.ID) && match(Token.TokenType.PUNCTUATION_COLON) && Type()
                   && arrayOrObject() && match(Token.TokenType.PUNCTUATION_SEMICOLON) || inErrorRecovery) {
                inErrorRecovery = false;
                return true;
            } else {
                return false;
            }
        } else {
            error(funcName(), missingStatementMessage);
            return true;
        }
    }

    //APARAMS                    -> EXPR REPTAPARAMS1 | EPSILON
    //first: {lpar, floatlit, id, intlit, not, minus, plus}
    private boolean aParams() throws IOException {
        if (getType() == Token.TokenType.PUNCTUATION_LPAREN || getType() == Token.TokenType.FLOAT || getType() == Token.TokenType.INTEGER
                || getType() == Token.TokenType.RESERVED_NOT || getType() == Token.TokenType.ID || getType() == Token.TokenType.OPERATOR_PLUS
                || getType() == Token.TokenType.OPERATOR_MINUS) {
            if (expr() && reptAParams1() || inErrorRecovery) {
                inErrorRecovery = false;
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.PUNCTUATION_RPAREN) {
            return true;
        } else {
            error(funcName(), missingStatementMessage);
            return true;
        }
    }

    //EXPR                       -> ARITHEXPR EXPR2
    //FIRST(0) = {lpar, floatlit, id, intlit, not, minus, plus}
    private boolean expr() throws IOException {
        if (getType() == Token.TokenType.PUNCTUATION_LPAREN || getType() == Token.TokenType.FLOAT || getType() == Token.TokenType.INTEGER
                || getType() == Token.TokenType.RESERVED_NOT || getType() == Token.TokenType.ID || getType() == Token.TokenType.OPERATOR_PLUS
                || getType() == Token.TokenType.OPERATOR_MINUS) {
            if (arithExpr() && expr2() || inErrorRecovery) {
                inErrorRecovery = false;
                return true;
            } else {
                return false;
            }
        } else {
            error(funcName(), missingStatementMessage);
            return true;
        }
    }

    //EXPR2                      -> RELOP ARITHEXPR | EPSILON
    private boolean expr2() throws IOException {
        if (getType() == Token.TokenType.OPERATOR_EQ || getType() == Token.TokenType.OPERATOR_GT || getType() == Token.TokenType.OPERATOR_GE
                || getType() == Token.TokenType.OPERATOR_LE || getType() == Token.TokenType.OPERATOR_LT || getType() == Token.TokenType.OPERATOR_NE) {
            if (relOp() && arithExpr() || inErrorRecovery) {
                inErrorRecovery = false;
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.PUNCTUATION_RPAREN || getType() == Token.TokenType.PUNCTUATION_COMMA
                || getType() == Token.TokenType.PUNCTUATION_SEMICOLON) {
            return true;
        } else {
            error(funcName(), missingStatementMessage);
            return true;
        }
    }

    //RELOP                      -> eq | geq | gt | leq | lt | neq
    private boolean relOp() throws IOException {
        if (getType() == Token.TokenType.OPERATOR_GT) {
            if (match(Token.TokenType.OPERATOR_GT) || inErrorRecovery) {
                inErrorRecovery = false;
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.OPERATOR_GE) {
            if (match(Token.TokenType.OPERATOR_GE) || inErrorRecovery) {
                inErrorRecovery = false;
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.OPERATOR_LE) {
            if (match(Token.TokenType.OPERATOR_LE) || inErrorRecovery) {
                inErrorRecovery = false;
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.OPERATOR_LT) {
            if (match(Token.TokenType.OPERATOR_LT) || inErrorRecovery) {
                inErrorRecovery = false;
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.OPERATOR_NE) {
            if (match(Token.TokenType.OPERATOR_NE) || inErrorRecovery) {
                inErrorRecovery = false;
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.OPERATOR_EQ) {
            if (match(Token.TokenType.OPERATOR_EQ) || inErrorRecovery) {
                inErrorRecovery = false;
                return true;
            } else {
                return false;
            }
        } else {
            error(funcName(), missingStatementMessage);
            return true;
        }
    }

    //ARITHEXPR                  -> TERM RIGHTRECARITHEXPR
    //FIRST(0) = {	lpar, floatlit, id, intlit, not, minus, plus}
    private boolean arithExpr() throws IOException {
        if (getType() == Token.TokenType.PUNCTUATION_LPAREN || getType() == Token.TokenType.FLOAT || getType() == Token.TokenType.INTEGER
                || getType() == Token.TokenType.RESERVED_NOT || getType() == Token.TokenType.ID || getType() == Token.TokenType.OPERATOR_PLUS
                || getType() == Token.TokenType.OPERATOR_MINUS) {
            if (term() && rightRecArithExpr() || inErrorRecovery) {
                inErrorRecovery = false;
                return true;
            } else {
                return false;
            }
        } else {
            error(funcName(), missingStatementMessage);
            return true;
        }
    }
    //RIGHTRECARITHEXPR          -> ADDOP TERM RIGHTRECARITHEXPR
    //                               | EPSILON
    //follow: rpar, comma, semi, eq, geq, gt, leq, lt, neq, rsqbr
    private boolean rightRecArithExpr() throws IOException {
        if (getType() == Token.TokenType.OPERATOR_PLUS || getType() == Token.TokenType.OPERATOR_MINUS || getType() == Token.TokenType.RESERVED_OR) {
            if (addOp() && term() && rightRecArithExpr() || inErrorRecovery) {
                inErrorRecovery = false;
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.PUNCTUATION_RPAREN || getType() == Token.TokenType.PUNCTUATION_COMMA || getType() == Token.TokenType.PUNCTUATION_SEMICOLON
                || getType() == Token.TokenType.OPERATOR_EQ || getType() == Token.TokenType.OPERATOR_GE || getType() == Token.TokenType.OPERATOR_GT || getType() == Token.TokenType.OPERATOR_LE
                || getType() == Token.TokenType.OPERATOR_LT || getType() == Token.TokenType.OPERATOR_NE || getType() == Token.TokenType.PUNCTUATION_RBRACKET) {
            return true;
        } else {
            error(funcName(), missingStatementMessage);
            return true;
        }
    }

    //   ADDOP                      -> minus | or | plus
    private boolean addOp() throws IOException {
        if (getType() == Token.TokenType.RESERVED_OR) {
            if (match(Token.TokenType.RESERVED_OR) || inErrorRecovery) {
                inErrorRecovery = false;
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.OPERATOR_PLUS) {
            if (match(Token.TokenType.OPERATOR_PLUS) || inErrorRecovery) {
                inErrorRecovery = false;
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.OPERATOR_MINUS) {
            if (match(Token.TokenType.OPERATOR_MINUS) || inErrorRecovery) {
                inErrorRecovery = false;
                return true;
            } else {
                return false;
            }
        } else {
            error(funcName(), missingStatementMessage);
            return true;
        }
    }

    // TERM                       -> FACTOR RIGHTRECTERM
    private boolean term() throws IOException {
        if (getType() == Token.TokenType.PUNCTUATION_LPAREN || getType() == Token.TokenType.FLOAT || getType() == Token.TokenType.INTEGER
                || getType() == Token.TokenType.RESERVED_NOT || getType() == Token.TokenType.ID || getType() == Token.TokenType.OPERATOR_PLUS
                || getType() == Token.TokenType.OPERATOR_MINUS) {
            if (factor() && rightRecTerm() || inErrorRecovery) {
                inErrorRecovery = false;
                return true;
            } else {
                return false;
            }
        } else {
            error(funcName(), missingStatementMessage);
            return true;
        }
    }

    //FACTOR                     -> lpar ARITHEXPR rpar
    //                               | floatlit
    //                               | id FACTOR2 REPTVARIABLEORFUNCTIONCALL
    //                               | intlit
    //                               | not FACTOR
    //                               | SIGN FACTOR
    private boolean factor() throws IOException {
        if (getType() == Token.TokenType.OPERATOR_PLUS || getType() == Token.TokenType.OPERATOR_MINUS) {
            if (sign() && factor() || inErrorRecovery) {
                inErrorRecovery = false;
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.ID) {
            if (match(Token.TokenType.ID) && factor2() && reptVariableOrFunctionCall() || inErrorRecovery) {
                inErrorRecovery = false;
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.RESERVED_NOT) {
            if (match(Token.TokenType.RESERVED_NOT) && factor() || inErrorRecovery) {
                inErrorRecovery = false;
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.INTEGER) {
            if (match(Token.TokenType.INTEGER) || inErrorRecovery) {
                inErrorRecovery = false;
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.FLOAT) {
            if (match(Token.TokenType.FLOAT) || inErrorRecovery) {
                inErrorRecovery = false;
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.PUNCTUATION_LPAREN) {
            if (match(Token.TokenType.PUNCTUATION_LPAREN) && arithExpr() && match(Token.TokenType.PUNCTUATION_RPAREN) || inErrorRecovery) {
                inErrorRecovery = false;
                return true;
            } else {
                return false;
            }
        } else {
            error(funcName(), missingStatementMessage);
            return true;
        }
    }

    //SIGN                       -> minus | plus
    private boolean sign() throws IOException {
        if (getType() == Token.TokenType.OPERATOR_MINUS) {
            if (match(Token.TokenType.OPERATOR_MINUS) || inErrorRecovery) {
                inErrorRecovery = false;
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.OPERATOR_PLUS) {
            if (match(Token.TokenType.OPERATOR_PLUS) || inErrorRecovery) {
                inErrorRecovery = false;
                return true;
            } else {
                return false;
            }
        } else {
            error(funcName(), missingStatementMessage);
            return true;
        }
    }

    //REPTVARIABLEORFUNCTIONCALL -> IDNEST REPTVARIABLEORFUNCTIONCALL | EPSILON
    private boolean reptVariableOrFunctionCall() throws IOException {
        if (getType() == Token.TokenType.PUNCTUATION_DOT) {
            if (idnest() && reptVariableOrFunctionCall() || inErrorRecovery) {
                inErrorRecovery = false;
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.PUNCTUATION_RPAREN || getType() == Token.TokenType.PUNCTUATION_COMMA || getType() == Token.TokenType.PUNCTUATION_SEMICOLON
                || getType() == Token.TokenType.OPERATOR_EQ || getType() == Token.TokenType.OPERATOR_GE || getType() == Token.TokenType.OPERATOR_GT || getType() == Token.TokenType.OPERATOR_LE
                || getType() == Token.TokenType.OPERATOR_LT || getType() == Token.TokenType.OPERATOR_NE || getType() == Token.TokenType.PUNCTUATION_RBRACKET || getType() == Token.TokenType.OPERATOR_MINUS
                || getType() == Token.TokenType.RESERVED_OR || getType() == Token.TokenType.OPERATOR_PLUS) {
            return true;
        } else {
            error(funcName(), missingStatementMessage);
            return true;
        }
    }

    //FACTOR2                    -> lpar APARAMS rpar | REPTIDNEST1
    //follow: rpar, comma, semi, eq, geq, gt, leq, lt, neq, rsqbr, minus, or, plus, and, div, mult, dot
    private boolean factor2() throws IOException {
        if (getType() == Token.TokenType.PUNCTUATION_LPAREN) {
            if (match(Token.TokenType.PUNCTUATION_LPAREN) && aParams() && match(Token.TokenType.PUNCTUATION_RPAREN) || inErrorRecovery) {
                inErrorRecovery = false;
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.PUNCTUATION_RPAREN || getType() == Token.TokenType.PUNCTUATION_COMMA
                || getType() == Token.TokenType.PUNCTUATION_SEMICOLON || getType() == Token.TokenType.OPERATOR_EQ || getType() == Token.TokenType.OPERATOR_GE || getType() == Token.TokenType.OPERATOR_GT
                || getType() == Token.TokenType.OPERATOR_LT || getType() == Token.TokenType.OPERATOR_LE || getType() == Token.TokenType.OPERATOR_NE || getType() == Token.TokenType.PUNCTUATION_RBRACKET
                || getType() == Token.TokenType.OPERATOR_MINUS || getType() == Token.TokenType.RESERVED_OR || getType() == Token.TokenType.OPERATOR_PLUS || getType() == Token.TokenType.RESERVED_AND
                || getType() == Token.TokenType.OPERATOR_DIV || getType() == Token.TokenType.OPERATOR_MULT || getType() == Token.TokenType.PUNCTUATION_DOT || getType() == Token.TokenType.OPERATOR_ASSIGN
                || getType() == Token.TokenType.PUNCTUATION_LBRACKET) {
            if (reptIdnest1() || inErrorRecovery) {
                inErrorRecovery = false;
                return true;
            } else {
                return false;
            }
        } else {
            error(funcName(), missingStatementMessage);
            return true;
        }
    }

    //RIGHTRECTERM               -> MULTOP FACTOR RIGHTRECTERM
    //                               | EPSILON
    //follow: rpar, comma, semi, eq, geq, gt, leq, lt, neq, rsqbr, minus, or, plus
    private boolean rightRecTerm() throws IOException {
        if (getType() == Token.TokenType.OPERATOR_MULT || getType() == Token.TokenType.OPERATOR_DIV || getType() == Token.TokenType.RESERVED_AND) {
            if (multOp() && factor() && rightRecTerm() || inErrorRecovery) {
                inErrorRecovery = false;
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.PUNCTUATION_RPAREN || getType() == Token.TokenType.PUNCTUATION_COMMA || getType() == Token.TokenType.PUNCTUATION_SEMICOLON
                || getType() == Token.TokenType.OPERATOR_EQ || getType() == Token.TokenType.OPERATOR_GE || getType() == Token.TokenType.OPERATOR_GT || getType() == Token.TokenType.OPERATOR_LE
                || getType() == Token.TokenType.OPERATOR_LT || getType() == Token.TokenType.OPERATOR_NE || getType() == Token.TokenType.PUNCTUATION_RBRACKET
                || getType() == Token.TokenType.OPERATOR_PLUS || getType() == Token.TokenType.OPERATOR_MINUS || getType() == Token.TokenType.RESERVED_OR) {
            return true;

        } else {
            error(funcName(), missingStatementMessage);
            return true;
        }
    }
    //MULTOP                     -> and | div | mult
    private boolean multOp() throws IOException {
        if (getType() == Token.TokenType.RESERVED_AND) {
            if (match(Token.TokenType.RESERVED_AND) || inErrorRecovery) {
                inErrorRecovery = false;
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.OPERATOR_DIV) {
            if (match(Token.TokenType.OPERATOR_DIV) || inErrorRecovery) {
                inErrorRecovery = false;
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.OPERATOR_MULT) {
            if (match(Token.TokenType.OPERATOR_MULT) || inErrorRecovery) {
                inErrorRecovery = false;
                return true;
            } else {
                return false;
            }
        } else {
            error(funcName(), missingStatementMessage);
            return true;
        }
    }
    //REPTAPARAMS1               -> APARAMSTAIL REPTAPARAMS1 | EPSILON
    private boolean reptAParams1() throws IOException {
        if (getType() == Token.TokenType.PUNCTUATION_COMMA) {
            if (aParamsTail() && reptAParams1() || inErrorRecovery) {
                inErrorRecovery = false;
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.PUNCTUATION_RPAREN) {
            return true;
        } else {
            error(funcName(), missingStatementMessage);
            return true;
        }
    }

    //APARAMSTAIL                -> comma EXPR
    private boolean aParamsTail() throws IOException {
        if (getType() == Token.TokenType.PUNCTUATION_COMMA) {
            if (match(Token.TokenType.PUNCTUATION_COMMA) && expr() || inErrorRecovery) {
                inErrorRecovery = false;
                return true;
            } else {
                return false;
            }
        } else {
            error(funcName(), missingStatementMessage);
            return true;
        }
    }

    //FUNCHEADTAIL               -> lpar FPARAMS rpar arrow RETURNTYPE  | sr FUNCHEADMEMBERTAIL
    private boolean funcHeadTail() throws IOException {
        if (getType() == Token.TokenType.SCOPE_RESOLUTION) {
            if (match(Token.TokenType.SCOPE_RESOLUTION) && funcHeadMemberTail() || inErrorRecovery) {
                inErrorRecovery = false;
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.PUNCTUATION_LPAREN) {
            if (match(Token.TokenType.PUNCTUATION_LPAREN) && fParams() && match(Token.TokenType.PUNCTUATION_RPAREN)
                    && match(Token.TokenType.OPERATOR_ARROW) && returnType() || inErrorRecovery) {
                inErrorRecovery = false;
                return true;
            } else {
                return false;
            }
        } else {
            error(funcName(), missingStatementMessage);
            return true;
        }
    }

    //FUNCHEADMEMBERTAIL         -> id lpar FPARAMS rpar arrow RETURNTYPE | constructorkeyword lpar FPARAMS rpar
    private boolean funcHeadMemberTail() throws IOException {
        if (getType() == Token.TokenType.RESERVED_CONSTRUCTOR) {
            if (match(Token.TokenType.RESERVED_CONSTRUCTOR) && match(Token.TokenType.PUNCTUATION_LPAREN) && fParams()
                    && match(Token.TokenType.PUNCTUATION_RPAREN) || inErrorRecovery) {
                inErrorRecovery = false;
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.ID) {
            if (match(Token.TokenType.ID) && match(Token.TokenType.PUNCTUATION_LPAREN) && fParams() && match(Token.TokenType.PUNCTUATION_RPAREN)
                    && match(Token.TokenType.OPERATOR_ARROW) && returnType() || inErrorRecovery) {
                inErrorRecovery = false;
                return true;
            } else {
                return false;
            }
        } else {
            error(funcName(), missingStatementMessage);
            return true;
        }
    }



    // OPTINHERITS                -> isa id REPTINHERITSLIST | EPSILON
    private boolean optInherits() throws IOException {
        if (getType() == Token.TokenType.RESERVED_ISA) {
            if (match(Token.TokenType.RESERVED_ISA) && match(Token.TokenType.ID) && reptInheritsList() || inErrorRecovery) {
                inErrorRecovery = false;
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.PUNCTUATION_LCURLY) {
            semanticActions.createInheritList();
            return true;
        } else {
            error(funcName(), missingStatementMessage);
            return true;
        }
    }

    //REPTINHERITSLIST           -> comma id REPTINHERITSLIST | EPSILON
    private boolean reptInheritsList() throws IOException {
        if(getType() == Token.TokenType.PUNCTUATION_COMMA){
            if (match(Token.TokenType.PUNCTUATION_COMMA) && match(Token.TokenType.ID) && reptInheritsList() || inErrorRecovery) {
                inErrorRecovery = false;
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.PUNCTUATION_LCURLY) {
            return true;
        } else {
            error(funcName(), missingStatementMessage);
            return true;
        }
    }
    //REPTMEMBERDECL             -> VISIBILITY MEMBERDECL REPTMEMBERDECL | EPSILON
    private boolean reptMemberDecl() throws IOException {
        semanticActions.createEpsilon(null);
        if (getType() == Token.TokenType.RESERVED_PRIVATE || getType() == Token.TokenType.RESERVED_PUBLIC) {
            if (visibility() && memberDecl() && reptMemberDecl() || inErrorRecovery) {
                inErrorRecovery = false;
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.PUNCTUATION_RCURLY) {
            return true;
        } else {
            error(funcName(), missingStatementMessage);
            return true;
        }
    }

    //VISIBILITY                 -> private | public
    private boolean visibility() throws IOException {
        if (getType() == Token.TokenType.RESERVED_PUBLIC) {
            if (match(Token.TokenType.RESERVED_PUBLIC) || inErrorRecovery) {
                inErrorRecovery = false;
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.RESERVED_PRIVATE) {
            if (match(Token.TokenType.RESERVED_PRIVATE) || inErrorRecovery) {
                inErrorRecovery = false;
                return true;
            } else {
                return false;
            }
        } else {
            error(funcName(), missingStatementMessage);
            return true;
        }
    }
    //MEMBERDECL                 -> MEMBERVARDECL | MEMBERFUNCDECL
    private boolean memberDecl() throws IOException {
        if (getType() == Token.TokenType.RESERVED_FUNCTION || getType() == Token.TokenType.RESERVED_CONSTRUCTOR) {
            if(memberFuncDecl() || inErrorRecovery){
                inErrorRecovery = false;
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.RESERVED_ATTRIBUTE) {
            if(memberVarDecl() || inErrorRecovery){
                inErrorRecovery = false;
                return true;
            } else {
                return false;
            }
        } else {
            error(funcName(), missingStatementMessage);
            return true;
        }
    }
    //MEMBERFUNCDECL             -> MEMBERFUNCHEAD semi
    private boolean memberFuncDecl() throws IOException {
        if (getType() == Token.TokenType.RESERVED_FUNCTION || getType() == Token.TokenType.RESERVED_CONSTRUCTOR) {
            if (memberFuncHead() && match(Token.TokenType.PUNCTUATION_SEMICOLON) || inErrorRecovery) {
                inErrorRecovery = false;
                return true;
            } else {
                return false;
            }
        } else {
            error(funcName(), missingStatementMessage);
            return true;
        }
    }
    //MEMBERFUNCHEAD             -> function id colon lpar FPARAMS rpar arrow RETURNTYPE
    //                               | constructorkeyword colon lpar FPARAMS rpar
    private boolean memberFuncHead() throws IOException {
        if(getType() == Token.TokenType.RESERVED_CONSTRUCTOR){
            if(match(Token.TokenType.RESERVED_CONSTRUCTOR) && match(Token.TokenType.PUNCTUATION_COLON) && match(Token.TokenType.PUNCTUATION_LPAREN)
            && fParams() && match(Token.TokenType.PUNCTUATION_RPAREN) || inErrorRecovery){
                inErrorRecovery = false;
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.RESERVED_FUNCTION) {
            if(match(Token.TokenType.RESERVED_FUNCTION) && match(Token.TokenType.ID) && match(Token.TokenType.PUNCTUATION_COLON)
            && match(Token.TokenType.PUNCTUATION_LPAREN) && fParams() && match(Token.TokenType.PUNCTUATION_RPAREN) && match(Token.TokenType.OPERATOR_ARROW)
            && returnType() || inErrorRecovery){
                inErrorRecovery = false;
                return true;
            } else{
                return false;
            }
        } else {
            error(funcName(), missingStatementMessage);
            return true;
        }
    }
    //FPARAMS                    -> id colon TYPE REPTFPARAMS3 REPTFPARAMS4 | EPSILON
    private boolean fParams() throws IOException {
        if (getType() == Token.TokenType.ID) {
            if (match(Token.TokenType.ID) && match(Token.TokenType.PUNCTUATION_COLON) && Type() && reptFParams3() && reptFParams4() || inErrorRecovery) {
                inErrorRecovery = false;
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.PUNCTUATION_RPAREN) {
            return true;
        } else {
            error(funcName(), missingStatementMessage);
            return true;
        }
    }
    //REPTFPARAMS3               -> ARRAYSIZE REPTFPARAMS3 | EPSILON
    private boolean reptFParams3() throws IOException {
        if (getType() == Token.TokenType.PUNCTUATION_LBRACKET) {
            if (arraySize() && reptFParams3() || inErrorRecovery) {
                inErrorRecovery = false;
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.PUNCTUATION_RPAREN || getType() == Token.TokenType.PUNCTUATION_COMMA) {
            return true;
        } else {
            error(funcName(), missingStatementMessage);
            return true;
        }
    }
    //REPTFPARAMS4               -> FPARAMSTAIL REPTFPARAMS4 | EPSILON
    private boolean reptFParams4() throws IOException {
        if (getType() == Token.TokenType.PUNCTUATION_COMMA) {
            if (fParamsTail() && reptFParams4() || inErrorRecovery) {
                inErrorRecovery = false;
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.PUNCTUATION_RPAREN) {
            return true;
        } else {
            error(funcName(), missingStatementMessage);
            return true;
        }
    }

    // FPARAMSTAIL                -> comma id colon TYPE REPTFPARAMSTAIL4
    private boolean fParamsTail() throws IOException {
        if (getType() == Token.TokenType.PUNCTUATION_COMMA) {
            if (match(Token.TokenType.PUNCTUATION_COMMA) && match(Token.TokenType.ID) && match(Token.TokenType.PUNCTUATION_COLON)
                    && Type() && reptFParamsTail4() || inErrorRecovery) {
                inErrorRecovery = false;
                return true;
            } else {
                return false;
            }
        } else {
            error(funcName(), missingStatementMessage);
            return true;
        }
    }
    //REPTFPARAMSTAIL4           -> ARRAYSIZE REPTFPARAMSTAIL4 | EPSILON
    private boolean reptFParamsTail4() throws IOException {
        if (getType() == Token.TokenType.PUNCTUATION_LBRACKET) {
            if (arraySize() && reptFParamsTail4() || inErrorRecovery) {
                inErrorRecovery = false;
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.PUNCTUATION_RPAREN || getType() == Token.TokenType.PUNCTUATION_COMMA) {
            return true;
        } else {
            error(funcName(), missingStatementMessage);
            return true;
        }
    }

    // RETURNTYPE                 -> void | TYPE
    private boolean returnType() throws IOException {
        if (getType() == Token.TokenType.RESERVED_FLOAT || getType() == Token.TokenType.ID || getType() == Token.TokenType.RESERVED_INTEGER) {
            if(Type() || inErrorRecovery){
                inErrorRecovery = false;
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.RESERVED_VOID) {
            if (match(Token.TokenType.RESERVED_VOID) || inErrorRecovery) {
                inErrorRecovery = false;
                return true;
            } else {
                return false;
            }
        } else {
            error(funcName(), missingStatementMessage);
            return true;
        }
    }

    //MEMBERVARDECL              -> attribute id colon TYPE REPTARRAYSIZE semi
    private boolean memberVarDecl() throws IOException {
        if (getType() == Token.TokenType.RESERVED_ATTRIBUTE) {
            if (match(Token.TokenType.RESERVED_ATTRIBUTE) && match(Token.TokenType.ID) && match(Token.TokenType.PUNCTUATION_COLON)
                    && Type() && reptArraySize() && match(Token.TokenType.PUNCTUATION_SEMICOLON) || inErrorRecovery) {
                inErrorRecovery = false;
                return true;
            } else {
                return false;
            }
        } else {
            error(funcName(), missingStatementMessage);
            return true;
        }
    }

    //TYPE                       -> float | id | integer
    private boolean Type() throws IOException {
        if (getType() == Token.TokenType.RESERVED_INTEGER) {
            if (match(Token.TokenType.RESERVED_INTEGER) || inErrorRecovery) {
                inErrorRecovery = false;
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.ID) {
            if (match(Token.TokenType.ID) || inErrorRecovery) {
                inErrorRecovery = false;
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.RESERVED_FLOAT) {
            if (match(Token.TokenType.RESERVED_FLOAT) || inErrorRecovery) {
                inErrorRecovery = false;
                return true;
            } else {
                return false;
            }
        } else {
            error(funcName(), missingStatementMessage);
            return true;
        }
    }


    //ARRAYSIZE                  -> lsqbr ARRAYSIZE2
    private boolean arraySize() throws IOException {
        if (getType() == Token.TokenType.PUNCTUATION_LBRACKET) {
            if (match(Token.TokenType.PUNCTUATION_LBRACKET) && arraySize2() || inErrorRecovery) {
                inErrorRecovery = false;
                return true;
            } else {
                return false;
            }
        } else {
            error(funcName(), missingStatementMessage);
            return true;
        }
    }

    // ARRAYSIZE2                 -> intlit rsqbr | rsqbr
    private boolean arraySize2() throws IOException {
        if (getType() == Token.TokenType.PUNCTUATION_RBRACKET) {
            if (match(Token.TokenType.PUNCTUATION_RBRACKET) || inErrorRecovery) {
                inErrorRecovery = false;
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.INTEGER) {
            if (match(Token.TokenType.INTEGER) && match(Token.TokenType.PUNCTUATION_RBRACKET) || inErrorRecovery) {
                inErrorRecovery = false;
                return true;
            } else {
                return false;
            }
        } else {
            error(funcName(), missingStatementMessage);
            return true;
        }
    }

    //ARRAYOROBJECT              -> lpar APARAMS rpar | REPTARRAYSIZE
    private boolean arrayOrObject() throws IOException {
        if (getType() == Token.TokenType.PUNCTUATION_LPAREN) {
            if (match(Token.TokenType.PUNCTUATION_LPAREN) && aParams() && match(Token.TokenType.PUNCTUATION_RPAREN) || inErrorRecovery) {
                inErrorRecovery = false;
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.PUNCTUATION_SEMICOLON) {
            return true;
        } else if (getType() == Token.TokenType.PUNCTUATION_LBRACKET) {
            if (reptArraySize() || inErrorRecovery) {
                inErrorRecovery = false;
                return true;
            } else {
                return false;
            }
        } else {
            error(funcName(), missingStatementMessage);
            return true;
        }
    }

    // REPTARRAYSIZE              -> ARRAYSIZE REPTARRAYSIZE | EPSILON
    private boolean reptArraySize() throws IOException {
        if (getType() == Token.TokenType.PUNCTUATION_LBRACKET) {
            if (arraySize() && reptArraySize() || inErrorRecovery) {
                inErrorRecovery = false;
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.PUNCTUATION_SEMICOLON) {
            return true;
        } else {
            error(funcName(), missingStatementMessage);
            return true;
        }
    }

    //PROG                       -> REPTPROG0
    private boolean prog() throws IOException {
        semanticActions.createEpsilon(null);
        if(getType() == Token.TokenType.RESERVED_FUNCTION || getType() == Token.TokenType.RESERVED_CLASS
        || getType() == Token.TokenType.EOF){
            if (reptProg0() || inErrorRecovery) {
                semanticActions.createRoot();
                inErrorRecovery = false;
                return true;
            } else {
                return false;
            }
        } else {
            error(funcName(), missingStatementMessage);
            return true;
        }
    }
    
    // REPTPROG0                  -> CLASSDECLORFUNCDEF REPTPROG0 | EPSILON
    private boolean reptProg0() throws IOException {
        if(getType() == Token.TokenType.RESERVED_FUNCTION || getType() == Token.TokenType.RESERVED_CLASS){
            if (classDeclOrFuncDef() && reptProg0() || inErrorRecovery) {
                inErrorRecovery = false;
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.EOF) {
            return true;
        } else {
            error(funcName(), missingStatementMessage);
            return true;
        }
    }

    //REPTVARIABLE               -> VARIDNEST REPTVARIABLE | EPSILON
    private boolean reptVariable() throws IOException {
        if(getType() == Token.TokenType.PUNCTUATION_DOT){
            if(varIdnest() && reptVariable() || inErrorRecovery){
                inErrorRecovery = false;
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.PUNCTUATION_RPAREN) {
            return true;
        } else{
            error(funcName(), missingStatementMessage);
            return true;
        }
    }
    
    // VARIDNEST                  -> dot id VARIDNEST2
    private boolean varIdnest() throws IOException {
        if(getType() == Token.TokenType.PUNCTUATION_DOT){
            if(match(Token.TokenType.PUNCTUATION_DOT) && match(Token.TokenType.ID) && varIdnest2() || inErrorRecovery){
                inErrorRecovery = false;
                return true;
            } else {
                return false;
            }
        } else {
            error(funcName(), missingStatementMessage);
            return true;
        }
    }

    // VARIDNEST2                 -> lpar APARAMS rpar VARIDNEST | REPTIDNEST1
    private boolean varIdnest2() throws IOException {
        if (getType() == Token.TokenType.PUNCTUATION_LPAREN) {
            if (match(Token.TokenType.PUNCTUATION_LPAREN) && aParams() && match(Token.TokenType.PUNCTUATION_RPAREN) && varIdnest() || inErrorRecovery) {
                inErrorRecovery = false;
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.PUNCTUATION_LBRACKET || getType() == Token.TokenType.PUNCTUATION_RPAREN || getType() == Token.TokenType.PUNCTUATION_DOT) {
           if(reptIdnest1() || inErrorRecovery){
               inErrorRecovery = false;
               return true;
           } else {
               return false;
           }
        } else {
            error(funcName(), missingStatementMessage);
            return true;
        }
    }
}