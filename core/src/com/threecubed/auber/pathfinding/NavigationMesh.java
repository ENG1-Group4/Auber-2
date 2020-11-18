package com.threecubed.auber.pathfinding;

import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.math.Vector2;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.PriorityQueue;


public class NavigationMesh {
  private boolean[][] mesh;
  TiledMapTileLayer navigationLayer;

  public NavigationMesh(TiledMapTileLayer navigationLayer) {
    this.navigationLayer = navigationLayer;

    mesh = new boolean[navigationLayer.getHeight()][navigationLayer.getWidth()];

    for (int y = 0; y < navigationLayer.getHeight(); y++) {
      for (int x = 0; x < navigationLayer.getWidth(); x++) {
        Cell currentCell = navigationLayer.getCell(x, y);
        setCell(x, y, currentCell == null ? false : true);
      }
    }
  }

  public void setCell(int x, int y, boolean value) {
    mesh[y][x] = value;
  }

  public boolean cellAccessible(int x, int y) {
    return mesh[y][x];
  }

  /**
   * Return the coordinates of the tile in the actual game world.
   *
   * @param x The x coordinate to convert 
   * @param y The y coordinate to convert
   *
   * @return A {@link Vector2} of converted coordinates
   * */
  public Vector2 getWorldCoordinates(int x, int y) {
    return new Vector2((float) x * navigationLayer.getTileWidth(),
                       (float) y * navigationLayer.getTileHeight());
  }

  public int[] getTilemapCoordinates(float x, float y) {
    return new int[] {(int) Math.floor(x / navigationLayer.getTileWidth()),
                      (int) Math.floor(y / navigationLayer.getTileHeight())};
  }

  /**
   * Return an {@link ArrayList} of all nodes surrounding a point.
   * */
  public ArrayList<PathNode> getSuccessorNodes(PathNode node, int[] destination) {
    int[][] surroundingCoordinates = {
        {0, -1},
        {-1, 0}, {1, 0},
        {0, 1}
      };

    int[][] diagonalCoordinates = {
      {-1, -1}, {1, -1},
      {-1, 1}, {1, 1}
      };

    ArrayList<PathNode> output = new ArrayList<>();
    for (int[] coordinates : surroundingCoordinates) {
      int targetX = node.position[0] + coordinates[0];
      int targetY = node.position[1] + coordinates[1];

      if (targetX > 0 && targetX < mesh[0].length - 1
          && targetY > 0 && targetY < mesh.length - 1
          && cellAccessible(targetX, targetY)) {
        output.add(new PathNode(new int[] {targetX, targetY}, node, destination));
      }
    }
    for (int[] coordinates : diagonalCoordinates) {
      int targetX = node.position[0] + coordinates[0];
      int targetY = node.position[1] + coordinates[1];

      // Coordinates of cells that must also be empty to make a diagonal move.
      // Example:
      // -------
      // | |A|T|  To make a move to target cell T,
      // -------  cells A and B must both be free.
      // | |.|B|
      // -------
      // | | | |
      // -------

      if (targetX > 0 && targetX < mesh[0].length - 1
          && targetY > 0 && targetY < mesh.length - 1
          && cellAccessible(targetX, targetY)
          && cellAccessible(node.position[0], targetY)
          && cellAccessible(targetX, node.position[1])) {
        output.add(new PathNode(new int[] {targetX, targetY}, node, destination));
      }
    }
    return output;
  }

  /**
   * Generate a path in terms of tilemap coordinates to a given tile.
   *
   * @param start The point to start at
   * @param destination The point to pathfind to
   *
   * @return An {@link ArrayList} of points representing a path between the 2 given coordinates
   * */
  public ArrayList<int[]> generateTilemapPathToPoint(final int[] start, final int[] destination) {
    ArrayList<int[]> path = new ArrayList<>();

    PathNode startNode = new PathNode(start, null, destination);

    Comparator<PathNode> distanceComparator = new Comparator<PathNode>() {
      @Override
      public int compare(PathNode firstPoint, PathNode secondPoint) {
        return (int) (firstPoint.heuristic * 1000) - (int) (secondPoint.heuristic * 1000);
      }
    };

    PriorityQueue<PathNode> openNodes = new PriorityQueue<>(distanceComparator);
    openNodes.add(startNode);

    ArrayList<PathNode> closedNodes = new ArrayList<>();

    while (!openNodes.isEmpty()) {
      PathNode currentNode = openNodes.remove();
      ArrayList<PathNode> successorNodes = getSuccessorNodes(currentNode, destination);
      for (PathNode successor : successorNodes) {
        if (Arrays.equals(successor.position, destination)) {
          // YAY
          while (successor.parent != null) {
            path.add(successor.position);
            successor = successor.parent;
          }
          Collections.reverse(path);
          return path;
        } else if (!closedNodes.contains(successor)) {
          openNodes.add(successor);
        }
      }
      closedNodes.add(currentNode);
    }
    throw new IllegalArgumentException("No path between the 2 given points could be found");
  }

  /**
   * Generate a path to a point in terms of real world coordinates.
   *
   * @param start A {@link Vector2} representing the start position
   * @param destination A {@link Vector2} representing the end position
   * */
  public ArrayList<Vector2> generateWorldPathToPoint(Vector2 start, Vector2 destination) {
    int[] startTile = {(int) start.x / navigationLayer.getTileWidth(),
                       (int) start.y / navigationLayer.getTileHeight()};
    System.out.println(startTile);

    int[] destinationTile = {(int) destination.x / navigationLayer.getTileWidth(),
                             (int) destination.y / navigationLayer.getTileHeight()};

    ArrayList<int[]> tilemapPath = generateTilemapPathToPoint(startTile, destinationTile);
    ArrayList<Vector2> worldPath = new ArrayList<>();

    for (int[] node : tilemapPath) {
      worldPath.add(new Vector2(node[0] * navigationLayer.getTileWidth(),
                                node[1] * navigationLayer.getTileHeight()));
    }
    worldPath.add(destination);

    return worldPath;
  }

  /**
   * Return the euclidian distance between 2 points.
   *
   * @param firstPoint The first point to test from
   * @param secondPoint The second point to test from
   *
   * @return The euclidian distance between the 2 points
   * */
  public static float getEuclidianDistance(int[] firstPoint, int[] secondPoint) {
    int horizontalDistance = secondPoint[0] - firstPoint[0];
    int verticalDistance = secondPoint[1] - firstPoint[1];
    return (float) Math.sqrt(Math.pow(horizontalDistance, 2) + Math.pow(verticalDistance, 2));
  }
}
