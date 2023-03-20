package AST;

import lexicalAnalyzer.Token;

public class ASTNode {
    private String name;
    private int height = 0;
    private ASTNode father;
    private ASTNode rightSibling;
    private ASTNode leftmostSibling;
    private ASTNode leftmostChild;
    private Token token;
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

    public void addSubtree(ASTNode subtreeRoot) {
        addChild(subtreeRoot);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public ASTNode getFather() {
        return father;
    }

    public void setFather(ASTNode father) {
        this.father = father;
    }

    public ASTNode getRightSibling() {
        return rightSibling;
    }

    public void setRightSibling(ASTNode rightSibling) {
        this.rightSibling = rightSibling;
    }

    public ASTNode getLeftmostSibling() {
        return leftmostSibling;
    }

    public void setLeftmostSibling(ASTNode leftmostSibling) {
        this.leftmostSibling = leftmostSibling;
    }

    public ASTNode getLeftmostChild() {
        return leftmostChild;
    }

    public void setLeftmostChild(ASTNode leftmostChild) {
        this.leftmostChild = leftmostChild;
    }
}
