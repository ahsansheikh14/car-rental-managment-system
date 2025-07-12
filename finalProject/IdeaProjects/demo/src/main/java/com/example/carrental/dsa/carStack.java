package com.example.carrental.dsa;

import com.example.carrental.models.car;
import java.util.Stack;

public class carStack {
    private Stack<car> stack = new Stack<>();

    public void push(car c) {
        stack.push(c);
    }

    public car pop() {
        return stack.isEmpty() ? null : stack.pop();
    }

    public car peek() {
        return stack.isEmpty() ? null : stack.peek();
    }

    public boolean isEmpty() {
        return stack.isEmpty();
    }
}