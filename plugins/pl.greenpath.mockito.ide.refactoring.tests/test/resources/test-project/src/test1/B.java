package test1;

public class B {
    public void a() {
        b(testMock);
    }

    public void b(final String a) {
        System.out.println(c());
    }

    public Object c() {
        return new Object() {
            @Override
            public String toString() {
                return test;
            }
        };
    }
}