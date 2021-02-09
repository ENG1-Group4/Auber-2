package com.group4.tests.tests;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Vector2;
import com.group4.tests.GdxTestRunner;
import com.threecubed.auber.World;
import com.threecubed.auber.entities.Infiltrator;
import com.threecubed.auber.entities.Player;
import com.threecubed.auber.entities.Projectile;
import com.threecubed.auber.pathfinding.NavigationMesh;
import org.junit.runner.RunWith;
import org.junit.Test;
import org.junit.Before;
import org.mockito.Mock;
import java.util.ArrayList;
import java.util.Arrays;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

/**
 * Test that the infiltrators work
 *
 * @author Robert Watts
 * @version 1.0
 * @since 2.0
 * */
@RunWith(GdxTestRunner.class)
public class InfiltratorsTest {

    /**
     * Create a mock world
     */
    World world;

    /**
     * Create a mock player
     */
    Player player;

    /**
     * Create a mock infiltrator
     */
    Infiltrator infiltrator;

    @Before
    public void setUp(){
        world = mock(World.class);
        world.atlas = new TextureAtlas("auber.atlas");
        world.navigationMesh = new NavigationMesh(
                (TiledMapTileLayer) world.map.getLayers().get("navigation_layer")
        );

        player = mock(Player.class);
        infiltrator = mock(Infiltrator.class);
        player.position = new Vector2(0,0);
        player.sprite = world.atlas.createSprite("player");

        world.player = player;
        world.entities = new ArrayList<>();
        world.entities.add(player);
        when(world.getEntities()).thenReturn(world.entities);


    }


    @Test
    public void check_easy_level_has_correct_number_of_infiltrators() {
        world.changeDifficulty("easy");
        assertEquals("The normal level should have 8 infiltrators", this.world.MAX_INFILTRATORS, 8);
    }

    @Test
    public void check_normal_level_has_correct_number_of_infiltrators() {
        World.changeDifficulty("normal");
        assertEquals("The normal level should have 8 infiltrators", world.MAX_INFILTRATORS, 8);
    }

    @Test
    public void check_hard_level_has_correct_number_of_infiltrators() {
        World.changeDifficulty("hard");
        assertEquals("The medium level should have 8 infiltrators", world.MAX_INFILTRATORS, 8);
    }

    @Test
    public void test_blinded_attack(){
        Projectile projectile = new Projectile(0, 0, new Vector2(0,0), infiltrator, Projectile.CollisionActions.BLIND, world);
        projectile.update(world);
        assertTrue("Player should be confused", world.player.blinded);
    }

    @Test
    public void test_slow_attack(){
        Projectile projectile = new Projectile(0, 0, new Vector2(0,0), infiltrator, Projectile.CollisionActions.SLOW, world);
        projectile.update(world);
        assertTrue("Player should be confused", world.player.slowed);
    }

    @Test
    public void test_confused_attack(){
        Projectile projectile = new Projectile(0, 0, new Vector2(0,0), infiltrator, Projectile.CollisionActions.CONFUSE, world);
        projectile.update(world);
        assertTrue("Player should be confused", world.player.confused);
    }

    @Test
    public void test_random_action(){
        for (int i = 0; i < 20; i++) {
            Projectile.CollisionActions action = Projectile.CollisionActions.randomAction();

            assertTrue("is not a valid action.",
                    Arrays.asList(
                            Projectile.CollisionActions.CONFUSE,
                            Projectile.CollisionActions.SLOW,
                            Projectile.CollisionActions.BLIND
                    ).contains(action));
        }
    }


}