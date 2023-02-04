import java.io.*;

public class Main {
    private static int lineCounter = 1;//counts the lines the parser is on.
    private static int positionCounter = 0;//counts the position of the line the parser is on.
    private static String lexeme = "";
    private static Token.TokenType tokenType = null;

    public static void main(String[] args) {
        File folder = new File("./test");
        File[] listOfFiles = folder.listFiles();

        for (File file : listOfFiles) {
            if (file.isFile()) {
                if(file.getName().endsWith(".src")){
                    File input = new File("./test/" + file.getName());
                    File output = new File("./test/"+ file.getName().substring(0, file.getName().length()-4) + ".lextokens");
                    File outputError = new File("./test/"+ file.getName().substring(0, file.getName().length()-4)+ ".lexerrors");
                    lineCounter = 1;
                    PrintWriter pw = null;
                    PrintWriter pwe = null;
                    PushbackReader reader = null;
                    try {
                        reader = new PushbackReader(new FileReader(file));
                        pw = new PrintWriter(output);
                        pwe = new PrintWriter((outputError));
                        Token token;
                        while ((token = nextToken(reader)) != null) {
                            if (token.getType() == Token.TokenType.EOF) {
                                break;
                            }
                            if(tokenType == Token.TokenType.UNKNOWN){
                                pwe.println(token.toString());
                            } else {
                                pw.println(token.toString());
                            }

                            lexeme = "";
                            tokenType = null;

                        }
                    } catch (FileNotFoundException e) {
                        System.out.println("File not found: " + e.getMessage());
                    } catch (IOException e) {
                        System.out.println("Error reading file: " + e.getMessage());
                    } catch (NullPointerException e) {
                        System.out.println("NullPointerException: " + e.getMessage());
                        e.printStackTrace();
                    } finally {
                        try {
                            if (reader != null) {
                                reader.close();
                            }
                            pw.close();
                            pwe.close();
                        } catch (IOException e) {
                            System.out.println("Error closing file: " + e.getMessage());
                        } catch (NullPointerException e){
                            System.out.println("NullPointerException: " + e.getMessage());
                        }
                    }
                }
            }
        }

    }


    private static Token nextToken(PushbackReader read) throws IOException {
        int intValue;
        while ((intValue = read.read()) != -1) {

            char c = (char) intValue;
            if (c != ' ' && c != '\n' && c != '\r' && c != '\t')
                lexeme += c;
            if (Character.isLetter(c)) {
                handleLetters(read);
                return new Token(lineCounter + " " + positionCounter, lexeme, tokenType);
            } else if (Character.isDigit(c)) {
                handleNumbers(read);
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
                        handleEqual(read);
                        return new Token(lineCounter + " " + positionCounter, lexeme, tokenType);
                    case '<':
                        handleLessThan(read);
                        return new Token(lineCounter + " " + positionCounter, lexeme, tokenType);
                    case '>':
                        handleGreaterThan(read);
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
                        handleColon(read);
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
                        return handleSlash(read);
                    default:
                        handleError(read);
                        return new Token(lineCounter + " " + positionCounter, lexeme, tokenType);
                }
                positionCounter++;
                return new Token(lineCounter + " " + positionCounter, lexeme, tokenType);
            }
        }
        positionCounter++;
        return new Token(lineCounter + " " + positionCounter, "", Token.TokenType.EOF);
    }

    private static void handleError(PushbackReader read) throws IOException {
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

    private static void handleLetters(PushbackReader read) throws IOException {
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

    private static void handleNumbers(PushbackReader read) throws IOException {
        int intValue;
        if(lexeme.equals("0")){
            read.unread('0');
            lexeme = "";
        }
        while ((intValue = read.read()) != -1) {
            positionCounter++;
            char c = (char) intValue;
            if(Character.isDigit(c)){
                lexeme += c;
                if(lexeme.equals("0")){
                    tokenType = Token.TokenType.INTEGER;
                    return;
                }
            }else if (c == '.') {
                lexeme += c;
                handleDecimal(read);
                if(tokenType == null)
                    tokenType = Token.TokenType.FLOAT;
                return;
            } else {
                positionCounter--;
                read.unread(intValue);
                break;
            }

        }
        tokenType = Token.TokenType.INTEGER;
    }

    private static void handleDecimal(PushbackReader read) throws IOException {
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
                handleExponant(read);
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

    private static void handleExponant(PushbackReader read) throws IOException {
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

    private static void handleEqual(PushbackReader read) throws IOException {
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

    private static void handleLessThan(PushbackReader read) throws IOException {
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

    private static Token handleSlash(PushbackReader read) throws IOException {
        int intValue = read.read();

        char c = (char) intValue;
        if (c == '*') {
            int tempLine = lineCounter;
            lexeme += c;
            handleBlockComment(read);
            return new Token(tempLine + " " + positionCounter, lexeme, tokenType);
        } else if (c == '/') {
            lexeme += c;
            handleLineComment(read);
            return new Token(lineCounter + " " + positionCounter, lexeme, tokenType);
        } else {
            positionCounter--;
            read.unread(c);
            tokenType = Token.TokenType.OPERATOR_DIV;
            return new Token(lineCounter + " " + positionCounter, lexeme, tokenType);
        }
    }

    private static void handleBlockComment(PushbackReader read) throws IOException {
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
                handleSlash(read);
            }
        }
        tokenType = Token.TokenType.UNKNOWN;
    }

    private static void handleLineComment(PushbackReader read) throws IOException {
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


    private static void handleGreaterThan(PushbackReader read) throws IOException {
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

    private static void handleColon(PushbackReader read) throws IOException {
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




