package com.threecubed.auber.pathfinding;

import java.util.Arrays;


/**
 * A class that represents a node in a given pathfinding attempt.
 *
 * @author Daniel O'Brien
 * @version 1.0
 * @since 1.0
 * */
public class PathNode {
  public int[] position;
  public float heuristic;
  public PathNode parent;
  public int pathCost = 0;

  /**
   * Initialise the PathNode with a given position, parent and destination.
   * parent is used when traversing back a valid path, position is used to
   * identify a PathNode and calculate its heuristic, destination is also used
   * to calculate the heuristic
   *
   * @param position The position of the node
   * @param parent The node the NPC passed through to reach this node
   * @param destination The point the pathfinding algorithm is trying to reach
   * */
  public PathNode(int[] position, PathNode parent, int[] destination) {
    this.position = position;
    this.parent = parent;

    if (parent != null) {
      pathCost = parent.pathCost + 1;
    }

    heuristic = (int) (NavigationMesh.getEuclidianDistance(position, destination)) + pathCost;
  }


  @Override
  public String toString() {
    return Arrays.toString(position);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(position);
    return result;
  }

  /**
   * Override the equals method so that 2 PathNodes will be considered equal if their coordinates
   * are the same.
   *
   * @param obj The other object to test against
   * */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    PathNode other = (PathNode) obj;
    if (!Arrays.equals(position, other.position)) {
      return false;
    }
    return true;
  }
}
