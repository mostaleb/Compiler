package AST;

import lexicalAnalyzer.Token;
import semanticAnalyzer.SymbTabCreationConcreteVisitor;
import semanticAnalyzer.SymbTabEntry;
import semanticAnalyzer.SymbolTable;

public class ASTNode {
    private String name;
    private int height = 0;
    private ASTNode father = null;
    private ASTNode rightSibling = null;
    private ASTNode leftmostSibling = null;
    private ASTNode leftmostChild = null;
    private Token token;

    private SymbolTable symbolTable = null;
    public ASTNode(String name, Token token) {
        this.name = name;
        this.token = token;
    }

    public void addChild(ASTNode child) {
        if (leftmostChild == null) {
            leftmostChild = child;
        } else {
            ASTNode sibling = leftmostChild;
            while (sibling.rightSibling != null) {
                sibling = sibling.rightSibling;
            }
            sibling.rightSibling = child;
            child.leftmostSibling = leftmostChild;
        }
        child.father = this;
        child.height = this.height + 1;
    }
    public void printTree(StringBuilder builder, String indent) {
        builder.append(indent);
        builder.append(name);
        if (token != null) {
            builder.append(" (").append(token.toString()).append(")");
        }
        builder.append("\n");
        SymbTabCreationConcreteVisitor creationConcreteVisitor = new SymbTabCreationConcreteVisitor();
        creationConcreteVisitor.visit(this);
        if (leftmostChild != null) {
            leftmostChild.printTree(builder, indent + "  ");
        }

        if (rightSibling != null) {
            rightSibling.printTree(builder, indent);
        }
    }

    public void createNewTable(SymbolTable symbolTable){
     if(this.symbolTable == null){
         this.symbolTable = symbolTable;
     } else {
         throw new RuntimeException("This AST Node already have a Symbol Table.");
     }
    }

    public void insert(SymbTabEntry symbTabEntry){
        if(this.symbolTable != null){
            this.symbolTable.addEntry(symbTabEntry);
        } else {
            throw new RuntimeException("This AST Node doesn't have a Symbol Table");
        }
    }

    public ASTNode getLeftmostChild(){
        return leftmostChild;
    }
    public ASTNode getRightSibling(){
        return rightSibling;
    }
    public String getName() {
        return name;
    }
}
