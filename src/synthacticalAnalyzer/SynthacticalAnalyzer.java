package synthacticalAnalyzer;

import com.sun.org.apache.xpath.internal.operations.NotEquals;
import lexicalAnalyzer.LexicalAnalyzer;
import lexicalAnalyzer.Token;

import java.io.*;
import java.util.Locale;

public class SynthacticalAnalyzer {
    private Token lookahead;
    private LexicalAnalyzer lexer;
    private PrintWriter pwDerivation = new PrintWriter(new FileWriter("parser/example-polynomial.outderivation.src", true));;
    private PrintWriter pwError = new PrintWriter(new FileWriter("parser/example-polynomial.outsyntaxerror.src", true));;
    public SynthacticalAnalyzer(LexicalAnalyzer lexer) throws IOException {
        this.lexer = lexer;
        this.lookahead = lexer.nextToken();

    }

    private boolean match(Token.TokenType terminal) throws IOException {
        skip();
        if (getType() == terminal) {
            write(terminal.getValue(), terminal.getValue());
            return true;
        } else {
            error(terminal.getValue(), terminal.getValue());
            return false;
        }
    }
    private void write(String LHS, String RHS) throws IOException {
        pwDerivation.println(LHS + " -> " + RHS);
    }
    private void skip() throws IOException {
        lookahead = lexer.nextToken();
    }

    private void error(String LHS, String RHS) throws IOException {
        pwError.println(LHS + " -> " + RHS);

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

    private boolean start() throws IOException {
        return reptStart0();
    }

    private boolean reptStart0() throws IOException {
        if (getType() == Token.TokenType.RESERVED_FUNCTION || getType() == Token.TokenType.RESERVED_CLASS) {
            if (classDeclOrFuncDef() && reptStart0()) {
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

    private boolean funcHead() throws IOException {
        if (getType() == Token.TokenType.RESERVED_FUNCTION) {
            if (match(Token.TokenType.RESERVED_FUNCTION) && match(Token.TokenType.ID) && funcHead1()) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private boolean funcBody() throws IOException {
        if (getType() == Token.TokenType.PUNCTUATION_LCURLY) {
            if (match(Token.TokenType.PUNCTUATION_LCURLY) && reptFuncBody1() && match(Token.TokenType.PUNCTUATION_RCURLY)) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private boolean reptFuncBody1() throws IOException {
        if (getType() == Token.TokenType.RESERVED_IF || getType() == Token.TokenType.RESERVED_READ || getType() == Token.TokenType.RESERVED_RETURN
                || getType() == Token.TokenType.RESERVED_WHILE || getType() == Token.TokenType.RESERVED_WRITE || getType() == Token.TokenType.ID
                || getType() == Token.TokenType.RESERVED_LOCALVAR) {
            if (localVarDeclOrStmt() && reptFuncBody1()) {
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

    private boolean localVarDeclOrStmt() throws IOException {
        if (getType() == Token.TokenType.RESERVED_LOCALVAR) {
            return localVarDecl();
        } else if (getType() == Token.TokenType.RESERVED_IF || getType() == Token.TokenType.RESERVED_READ || getType() == Token.TokenType.RESERVED_RETURN
                || getType() == Token.TokenType.RESERVED_WHILE || getType() == Token.TokenType.RESERVED_WRITE || getType() == Token.TokenType.ID) {
            return statement();
        } else {
            return false;
        }
    }

    private boolean statement() throws IOException {
        if (getType() == Token.TokenType.ID) {
            if (reptVariable0() && t1() && match(Token.TokenType.PUNCTUATION_SEMICOLON)) {
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
                    && match(Token.TokenType.PUNCTUATION_SEMICOLON)) {
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

    //There is a left recursion here
    private boolean reptVariable0() throws IOException {
        if (getType() == Token.TokenType.ID) {
            if (reptVariable0() && idnest()) {
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.ID) {
            return true;
        } else {
            return false;
        }
    }

    private boolean idnest() throws IOException {
        if (getType() == Token.TokenType.ID) {
            if (match(Token.TokenType.ID) && idnest1()) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private boolean idnest1() throws IOException {
        if (getType() == Token.TokenType.PUNCTUATION_LBRACKET || getType() == Token.TokenType.PUNCTUATION_DOT) {
            if (reptIdnest1() && match(Token.TokenType.PUNCTUATION_DOT)) {
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.PUNCTUATION_LPAREN) {
            if (match(Token.TokenType.PUNCTUATION_LPAREN) && aParams() && match(Token.TokenType.PUNCTUATION_RPAREN) && match(Token.TokenType.PUNCTUATION_DOT)) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private boolean reptIdnest1() throws IOException {
        if (getType() == Token.TokenType.PUNCTUATION_LBRACKET) {
            if (indice() && reptIdnest1()) {
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.PUNCTUATION_DOT) {
            return true;
        } else {
            return false;
        }
    }

    private boolean t1() throws IOException {
        if (getType() == Token.TokenType.ID) {
            if (match(Token.TokenType.ID) && t2()) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private boolean t2() throws IOException {
        if (getType() == Token.TokenType.PUNCTUATION_LBRACKET || getType() == Token.TokenType.OPERATOR_ASSIGN) {
            if (reptVariable2() && assignOp() && expr()) {
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

    private boolean variable() throws IOException {
        if (getType() == Token.TokenType.ID) {
            if (reptVariable0() && match(Token.TokenType.ID) && reptVariable2()) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private boolean reptVariable2() throws IOException {
        if (getType() == Token.TokenType.PUNCTUATION_LBRACKET) {
            if (indice() && reptVariable2()) {
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.PUNCTUATION_RPAREN && getType() == Token.TokenType.PUNCTUATION_COMMA || getType() == Token.TokenType.PUNCTUATION_SEMICOLON
                || getType() == Token.TokenType.OPERATOR_EQ || getType() == Token.TokenType.OPERATOR_GE || getType() == Token.TokenType.OPERATOR_GT || getType() == Token.TokenType.OPERATOR_LE
                || getType() == Token.TokenType.OPERATOR_LT || getType() == Token.TokenType.OPERATOR_NE || getType() == Token.TokenType.PUNCTUATION_RBRACKET
                || getType() == Token.TokenType.OPERATOR_PLUS || getType() == Token.TokenType.OPERATOR_MINUS || getType() == Token.TokenType.RESERVED_OR || getType() == Token.TokenType.OPERATOR_MULT
                || getType() == Token.TokenType.OPERATOR_DIV || getType() == Token.TokenType.RESERVED_AND || getType() == Token.TokenType.OPERATOR_ASSIGN) {
            return true;
        } else {
            return false;
        }
    }

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

    private boolean reptStatBlock1() throws IOException {
        if(getType() == Token.TokenType.RESERVED_IF || getType() == Token.TokenType.RESERVED_READ || getType() == Token.TokenType.RESERVED_RETURN
        || getType() == Token.TokenType.RESERVED_WHILE || getType() == Token.TokenType.RESERVED_WRITE || getType() == Token.TokenType.ID){
            if(statement() && reptStatBlock1()){
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.PUNCTUATION_RCURLY) {
             return true;
        } else{
            return false;
        }
    }

    private boolean localVarDecl() throws IOException {
        if (getType() == Token.TokenType.RESERVED_LOCALVAR) {
            if (match(Token.TokenType.RESERVED_LOCALVAR) && match(Token.TokenType.ID) && match(Token.TokenType.PUNCTUATION_COLON) && Type()
                    && localVarDecl1()) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private boolean localVarDecl1() throws IOException {
        if (getType() == Token.TokenType.PUNCTUATION_LBRACKET || getType() == Token.TokenType.PUNCTUATION_SEMICOLON) {
            if (reptLocalVarDecl4() && match(Token.TokenType.PUNCTUATION_SEMICOLON)) {
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.PUNCTUATION_LPAREN) {
            if (match(Token.TokenType.PUNCTUATION_LPAREN) && aParams() && match(Token.TokenType.PUNCTUATION_RPAREN) && match(Token.TokenType.PUNCTUATION_SEMICOLON)) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private boolean reptLocalVarDecl4() throws IOException {
        if(getType() == Token.TokenType.PUNCTUATION_LBRACKET){
            if(arraySize() && reptLocalVarDecl4()){
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

    private boolean rightRecArithExpr() throws IOException {
        if(getType() == Token.TokenType.OPERATOR_PLUS || getType() == Token.TokenType.OPERATOR_MINUS || getType() == Token.TokenType.RESERVED_OR){
            if(addOp() && term() && rightRecArithExpr()){
                return true;
            } else {
                return false;
            }
        } else if(getType() == Token.TokenType.PUNCTUATION_RPAREN && getType() == Token.TokenType.PUNCTUATION_COMMA || getType() == Token.TokenType.PUNCTUATION_SEMICOLON
                || getType() == Token.TokenType.OPERATOR_EQ || getType() == Token.TokenType.OPERATOR_GE || getType() == Token.TokenType.OPERATOR_GT || getType() == Token.TokenType.OPERATOR_LE
                || getType() == Token.TokenType.OPERATOR_LT || getType() == Token.TokenType.OPERATOR_NE || getType() == Token.TokenType.PUNCTUATION_RBRACKET){
            return true;
        } else {
            return false;
        }
    }
    //   ADDOP                      -> minus | or | plus
    private boolean addOp() throws IOException {
        if(getType() == Token.TokenType.RESERVED_OR){
            if(match(Token.TokenType.RESERVED_OR)){
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.OPERATOR_PLUS) {
            if(match(Token.TokenType.OPERATOR_PLUS)){
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.OPERATOR_MINUS) {
            if(match(Token.TokenType.OPERATOR_MINUS)){
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

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
        if(getType() == Token.TokenType.OPERATOR_PLUS || getType() == Token.TokenType.OPERATOR_MINUS){
            if(sign() && factor()){
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.ID) {
            if(reptVariable0() && factor2() && reptVariableOrFunctionCall()){
                return true;
            } else {
                return  false;
            }
        } else if (getType() == Token.TokenType.RESERVED_NOT) {
            if(match(Token.TokenType.RESERVED_NOT) && factor()){
                return true;
            } else{
                return false;
            }
        } else if (getType() == Token.TokenType.INTEGER) {
            if(match(Token.TokenType.INTEGER)){
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.FLOAT) {
            if(match(Token.TokenType.FLOAT)){
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.PUNCTUATION_LPAREN) {
            if(match(Token.TokenType.PUNCTUATION_LPAREN) && arithExpr() && match(Token.TokenType.PUNCTUATION_RPAREN)){
                return true;
            } else{
                return false;
            }
        } else {
            return false;
        }
    }

    private boolean sign() throws IOException {
        if(getType() == Token.TokenType.OPERATOR_MINUS){
            if(match(Token.TokenType.OPERATOR_MINUS)){
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.OPERATOR_PLUS) {
            if(match(Token.TokenType.OPERATOR_PLUS)){
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private boolean factor2() throws IOException {
        if(getType() == Token.TokenType.PUNCTUATION_LPAREN){
            if(match(Token.TokenType.PUNCTUATION_LPAREN) && aParams() && match(Token.TokenType.PUNCTUATION_RPAREN)){
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private boolean temp2() throws IOException {
        if(getType() == Token.TokenType.PUNCTUATION_LBRACKET){
            if(reptVariable2()){
                return true;
            } else{
                return false;
            }
        } else if (getType() == Token.TokenType.PUNCTUATION_LPAREN) {
            if(match(Token.TokenType.PUNCTUATION_LPAREN) && aParams() && match(Token.TokenType.PUNCTUATION_RPAREN)){
                return true;
            } else {
                return false;
            }
        } else if(getType() == Token.TokenType.PUNCTUATION_RPAREN && getType() == Token.TokenType.PUNCTUATION_COMMA || getType() == Token.TokenType.PUNCTUATION_SEMICOLON
                || getType() == Token.TokenType.OPERATOR_EQ || getType() == Token.TokenType.OPERATOR_GE || getType() == Token.TokenType.OPERATOR_GT || getType() == Token.TokenType.OPERATOR_LE
                || getType() == Token.TokenType.OPERATOR_LT || getType() == Token.TokenType.OPERATOR_NE || getType() == Token.TokenType.PUNCTUATION_RBRACKET
                || getType() == Token.TokenType.OPERATOR_PLUS || getType() == Token.TokenType.OPERATOR_MINUS || getType() == Token.TokenType.RESERVED_OR || getType() == Token.TokenType.OPERATOR_MULT
                || getType() == Token.TokenType.OPERATOR_DIV || getType() == Token.TokenType.RESERVED_AND){
            return true;
        } else {
            return false;
        }
    }

    private boolean rightRecTerm() throws IOException {
        if (getType() == Token.TokenType.OPERATOR_MULT || getType() == Token.TokenType.OPERATOR_DIV || getType() == Token.TokenType.RESERVED_AND) {
            if (multOp() && factor() && rightRecTerm()) {
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.PUNCTUATION_RPAREN && getType() == Token.TokenType.PUNCTUATION_COMMA || getType() == Token.TokenType.PUNCTUATION_SEMICOLON
                || getType() == Token.TokenType.OPERATOR_EQ || getType() == Token.TokenType.OPERATOR_GE || getType() == Token.TokenType.OPERATOR_GT || getType() == Token.TokenType.OPERATOR_LE
                || getType() == Token.TokenType.OPERATOR_LT || getType() == Token.TokenType.OPERATOR_NE || getType() == Token.TokenType.PUNCTUATION_RBRACKET
                || getType() == Token.TokenType.OPERATOR_PLUS || getType() == Token.TokenType.OPERATOR_MINUS || getType() == Token.TokenType.RESERVED_OR) {
            return true;

        } else {
            return false;
        }
    }

    private boolean multOp() throws IOException {
        if(getType() == Token.TokenType.RESERVED_AND){
            if(match(Token.TokenType.RESERVED_AND)){
                return true;
            } else {
                return false;
            }
        } else if(getType() == Token.TokenType.OPERATOR_DIV){
            if(match(Token.TokenType.OPERATOR_DIV)){
                return true;
            } else {
                return false;
            }
        } else if(getType() == Token.TokenType.OPERATOR_MULT){
            if(match(Token.TokenType.OPERATOR_MULT)){
                return true;
            } else {
                return false;
            }
        }  else {
            return false;
        }
    }

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


    private boolean funcHead1() throws IOException {
        if (getType() == Token.TokenType.SCOPE_RESOLUTION) {
            if (match(Token.TokenType.SCOPE_RESOLUTION) && funcHead2()) {
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.PUNCTUATION_RPAREN) {
            if (match(Token.TokenType.PUNCTUATION_RPAREN) && fParams() && match(Token.TokenType.PUNCTUATION_LPAREN)
                    && match(Token.TokenType.OPERATOR_ARROW) && returnType()) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private boolean funcHead2() throws IOException {
        if (getType() == Token.TokenType.RESERVED_CONSTRUCTOR) {
            if (match(Token.TokenType.RESERVED_CONSTRUCTOR) && match(Token.TokenType.PUNCTUATION_RPAREN) && fParams()
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
        } else if (getType() == Token.TokenType.RESERVED_ATTRIBUTE || getType() == Token.TokenType.RESERVED_FUNCTION
                || getType() == Token.TokenType.RESERVED_CONSTRUCTOR) {
            return true;
        } else {
            return false;
        }
    }

    private boolean memberDecl() throws IOException {
        if (getType() == Token.TokenType.RESERVED_FUNCTION || getType() == Token.TokenType.RESERVED_CONSTRUCTOR) {
            return memberFuncDecl();
        } else if (getType() == Token.TokenType.RESERVED_ATTRIBUTE) {
            return memberVarDecl();
        } else {
            return false;
        }
    }

    private boolean memberFuncDecl() throws IOException {
        if (getType() == Token.TokenType.RESERVED_CONSTRUCTOR) {
            if (match(Token.TokenType.RESERVED_CONSTRUCTOR) && match(Token.TokenType.PUNCTUATION_COLON) && match(Token.TokenType.PUNCTUATION_LPAREN)
                    && fParams() && match(Token.TokenType.PUNCTUATION_RPAREN) && match(Token.TokenType.PUNCTUATION_SEMICOLON)) {
                return true;
            } else {
                return false;
            }
        } else if (getType() == Token.TokenType.RESERVED_FUNCTION) {
            if (match(Token.TokenType.RESERVED_FUNCTION) && match(Token.TokenType.ID) && match(Token.TokenType.PUNCTUATION_LPAREN)
                    && fParams() && match(Token.TokenType.PUNCTUATION_RPAREN) && match(Token.TokenType.OPERATOR_ARROW) && returnType()
                    && match(Token.TokenType.PUNCTUATION_SEMICOLON)) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private boolean fParams() throws IOException {
        if (getType() == Token.TokenType.ID) {
            if (match(Token.TokenType.ID) && match(Token.TokenType.PUNCTUATION_COLON) && Type() && reptFParams3() && reptFParams4()) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

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


    private boolean returnType() throws IOException {
        if (getType() == Token.TokenType.RESERVED_FLOAT || getType() == Token.TokenType.ID || getType() == Token.TokenType.RESERVED_INTEGER) {
            return Type();
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


    private boolean memberVarDecl() throws IOException {
        if (getType() == Token.TokenType.RESERVED_ATTRIBUTE) {
            if (match(Token.TokenType.RESERVED_ATTRIBUTE) && match(Token.TokenType.ID) && match(Token.TokenType.PUNCTUATION_COLON)
                    && Type() && reptMemberVarDecl4() && match(Token.TokenType.PUNCTUATION_SEMICOLON)) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

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

    private boolean reptMemberVarDecl4() throws IOException {
        if (getType() == Token.TokenType.PUNCTUATION_LBRACKET) {
            if (arraySize() && reptOptClassDecl22()) {
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

    private boolean reptOptClassDecl22() throws IOException {
        if (getType() == Token.TokenType.PUNCTUATION_COMMA) {
            if (match(Token.TokenType.PUNCTUATION_COMMA) && match(Token.TokenType.ID) && reptOptClassDecl22()) {
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
    //ARRAYOROBJECT              -> lpar APARAMS rpar | REPTARRAYSIZE
    private boolean arrayOrObject() throws IOException {
        if(getType() == Token.TokenType.PUNCTUATION_LPAREN){
            if(match(Token.TokenType.PUNCTUATION_LPAREN) && aParams() && match(Token.TokenType.PUNCTUATION_RPAREN)){
                return true;
            } else {
                return false;
            }
        } else if(getType() == Token.TokenType.PUNCTUATION_SEMICOLON){
            return true;
        } else if(getType() == Token.TokenType.PUNCTUATION_LBRACKET){
            if(reptArraySize()){
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
        if(getType() == Token.TokenType.PUNCTUATION_LBRACKET){
            if(arraySize() && reptArraySize()){
                return true;
            } else {
                return false;
            }
        } else if(getType() == Token.TokenType.PUNCTUATION_SEMICOLON){
            return true;
        } else {
            return false;
        }
    }

}