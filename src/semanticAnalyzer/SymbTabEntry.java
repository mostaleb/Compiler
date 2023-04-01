package semanticAnalyzer;

public class SymbTabEntry {
    //Name of the entry (function name, class name, variable name)
    private String name;
    //Kind of entry (function, class, parameter, variable)
    private String kind;
    //Type of the entry (integer, float, int[]][])
    private String type;
    private SymbolTable symbolTable;

    public SymbTabEntry(String name, String kind, String type, SymbolTable symbolTable){
        this.name = name;
        this.kind = kind;
        this.type = type;
        this.symbolTable = symbolTable;
    }

    public String getName() {
        return name;
    }

    public String getKind() {
        return kind;
    }

    public String getType() {
        return type;
    }

    public SymbolTable getSymbolTable() {
        return symbolTable;
    }
}
