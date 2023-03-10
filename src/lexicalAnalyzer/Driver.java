package lexicalAnalyzer;

import java.io.FileNotFoundException;
import java.io.IOException;

public class Driver {
    public static void main(String[] args) throws FileNotFoundException {
        LexicalAnalyzer lexicalAnalyzer = new LexicalAnalyzer("parser/example-bubblesort.src");
        while(true){
            try {
                Token token = lexicalAnalyzer.nextToken();
                System.out.println(token.toString());
                if(token.getType().getValue().equals("eof")){
                    break;
                }
            } catch (IOException ioe){
                System.out.println("FINI");
                break;
            }
        }
    }
}
