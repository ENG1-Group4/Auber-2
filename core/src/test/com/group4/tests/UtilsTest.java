package com.group4.tests;

import com.threecubed.auber.Utils;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class UtilsTest {

    @Test
    void randomFloatInRange() {
        Random generator = new Random();
        float[][] test = {{10.1f,21.1f},{21,22},{21.1f,21.2f}};
        for (float[] values:test) {
            float random = Utils.randomFloatInRange(generator, values[0], values[1]);
            assertTrue(values[1] >= random, "Error, random is too high");
            assertTrue(values[0]  <= random, "Error, random is too low");
        }
    }
}