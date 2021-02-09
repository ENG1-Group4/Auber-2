package com.group4.tests.tests;

import com.group4.tests.GdxTestRunner;
import com.threecubed.auber.AuberGame;
import com.threecubed.auber.screens.GameOverScreen;
import com.threecubed.auber.screens.GameScreen;
import com.threecubed.auber.screens.LoadScreen;
import com.threecubed.auber.screens.MenuScreen;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.mockito.Mockito.mock;
import static org.junit.Assert.*;

/**
 * Test that all the screens work
 *
 * @author Robert Watts
 * @version 1.0
 * @since 2.0
 * */
@RunWith(GdxTestRunner.class)
public class ScreensTest {

    AuberGame game;


    @Before
    public void setUp(){
        game = new AuberGame();
    }

    @Test
    public void test_main_menu_screen(){
        game.setScreen(mock(MenuScreen.class));

        assertTrue("Make sure that the screen was set correctly", game.getScreen() instanceof MenuScreen);
    }

    @Test
    public void test_load_screen(){
        game.setScreen(mock(LoadScreen.class));

        assertTrue("Make sure that the screen was set correctly", game.getScreen() instanceof LoadScreen);
    }

    @Test
    public void test_game_screen(){
        game.setScreen(mock(GameScreen.class));

        assertTrue("Make sure that the screen was set correctly", game.getScreen() instanceof GameScreen);
    }

    @Test
    public void test_GameOverScreen(){
        game.setScreen(mock(GameOverScreen.class));

        assertTrue("Make sure that the screen was set correctly", game.getScreen() instanceof GameOverScreen);
    }
}
