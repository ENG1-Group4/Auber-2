package com.threecubed.auber;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import java.util.List;
import java.util.Random;


/**
 * A collection of utility functions that don't belong within a specific class.
 *
 * @author Daniel O'Brien
 * @version 1.0
 * @since 1.0
 * */
public final class Utils {
  /**
   * Get the mouse coordinates within the game world.
   *
   * @param camera The gamera of the game world
   * @return The X and Y position of the mouse in the game world in the form of a Vector2
   * */
  public static Vector2 getMouseCoordinates(Camera camera) {
    Vector3 mousePosition = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
    camera.unproject(mousePosition);
    return new Vector2(mousePosition.x, mousePosition.y);
  }

  /**
   * Generate a random float, inclusive of both bounds.
   *
   * @param randomNumberGenerator The random number generator to use
   * @param lowerBound The lower bound to generate a random int from (inclusive)
   * @param upperBound The upper bound to generate a random int from (inclusive)
   *
   * @return A randomly generated integer between the 2 bounds
   * */
  public static float randomFloatInRange(Random randomNumberGenerator, float lowerBound,
      float upperBound) {
    return (randomNumberGenerator.nextFloat() * (upperBound - lowerBound)) + lowerBound;
  }

  /**
   * Generate a random integer, inclusive of both bounds.
   *
   * @param randomNumberGenerator The random number generator to use
   * @param lowerBound The lower bound to generate a random int from (inclusive)
   * @param upperBound The upper bound to generate a random int from (inclusive)
   *
   * @return A randomly generated integer between the 2 bounds
   * */
  public static int randomIntInRange(Random randomNumberGenerator, int lowerBound, int upperBound) {
    return randomNumberGenerator.nextInt(upperBound - lowerBound + 1) + lowerBound;
  }

  public static <T> T randomListItem(Random randomNumberGenerator, List<T> list) {
    return list.get(randomIntInRange(randomNumberGenerator, 0, list.size()));
  }
}
