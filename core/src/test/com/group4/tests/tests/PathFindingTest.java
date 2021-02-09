package com.group4.tests.tests;
import com.threecubed.auber.pathfinding.NavigationMesh;
import com.threecubed.auber.pathfinding.PathNode;
import org.junit.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test the path finding algorithms
 *
 * @author Robert Watts
 * @version 1.0
 * @since 2.0
 * */
public class PathFindingTest {


    @Test
    public void test_path_node_equals() {

        PathNode pathNode = new PathNode(new int[]{10,10}, null, new int[]{20,10});
        PathNode pathNodeSame = new PathNode(new int[]{10,10}, pathNode, new int[]{20,30});
        PathNode pathNodeDifferent = new PathNode(new int[]{17,21}, pathNode, new int[]{50,30});

        String string = "This is a test";

        assertTrue(pathNode.equals(pathNodeSame), "The path nodes should be the same");
        assertFalse(pathNode.equals(pathNodeDifferent), "The path nodes are not equal");
        assertFalse(pathNode.equals(string), "The path nodes and string are not equal");
    }

    @Test
    public void test_path_node_path_cost() {
        PathNode firstPathNode = new PathNode(new int[]{10,10}, null, new int[]{11,11});
        PathNode secondPathNode = new PathNode(new int[]{11,11}, firstPathNode, new int[]{12,12});
        PathNode thirdPathNode = new PathNode(new int[]{12,12}, secondPathNode, new int[]{13,13});

        assertEquals(0, firstPathNode.pathCost, "The path cost for the first node should be 0");
        assertEquals(1, secondPathNode.pathCost, "The path cost for the first node should be 1");
        assertEquals(2, thirdPathNode.pathCost, "The path cost for the first node should be 2");
    }

    @Test
    public void test_path_node_heuristic() {
        PathNode firstPathNode = new PathNode(new int[]{0,0}, null, new int[]{0,0});
        PathNode secondPathNode = new PathNode(new int[]{0,0}, firstPathNode, new int[]{0,0});
        PathNode thirdPathNode = new PathNode(new int[]{5,0}, null, new int[]{0,7});

        assertEquals(0,firstPathNode.heuristic, "The heuristic should be 0");
        assertEquals(1,secondPathNode.heuristic, "The heuristic should be 1 to test the path cost is added");
        assertEquals(8,thirdPathNode.heuristic, "The heuristic should be 8");

    }

    @Test
    public void test_navigation_mesh_euclidian_distance() {
        float[][][] values = {
                //(X value, Y Value), (X value, Y Value)
                {{0,0}, {0,0}},
                {{1,0}, {0,0}},
                {{5,0}, {0,7}},
                {{5,0}, {-14,3}},
                {{5.1f,0.3f}, {0.3f,7.5f}},
        };

        float[] results = {0,1,8.602325f,19.235384f,8.653323f};

        for (int i = 0; i < values.length; i++) {
            assertEquals(results[i], NavigationMesh.getEuclidianDistance(values[i][0], values[i][1]),
                    String.format("The Euclidian Distance between the points ({0},{1})) and ({3},{4}) should be {5}",
                            values[i][0][0],values[i][0][1],values[i][1][0],values[i][1][1], results[i]
                            ));
        }

    }



}

