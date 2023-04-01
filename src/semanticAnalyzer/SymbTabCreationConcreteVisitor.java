package semanticAnalyzer;

import AST.ASTNode;

public class SymbTabCreationConcreteVisitor implements Visitor{
    private ASTNode globalScope  = null;
    @Override
    public void visit(ASTNode node) {
        switch (node.getName()){
            case "multOp":

                break;
            case "addOp":

                break;
            case "relOp":

                break;
            case "sign":

                break;
            case "root":
                SymbolTable global = new SymbolTable("Global");
                printChild(node);
                break;
            case "inheritList":

                break;
            case "declList":

                break;
            case "classDecl":

                break;
            case "constrDecl":

                break;
            case "fParamList":

                break;
            case "funcDecl":

                break;
            case "dimList":

                break;
            case "mVarDecl":

                break;
            case "fParam":

                break;
            case "aParam":

                break;
            case "lVarDecl":

                break;
            case "indexList":

                break;
            case "statBlock":

                break;
            case "readStmt":

                break;
            case "writeStmt":

                break;
            case "returnStmt":

                break;
            case "assignStmt":

                break;
            case "whileStmt":

                break;
            case "ifStmt":

                break;
            case "not":

                break;
            case "dot":

                break;
            case "dataMember":

                break;
            case "funcCall":

                break;
            case "funcBody":

                break;
            case "funcHead":

                break;
            default:
                //Does Nothing
                break;
        }
    }

    private void printChild(ASTNode node){
        if(node.getLeftmostChild() != null){
            node = node.getLeftmostChild();
            while (node != null){
                System.out.println(node.getName());
                node = node.getRightSibling();
            }
        }
    }
}
