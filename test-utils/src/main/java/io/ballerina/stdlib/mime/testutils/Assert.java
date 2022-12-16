package io.ballerina.stdlib.mime.testutils;

/**
 * Contains assert functions used by mime test cases.
 */
public class Assert {

    public static void assertEquals(Object actual, Object expected) {
        if (!areEqual(actual, expected)) {
            fail();
        }
    }

    public static void assertNotNull(Object object) {
        if (object == null) {
            fail();
        }
    }

    private static boolean areEqual(Object actual, Object expected) {
        if ((expected == null) && (actual == null)) {
            return true;
        }
        if (expected == null ^ actual == null) {
            return false;
        }
        if (expected.equals(actual) && actual.equals(expected)) {
            return true;
        }
        return false;
    }

    public static void fail() {
        throw new AssertionError();
    }
}
