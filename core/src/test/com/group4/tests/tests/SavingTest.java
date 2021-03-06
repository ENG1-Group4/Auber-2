package com.group4.tests.tests;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.group4.tests.GdxTestRunner;
import com.threecubed.auber.World;
import com.threecubed.auber.entities.Infiltrator;
import com.threecubed.auber.entities.Player;
import com.threecubed.auber.pathfinding.NavigationMesh;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import java.util.ArrayList;
import java.util.Random;

import static org.mockito.Mockito.mock;
import static org.junit.Assert.*;

/**
 * Test the saving mechanism
 *
 * @author Robert Watts
 * @version 1.0
 * @since 2.0
 * */
@RunWith(GdxTestRunner.class)
public class SavingTest {

    World world;

    @Before
    public void setUp(){
        world = mock(World.class);
        world.atlas = new TextureAtlas("auber.atlas");
        world.randomNumberGenerator = new Random();
        world.systems = new ArrayList<RectangleMapObject>();

        world.navigationMesh = new NavigationMesh(
                (TiledMapTileLayer) World.map.getLayers().get("navigation_layer")
        );
    }

    @Test
    public void test_player_to_json(){

        Player player = new Player(50,50, world);
        world.player = player;

        assertEquals("{\"confused\":false,\"shield\":0,\"slowed\":false,\"fast\":false,\"x\":50,\"y\":50,\"health\":1,\"maxSpeed\":2,\"blinded\":false,\"tasks\":[],\"invinc\":false}",
                player.toJSON(world).toString());

        JSONAssert.assertEquals("{\"confused\":false,\"shield\":0,\"slowed\":false,\"fast\":false,\"x\":50,\"y\":50,\"health\":1,\"maxSpeed\":2,\"blinded\":false,\"tasks\":[],\"invinc\":false}",
                player.toJSON(world).toString(), JSONCompareMode.STRICT);
    }



}
