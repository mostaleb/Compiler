import lexicalAnalyzer.LexicalAnalyzer;
import lexicalAnalyzer.Token;

import java.io.*;

public class Driver {

    public static void main(String[] args) {
        File folder = new File("./test");
        File[] listOfFiles = folder.listFiles();
        LexicalAnalyzer l = new LexicalAnalyzer();
        for (File file : listOfFiles) {
            if (file.isFile()) {
                if(file.getName().endsWith(".src")){
                    File input = new File("./test/" + file.getName());
                    File output = new File("./test/"+ file.getName().substring(0, file.getName().length()-4) + ".lextokens");
                    File outputError = new File("./test/"+ file.getName().substring(0, file.getName().length()-4)+ ".lexerrors");
                    l.setLineCounter(1);
                    PrintWriter pw = null;
                    PrintWriter pwe = null;
                    PushbackReader reader = null;
                    try {
                        reader = new PushbackReader(new FileReader(file));
                        pw = new PrintWriter(output);
                        pwe = new PrintWriter((outputError));
                        Token token;
                        while ((token = l.nextToken(reader)) != null) {
                            if (token.getType() == Token.TokenType.EOF) {
                                break;
                            }
                            if(l.getTokenType() == Token.TokenType.UNKNOWN){
                                pwe.println(token.toString());
                                pw.println(token.toString());
                            } else {
                                pw.println(token.toString());
                            }

                            l.setLexeme("");
                            l.setTokenType(null);

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
}
