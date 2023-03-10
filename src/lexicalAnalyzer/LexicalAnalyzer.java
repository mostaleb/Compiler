package lexicalAnalyzer;

import java.io.*;

public class LexicalAnalyzer {
    private int lineCounter = 1;//counts the lines the parser is on.
    private int positionCounter = 0;//counts the position of the line the parser is on.
    private String lexeme = "";
    private Token.TokenType tokenType = null;
    private File file;
    PushbackReader read;
    public LexicalAnalyzer(String fileName) throws FileNotFoundException {
        file = new File(fileName);
        read = new PushbackReader(new FileReader(file));
    }
    public void setLineCounter(int lineCounter) {
        this.lineCounter = lineCounter;
    }

    public  void setLexeme(String lexeme) {
        this.lexeme = lexeme;
    }

    public  void setTokenType(Token.TokenType tokenType) {
        this.tokenType = tokenType;
    }

    public Token.TokenType getTokenType() {
        return tokenType;
    }


    public Token nextToken() throws IOException {
        tokenType = null;
        lexeme = "";
        int intValue;
        while ((intValue = read.read()) != -1) {
            char c = (char) intValue;
            if(c == 'i'){
                System.out.println();
            }
            if (c != ' ' && c != '\n' && c != '\r' && c != '\t')
                lexeme += c;
            if (Character.isLetter(c)) {
                handleLetters();
                return new Token(lineCounter + " " + positionCounter, lexeme, tokenType);
            } else if (Character.isDigit(c)) {
                handleNumbers();
                return new Token(lineCounter + " " + positionCounter, lexeme, tokenType);

            } else {
                switch (c) {
                    case '+':
                        tokenType = Token.TokenType.OPERATOR_PLUS;
                        break;
                    case '-':
                        tokenType = Token.TokenType.OPERATOR_MINUS;
                        break;
                    case '*':
                        tokenType = Token.TokenType.OPERATOR_MULT;
                        break;
                    case '=':
                        handleEqual();
                        return new Token(lineCounter + " " + positionCounter, lexeme, tokenType);
                    case '<':
                        handleLessThan();
                        return new Token(lineCounter + " " + positionCounter, lexeme, tokenType);
                    case '>':
                        handleGreaterThan();
                        return new Token(lineCounter + " " + positionCounter, lexeme, tokenType);
                    case '(':
                        tokenType = Token.TokenType.PUNCTUATION_LPAREN;
                        break;
                    case ')':
                        tokenType = Token.TokenType.PUNCTUATION_RPAREN;
                        break;
                    case '{':
                        tokenType = Token.TokenType.PUNCTUATION_LCURLY;
                        break;
                    case '}':
                        tokenType = Token.TokenType.PUNCTUATION_RCURLY;
                        break;
                    case '[':
                        tokenType = Token.TokenType.PUNCTUATION_LBRACKET;
                        break;
                    case ']':
                        tokenType = Token.TokenType.PUNCTUATION_RBRACKET;
                        break;
                    case ';':
                        tokenType = Token.TokenType.PUNCTUATION_SEMICOLON;
                        break;
                    case ',':
                        tokenType = Token.TokenType.PUNCTUATION_COMMA;
                        break;
                    case '.':
                        tokenType = Token.TokenType.PUNCTUATION_DOT;
                        break;
                    case ':':
                        handleColon();
                        return new Token(lineCounter + " " + positionCounter, lexeme, tokenType);
                    case ' ':
                        positionCounter++;
                        continue;
                    case '\t':
                    case '\r':
                        continue;
                    case '\n':
                        lineCounter++;

                        positionCounter = 0;
                        continue;
                    case '/':
                        return handleSlash();
                    default:
                        handleError();
                        return new Token(lineCounter + " " + positionCounter, lexeme, tokenType);
                }
                positionCounter++;
                return new Token(lineCounter + " " + positionCounter, lexeme, tokenType);
            }
        }
        positionCounter++;
        return new Token(lineCounter + " " + positionCounter, "", Token.TokenType.EOF);
    }

    private void handleError() throws IOException {
        int intValue;
        while ((intValue = read.read()) != -1) {
            positionCounter++;
            char c = (char) intValue;
            if (c == ' ' || c == '\n' || c == '\r') {
                positionCounter--;
                read.unread(c);
                break;
            }
            lexeme += c;
        }
        tokenType = Token.TokenType.UNKNOWN;
    }

    private void handleLetters() throws IOException {
        int intValue;
        while ((intValue = read.read()) != -1) {
            positionCounter++;
            char c = (char) intValue;
            if (Character.isLetter(c) || Character.isDigit(c) || c == '_') {
                lexeme += c;
            } else {
                positionCounter--;
                read.unread(intValue);
                break;
            }
        }
        if (lexeme.equals("if")) {
            tokenType = Token.TokenType.RESERVED_IF;
        } else if (lexeme.equals("else")) {
            tokenType = Token.TokenType.RESERVED_ELSE;
        } else if (lexeme.equals("while")) {
            tokenType = Token.TokenType.RESERVED_WHILE;
        } else if (lexeme.equals("integer")) {
            tokenType = Token.TokenType.RESERVED_INTEGER;
        } else if (lexeme.equals("float")) {
            tokenType = Token.TokenType.RESERVED_FLOAT;
        } else if (lexeme.equals("return")) {
            tokenType = Token.TokenType.RESERVED_RETURN;
        } else if (lexeme.equals("or")) {
            tokenType = Token.TokenType.RESERVED_OR;
        } else if (lexeme.equals("and")) {
            tokenType = Token.TokenType.RESERVED_AND;
        } else if (lexeme.equals("not")) {
            tokenType = Token.TokenType.RESERVED_NOT;
        } else if (lexeme.equals("constructor")) {
            tokenType = Token.TokenType.RESERVED_CONSTRUCTOR;
        } else if (lexeme.equals("localvar")) {
            tokenType = Token.TokenType.RESERVED_LOCALVAR;
        } else if (lexeme.equals("attribute")) {
            tokenType = Token.TokenType.RESERVED_ATTRIBUTE;
        } else if (lexeme.equals("void")) {
            tokenType = Token.TokenType.RESERVED_VOID;
        } else if (lexeme.equals("then")) {
            tokenType = Token.TokenType.RESERVED_THEN;
        } else if (lexeme.equals("class")) {
            tokenType = Token.TokenType.RESERVED_CLASS;
        } else if (lexeme.equals("function")) {
            tokenType = Token.TokenType.RESERVED_FUNCTION;
        } else if (lexeme.equals("isa")) {
            tokenType = Token.TokenType.RESERVED_ISA;
        } else if (lexeme.equals("write")) {
            tokenType = Token.TokenType.RESERVED_WRITE;
        } else if (lexeme.equals("public")) {
            tokenType = Token.TokenType.RESERVED_PUBLIC;
        } else if (lexeme.equals("private")) {
            tokenType = Token.TokenType.RESERVED_PRIVATE;
        } else if (lexeme.equals("self")) {
            tokenType = Token.TokenType.RESERVED_SELF;
        } else if (lexeme.equals("read")) {
            tokenType = Token.TokenType.RESERVED_READ;
        } else {
            tokenType = Token.TokenType.ID;
        }

    }

    private void handleNumbers() throws IOException {
        int intValue;
        boolean ignore = false;
        while ((intValue = read.read()) != -1) {
            positionCounter++;
            char c = (char) intValue;
            if(Character.isDigit(c)){
                lexeme += c;
            }else if (c == '.') {
                lexeme += c;
                handleDecimal();
                zeroVerification();
                if(tokenType == null)
                    tokenType = Token.TokenType.FLOAT;
                return;
            } else if(Character.isLetter(c)){
              lexeme += c;
              tokenType = Token.TokenType.UNKNOWN;
            } else {
                positionCounter--;
                read.unread(c);
                break;
            }

        }
        if(tokenType == null){
            if(lexeme.contains(".")){
                if(lexeme.split(".")[0].charAt(0) == '0' && lexeme.length() > 1){
                    tokenType = Token.TokenType.UNKNOWN;
                } else {
                    tokenType = Token.TokenType.INTEGER;
                }
            } else if (lexeme.contains("e")){
                if(lexeme.split("e")[0].charAt(0) == '0' && lexeme.length() > 1){
                    tokenType = Token.TokenType.UNKNOWN;
                } else {
                    tokenType = Token.TokenType.INTEGER;
                }
            } else {
                if(lexeme.charAt(0) == '0' && lexeme.length() > 1){
                    tokenType = Token.TokenType.UNKNOWN;
                } else {
                    tokenType = Token.TokenType.INTEGER;
                }
            }

        }

    }
    private void zeroVerification(){
        if(tokenType == Token.TokenType.FLOAT || tokenType == null && !lexeme.isEmpty()){
            if(lexeme.contains(".")){
                if(lexeme.split("\\.")[0].charAt(0) == '0' && lexeme.split("\\.")[0].length() > 1){
                    tokenType = Token.TokenType.UNKNOWN;
                } else {
                    tokenType = Token.TokenType.FLOAT;
                }
            } else if (lexeme.contains("e")){
                if(lexeme.split("e")[0].charAt(0) == '0' && lexeme.split("e")[0].length() > 1){
                    tokenType = Token.TokenType.UNKNOWN;
                } else {
                    tokenType = Token.TokenType.FLOAT;
                }
            } else {
                if(lexeme.charAt(0) == '0' && lexeme.length() > 1){
                    tokenType = Token.TokenType.UNKNOWN;
                } else {
                    tokenType = Token.TokenType.FLOAT;
                }
            }
        }
    }
    private void handleDecimal() throws IOException {
        int intValue;
        String tempLexeme = "";
        while ((intValue = read.read()) != -1) {
            positionCounter++;
            char c = (char) intValue;
            if (Character.isDigit(c)) {
                lexeme += c;
                tempLexeme += c;
            } else if (c == 'e') {
                lexeme += c;
                handleExponant();
                if (tokenType == null)
                    tokenType = Token.TokenType.FLOAT;
                break;
            } else {
                positionCounter--;
                read.unread(intValue);
                break;
            }

        }
        if(tempLexeme.endsWith("0") && tempLexeme.length() > 1){
            tokenType = Token.TokenType.UNKNOWN;
        }



    }

    private void handleExponant() throws IOException {
        int intValue;
        while ((intValue = read.read()) != -1) {
            positionCounter++;
            char c = (char) intValue;
            if (c == '-' || c == '+') {
                lexeme += c;
            } else if (Character.isDigit(c)) {
                lexeme += c;
            } else if(c == '\t' || c == '\n' || c == '\r' || c == ' '){
                read.unread(c);
                break;
            } else {
                tokenType = Token.TokenType.UNKNOWN;
                return;
            }
        }

        if(lexeme.split("e")[1].charAt(0) == '-' || lexeme.split("e")[1].charAt(0) == '+'){
            if(lexeme.split("e")[1].substring(1).charAt(0) == '0' && lexeme.split("e")[1].substring(1).length() > 1){
                tokenType = Token.TokenType.UNKNOWN;
            }
        } else {
            if(lexeme.split("e")[1].charAt(0) == '0' && lexeme.split("e")[1].length() > 1){
                tokenType = Token.TokenType.UNKNOWN;
            }
        }
    }

    private void handleEqual() throws IOException {
        int intValue;
        if ((intValue = read.read()) != -1) {
            positionCounter++;
            char c = (char) intValue;
            if (c == '=') {
                lexeme += c;
                tokenType = Token.TokenType.OPERATOR_EQ;
            } else if(c == '>'){
                lexeme += c;
                tokenType = Token.TokenType.OPERATOR_ARROW;
            } else {
                positionCounter--;
                read.unread(intValue);
                tokenType = Token.TokenType.OPERATOR_ASSIGN;
            }
        }
    }

    private void handleLessThan() throws IOException {
        int intValue = read.read();
        if (intValue == -1) {
            tokenType = Token.TokenType.OPERATOR_LT;
            return;
        }
        char c = (char) intValue;
        if (c == '=') {
            lexeme += c;
            tokenType = Token.TokenType.OPERATOR_LE;
        } else if (c == '>') {
            lexeme += c;
            tokenType = Token.TokenType.OPERATOR_NE;
        } else {
            positionCounter--;
            read.unread(c);
            tokenType = Token.TokenType.OPERATOR_LT;
        }
    }

    private Token handleSlash() throws IOException {
        int intValue = read.read();

        char c = (char) intValue;
        if (c == '*') {
            int tempLine = lineCounter;
            lexeme += c;
            handleBlockComment();
            return new Token(tempLine + " " + positionCounter, lexeme, tokenType);
        } else if (c == '/') {
            lexeme += c;
            handleLineComment();
            return new Token(lineCounter + " " + positionCounter, lexeme, tokenType);
        } else {
            positionCounter--;
            read.unread(c);
            tokenType = Token.TokenType.OPERATOR_DIV;
            return new Token(lineCounter + " " + positionCounter, lexeme, tokenType);
        }
    }

    private void handleBlockComment() throws IOException {
        tokenType = Token.TokenType.COMMENT_BLOCK;
        int intValue;
        while ((intValue = read.read()) != -1) {
            char c = (char) intValue;
            if (c == '\n') {
                lineCounter++;
                lexeme += "\\" + "n";
            } else if(c == '\r'){

            } else {
                lexeme += c;
            }
            positionCounter++;
            if (c == '*') {
                intValue = read.read();
                c = (char) intValue;
                if (c == '/') {
                    lexeme += c;
                    positionCounter++;
                    return;
                }
            } else if(c == '/'){
                handleSlash();
            }
        }
        tokenType = Token.TokenType.UNKNOWN;
    }

    private void handleLineComment() throws IOException {
        tokenType = Token.TokenType.COMMENT_INLINE;
        int intValue;
        while ((intValue = read.read()) != -1) {
            char c = (char) intValue;
            positionCounter++;
            if (c == '\n' || c == '\t' || c == '\r') {
                return;
            } else {
                lexeme += c;
            }
        }
    }


    private void handleGreaterThan() throws IOException {
        int intValue = read.read();
        if (intValue == -1) {
            tokenType = Token.TokenType.OPERATOR_GT;
            return;
        }
        char c = (char) intValue;

        if (c == '=') {
            lexeme += c;
            tokenType = Token.TokenType.OPERATOR_GE;
        } else {
            positionCounter--;
            read.unread(c);
            tokenType = Token.TokenType.OPERATOR_GT;
        }
    }

    private void handleColon() throws IOException {
        int intValue = read.read();
        if (intValue == -1) {
            tokenType = Token.TokenType.PUNCTUATION_COLON;
            return;
        }
        char c = (char) intValue;

        if (c == ':') {
            lexeme += c;
            tokenType = Token.TokenType.OPERATOR_SIGNATURE;
        } else {
            positionCounter--;
            read.unread(c);
            tokenType = Token.TokenType.PUNCTUATION_COLON;
        }
    }

}




