package com.group4.tests.tests;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.group4.tests.GdxTestRunner;
import com.threecubed.auber.World;
import com.threecubed.auber.entities.Player;

import org.junit.Test;
import org.junit.Before;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Test The the player health works as expected
 *
 * @author Robert Watts
 * @version 1.0
 * @since 2.0
 * */
@RunWith(GdxTestRunner.class)
public class PlayerHealthTest {

        public static float INITIAL_PLAYER_HEALTH = 0.5f;

        World world;

        Player player;

        @Before
        public void setUp(){
            world = mock(World.class);
            world.atlas = new TextureAtlas("auber.atlas");
            world.camera = new OrthographicCamera();
            world.medbay = new RectangleMapObject();

            player = new Player(0, 0, world);
            world.player = player;
            player.health = INITIAL_PLAYER_HEALTH;
        }

        @Test
        public void test_that_player_can_heal() {
            player.health += 0.1f;
            assertEquals("The player can heal",
                    INITIAL_PLAYER_HEALTH + 0.1f, player.health, 0.001);
        }

        @Test
        public void test_that_player_can_sustain_damage() {
            player.damage(0.1f);
            assertEquals("The player can sustain damage",
                    INITIAL_PLAYER_HEALTH - 0.1f, player.health, 0.001);
        }


        @Test
        public void test_that_invisible_player_cannot_sustain_damage() {
            player.invinc = true;
            player.damage(0.1f);
            assertEquals("The player cannot lose health when invisible",
                    INITIAL_PLAYER_HEALTH, player.health, 0.001);
        }

        @Test
        public void test_that_player_with_shield_cannot_sustain_damage() {
            player.shield = 2;
            player.damage(0.1f);
            assertEquals("The player cannot sustain damage with two shields",
                    INITIAL_PLAYER_HEALTH, player.health, 0.001);
            assertEquals("The player shield should drop by 1 when damaged", 1, player.shield);

            player.damage(0.1f);
            assertEquals("The player cannot sustain damage with two shields",
                    INITIAL_PLAYER_HEALTH, player.health, 0.001);
            assertEquals("The player shield should drop by 1 when damaged", 0, player.shield);

            player.damage(0.1f);
            assertEquals("The player can sustain damage when there is no shield",
                    INITIAL_PLAYER_HEALTH - 0.1f, player.health, 0.001);
        }

    }

