package com.example.demo.java;

public class ObjectBook {
    public static void main(String[] args) {
        new Sub().print();
    }
}

class Super {
    public void print() {
        hello();
        StringBuffer buffer = new StringBuffer();
    }

    public void hello() {
        System.out.println("super hello");
    }
}

class Sub extends Super {
    public void hello() {
        System.out.println("sub hello");
    }
}
