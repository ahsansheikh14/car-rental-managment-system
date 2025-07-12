package com.example.carrental.dsa;

import com.example.carrental.models.booking;
import java.util.LinkedList;
import java.util.Queue;

public class bookingQueue {
    private Queue<booking> queue = new LinkedList<>();

    public void enqueue(booking b) {
        queue.add(b);
    }

    public booking dequeue() {
        return queue.poll();
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public int size() {
        return queue.size();
    }
}