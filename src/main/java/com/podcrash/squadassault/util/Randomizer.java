package com.podcrash.squadassault.util;

import java.util.Random;

public final class Randomizer {

    private static final Random random = new Random();

    public static boolean randomBool() {
        return random.nextBoolean();
    }

    public static int randomInt(int bound) {
        return random.nextInt(bound);
    }

}
