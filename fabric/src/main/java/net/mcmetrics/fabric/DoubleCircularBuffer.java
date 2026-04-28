package net.mcmetrics.fabric;

import lombok.Getter;

import java.util.Arrays;

@Getter
public class DoubleCircularBuffer {

    private final int capacity;
    private final double[] buffer;

    private int index = 0;
    private boolean full = false;

    public DoubleCircularBuffer(int capacity) {
        this.capacity = capacity;
        this.buffer = new double[capacity];
    }

    public void push(double obj) {
        index++;
        if (index == capacity) {
            index = 0;
            full = true;
        }
        buffer[index] = obj;
    }

    public double average() {
        double sum = Arrays.stream(buffer).sum();
        double n = full ? capacity : index + 1;
        return sum / n;
    }
}
