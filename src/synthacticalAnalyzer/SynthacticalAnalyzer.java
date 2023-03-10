package synthacticalAnalyzer;
import lexicalAnalyzer.LexicalAnalyzer;
import lexicalAnalyzer.Token;

import java.io.*;

public class SynthacticalAnalyzer {
    private Token lookahead;
    private LexicalAnalyzer lexer;
    private PrintWriter pwDerivation = new PrintWriter(new FileWriter("parser/example-polynomial.outderivation.src", true));
    private PrintWriter pwError = new PrintWriter(new FileWriter("parser/example-polynomial.outsyntaxerror.src", true));
    private String currentLHS = "";
    private boolean inErrorRecovery = false;
    public SynthacticalAnalyzer(LexicalAnalyzer lexer) throws IOException {
        this.lexer = lexer;
        this.lookahead = lexer.nextToken();

    }

    private boolean match(Token.TokenType terminal) throws IOException {
        String signature = Thread.currentThread().getStackTrace()[2].getMethodName();
        if(inErrorRecovery)
            errorRecovery(signature, terminal.getValue());
        else if (getType() == terminal) {
            write(signature, terminal.getValue());
            skip();
        } else {
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
            inErrorRecovery = true;
            error(currentLHS, "Expecting: " + terminal + " at: " + getLocation());
        }

        if(signature.equals(currentLHS))
            lookahead = lexer.nextToken();
        else
            inErrorRecovery = false;
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
        return prog() && match(Token.TokenType.EOF);
    }

    //CLASSDECLORFUNCDEF         -> FUNCDEF | CLASSDECL
    private boolean classDeclOrFuncDef() throws IOException {
        if (getType() == Token.TokenType.RESERVED_CLASS) {
            return classDecl();
        } else if (getType() == Token.TokenType.RESERVED_FUNCTION) {
            return funcDef();
        } else {
            return false;
        }
    }

    //FUNCDEF                    -> FUNCHEAD FUNCBODY
    private boolean funcDef() throws IOException {
        if (getType() == Token.TokenType.RESERVED_FUNCTION) {
            if (funcHead() && funcBody()) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    //FUNCHEAD                   -> function id FUNCHEADTAIL
    private boolean funcHead() throws IOException {
        if (getType() == Token.TokenType.RESERVED_FUNCTION) {
            if (match(Token.TokenType.RESERVED_FUNCTION) && match(Token.TokenType.ID) && funcHeadTail()) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    //FUNCBODY                   -> lcurbr REPTLOCALVARORSTAT rcurbr
    private boolean funcBody() throws IOException {
        if (getType() == Token.TokenType.PUNCTUATION_LCURLY) {
            if (match(Token.TokenType.PUNCTUATION_LCURLY) && reptLocalVarOrStat() && match(Token.TokenType.PUNCTUATION_RCURLY)) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    //REPTLOCALVARORSTAT         -> LOCALVARORSTAT REPTLOCALVARORSTAT | EPSILON
    private boolean reptLocalVarOrStat() throws IOException {
        if (getType() == Token.TokenType.RESERVED_IF || getType() == Token.TokenType.RESERVED_READ || getType() == Token.TokenType.RESERVED_RETURN
                || getType() == Token.TokenType.RESERVED_WHILE || getType() == Token.TokenType.RESERVED_WRITE || getType() == Token.TokenType.ID
                || getType() == Token.TokenType.RESERVED_LOCALVAR) {
            if (localVarOrStat() && reptLocalVarOrStat()) {
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.PUNCTUATION_RCURLY) {
            return true;
        } else {
            return false;
        }
    }
    //LOCALVARORSTAT             -> STATEMENT | LOCALVARDECL
    private boolean localVarOrStat() throws IOException {
        if (getType() == Token.TokenType.RESERVED_LOCALVAR) {
            if (localVarDecl()) {
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.RESERVED_IF || getType() == Token.TokenType.RESERVED_READ || getType() == Token.TokenType.RESERVED_RETURN
                || getType() == Token.TokenType.RESERVED_WHILE || getType() == Token.TokenType.RESERVED_WRITE || getType() == Token.TokenType.ID) {
            if (statement()) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
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
            if (match(Token.TokenType.ID) && statementIdnest() && match(Token.TokenType.PUNCTUATION_SEMICOLON)) {
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.RESERVED_WRITE) {
            if (match(Token.TokenType.RESERVED_WRITE) && match(Token.TokenType.PUNCTUATION_LPAREN) && expr() && match(Token.TokenType.PUNCTUATION_RPAREN)
                    && match(Token.TokenType.PUNCTUATION_SEMICOLON)) {
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.RESERVED_WHILE) {
            if (match(Token.TokenType.RESERVED_WHILE) && match(Token.TokenType.PUNCTUATION_LPAREN) && relExpr() && match(Token.TokenType.PUNCTUATION_RPAREN)
                    && statBlock() && match(Token.TokenType.PUNCTUATION_SEMICOLON)) {
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.RESERVED_RETURN) {
            if (match(Token.TokenType.RESERVED_RETURN) && match(Token.TokenType.PUNCTUATION_LPAREN) && expr() && match(Token.TokenType.PUNCTUATION_RPAREN)
                  && statBlock() && match(Token.TokenType.PUNCTUATION_SEMICOLON)) {
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.RESERVED_READ) {
            if (match(Token.TokenType.RESERVED_READ) && match(Token.TokenType.PUNCTUATION_LPAREN) && variable() && match(Token.TokenType.PUNCTUATION_RPAREN)
                    && match(Token.TokenType.PUNCTUATION_SEMICOLON)) {
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.RESERVED_IF) {
            if (match(Token.TokenType.RESERVED_IF) && match(Token.TokenType.PUNCTUATION_LPAREN) && relExpr() && match(Token.TokenType.PUNCTUATION_RPAREN)
                    && match(Token.TokenType.RESERVED_THEN) && statBlock() && match(Token.TokenType.RESERVED_ELSE) && statBlock() && match(Token.TokenType.PUNCTUATION_SEMICOLON)) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    // STATEMENTIDNEST            -> lpar APARAMS rpar STATEMENTIDNEST2
    //                               | dot id STATEMENTIDNEST
    //                               | ASSIGNOP EXPR
    //                               | INDICE REPTIDNEST1 STATEMENTIDNEST3
    private boolean statementIdnest() throws IOException {
        if(getType() == Token.TokenType.PUNCTUATION_LBRACKET){
            if(indice() && reptIdnest1() && statementIdnest3()){
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.OPERATOR_ASSIGN) {
            if(assignOp() && expr()){
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.PUNCTUATION_DOT) {
            if (match(Token.TokenType.PUNCTUATION_DOT) && match(Token.TokenType.ID) && statementIdnest()){
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.PUNCTUATION_LPAREN) {
            if(match(Token.TokenType.PUNCTUATION_LPAREN) && aParams() && match(Token.TokenType.PUNCTUATION_RPAREN) && statementIdnest2()){
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    //STATEMENTIDNEST2           -> dot id STATEMENTIDNEST
    //                               | EPSILON
    private boolean statementIdnest2() throws IOException {
        if(getType() == Token.TokenType.PUNCTUATION_DOT){
            if(match(Token.TokenType.PUNCTUATION_DOT) && match(Token.TokenType.ID) && statementIdnest()){
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.PUNCTUATION_SEMICOLON) {
            return true;
        } else {
            return false;
        }
    }
    
    //STATEMENTIDNEST3           -> dot id STATEMENTIDNEST
    //                               | ASSIGNOP EXPR
    private boolean statementIdnest3() throws IOException {
        if(getType() == Token.TokenType.PUNCTUATION_DOT){
            if(match(Token.TokenType.PUNCTUATION_DOT) && match(Token.TokenType.ID) && statementIdnest()){
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.OPERATOR_ASSIGN) {
            if(assignOp() && expr()){
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    // IDNEST                     -> dot id IDNEST2
    private boolean idnest() throws IOException {
        if (getType() == Token.TokenType.PUNCTUATION_DOT) {
            if (match(Token.TokenType.PUNCTUATION_DOT) && match(Token.TokenType.ID) && idnest2()) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
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
            if (reptIdnest1()) {
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.PUNCTUATION_LPAREN) {
            if (match(Token.TokenType.PUNCTUATION_LPAREN) && aParams() && match(Token.TokenType.PUNCTUATION_RPAREN)) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
    // REPTIDNEST1                -> INDICE REPTIDNEST1 | EPSILON
    //follow: rpar, comma, semi, eq, geq, gt, leq, lt, neq, rsqbr, minus, or, plus, and, div, mult, dot, equal
    private boolean reptIdnest1() throws IOException {
        if (getType() == Token.TokenType.PUNCTUATION_LBRACKET) {
            if (indice() && reptIdnest1()) {
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
            return false;
        }
    }



    //ASSIGNOP                   -> equal
    private boolean assignOp() throws IOException {
        if (getType() == Token.TokenType.OPERATOR_ASSIGN) {
            if (match(Token.TokenType.OPERATOR_ASSIGN)) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    //VARIABLE                   -> id VARIABLE2
    private boolean variable() throws IOException {
        if (getType() == Token.TokenType.ID) {
            if (match(Token.TokenType.ID) && variable2()) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    //VARIABLE2                  -> lpar APARAMS rpar VARIDNEST
    //                               | REPTIDNEST1 REPTVARIABLE
    private boolean variable2() throws IOException {
        if(getType() == Token.TokenType.PUNCTUATION_LPAREN){
            if (match(Token.TokenType.PUNCTUATION_LPAREN) && aParams() && match(Token.TokenType.PUNCTUATION_RPAREN) && varIdnest()) {
                return true;
            } else{
                return false;
            }
        } else if (getType() == Token.TokenType.PUNCTUATION_LBRACKET || getType() == Token.TokenType.PUNCTUATION_DOT || getType() == Token.TokenType.PUNCTUATION_RPAREN) {
             if(reptIdnest1() && reptVariable()){
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
            if (match(Token.TokenType.PUNCTUATION_LBRACKET) && arithExpr() && match(Token.TokenType.PUNCTUATION_RBRACKET)) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    //RELEXPR                    -> ARITHEXPR RELOP ARITHEXPR
    private boolean relExpr() throws IOException {
        if (getType() == Token.TokenType.PUNCTUATION_LPAREN || getType() == Token.TokenType.FLOAT || getType() == Token.TokenType.INTEGER
                || getType() == Token.TokenType.RESERVED_NOT || getType() == Token.TokenType.ID || getType() == Token.TokenType.OPERATOR_PLUS
                || getType() == Token.TokenType.OPERATOR_MINUS) {
            if (arithExpr() && relOp() && arithExpr()) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    // STATBLOCK                  -> lcurbr REPTSTATBLOCK1 rcurbr
    //                               | STATEMENT
    //                               | EPSILON
    private boolean statBlock() throws IOException {
        if (getType() == Token.TokenType.RESERVED_IF || getType() == Token.TokenType.RESERVED_READ || getType() == Token.TokenType.RESERVED_RETURN
                || getType() == Token.TokenType.RESERVED_WHILE || getType() == Token.TokenType.RESERVED_WRITE || getType() == Token.TokenType.ID) {
            if (statement()) {
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.PUNCTUATION_LCURLY) {
            if (match(Token.TokenType.PUNCTUATION_LCURLY) && reptStatBlock1() && match(Token.TokenType.PUNCTUATION_RCURLY)) {
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.RESERVED_ELSE || getType() == Token.TokenType.PUNCTUATION_SEMICOLON) {
            return true;
        } else {
            return false;
        }
    }

    // REPTSTATBLOCK1             -> STATEMENT REPTSTATBLOCK1 | EPSILON
    private boolean reptStatBlock1() throws IOException {
        if (getType() == Token.TokenType.RESERVED_IF || getType() == Token.TokenType.RESERVED_READ || getType() == Token.TokenType.RESERVED_RETURN
                || getType() == Token.TokenType.RESERVED_WHILE || getType() == Token.TokenType.RESERVED_WRITE || getType() == Token.TokenType.ID) {
            if (statement() && reptStatBlock1()) {
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.PUNCTUATION_RCURLY) {
            return true;
        } else {
            return false;
        }
    }
    //LOCALVARDECL               -> localvar id colon TYPE ARRAYOROBJECT semi
    private boolean localVarDecl() throws IOException {
        if (getType() == Token.TokenType.RESERVED_LOCALVAR) {
            if (match(Token.TokenType.RESERVED_LOCALVAR) && match(Token.TokenType.ID) && match(Token.TokenType.PUNCTUATION_COLON) && Type()
                   && arrayOrObject() && match(Token.TokenType.PUNCTUATION_SEMICOLON)) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    //APARAMS                    -> EXPR REPTAPARAMS1 | EPSILON
    //first: {lpar, floatlit, id, intlit, not, minus, plus}
    private boolean aParams() throws IOException {
        if (getType() == Token.TokenType.PUNCTUATION_LPAREN || getType() == Token.TokenType.FLOAT || getType() == Token.TokenType.INTEGER
                || getType() == Token.TokenType.RESERVED_NOT || getType() == Token.TokenType.ID || getType() == Token.TokenType.OPERATOR_PLUS
                || getType() == Token.TokenType.OPERATOR_MINUS) {
            if (expr() && reptAParams1()) {
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.PUNCTUATION_RPAREN) {
            return true;
        } else {
            return false;
        }
    }

    //EXPR                       -> ARITHEXPR EXPR2
    //FIRST(0) = {lpar, floatlit, id, intlit, not, minus, plus}
    private boolean expr() throws IOException {
        if (getType() == Token.TokenType.PUNCTUATION_LPAREN || getType() == Token.TokenType.FLOAT || getType() == Token.TokenType.INTEGER
                || getType() == Token.TokenType.RESERVED_NOT || getType() == Token.TokenType.ID || getType() == Token.TokenType.OPERATOR_PLUS
                || getType() == Token.TokenType.OPERATOR_MINUS) {
            if (arithExpr() && expr2()) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    //EXPR2                      -> RELOP ARITHEXPR | EPSILON
    private boolean expr2() throws IOException {
        if (getType() == Token.TokenType.OPERATOR_EQ || getType() == Token.TokenType.OPERATOR_GT || getType() == Token.TokenType.OPERATOR_GE
                || getType() == Token.TokenType.OPERATOR_LE || getType() == Token.TokenType.OPERATOR_LT || getType() == Token.TokenType.OPERATOR_NE) {
            if (relOp() && arithExpr()) {
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.PUNCTUATION_RPAREN || getType() == Token.TokenType.PUNCTUATION_COMMA
                || getType() == Token.TokenType.PUNCTUATION_SEMICOLON) {
            return true;
        } else {
            return false;
        }
    }

    //RELOP                      -> eq | geq | gt | leq | lt | neq
    private boolean relOp() throws IOException {
        if (getType() == Token.TokenType.OPERATOR_GT) {
            if (match(Token.TokenType.OPERATOR_GT)) {
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.OPERATOR_GE) {
            if (match(Token.TokenType.OPERATOR_GE)) {
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.OPERATOR_LE) {
            if (match(Token.TokenType.OPERATOR_LE)) {
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.OPERATOR_LT) {
            if (match(Token.TokenType.OPERATOR_LT)) {
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.OPERATOR_NE) {
            if (match(Token.TokenType.OPERATOR_NE)) {
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.OPERATOR_EQ) {
            if (match(Token.TokenType.OPERATOR_EQ)) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    //ARITHEXPR                  -> TERM RIGHTRECARITHEXPR
    //FIRST(0) = {	lpar, floatlit, id, intlit, not, minus, plus}
    private boolean arithExpr() throws IOException {
        if (getType() == Token.TokenType.PUNCTUATION_LPAREN || getType() == Token.TokenType.FLOAT || getType() == Token.TokenType.INTEGER
                || getType() == Token.TokenType.RESERVED_NOT || getType() == Token.TokenType.ID || getType() == Token.TokenType.OPERATOR_PLUS
                || getType() == Token.TokenType.OPERATOR_MINUS) {
            if (term() && rightRecArithExpr()) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
    //RIGHTRECARITHEXPR          -> ADDOP TERM RIGHTRECARITHEXPR
    //                               | EPSILON
    //follow: rpar, comma, semi, eq, geq, gt, leq, lt, neq, rsqbr
    private boolean rightRecArithExpr() throws IOException {
        if (getType() == Token.TokenType.OPERATOR_PLUS || getType() == Token.TokenType.OPERATOR_MINUS || getType() == Token.TokenType.RESERVED_OR) {
            if (addOp() && term() && rightRecArithExpr()) {
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.PUNCTUATION_RPAREN || getType() == Token.TokenType.PUNCTUATION_COMMA || getType() == Token.TokenType.PUNCTUATION_SEMICOLON
                || getType() == Token.TokenType.OPERATOR_EQ || getType() == Token.TokenType.OPERATOR_GE || getType() == Token.TokenType.OPERATOR_GT || getType() == Token.TokenType.OPERATOR_LE
                || getType() == Token.TokenType.OPERATOR_LT || getType() == Token.TokenType.OPERATOR_NE || getType() == Token.TokenType.PUNCTUATION_RBRACKET) {
            return true;
        } else {
            return false;
        }
    }

    //   ADDOP                      -> minus | or | plus
    private boolean addOp() throws IOException {
        if (getType() == Token.TokenType.RESERVED_OR) {
            if (match(Token.TokenType.RESERVED_OR)) {
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.OPERATOR_PLUS) {
            if (match(Token.TokenType.OPERATOR_PLUS)) {
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.OPERATOR_MINUS) {
            if (match(Token.TokenType.OPERATOR_MINUS)) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    // TERM                       -> FACTOR RIGHTRECTERM
    private boolean term() throws IOException {
        if (getType() == Token.TokenType.PUNCTUATION_LPAREN || getType() == Token.TokenType.FLOAT || getType() == Token.TokenType.INTEGER
                || getType() == Token.TokenType.RESERVED_NOT || getType() == Token.TokenType.ID || getType() == Token.TokenType.OPERATOR_PLUS
                || getType() == Token.TokenType.OPERATOR_MINUS) {
            if (factor() && rightRecTerm()) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
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
            if (sign() && factor()) {
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.ID) {
            if (match(Token.TokenType.ID) && factor2() && reptVariableOrFunctionCall()) {
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.RESERVED_NOT) {
            if (match(Token.TokenType.RESERVED_NOT) && factor()) {
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.INTEGER) {
            if (match(Token.TokenType.INTEGER)) {
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.FLOAT) {
            if (match(Token.TokenType.FLOAT)) {
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.PUNCTUATION_LPAREN) {
            if (match(Token.TokenType.PUNCTUATION_LPAREN) && arithExpr() && match(Token.TokenType.PUNCTUATION_RPAREN)) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    //SIGN                       -> minus | plus
    private boolean sign() throws IOException {
        if (getType() == Token.TokenType.OPERATOR_MINUS) {
            if (match(Token.TokenType.OPERATOR_MINUS)) {
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.OPERATOR_PLUS) {
            if (match(Token.TokenType.OPERATOR_PLUS)) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    //REPTVARIABLEORFUNCTIONCALL -> IDNEST REPTVARIABLEORFUNCTIONCALL | EPSILON
    private boolean reptVariableOrFunctionCall() throws IOException {
        if (getType() == Token.TokenType.PUNCTUATION_DOT) {
            if (idnest() && reptVariableOrFunctionCall()) {
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
            return false;
        }
    }

    //FACTOR2                    -> lpar APARAMS rpar | REPTIDNEST1
    //follow: rpar, comma, semi, eq, geq, gt, leq, lt, neq, rsqbr, minus, or, plus, and, div, mult, dot
    private boolean factor2() throws IOException {
        if (getType() == Token.TokenType.PUNCTUATION_LPAREN) {
            if (match(Token.TokenType.PUNCTUATION_LPAREN) && aParams() && match(Token.TokenType.PUNCTUATION_RPAREN)) {
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
            if (reptIdnest1()) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    //RIGHTRECTERM               -> MULTOP FACTOR RIGHTRECTERM
    //                               | EPSILON
    //follow: rpar, comma, semi, eq, geq, gt, leq, lt, neq, rsqbr, minus, or, plus
    private boolean rightRecTerm() throws IOException {
        if (getType() == Token.TokenType.OPERATOR_MULT || getType() == Token.TokenType.OPERATOR_DIV || getType() == Token.TokenType.RESERVED_AND) {
            if (multOp() && factor() && rightRecTerm()) {
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
            return false;
        }
    }
    //MULTOP                     -> and | div | mult
    private boolean multOp() throws IOException {
        if (getType() == Token.TokenType.RESERVED_AND) {
            if (match(Token.TokenType.RESERVED_AND)) {
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.OPERATOR_DIV) {
            if (match(Token.TokenType.OPERATOR_DIV)) {
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.OPERATOR_MULT) {
            if (match(Token.TokenType.OPERATOR_MULT)) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
    //REPTAPARAMS1               -> APARAMSTAIL REPTAPARAMS1 | EPSILON
    private boolean reptAParams1() throws IOException {
        if (getType() == Token.TokenType.PUNCTUATION_COMMA) {
            if (aParamsTail() && reptAParams1()) {
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.PUNCTUATION_RPAREN) {
            return true;
        } else {
            return false;
        }
    }

    //APARAMSTAIL                -> comma EXPR
    private boolean aParamsTail() throws IOException {
        if (getType() == Token.TokenType.PUNCTUATION_COMMA) {
            if (match(Token.TokenType.PUNCTUATION_COMMA) && expr()) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    //FUNCHEADTAIL               -> lpar FPARAMS rpar arrow RETURNTYPE  | sr FUNCHEADMEMBERTAIL
    private boolean funcHeadTail() throws IOException {
        if (getType() == Token.TokenType.SCOPE_RESOLUTION) {
            if (match(Token.TokenType.SCOPE_RESOLUTION) && funcHeadMemberTail()) {
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.PUNCTUATION_LPAREN) {
            if (match(Token.TokenType.PUNCTUATION_LPAREN) && fParams() && match(Token.TokenType.PUNCTUATION_RPAREN)
                    && match(Token.TokenType.OPERATOR_ARROW) && returnType()) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    //FUNCHEADMEMBERTAIL         -> id lpar FPARAMS rpar arrow RETURNTYPE | constructorkeyword lpar FPARAMS rpar
    private boolean funcHeadMemberTail() throws IOException {
        if (getType() == Token.TokenType.RESERVED_CONSTRUCTOR) {
            if (match(Token.TokenType.RESERVED_CONSTRUCTOR) && match(Token.TokenType.PUNCTUATION_LPAREN) && fParams()
                    && match(Token.TokenType.PUNCTUATION_RPAREN)) {
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.ID) {
            if (match(Token.TokenType.ID) && match(Token.TokenType.PUNCTUATION_LPAREN) && fParams() && match(Token.TokenType.PUNCTUATION_RPAREN)
                    && match(Token.TokenType.OPERATOR_ARROW) && returnType()) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    //CLASSDECL                  -> class id OPTINHERITS lcurbr REPTMEMBERDECL rcurbr semi
    private boolean classDecl() throws IOException {
        if (getType() == Token.TokenType.RESERVED_CLASS) {
            if (match(Token.TokenType.RESERVED_CLASS) && match(Token.TokenType.ID) && optInherits() && match(Token.TokenType.PUNCTUATION_LCURLY)
                    && reptMemberDecl() && match(Token.TokenType.PUNCTUATION_RCURLY) && match(Token.TokenType.PUNCTUATION_SEMICOLON)) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    // OPTINHERITS                -> isa id REPTINHERITSLIST | EPSILON
    private boolean optInherits() throws IOException {
        if (getType() == Token.TokenType.RESERVED_ISA) {
            if (match(Token.TokenType.RESERVED_ISA) && match(Token.TokenType.ID) && reptInheritsList()) {
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.PUNCTUATION_LCURLY) {
            return true;
        } else {
            return false;
        }
    }

    //REPTINHERITSLIST           -> comma id REPTINHERITSLIST | EPSILON
    private boolean reptInheritsList() throws IOException {
        if(getType() == Token.TokenType.PUNCTUATION_COMMA){
            if (match(Token.TokenType.PUNCTUATION_COMMA) && match(Token.TokenType.ID) && reptInheritsList()) {
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.PUNCTUATION_LCURLY) {
            return true;
        } else {
            return false;
        }
    }
    //REPTMEMBERDECL             -> VISIBILITY MEMBERDECL REPTMEMBERDECL | EPSILON
    private boolean reptMemberDecl() throws IOException {
        if (getType() == Token.TokenType.RESERVED_PRIVATE || getType() == Token.TokenType.RESERVED_PUBLIC) {
            if (visibility() && memberDecl() && reptMemberDecl()) {
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.PUNCTUATION_RCURLY) {
            return true;
        } else {
            return false;
        }
    }

    //VISIBILITY                 -> private | public
    private boolean visibility() throws IOException {
        if (getType() == Token.TokenType.RESERVED_PUBLIC) {
            if (match(Token.TokenType.RESERVED_PUBLIC)) {
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.RESERVED_PRIVATE) {
            if (match(Token.TokenType.RESERVED_PRIVATE)) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
    //MEMBERDECL                 -> MEMBERVARDECL | MEMBERFUNCDECL
    private boolean memberDecl() throws IOException {
        if (getType() == Token.TokenType.RESERVED_FUNCTION || getType() == Token.TokenType.RESERVED_CONSTRUCTOR) {
            if(memberFuncDecl()){
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.RESERVED_ATTRIBUTE) {
            if(memberVarDecl()){
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
    //MEMBERFUNCDECL             -> MEMBERFUNCHEAD semi
    private boolean memberFuncDecl() throws IOException {
        if (getType() == Token.TokenType.RESERVED_FUNCTION || getType() == Token.TokenType.RESERVED_CONSTRUCTOR) {
            if (memberFuncHead() && match(Token.TokenType.PUNCTUATION_SEMICOLON)) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
    //MEMBERFUNCHEAD             -> function id colon lpar FPARAMS rpar arrow RETURNTYPE
    //                               | constructorkeyword colon lpar FPARAMS rpar
    private boolean memberFuncHead() throws IOException {
        if(getType() == Token.TokenType.RESERVED_CONSTRUCTOR){
            if(match(Token.TokenType.RESERVED_CONSTRUCTOR) && match(Token.TokenType.PUNCTUATION_COLON) && match(Token.TokenType.PUNCTUATION_LPAREN)
            && fParams() && match(Token.TokenType.PUNCTUATION_RPAREN)){
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.RESERVED_FUNCTION) {
            if(match(Token.TokenType.RESERVED_FUNCTION) && match(Token.TokenType.ID) && match(Token.TokenType.PUNCTUATION_COLON)
            && match(Token.TokenType.PUNCTUATION_LPAREN) && fParams() && match(Token.TokenType.PUNCTUATION_RPAREN) && match(Token.TokenType.OPERATOR_ARROW)
            && returnType()){
                return true;
            } else{
                return false;
            }
        } else {
            return false;
        }
    }
    //FPARAMS                    -> id colon TYPE REPTFPARAMS3 REPTFPARAMS4 | EPSILON
    private boolean fParams() throws IOException {
        if (getType() == Token.TokenType.ID) {
            if (match(Token.TokenType.ID) && match(Token.TokenType.PUNCTUATION_COLON) && Type() && reptFParams3() && reptFParams4()) {
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.PUNCTUATION_RPAREN) {
            return true;
        } else {
            return false;
        }
    }
    //REPTFPARAMS3               -> ARRAYSIZE REPTFPARAMS3 | EPSILON
    private boolean reptFParams3() throws IOException {
        if (getType() == Token.TokenType.PUNCTUATION_LBRACKET) {
            if (arraySize() && reptFParams3()) {
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.PUNCTUATION_RPAREN || getType() == Token.TokenType.PUNCTUATION_COMMA) {
            return true;
        } else {
            return false;
        }
    }
    //REPTFPARAMS4               -> FPARAMSTAIL REPTFPARAMS4 | EPSILON
    private boolean reptFParams4() throws IOException {
        if (getType() == Token.TokenType.PUNCTUATION_COMMA) {
            if (fParamsTail() && reptFParams4()) {
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.PUNCTUATION_RPAREN) {
            return true;
        } else {
            return false;
        }
    }

    // FPARAMSTAIL                -> comma id colon TYPE REPTFPARAMSTAIL4
    private boolean fParamsTail() throws IOException {
        if (getType() == Token.TokenType.PUNCTUATION_COMMA) {
            if (match(Token.TokenType.PUNCTUATION_COMMA) && match(Token.TokenType.ID) && match(Token.TokenType.PUNCTUATION_COLON)
                    && Type() && reptFParamsTail4()) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
    //REPTFPARAMSTAIL4           -> ARRAYSIZE REPTFPARAMSTAIL4 | EPSILON
    private boolean reptFParamsTail4() throws IOException {
        if (getType() == Token.TokenType.PUNCTUATION_LBRACKET) {
            if (arraySize() && reptFParamsTail4()) {
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.PUNCTUATION_RPAREN || getType() == Token.TokenType.PUNCTUATION_COMMA) {
            return true;
        } else {
            return false;
        }
    }

    // RETURNTYPE                 -> void | TYPE
    private boolean returnType() throws IOException {
        if (getType() == Token.TokenType.RESERVED_FLOAT || getType() == Token.TokenType.ID || getType() == Token.TokenType.RESERVED_INTEGER) {
            if(Type()){
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.RESERVED_VOID) {
            if (match(Token.TokenType.RESERVED_VOID)) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    //MEMBERVARDECL              -> attribute id colon TYPE REPTARRAYSIZE semi
    private boolean memberVarDecl() throws IOException {
        if (getType() == Token.TokenType.RESERVED_ATTRIBUTE) {
            if (match(Token.TokenType.RESERVED_ATTRIBUTE) && match(Token.TokenType.ID) && match(Token.TokenType.PUNCTUATION_COLON)
                    && Type() && reptArraySize() && match(Token.TokenType.PUNCTUATION_SEMICOLON)) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    //TYPE                       -> float | id | integer
    private boolean Type() throws IOException {
        if (getType() == Token.TokenType.RESERVED_INTEGER) {
            if (match(Token.TokenType.RESERVED_INTEGER)) {
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.ID) {
            if (match(Token.TokenType.ID)) {
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.RESERVED_FLOAT) {
            if (match(Token.TokenType.RESERVED_FLOAT)) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }


    //ARRAYSIZE                  -> lsqbr ARRAYSIZE2
    private boolean arraySize() throws IOException {
        if (getType() == Token.TokenType.PUNCTUATION_LBRACKET) {
            if (match(Token.TokenType.PUNCTUATION_LBRACKET) && arraySize2()) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    // ARRAYSIZE2                 -> intlit rsqbr | rsqbr
    private boolean arraySize2() throws IOException {
        if (getType() == Token.TokenType.PUNCTUATION_RBRACKET) {
            if (match(Token.TokenType.PUNCTUATION_RBRACKET)) {
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.INTEGER) {
            if (match(Token.TokenType.INTEGER) && match(Token.TokenType.PUNCTUATION_RBRACKET)) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    //ARRAYOROBJECT              -> lpar APARAMS rpar | REPTARRAYSIZE
    private boolean arrayOrObject() throws IOException {
        if (getType() == Token.TokenType.PUNCTUATION_LPAREN) {
            if (match(Token.TokenType.PUNCTUATION_LPAREN) && aParams() && match(Token.TokenType.PUNCTUATION_RPAREN)) {
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.PUNCTUATION_SEMICOLON) {
            return true;
        } else if (getType() == Token.TokenType.PUNCTUATION_LBRACKET) {
            if (reptArraySize()) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    // REPTARRAYSIZE              -> ARRAYSIZE REPTARRAYSIZE | EPSILON
    private boolean reptArraySize() throws IOException {
        if (getType() == Token.TokenType.PUNCTUATION_LBRACKET) {
            if (arraySize() && reptArraySize()) {
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.PUNCTUATION_SEMICOLON) {
            return true;
        } else {
            return false;
        }
    }

    //PROG                       -> REPTPROG0
    private boolean prog() throws IOException {
        if(getType() == Token.TokenType.RESERVED_FUNCTION || getType() == Token.TokenType.RESERVED_CLASS
        || getType() == Token.TokenType.EOF){
            if (reptProg0()) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
    
    // REPTPROG0                  -> CLASSDECLORFUNCDEF REPTPROG0 | EPSILON
    private boolean reptProg0() throws IOException {
        if(getType() == Token.TokenType.RESERVED_FUNCTION || getType() == Token.TokenType.RESERVED_CLASS){
            if (classDeclOrFuncDef() && reptProg0()) {
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.EOF) {
            return true;
        } else {
            return false;
        }
    }

    //REPTVARIABLE               -> VARIDNEST REPTVARIABLE | EPSILON
    private boolean reptVariable() throws IOException {
        if(getType() == Token.TokenType.PUNCTUATION_DOT){
            if(varIdnest() && reptVariable()){
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.PUNCTUATION_RPAREN) {
            return true;
        } else{
            return false;
        }
    }
    
    // VARIDNEST                  -> dot id VARIDNEST2
    private boolean varIdnest() throws IOException {
        if(getType() == Token.TokenType.PUNCTUATION_DOT){
            if(match(Token.TokenType.PUNCTUATION_DOT) && match(Token.TokenType.ID) && varIdnest2()){
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    // VARIDNEST2                 -> lpar APARAMS rpar VARIDNEST | REPTIDNEST1
    private boolean varIdnest2() throws IOException {
        if (getType() == Token.TokenType.PUNCTUATION_LPAREN) {
            if (match(Token.TokenType.PUNCTUATION_LPAREN) && aParams() && match(Token.TokenType.PUNCTUATION_RPAREN) && varIdnest()) {
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.PUNCTUATION_LBRACKET || getType() == Token.TokenType.PUNCTUATION_RPAREN || getType() == Token.TokenType.PUNCTUATION_DOT) {
           if(reptIdnest1()){
               return true;
           } else {
               return false;
           }
        } else {
            return false;
        }
    }
}