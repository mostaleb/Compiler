package lexicalAnalyzer;

public class Token {
    //location is a string with 2 values seperated by a " "
    private String location;

    public String getLexeme() {
        return lexeme;
    }

    private String lexeme;
    private TokenType type;

    public Token(String location, String lexeme, TokenType type) {
        this.location = location;
        this.lexeme = lexeme;
        this.type = type;
    }

    @Override
    public String toString() {
        return "[" +
                type.getValue() +
                ", " + lexeme +
                ", " + location.split(" ")[0] +
                "]";
    }

    public TokenType getType() {
        return type;
    }

    public enum TokenType {
        ID("id"),
        INTEGER("integer"),
        FLOAT("float"),
        OPERATOR_EQ("=="),
        OPERATOR_PLUS("+"),
        RESERVED_OR("or"),
        PUNCTUATION_LPAREN("("),
        PUNCTUATION_SEMICOLON(";"),
        RESERVED_INTEGER("integer"),
        RESERVED_WHILE("while"),
        RESERVED_LOCALVAR("localvar"),
        OPERATOR_NE("<>"),
        OPERATOR_MINUS("-"),
        RESERVED_AND("and"),
        PUNCTUATION_RPAREN(")"),
        PUNCTUATION_COMMA(","),
        RESERVED_FLOAT("float"),
        RESERVED_IF("if"),
        RESERVED_CONSTRUCTOR("constructor"),
        OPERATOR_LT("<"),
        OPERATOR_MULT("*"),
        RESERVED_NOT("not"),
        PUNCTUATION_LCURLY("{"),
        PUNCTUATION_DOT("."),
        RESERVED_VOID("void"),
        RESERVED_THEN("then"),
        RESERVED_ATTRIBUTE("attribute"),
        OPERATOR_GT(">"),
        OPERATOR_DIV("/"),
        PUNCTUATION_RCURLY("}"),
        PUNCTUATION_COLON(":"),
        RESERVED_CLASS("class"),
        RESERVED_ELSE("else"),
        RESERVED_FUNCTION("function"),
        OPERATOR_LE("<="),
        OPERATOR_ARROW("=>"),
        OPERATOR_ASSIGN("="),
        PUNCTUATION_LBRACKET("["),
        OPERATOR_GE(">="),
        PUNCTUATION_RBRACKET("]"),
        OPERATOR_SIGNATURE("::"),//This name might change as it has different names in different languages
        RESERVED_ISA("isa"),
        RESERVED_WRITE("write"),
        RESERVED_PUBLIC("public"),
        RESERVED_RETURN("return"),
        RESERVED_PRIVATE("private"),
        RESERVED_SELF("self"),
        RESERVED_READ("read"),
        COMMENT_BLOCK("/**/"),
        COMMENT_INLINE("//"),
        UNKNOWN("ukn"),
        EOF("eof");

        private final String value;

        TokenType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}

