package edu.illinois.wala;

public class Foo {
    int x = 0;

    static void main(String[] args) {
        Foo foo = new Foo();
        foo.bar();
    }

    void bar() {
        int y = seven();
        int z = seven();
        x = y;
        Foo anotherFoo = new Foo();
        anotherFoo.x = z;
    }

    int seven() {
        return 7;
    }
}
