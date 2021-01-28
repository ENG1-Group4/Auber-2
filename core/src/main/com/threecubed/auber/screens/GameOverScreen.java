package com.threecubed.auber.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.Input;
import com.threecubed.auber.AuberGame;
import com.badlogic.gdx.audio.Sound;

/**
 * The game over screen is the screen that the game is set to when a win/lose condition has been
 * reached.
 *
 * @author Bogdan Bodnariu-Lescinschi
 * @version 1.0
 * @since 2.0
 * */
public class GameOverScreen extends ScreenAdapter {
  AuberGame game;
  boolean userWon;
  private SpriteBatch batch = new SpriteBatch();

  //<changed>
  private Sound gameWinSound = Gdx.audio.newSound(Gdx.files.internal("audio/gameWin.wav"));
  private Sound gameOverSound = Gdx.audio.newSound(Gdx.files.internal("audio/gameOver.wav"));
  private final TextureRegion gameWin = new TextureRegion(new Texture("gameWin.png"), 0, 0, 1920, 1080);
  private final TextureRegion gameLose = new TextureRegion(new Texture("gameLose.png"), 0, 0, 1920, 1080);
  //</changed>

  /**
   * Instantiate the screen with an {@link AuberGame} object.
   *
   * @param game The game object. 
   * @param userWon Whether the user won or lost
   * */
  public GameOverScreen(AuberGame game, boolean userWon) {
    this.game = game;
    this.userWon = userWon;
    if(userWon){
      gameWinSound.play(0.35f);
    } else {
      gameOverSound.play(0.5f);
    }
  }

  @Override
  public void render(float deltaTime) {
    // Set the background color
    Gdx.gl.glClearColor(0, 0, 0, 1);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

    if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
      game.setScreen(new MenuScreen(game));
    }

    //Load the correct image depending on if the player won the game
    if(userWon){
      batch.begin();
      batch.draw(gameWin, 0, 0);
      batch.end();
    } else {
      batch.begin();
      batch.draw(gameLose, 0, 0);
      batch.end();
    }
  }
}