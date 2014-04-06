package test1;

import java.util.ArrayList;

public class D {
    public void a() {
        b(testMock);
    }

    public void b(final String a) {
    }

    public void c() {
        final Double[] d = new Double[] { test2Mock };
    }

    public void d() {
        final ArrayList<String> s = new ArrayList<String>(test2Mock);
    }

    public void e() {
        final int[] arr = new int[2];
        arr[0] = test3Mock;
    }

    public String f() {
        return test4Mock;
    }

    public void g() {
        final long t = test4Mock;
    }
}
