package synthacticalAnalyzer;

import lexicalAnalyzer.LexicalAnalyzer;
import lexicalAnalyzer.Token;

import java.io.IOException;
import java.io.PushbackReader;
import java.util.Locale;

public class SynthacticalAnalyzer {
    private Token lookahead;
    private LexicalAnalyzer lexer;

    public SynthacticalAnalyzer(LexicalAnalyzer lexer) throws IOException {
        this.lexer = lexer;
        this.lookahead = lexer.nextToken();

    }

    private boolean match(Token.TokenType terminal) throws IOException {
        skip();
        if (getType() == terminal) {
            return true;
        } else {
            error();
            return false;
        }
    }

    private void skip() throws IOException {
        lookahead = lexer.nextToken();
    }

    private void error() {
        //write the error file.
    }

    public boolean parse() throws IOException {
        return start();
    }
    private Token.TokenType getType() {
        return lookahead.getType();
    }
    private boolean start() throws IOException {
        return reptStart0();
    }

    private boolean reptStart0() throws IOException {
        if(getType() == Token.TokenType.RESERVED_FUNCTION || getType() == Token.TokenType.RESERVED_CLASS){
            if(classDeclOrFuncDef() && reptStart0()){
                return true;
            } else {
                return false;
            }
        } else if(getType() == Token.TokenType.EOF){
            return true;
        } else {
            return false;
        }
    }


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
        if(getType() == Token.TokenType.RESERVED_FUNCTION){
            if(match(Token.TokenType.RESERVED_FUNCTION) && match(Token.TokenType.ID) && funcHead1()){
                return  true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private boolean funcBody() throws IOException {
        if(getType() == Token.TokenType.PUNCTUATION_LCURLY){
            if(match(Token.TokenType.PUNCTUATION_LCURLY) && reptFuncBody1() && match(Token.TokenType.PUNCTUATION_RCURLY)){
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private boolean reptFuncBody1() throws IOException {
        if(getType() == Token.TokenType.RESERVED_IF || getType() == Token.TokenType.RESERVED_READ || getType() == Token.TokenType.RESERVED_RETURN
        || getType() == Token.TokenType.RESERVED_WHILE || getType() == Token.TokenType.RESERVED_WRITE || getType() == Token.TokenType.ID
        || getType() == Token.TokenType.RESERVED_LOCALVAR){
            if(localVarDeclOrStmt() && reptFuncBody1()){
                return true;
            } else {
                return  false;
            }
        } else if(getType() == Token.TokenType.PUNCTUATION_RCURLY){
            return true;
        } else {
            return false;
        }
    }

    private boolean localVarDeclOrStmt() throws IOException {
        if(getType() == Token.TokenType.RESERVED_LOCALVAR){
            return localVarDecl();
        } else if(getType() == Token.TokenType.RESERVED_IF || getType() == Token.TokenType.RESERVED_READ || getType() == Token.TokenType.RESERVED_RETURN
        || getType() == Token.TokenType.RESERVED_WHILE || getType() == Token.TokenType.RESERVED_WRITE || getType() == Token.TokenType.ID){
            return statement();
        } else {
            return false;
        }
    }

    private boolean statement() {
    }

    private boolean localVarDecl() throws IOException {
        if(getType() == Token.TokenType.RESERVED_LOCALVAR){
            if(match(Token.TokenType.RESERVED_LOCALVAR) && match(Token.TokenType.ID) && match(Token.TokenType.PUNCTUATION_COLON) && Type()
            && localVarDecl1()){
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private boolean localVarDecl1() throws IOException {
        if(getType() == Token.TokenType.PUNCTUATION_LBRACKET || getType() == Token.TokenType.PUNCTUATION_SEMICOLON){
            if(reptLocalVarDecl4() && match(Token.TokenType.PUNCTUATION_SEMICOLON)){
                return true;
            } else {
                return false;
            }
        } else if(getType() == Token.TokenType.PUNCTUATION_LPAREN){
            if(match(Token.TokenType.PUNCTUATION_LPAREN) && aParams() && match(Token.TokenType.PUNCTUATION_RPAREN) && match(Token.TokenType.PUNCTUATION_SEMICOLON)){
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private boolean reptLocalVarDecl4() {

    }

    private boolean aParams() {
        if(getType() == Token.TokenType.PUNCTUATION_LPAREN || getType() == Token.TokenType.FLOAT || getType() == Token.TokenType.INTEGER
        || getType() == Token.TokenType.RESERVED_NOT || getType() == Token.TokenType.ID || getType() == Token.TokenType.OPERATOR_PLUS
        || getType() == Token.TokenType.OPERATOR_MINUS){
            if(expr() && reptParams1()){
                return true;
        }  else {
                return false;
            }
        } else if(getType() == Token.TokenType.PUNCTUATION_RPAREN){
            return true;
        } else {
            return false;
        }
    }

    private boolean expr() {
    }

    private boolean reptParams1() {
    }


    private boolean funcHead1() throws IOException {
        if(getType() == Token.TokenType.SCOPE_RESOLUTION){
            if(match(Token.TokenType.SCOPE_RESOLUTION) && funcHead2()){
                return true;
            } else {
                return false;
            }
        } else if(getType() == Token.TokenType.PUNCTUATION_RPAREN){
            if(match(Token.TokenType.PUNCTUATION_RPAREN) && fParams() && match(Token.TokenType.PUNCTUATION_LPAREN)
            && match(Token.TokenType.OPERATOR_ARROW) && returnType()){
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private boolean funcHead2() throws IOException {
        if(getType() == Token.TokenType.RESERVED_CONSTRUCTOR){
            if(match(Token.TokenType.RESERVED_CONSTRUCTOR) && match(Token.TokenType.PUNCTUATION_RPAREN) && fParams()
            && match(Token.TokenType.PUNCTUATION_RPAREN)){
                return true;
            } else {
                return false;
            }
        } else if(getType() == Token.TokenType.ID){
            if(match(Token.TokenType.ID) && match(Token.TokenType.PUNCTUATION_LPAREN) && fParams() && match(Token.TokenType.PUNCTUATION_RPAREN)
            && match(Token.TokenType.OPERATOR_ARROW) && returnType()){
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private boolean classDecl() throws IOException {
        if (getType() == Token.TokenType.RESERVED_CLASS) {
            if (match(Token.TokenType.RESERVED_CLASS) && match(Token.TokenType.ID) && optClassDecl2() && match(Token.TokenType.PUNCTUATION_LCURLY)
                    && reptClassDecl4() && match(Token.TokenType.PUNCTUATION_RCURLY) && match(Token.TokenType.PUNCTUATION_SEMICOLON)) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private boolean optClassDecl2() throws IOException {
        if (getType() == Token.TokenType.RESERVED_ISA) {
            if (match(Token.TokenType.RESERVED_ISA) && match(Token.TokenType.ID) && reptOptClassDecl22()) {
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

    private boolean reptClassDecl4() throws IOException {
        if (getType() == Token.TokenType.RESERVED_PRIVATE) {
            if (visibility() && memberDecl() && reptClassDecl4()) {
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
        if(getType() == Token.TokenType.ID){
            if(match(Token.TokenType.ID) && match(Token.TokenType.PUNCTUATION_COLON) && Type() && reptFParams3() && reptFParams4()){
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private boolean reptFParams3() throws IOException {
        if(getType() == Token.TokenType.PUNCTUATION_LBRACKET) {
            if(arraySize() && reptFParams3()){
                return true;
            } else {
                return false;
            }
        } else if(getType() == Token.TokenType.PUNCTUATION_RPAREN || getType() == Token.TokenType.PUNCTUATION_COMMA){
            return true;
        } else {
            return false;
        }
    }

    private boolean reptFParams4() throws IOException {
        if(getType() == Token.TokenType.PUNCTUATION_COMMA){
            if(fParamsTail() && reptFParams4()){
                return true;
            } else {
                return false;
            }
        } else if(getType() == Token.TokenType.PUNCTUATION_RPAREN){
            return true;
        } else {
            return false;
        }
    }

    private boolean fParamsTail() throws IOException {
        if(getType() == Token.TokenType.PUNCTUATION_COMMA){
            if(match(Token.TokenType.PUNCTUATION_COMMA) && match(Token.TokenType.ID) && match(Token.TokenType.PUNCTUATION_COLON)
            && Type() && reptFParamsTail4()){
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private boolean reptFParamsTail4() throws IOException {
        if(getType() == Token.TokenType.PUNCTUATION_LBRACKET){
            if(arraySize() && reptFParamsTail4()){
                return true;
            } else {
                return false;
            }
        } else if(getType() == Token.TokenType.PUNCTUATION_RPAREN || getType() == Token.TokenType.PUNCTUATION_COMMA){
            return true;
        } else {
            return false;
        }
    }


    private boolean returnType() throws IOException {
        if(getType() == Token.TokenType.RESERVED_FLOAT || getType() == Token.TokenType.ID || getType() == Token.TokenType.RESERVED_INTEGER){
            return Type();
        } else if( getType() == Token.TokenType.RESERVED_VOID){
            if(match(Token.TokenType.RESERVED_VOID)){
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
        if(getType() == Token.TokenType.RESERVED_INTEGER){
            if(match(Token.TokenType.RESERVED_INTEGER)){
                return true;
            } else {
                return false;
            }
        } else if(getType() == Token.TokenType.ID){
            if(match(Token.TokenType.ID)){
                return true;
            } else {
                return false;
            }
        } else if(getType() == Token.TokenType.RESERVED_FLOAT){
            if(match(Token.TokenType.RESERVED_FLOAT)){
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private boolean reptMemberVarDecl4() throws IOException {
        if(getType() == Token.TokenType.PUNCTUATION_LBRACKET){
            if(arraySize() && reptOptClassDecl22()){
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

    private boolean arraySize() throws IOException {
        if(getType() == Token.TokenType.PUNCTUATION_LBRACKET){
            if(match(Token.TokenType.PUNCTUATION_LBRACKET) && arraySize1()){
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private boolean arraySize1() throws IOException {
        if(getType() == Token.TokenType.PUNCTUATION_RBRACKET){
            if(match(Token.TokenType.PUNCTUATION_RBRACKET)){
                return true;
            } else {
                return false;
            }
        } else if(getType() == Token.TokenType.INTEGER){
            if(match(Token.TokenType.INTEGER) && match(Token.TokenType.PUNCTUATION_RBRACKET)){
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
}