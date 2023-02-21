package synthacticalAnalyzer;

import lexicalAnalyzer.LexicalAnalyzer;
import lexicalAnalyzer.Token;

import java.io.IOException;
import java.io.PushbackReader;

public class SynthacticalAnalyzer {
    private Token token;
    private String lookahead;
    private LexicalAnalyzer lexer;
    public SynthacticalAnalyzer(LexicalAnalyzer lexer) throws IOException {
        this.token = lexer.nextToken();
        this.lexer = lexer;
        this.lookahead = this.token.getLexeme();

    }
    private boolean match(String terminal) throws IOException {
        skip();
        if(lookahead.equals(terminal)){
            return true;
        } else {
            error();
            return false;
        }
    }
    private void skip() throws IOException {
        this.token = lexer.nextToken();
        this.lookahead = this.token.getLexeme();
    }
    private void error(){
        //write to error file.
    }
    public boolean parse() throws IOException {
        return start();
    }

    private boolean start() throws IOException {
        return classDeclOrFuncDef();
    }

    private boolean classDeclOrFuncDef() throws IOException {
        if(lookahead.equals("class")){
            if(classDecl()){

            } else {
                return false;
            }
        } else if(lookahead.equals("function")){
            if (funcDef()){

            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private boolean classDecl() throws IOException {
        if(lookahead.equals("class")){
            if(match("class") && match("id") && optClassDecl2() && match("{") && reptClassDecl4 && match("}") && match(";")){
                return true;
            } else {
                return false;
            }
        }
    }
    private boolean funcDef() {

    }

    private boolean funcHead() {
        match("function");
        matchIdentifier();
        funcHead1();
    }

    private boolean funcHead1() {
        if (match("(")) {
            fParams();
            match(")");
            arrow();
            returnType();
        } else {
            sr();
            funcHead2();
        }
    }

    private boolean funcHead2() {
        matchIdentifier();
        match("(");
        fParams();
        match(")");
        arrow();
        returnType();
    }

    private boolean arrow() {
        match("->");
    }

    private boolean returnType() {
        if (match("boolean") || match("float") || match("integer") || matchIdentifier()) {
            // matched a type
        } else {
            error();
        }
    }

    private boolean fParams() {
        if (matchIdentifier()) {
            match(":");
            type();
            reptFParams3();
            reptFParams4();
        }
    }

    private boolean reptFParams3() {
        if (match("[")) {
            matchIntLiteral();
            match("]");
            reptFParams3();
        }
    }

    private boolean reptFParams4() {
        if (match(",")) {
            matchIdentifier();
            match(":");
            type();
            reptFParamsTail4();
        }
    }

    private boolean reptFParamsTail4() {
        if (match("[")) {
            matchIntLiteral();
            match("]");
            reptFParamsTail4();
        }
    }

    private boolean funcBody() {
        match("{");
        reptFuncBody1();
        match("}");
    }

    private boolean reptFuncBody1() {
        if (match("if")) {
            match("(");
            relExpr();
            match(")");
            match("then");
            statBlock();
            match("else");
            statBlock();
            match(";");
            reptFuncBody1();
        } else if (match("read")) {
            match("(");
            variable();
            match(")");
            match(";");
            reptFuncBody1();
        } else if (match("return")) {
            match("(");
            expr();
            match(")");
            match(";");
            reptFuncBody1();
        } else if (match("while")) {
            match("(");
            relExpr();
            match(")");
            statBlock();
            match(";");
            reptFuncBody1();
        } else if (match("write")) {
            match("(");
            expr();
            match(")");
            match(";");
            reptFuncBody1();
        } else if (matchIdentifier()) {
            temp1();
            match(";");
            reptFuncBody1

            private boolean arrow() {
                if (match("->")) {
                    // arrow is optional, so do nothing if matched
                } else {
                    error();
                }
            }

            private boolean reptFParams3() {
                if (match("[")) {
                    arraySize();
                    reptFParams3();
                }
            }

            private boolean reptFParams4() {
                if (match(",")) {
                    matchIdentifier();
                    match(":");
                    type();
                    reptFParamsTail4();
                }
            }

            private boolean reptFParamsTail4() {
                if (match("[")) {
                    arraySize();
                    reptFParamsTail4();
                }
            }

            private boolean fParams() {
                if (matchIdentifier()) {
                    match(":");
                    type();
                    reptFParams3();
                    reptFParams4();
                } else {
                    // fParams can be empty, so do nothing if no match
                }
            }

            private boolean reptAParams1() {
                if (match(",")) {
                    expr();
                    reptAParams1();
                } else {
                    // reptAParams1 can be empty, so do nothing if no match
                }
            }

            private boolean aParamsTail() {
                match(",");
                expr();
            }

            private boolean aParams() {
                if (match("(")) {
                    expr();
                    reptAParams1();
                    match(")");
                } else {
                    // aParams can be empty, so do nothing if no match
                }
            }

            private boolean term() {
                factor();
                rightRecTerm();
            }

            private boolean rightRecTerm() {
                if (match("*") || match("/") || match("and")) {
                    factor();
                    rightRecTerm();
                } else {
                    // rightRecTerm can be empty, so do nothing if no match
                }
            }

            private boolean rightRecArithExpr() {
                if (match("+") || match("-") || match("or")) {
                    term();
                    rightRecArithExpr();
                } else {
                    // rightRecArithExpr can be empty, so do nothing if no match
                }
            }

            private boolean arithExpr() {
                term();
                rightRecArithExpr();
            }

            private boolean x() {
                if (match("eq") || match("geq") || match("gt") || match("leq") || match("lt") || match("neq")) {
                    arithExpr();
                } else {
                    // x can be empty, so do nothing if no match
                }
            }

            private boolean expr() {
                arithExpr();
                x();
            }

            private boolean relExpr() {
                arithExpr();
                relOp();
                arithExpr();
            }

            private boolean factor() {
                if (match("(")) {
                    arithExpr();
                    match(")");
                } else if (matchFloatLiteral()) {
                    // matched a float literal, do nothing
                } else if (matchIntLiteral()) {
                    // matched an int literal, do nothing
                } else if (match("not")) {
                    factor();
                } else if (matchIdentifier()) {
                    reptVariable0();
                    temp1();
                } else if (match("+") || match("-")) {
                    match("+") || match("-");
                    factor();
                } else {
                    error();
                }
            }

            private boolean temp1() {
                matchIdentifier();
                temp2();
            }

            private boolean temp2() {
                if (match("(")) {
                    aParams();
                    match(")");
                } else {
                    reptVariable2();
                }
            }

            private boolean classDecl() {
                match("class");
                matchIdentifier();

                optClassDecl2();
                match("{");
                reptClassDecl4();
                match("}");
                match(";");
            }

            private boolean optClassDecl2() {
                if (match("isa")) {
                    matchIdentifier();
                    reptOptClassDecl22();
                } else {
                    // optClassDecl2 can be empty, so do nothing if no match
                }
            }

            private boolean reptOptClassDecl22() {
                if (match(",")) {
                    matchIdentifier();
                    reptOptClassDecl22();
                } else {
                    // reptOptClassDecl22 can be empty, so do nothing if no match
                }
            }

            private boolean visibility() {
                if (match("private") || match("public")) {
                    // visibility is optional, so do nothing if matched
                } else {
                    // visibility can be empty, so do nothing if no match
                }
            }

            private boolean localVar() {
                match("localVar");
                matchIdentifier();
                match(":");
                type();
                localVarDecl1();
            }

            private boolean localVarDecl1() {
                if (match("(")) {
                    aParams();
                    match(";");
                } else {
                    reptLocalVarDecl4();
                    match(";");
                }
            }

            private boolean localVarDeclOrStmt() {
                if (isNextToken("if") || isNextToken("read") || isNextToken("return")
                        || isNextToken("while") || isNextToken("write")) {
                    statement();
                } else {
                    localVar();
                }
            }

            private boolean memberDecl() {
                if (isNextToken("attribute")) {
                    memberVarDecl();
                } else {
                    memberFuncDecl();
                }
            }

            private boolean memberVarDecl() {
                match("attribute");
                matchIdentifier();
                match(":");
                type();
                reptMemberVarDecl4();
                match(";");
            }

            private boolean reptMemberVarDecl4() {
                if (match("[")) {
                    arraySize();
                    reptMemberVarDecl4();
                } else {
                    // reptMemberVarDecl4 can be empty, so do nothing if no match
                }
            }

            private boolean memberFuncDecl() {
                match("function");
                matchIdentifier();
                match(":");
                match("(");
                fParams();
                match(")");
                arrow();
                returnType();
                match(";");
            }

            private boolean funcDef() {
                funcHead();
                funcBody();
            }

            private boolean funcHead() {
                match("function");
                matchIdentifier();
                funcHead1();
            }

            private boolean funcHead1() {
                if (match("(")) {
                    fParams();
                    match(")");
                    arrow();
                    returnType();
                } else {
                    funcHead2();
                }
            }

            private boolean funcHead2() {
                matchIdentifier();
                match("(");
                fParams();
                match(")");
                arrow();
                returnType();
            }

            private boolean funcBody() {
                match("{");
                reptFuncBody1();
                match("}");
            }

            private boolean reptFuncBody1() {
                if (isNextToken("localVar")) {
                    localVarDeclOrStmt();
                    reptFuncBody1();
                } else {
                    // reptFuncBody1 can be empty, so do nothing if no match
                }
            }

            private boolean statement() {
                if (match("if")) {
                    match("(");
                    relExpr();
                    match(")");
                    match("then");
                    statBlock();
                    match("else");
                    statBlock();
                    match(";");
                } else if (match("read")) {
                    match("(");
                    variable();
                    match(")");
                    match(";");
                } else if (match("return

                (expr)) {
                    match(";");
                } else if (match("while")) {
                    match("(");
                    relExpr();
                    match(")");
                    statBlock();
                    match(";");
                } else if (match("write")) {
                    match("(");
                    expr();
                    match(")");
                    match(";");
                } else if (isNextToken("id")) {
                    reptVariable0();
                    t1();
                    match(";");
                } else {
// no match found
                    throw new RuntimeException("Invalid statement: " + currentToken());
                }
            }

            private boolean t1() { matchIdentifier(); t2(); } private boolean t2() { if (match("(")) { aParams(); match(")"); } else { reptVariable2(); assignOp(); expr(); } } private boolean reptVariable0() { if (match("id")) { idnest(); reptVariable0(); } else { // reptVariable0 can be empty, so do nothing if no match } } private boolean reptVariable2() { if (match("[")) { arithExpr(); match("]"); reptVariable2(); } else { // reptVariable2 can be empty, so do nothing if no match } } private boolean variable() { reptVariable0(); idnest(); reptVariable2(); } private boolean idnest() { match("id"); idnest1(); } private boolean idnest1() { if (match("(")) { aParams(); match(")"); if (match(".")) { // recurse into idnest1 idnest1(); } } else if (match(".")) { // recurse into idnest1 idnest1(); } else { // idnest1 can be empty, so do nothing if no match } } private boolean reptFParams3() { if (match("[")) { arraySize(); reptFParams3(); } else { // reptFParams3 can be empty, so do nothing if no match } } private boolean reptFParams4() { if (match(",")) { matchIdentifier(); match(":"); type(); reptFParams3(); reptFParams4(); } else { // reptFParams4 can be empty, so do nothing if no match } } private boolean reptAParams1() { if (match(",")) { expr(); reptAParams1(); } else { // reptAParams1 can be empty, so do nothing if no match } } private boolean reptClassDecl4() { if (isNextToken("private") || isNextToken("public") || isNextToken("attribute") || isNextToken("function") || isNextToken("constructor") || isNextToken("id")) { visibility(); memberDecl(); reptClassDecl4(); } else { // reptClassDecl4 can be empty, so do nothing if no match } } private boolean reptFuncBody1() { if (isNextToken("if") || isNextToken("read") || isNextToken("return") || isNextToken("while") || isNextToken("write") || isNextToken("localVar") || isNextToken("id")) { localVarDeclOrStmt(); reptFuncBody1(); } else { // reptFuncBody1 can be empty, so do nothing if no match
            }

                private boolean reptLocalVarDecl4() {
                    if (match("[")) {
                        arraySize();
                        reptLocalVarDecl4();
                    } else {
                        // reptLocalVarDecl4 can be empty, so do nothing if no match
                    }
                }

                private boolean reptMemberVarDecl4() {
                    if (match("[")) {
                        arraySize();
                        reptMemberVarDecl4();
                    } else {
                        // reptMemberVarDecl4 can be empty, so do nothing if no match
                    }
                }

                private boolean reptOptClassDecl22() {
                    if (match(",")) {
                        matchIdentifier();
                        reptOptClassDecl22();
                    } else {
                        // reptOptClassDecl22 can be empty, so do nothing if no match
                    }
                }

                private boolean relOp() {
                    if (match("eq") || match("geq") || match("gt") || match("leq") || match("lt") || match("neq")) {
                        // do nothing
                    } else {
                        throw new RuntimeException("Expected relational operator, but found: " + currentToken());
                    }
                }

                private boolean rightrecArithExpr() {
                    if (match("+") || match("-") || match("or")) {
                        term();
                        rightrecArithExpr();
                    } else {
                        // rightrecArithExpr can be empty, so do nothing if no match
                    }
                }

                private boolean rightrecTerm() {
                    if (match("*") || match("/") || match("and")) {
                        factor();
                        rightrecTerm();
                    } else {
                        // rightrecTerm can be empty, so do nothing if no match
                    }
                }

                private boolean sign() {
                    if (match("+") || match("-")) {
                        // do nothing
                    } else {
                        throw new RuntimeException("Expected sign, but found: " + currentToken());
                    }
                }

                private boolean statBlock() {
                    if (match("{")) {
                        reptStatBlock1();
                        match("}");
                    } else if (isNextToken("if") || isNextToken("read") || isNextToken("return")
                            || isNextToken("while") || isNextToken("write")
                            || isNextToken("id")) {
                        statement();
                    } else {
                        // statBlock can be empty, so do nothing if no match
                    }
                }

                private boolean reptStatBlock1() {
                    if (isNextToken("if") || isNextToken("read") || isNextToken("return")
                            || isNextToken("while") || isNextToken("write")
                            || isNextToken("localVar") || isNextToken("id")) {
                        statement();
                        reptStatBlock1();
                    } else {
                        // reptStatBlock1 can be empty, so do nothing if no match
                    }
                }

                private boolean term() {
                    factor();
                    rightrecTerm();
                }

                private boolean type() {
                    if (match("float") || match("id") || match("integer")) {
                        // do nothing
                    } else {
                        throw new RuntimeException("Expected type, but found: " + currentToken());
                    }
                }

                private boolean visibility() {
                    if (match("private") || match("public")) {
                        // do nothing
                    } else {
                        // visibility can be empty, so do nothing if no match
                    }
                }}}}}