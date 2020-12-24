package com.threecubed.auber.pathfinding;

import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.math.Vector2;
import com.threecubed.auber.entities.GameEntity;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.PriorityQueue;


/**
 * The NavigationMesh class is a wrapper around a 2d array of {@link Boolean}s representing which
 * tiles within the game world are accessible. It reads in a {@link TiledMapTileLayer} in order to
 * produce this array of booleans. If a given set of coordinates do not have a tile, it is
 * considered inaccessible. For this reason, the background layer of the map is used as the input
 * for the navigation mesh.
 * The class also contains functions required to facilitate A* pathfinding which entities that
 * inherit from Npc make use of.
 *
 * @author Daniel O'Brien
 * @version 1.0
 * @since 1.0
 * */
public class NavigationMesh {
  private boolean[][] mesh;
  TiledMapTileLayer navigationLayer;

  /**
   * Produce a navigation mesh from a given {@link TiledMapTileLayer}.
   * This works by iterating over the given layer and appending either true or false to the mesh
   * based upon whether a tile is present at the current coordinates
   *
   * @param navigationLayer The layer to produce a navigation mesh from
   * */
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

  /**
   * Setter for the {@link NavigationMesh#mesh} variable that flips the x and y values
   * so that the x and y values don't need to be flipped when accessing the array.
   *
   * @param x The x coordinate of the cell to set
   * @param y The y coordinate of the cell to set
   * @param value The value to set the cell to
   * */
  public void setCell(int x, int y, boolean value) {
    mesh[y][x] = value;
  }

  /**
   * Getter for {@link NavigationMesh#mesh} that returns whether a cell at given coordinates
   * is accessible.
   *
   * @param x The x coordinate to test
   * @param y The y coordinate to test
   *
   * @return A boolean representing whether the chosen cell is accessible
   * */
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
   * Return an {@link ArrayList} of all {@link PathNode}s surrounding a point.
   *
   * @param node The PathNode to find successors for
   * @param destination The target destination of the pathfinding algorithm
   *
   * @return An {@link ArrayList} of {@link PathNode}s of possible moves that the npc could make
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
          while (successor.parent != null) {
            path.add(successor.position);
            successor = successor.parent;
          }
          Collections.reverse(path);
          return path;
        } else if (!closedNodes.contains(successor) && !openNodes.contains(successor)) {
          openNodes.add(successor);
        }
      }
      closedNodes.add(currentNode);
    }
    throw new IllegalArgumentException("No path between the 2 given points could be found");
  }

  /**
   * Generate a path to a point in terms of real world coordinates as opposed to tilemap
   * coordinates.
   *
   * @param start A {@link Vector2} representing the start position
   * @param destination A {@link Vector2} representing the end position
   *
   * @return An {@link ArrayList} of {@link Vector2}s that represent a path to the requested point
   * */
  public ArrayList<Vector2> generateWorldPathToPoint(Vector2 start, Vector2 destination) {
    int[] startTile = {(int) start.x / navigationLayer.getTileWidth(),
                       (int) start.y / navigationLayer.getTileHeight()};

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
   * Get the coordinates of the furthest point from the given entity.
   *
   * @param entity The entity to find the furthest point from
   * @return The world coordinates of the furthest point from this entity
   * */
  public Vector2 getFurthestPointFromEntity(GameEntity entity) {
    int[] tileCoordinates = getTilemapCoordinates(entity.position.x, entity.position.y);
    float longestDistance = 0;
    int[] longestDistanceCoordinates = {0, 0};
    for (int y = 0; y < mesh.length; y++) {
      for (int x = 0; x < mesh[0].length; x++) {
        if (cellAccessible(x, y)) {
          int[] currentCellCoords = {x, y};
          float distance = getEuclidianDistance(tileCoordinates, currentCellCoords);
          if (distance > longestDistance) {
            longestDistance = distance;
            longestDistanceCoordinates = currentCellCoords;
          }
        }
      }
    }
    return getWorldCoordinates(longestDistanceCoordinates[0], longestDistanceCoordinates[1]);
  }

  /**
   * Return the euclidian distance between 2 float arrays (world coordinates).
   *
   * @param firstPoint The first point to test from
   * @param secondPoint The second point to test from
   *
   * @return The euclidian distance between the 2 points
   * */
  public static float getEuclidianDistance(float[] firstPoint, float[] secondPoint) {
    float horizontalDistance = secondPoint[0] - firstPoint[0];
    float verticalDistance = secondPoint[1] - firstPoint[1];
    return (float) Math.sqrt(Math.pow(horizontalDistance, 2) + Math.pow(verticalDistance, 2));
  }

  /**
   * Return the euclidian distance between 2 integer arrays (tilemap coordinates).
   *
   * @param firstPoint The first point to test from
   * @param secondPoint The second point to test from
   *
   * @return The euclidian distance between the 2 points
   * */
  public static float getEuclidianDistance(int[] firstPoint, int[] secondPoint) {
    float[] convertedFirstPoint = {(float) firstPoint[0], (float) firstPoint[1]};
    float[] convertedSecondPoint = {(float) secondPoint[0], (float) secondPoint[1]};

    return getEuclidianDistance(convertedFirstPoint, convertedSecondPoint);
  }
}
