package semanticAnalyzer;

import AST.ASTNode;

public interface Visitor {
    public void visit(ASTNode node);
}
