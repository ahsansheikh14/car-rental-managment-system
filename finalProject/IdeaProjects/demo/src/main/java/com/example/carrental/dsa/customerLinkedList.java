package com.example.carrental.dsa;

import com.example.carrental.models.customer;

public class customerLinkedList {
    private Node head;

    private static class Node {
        customer data;
        Node next;

        Node(customer data) {
            this.data = data;
        }
    }

    public void add(customer c) {
        Node newNode = new Node(c);
        if (head == null) head = newNode;
        else {
            Node temp = head;
            while (temp.next != null) temp = temp.next;
            temp.next = newNode;
        }
    }

    // Example: Print all customers
    public void printAll() {
        Node temp = head;
        while (temp != null) {
            System.out.println(temp.data.getName());
            temp = temp.next;
        }
    }
}