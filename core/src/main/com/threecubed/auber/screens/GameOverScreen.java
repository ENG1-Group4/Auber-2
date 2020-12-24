package com.threecubed.auber.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.threecubed.auber.AuberGame;


/**
 * The game over screen is the screen that the game is set to when a win/lose condition has been
 * reached.
 *
 * @author Joseph Krystek-Walton
 * @version 1.0
 * @since 1.0
 * */
public class GameOverScreen extends ScreenAdapter {
  AuberGame game;

  BitmapFont font = new BitmapFont();
  SpriteBatch batch = new SpriteBatch();
  GlyphLayout layout = new GlyphLayout();
  String resultText;

  /**
   * Instantiate the screen with an {@link AuberGame} object.
   *
   * @param game The game object. 
   * @param userWon Whether the user won or lost
   * */
  public GameOverScreen(AuberGame game, boolean userWon) {
    this.game = game;
    font.getData().setScale(2);

    resultText = "Game Over, you ";
    if (userWon) {
      resultText += "win!";
    } else {
      resultText += "lose.";
    }
    resultText += "\nPress \"Escape\" to return to the menu";
    layout.setText(font, resultText);
  }

  @Override
  public void render(float deltaTime) {
    // Set the background color
    Gdx.gl.glClearColor(0, 0, 0, 1);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

    if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
      game.setScreen(new MenuScreen(game));
    }

    batch.begin();
    font.draw(batch, resultText, (Gdx.graphics.getWidth() - layout.width) / 2,
        300 + (Gdx.graphics.getHeight() - layout.height) / 2);
    batch.end();
  }
  
}
