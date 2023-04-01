package semanticAnalyzer;


import java.util.ArrayList;

public class SymbolTable {
    private int depth = 0;
    private String name;
    private ArrayList<SymbTabEntry> entries = new ArrayList<SymbTabEntry>();

    public SymbolTable(String name){
        this.name = name;
    }

    public String getName() {
        return name;
    }
    public void addEntry(SymbTabEntry symbTabEntry){
        entries.add(symbTabEntry);
    }

    public void printAllRecords(int depth){
        if(depth == -1){
            depth = this.depth;
        }
       if(entries.isEmpty()){
           System.out.println("There is no record.");
       } else {
           for (int i = 0; i < entries.size(); i++) {
               System.out.println(tabs(depth) +"Name : " + entries.get(i).getName() + ", Kind : "
                       + entries.get(i).getKind() + ", Type : " + entries.get(i).getType());
               entries.get(i).getSymbolTable().printAllRecords(++depth);
           }
       }
    }

    private String tabs(int depth){
        String tab = "";
        for (int i = 0; i < depth; i++) {
            tab = tab + "\t";
        }
        return tab;
    }

    public boolean search(String name, String kind, String type){
        //this is for checking, will do later
        return true;
    }
}