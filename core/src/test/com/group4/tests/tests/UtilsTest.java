package com.group4.tests.tests;

import com.threecubed.auber.Utils;
import org.junit.Test;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test the utility functions
 *
 * @author Robert Watts
 * @version 1.0
 * @since 2.0
 * */
public class UtilsTest {

    @Test
    public void randomFloatInRange() {
        Random generator = new Random();
        float[][] test = {{10.1f,21.1f},{21,22},{21.1f,21.2f}};
        for (float[] values:test) {
            float random = Utils.randomFloatInRange(generator, values[0], values[1]);
            assertTrue(values[1] >= random, "Error, random is too high");
            assertTrue(values[0]  <= random, "Error, random is too low");
        }
    }
}