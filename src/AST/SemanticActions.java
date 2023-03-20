package AST;

import lexicalAnalyzer.Token;

import java.util.Stack;

public class SemanticActions {
    private Stack<ASTNode> astStack;

    public SemanticActions(Stack<ASTNode> astStack) {
        this.astStack = astStack;
    }

    // CreateLeaf method
    private ASTNode createLeaf(String name, Token token) {
        return new ASTNode(name, token);

    }

    public void createVisibility(Token token) {
        astStack.push(createLeaf("visibility", token));
    }

    public void createDimEmpty(Token token) {
        astStack.push(createLeaf("dimEmpty", token));
    }

    public void createDim(Token token) {
        astStack.push(createLeaf("dim", token));
    }

    public void createNumber(Token token) {
        astStack.push(createLeaf("number", token));
    }

    public void createEpsilon(Token token) {
        astStack.push(createLeaf("epsilon", token));
    }

    public void createId(Token token) {
        astStack.push(createLeaf("id", token));
    }

    public void createScope(Token token) {
        astStack.push(createLeaf("scope", token));
    }

    public void createReturnType(Token token) {
        astStack.push(createLeaf("returnType", token));
    }

    public void createType(Token token) {
        astStack.push(createLeaf("type", token));
    }

    public void createMult(Token token) {
        astStack.push(createLeaf("mult", token));
    }

    public void createAdd(Token token) {
        astStack.push(createLeaf("add", token));
    }

    public void createRel(Token token) {
        astStack.push(createLeaf("rel", token));
    }

    public void createSignVal(Token token) {
        astStack.push(createLeaf("signVal", token));
    }

    // ... Add other create* methods for the remaining semantic actions

    private ASTNode createSubtree(String name, Token token, ASTNode... children) {
        ASTNode rootNode = new ASTNode(name, token);

        for (ASTNode child : children) {
            rootNode.addChild(child);
        }

        return rootNode;
    }

    private ASTNode pop() {
        if (!astStack.isEmpty()) {
            return astStack.pop();
        } else {
            throw new RuntimeException("SemanticActions: Attempted to pop from an empty stack.");
        }
    }


    private ASTNode[] popUntilEpsilon() {
        // Assuming there is always an "epsilon" node on the stack
        Stack<ASTNode> tempStack = new Stack<>();
        ASTNode node;
        do {
            node = astStack.pop();
            if (!node.getName().equals("epsilon")) {
                tempStack.push(node);
            }
        } while (!node.getName().equals("epsilon"));

        return tempStack.toArray(new ASTNode[0]);
    }

    public void createMultOp(Token token) {
        astStack.push(createSubtree("multOp", token, pop(), pop(), pop(), createLeaf("multOp", token)));
    }

    public void createAddOp(Token token) {
        astStack.push(createSubtree("addOp", token, pop(), pop(), pop(), createLeaf("addOp", token)));
    }

    public void createRelOp(Token token) {
        astStack.push(createSubtree("relOp", token, pop(), pop(), pop(), createLeaf("relOp", token)));
    }

    public void createSign(Token token) {
        astStack.push(createSubtree("sign", token, pop(), pop(), createLeaf("sign", token)));
    }

    public void createRoot() {
        astStack.push(createSubtree("root", null, popUntilEpsilon()));
    }

    public void createInheritList() {
        astStack.push(createSubtree("inheritList", null, popUntilEpsilon()));
    }

    // ... continue with the remaining create* methods for the semantic actions

    public void createMemberDeclList() {
        astStack.push(createSubtree("declList", null, popUntilEpsilon()));
    }

    public void createClassDecl() {
        astStack.push(createSubtree("classDecl", null, pop(), pop(), pop()));
    }

    public void createMembConstrDecl() {
        astStack.push(createSubtree("constrDecl", null, pop(), pop()));
    }

    public void createFParamsList() {
        astStack.push(createSubtree("fParamList", null, popUntilEpsilon()));
    }

    public void createMembFuncDecl() {
        astStack.push(createSubtree("funcDecl", null, pop(), pop(), pop(), pop()));
    }

    public void createDimList() {
        astStack.push(createSubtree("dimList", null, popUntilEpsilon()));
    }

    public void createMembVarDecl() {
        astStack.push(createSubtree("mVarDecl", null, pop(), pop(), pop(), pop()));
    }

    public void createFParam() {
        astStack.push(createSubtree("fParam", null, pop(), pop(), pop()));
    }

    public void createAParams() {
        astStack.push(createSubtree("aParam", null, popUntilEpsilon()));
    }

    public void createVarDecl() {
        astStack.push(createSubtree("lVarDecl", null, pop(), pop(), pop()));
    }

    public void createIndexList() {
        astStack.push(createSubtree("indexList", null, popUntilEpsilon()));
    }

    public void createStatBlock() {
        astStack.push(createSubtree("statBlock", null, popUntilEpsilon()));
    }

    public void createReadStmt() {
        astStack.push(createSubtree("readStmt", null, pop()));
    }

    public void createWriteStmt() {
        astStack.push(createSubtree("writeStmt", null, pop()));
    }

    public void createReturnStmt() {
        astStack.push(createSubtree("returnStmt", null, pop()));
    }

    public void createAssignStmt() {
        astStack.push(createSubtree("assignStmt", null, pop(), pop()));
    }

    public void createWhileStmt() {
        astStack.push(createSubtree("whileStmt", null, pop(), pop()));
    }

    public void createIfStmt() {
        astStack.push(createSubtree("ifStmt", null, pop(), pop(), pop()));
    }

    public void createNot() {
        astStack.push(createSubtree("not", null, pop()));
    }

    public void createDot() {
        astStack.push(createSubtree("dot", null, pop(), pop()));
    }

    public void createDataMember() {
        astStack.push(createSubtree("dataMember", null, pop(), pop()));
    }

    public void createFuncCall() {
        astStack.push(createSubtree("funcCall", null, pop(), pop()));
    }

    public void createFuncBodyList() {
        astStack.push(createSubtree("funcBody", null, popUntilEpsilon()));
    }

    public void createFuncHead() {
        astStack.push(createSubtree("funcHead", null, popUntilEpsilon()));
    }
}


