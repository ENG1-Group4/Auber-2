package com.group4.tests.tests;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Vector2;
import com.group4.tests.GdxTestRunner;
import com.threecubed.auber.World;
import com.threecubed.auber.entities.Infiltrator;
import com.threecubed.auber.entities.Npc;
import com.threecubed.auber.entities.Player;
import com.threecubed.auber.entities.PowerUp;
import com.threecubed.auber.pathfinding.NavigationMesh;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;

/**
 * Test that all powerups work
 *
 * @author Robert Watts
 * @version 1.0
 * @since 2.0
 * */
@RunWith(GdxTestRunner.class)
public class PowerUps {

    World world;

    Player player;

    public static float INITIAL_PLAYER_HEALTH = 0.5f;

    private void handleShot(Infiltrator inf){
        if (inf.exposed){
            inf.aiEnabled = false;
        } else {
            inf.exposed = true;
        }
    }

    @Before
    public void setUp(){
        world = mock(World.class);
        world.atlas = new TextureAtlas("auber.atlas");
        world.navigationMesh = new NavigationMesh(
                (TiledMapTileLayer) world.map.getLayers().get("navigation_layer")
        );

        player = new Player(0, 0, world);
        player.position = new Vector2(0,0);
        player.sprite = world.atlas.createSprite("player");
        player.health = INITIAL_PLAYER_HEALTH;

        world.player = player;
        world.entities = new ArrayList<>();
        world.entities.add(player);
        when(world.getEntities()).thenReturn(world.entities);

        //Default to hard for all the tests
        World.changeDifficulty("hard");


    }

    @Test
    public void test_dud_power_up(){
        PowerUp powerUp = new PowerUp(0,0, PowerUp.PowerUpEffect.DUD, world);
        powerUp.handleCollisionWithPlayer(world);
        assertFalse("The dud power up should not effect the players speed", world.player.fast);
        assertFalse("The dud power up should not effect the invincibility of the player", world.player.invinc);
        assertEquals("The dud power up should not effect the shield of the player", 0, world.player.shield);
        assertEquals("The dud power up should not effect the health of the player", INITIAL_PLAYER_HEALTH, world.player.health, 0.001);
        assertEquals("The dud power up should not effect the max speed of the player", 2f, world.player.maxSpeed, 0.001);
    }

    @Test
    public void test_heal_power_up(){
        PowerUp powerUp = new PowerUp(0,0, PowerUp.PowerUpEffect.HEAL, world);
        powerUp.handleCollisionWithPlayer(world);

        assertFalse("The heal power up should not effect the players speed", world.player.fast);
        assertFalse("The heal power up should not effect the invincibility of the player", world.player.invinc);
        assertEquals("The heal power up should not effect the shield of the player", 0, world.player.shield);
        assertEquals("The heal power up should increase the health of the player", 0.8f, world.player.health, 0.001);
        assertEquals("The heal power up should not effect the max speed of the player", 2f, world.player.maxSpeed, 0.001);
    }


    @Test
    public void test_invincibility_power_up() {
        PowerUp powerUp = new PowerUp(0,0, PowerUp.PowerUpEffect.INVINC, world);
        powerUp.handleCollisionWithPlayer(world);

        assertFalse("The invincibility power up should not effect the players speed", world.player.fast);
        assertTrue("The invincibility power up should effect the invincibility of the player", world.player.invinc);
        assertEquals("The invincibility power up not should not effect the shield of the player", 0, world.player.shield);
        assertEquals("The invincibility power up not should increase the health of the player", INITIAL_PLAYER_HEALTH, world.player.health, 0.001);
        assertEquals("The invincibility power up should not effect the max speed of the player", 2f, world.player.maxSpeed, 0.001);
    }

    @Test
    public void test_shield_power_up() {
        PowerUp powerUp = new PowerUp(0,0, PowerUp.PowerUpEffect.SHIELD, world);
        powerUp.handleCollisionWithPlayer(world);

        assertFalse("The shield power up should not effect the players speed", world.player.fast);
        assertFalse("The shield power up should not effect the invincibility of the player", world.player.invinc);
        assertEquals("The shield power up should effect the shield of the player", 1, world.player.shield);
        assertEquals("The shield power up should not increase the health of the player", INITIAL_PLAYER_HEALTH, world.player.health, 0.001);
        assertEquals("The shield power up should not effect the max speed of the player", 2f, world.player.maxSpeed, 0.001);
    }

    @Test
    public void test_speed_power_up() {
        PowerUp powerUp = new PowerUp(0,0, PowerUp.PowerUpEffect.SPEED, world);
        powerUp.handleCollisionWithPlayer(world);

        assertTrue("The shield power up should effect the players speed", world.player.fast);
        assertFalse("The shield power up should not effect the invincibility of the player", world.player.invinc);
        assertEquals("The shield power up should not effect the shield of the player", 0, world.player.shield);
        assertEquals("The shield power up should not increase the health of the player", INITIAL_PLAYER_HEALTH, world.player.health, 0.001);
        assertEquals("The shield power up should effect the max speed of the player", 3f, world.player.maxSpeed, 0.001);
    }

    @Test
    public void test_boom_power_up(){
        PowerUp powerUp = new PowerUp(0,0, PowerUp.PowerUpEffect.BOOM, world);

        Infiltrator revealed = mock(Infiltrator.class);
        doAnswer(new Answer() {
            public Object answer(InvocationOnMock invocation) {
            handleShot((Infiltrator) invocation.getMock());
            return null;
        }}).when(revealed).handleTeleporterShot(world);
        revealed.position = new Vector2(0,0);
        revealed.exposed = false;
        world.entities.add(revealed);

        Infiltrator arrested = mock(Infiltrator.class);
        doAnswer(new Answer() {
            public Object answer(InvocationOnMock invocation) {
            handleShot((Infiltrator) invocation.getMock());
            return null;
        }}).when(arrested).handleTeleporterShot(world);
        arrested.position = new Vector2(240,240);
        revealed.exposed = true;
        revealed.aiEnabled = true;
        world.entities.add(arrested);
        
        Infiltrator out_of_range = mock(Infiltrator.class);
        doAnswer(new Answer() {
            public Object answer(InvocationOnMock invocation) {
            handleShot((Infiltrator) invocation.getMock());
            return null;
        }}).when(out_of_range).handleTeleporterShot(world);
        out_of_range.position = new Vector2(10f + world.POWERUP_BOOM_RANGE,10f + world.POWERUP_BOOM_RANGE);
        doNothing().when(out_of_range).navigateToNearestFleepoint(world);
        out_of_range.exposed = true;
        out_of_range.aiEnabled = true;
        world.entities.add(out_of_range);

        powerUp.handleCollisionWithPlayer(world);

        assertFalse("The boom power up should not effect the players speed", world.player.fast);
        assertFalse("The boom power up should not effect the invincibility of the player", world.player.invinc);
        assertEquals("The boom power up should not effect the shield of the player", 0, world.player.shield);
        assertEquals("The boom power up should not effect the health of the player", INITIAL_PLAYER_HEALTH, world.player.health, 0.001);
        assertEquals("The boom power up should not effect the max speed of the player", 2f, world.player.maxSpeed, 0.001);

        assertEquals("The boom power up should reveal the unexposed infiltrator",true,revealed.exposed);
        assertEquals("The boom power up should arrest the exposed infiltrator",false,arrested.aiEnabled);
        assertEquals("The boom power up should not affect the infiltrator out of range",true,out_of_range.aiEnabled);
    }

    @Test
    public void check_easy_level_has_correct_powerup_multipliers() {
        World.changeDifficulty("easy");
        assertEquals("The easy level should have a power up health amount of 0.8", 0.8 , World.POWERUP_HEALTH_AMOUNT, 0.001f);
        assertEquals("The easy level should have a power up shield amount of 2",2 , World.POWERUP_SHIELD_AMOUNT, 0.001f);
        assertEquals("The easy level should have a power up speed multiplier amount of 2.5",2.5 , World.POWERUP_SPEED_MULT, 0.001f);
        assertEquals("The easy level should have a power up boom power up range of 240", 240, World.POWERUP_BOOM_RANGE, 0.001f);
        assertEquals("The easy level should have a buffer time of 15", 15, World.AUBER_BUFF_TIME, 0.001f);
    }

    @Test
    public void check_normal_level_has_correct_powerup_multipliers() {
        World.changeDifficulty("normal");
        assertEquals("The normal level should have a power up health amount of 0.5", 0.5 , World.POWERUP_HEALTH_AMOUNT, 0.001f);
        assertEquals("The normal level should have a power up shield amount of 1",1 , World.POWERUP_SHIELD_AMOUNT, 0.001f);
        assertEquals("The normal level should have a power up speed multiplier amount of 2",2 , World.POWERUP_SPEED_MULT, 0.001f);
        assertEquals("The normal level should have a power up boom power up range of 160", 160, World.POWERUP_BOOM_RANGE, 0.001f);
        assertEquals("The normal level should have a buffer time of 10", 10, World.AUBER_BUFF_TIME, 0.001f);
    }

    @Test
    public void check_hard_level_has_correct_powerup_multipliers() {
        World.changeDifficulty("hard");
        assertEquals("The hard level should have a power up health amount of 0.3", 0.3 , World.POWERUP_HEALTH_AMOUNT, 0.001f);
        assertEquals("The hard level should have a power up shield amount of 1",1 , World.POWERUP_SHIELD_AMOUNT, 0.001f);
        assertEquals("The hard level should have a power up speed multiplier amount of 1.5",1.5 , World.POWERUP_SPEED_MULT, 0.001f);
        assertEquals("The hard level should have a power up boom power up range of 120", 120, World.POWERUP_BOOM_RANGE, 0.001f);
        assertEquals("The hard level should have a buffer time of 8", 8, World.AUBER_BUFF_TIME, 0.001f);
    }


}
