import lexicalAnalyzer.LexicalAnalyzer;
import lexicalAnalyzer.Token;
import synthacticalAnalyzer.SynthacticalAnalyzer;

import java.io.*;

public class Driver {

    public static void main(String[] args) throws IOException {
        LexicalAnalyzer lexer = new LexicalAnalyzer("parser/example-bubblesort.src");
        SynthacticalAnalyzer s = new SynthacticalAnalyzer(lexer);
        s.parse();
    }
}
