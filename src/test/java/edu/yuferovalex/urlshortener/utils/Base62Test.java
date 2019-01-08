package edu.yuferovalex.urlshortener.utils;

import org.junit.Test;

public class Base62Test {

    @Test(expected = Base62Exception.class)
    public void shouldThrowIfParameterNegative() {
        Base62.to(-1);
    }

    @Test(expected = Base62Exception.class)
    public void shouldThrowIfNotABase62String() {
        Base62.from("/?13");
    }

    @Test(expected = Base62Exception.class)
    public void shouldThrowIfIntegerOverflow() {
        Base62.from("2lkCB2"); // 2 ^ 31
    }

    @Test(expected = Base62Exception.class)
    public void shouldThrowIfInputStringTooLong() {
        Base62.from("2lkCB20"); // 2 ^ 31 * 62
    }

}