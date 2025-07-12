package com.example.carrental.dsa;

import com.example.carrental.models.car;

public class carTree {
    private class Node {
        car data;
        Node left, right;

        Node(car data) {
            this.data = data;
        }
    }

    private Node root;

    public void insert(car c) {
        root = insertRec(root, c);
    }

    private Node insertRec(Node root, car c) {
        if (root == null) return new Node(c);
        if (c.getModel().compareTo(root.data.getModel()) < 0)
            root.left = insertRec(root.left, c);
        else
            root.right = insertRec(root.right, c);
        return root;
    }

    // Example: In-order traversal
    public void inOrder() {
        inOrderRec(root);
    }

    private void inOrderRec(Node root) {
        if (root != null) {
            inOrderRec(root.left);
            System.out.println(root.data.getModel());
            inOrderRec(root.right);
        }
    }
}