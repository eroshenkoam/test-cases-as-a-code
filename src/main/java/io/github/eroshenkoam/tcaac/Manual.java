package io.github.eroshenkoam.tcaac;

public final class Manual {

    private Manual() {
    }

    public static String resource(final String name) {
        return ClassLoader.getSystemResourceAsStream(name).toString();
    }

}
