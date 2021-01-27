package com.threecubed.auber.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.threecubed.auber.AuberGame;
import com.threecubed.auber.World;
import com.threecubed.auber.ui.Button;
//<changed>
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
//</changed>

/**
 * The menu screen is the first screen that shows in the game and is responsible for controlling
 * when the game begins.
 *
 * @author Joseph Krystek-Walton
 * @version 1.0
 * @since 1.0
 * */
public class MenuScreen extends ScreenAdapter {
  World world;
  AuberGame game;

  Button playButton;
  //<changed>
  Button difficultyButton;
  String difficulty = "normal";
  Sprite diffEasy;
  Sprite diffNormal;
  Sprite diffHard;
  int delay = 0;
  //</changed>
  Button demoButton;
  OrthogonalTiledMapRenderer renderer;
  Sprite background;
  Sprite instructions;
  Sprite title;
  SpriteBatch spriteBatch;
  
  //<changed>
  public static Music menuMusic = Gdx.audio.newMusic(Gdx.files.internal("audio/menuMusic.mp3"));
  private final Sound menuSelect = Gdx.audio.newSound(Gdx.files.internal("audio/menuSelect.ogg"));
  private Boolean quittable = false;
  //</changed>
  
  /**
   * Instantiate the screen with the {@link AuberGame} object. Set the title and button up to be
   * rendered.
   *
   * @param game The game object
   * */
  public MenuScreen(final AuberGame game) {
    this.game = game;

    spriteBatch = new SpriteBatch();

    //<changed>
    menuMusic.play();
    menuMusic.setVolume(0.1f);
    menuMusic.setLooping(true);
    //</changed>

    background = game.atlas.createSprite("stars");
    instructions = game.atlas.createSprite("instructions");
    title = game.atlas.createSprite("auber_logo");
    diffEasy = game.atlas.createSprite("diffEasyButton");
    diffNormal = game.atlas.createSprite("diffNormalButton");
    diffHard= game.atlas.createSprite("diffHardButton");

    Runnable onPlayClick = new Runnable() {
      @Override
      public void run() {
        //<changed>
        menuMusic.stop();
        menuSelect.play(0.2f);
        quittable = false;
        //</changed>
        game.setScreen(new GameScreen(game, false));
      }
    };

    playButton = new Button(
        new Vector2(Gdx.graphics.getWidth() / 4, Gdx.graphics.getHeight() / 2 + 50),
        1f, game.atlas.createSprite("playButton"), game, onPlayClick);
    //<changed>
    Runnable onDifficultyClick = new Runnable() {
      @Override
      public void run() {
        if (delay > 0) {return;}
        switch (difficulty) {
          case "normal":
            difficulty = "hard";
            difficultyButton.setSprite(diffHard);
            break;
          case "hard":
            difficulty = "easy";
            difficultyButton.setSprite(diffEasy);
            break;
          case "easy":
            difficulty = "normal";
            difficultyButton.setSprite(diffNormal);
            break;
          default:
            difficulty = "normal";
        }
        delay = 20;
        world.changeDifficulty(difficulty);
      }
    };
    world.changeDifficulty("normal");
    difficultyButton = new Button(
      new Vector2(Gdx.graphics.getWidth() / 4, Gdx.graphics.getHeight() / 2 - 65f),
      1f, game.atlas.createSprite("diffNormalButton"), game, onDifficultyClick);
    //</changed>
    Runnable onDemoClick = new Runnable() {
      @Override
      public void run() {
        //<changed>
        menuMusic.stop();
        menuSelect.play(0.2f);
        quittable = false;
        //</changed>
        game.setScreen(new GameScreen(game, true));
      }
    };

    demoButton = new Button(
        new Vector2(Gdx.graphics.getWidth() / 4, Gdx.graphics.getHeight() / 2 - 180f),
        1f, game.atlas.createSprite("demoButton"), game, onDemoClick);
  }

  @Override
  public void render(float deltaTime) {
    //<changed>
    if (quittable){
      if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE)){
        Gdx.app.exit();
      }
    } else{
      if (!Gdx.input.isKeyPressed(Input.Keys.ESCAPE)){
        quittable = true;
      }
    }
    //</changed>
    if (Gdx.input.isKeyJustPressed(Input.Keys.D)) {
      game.setScreen(new GameScreen(game, true));
    }
    //<changed>
    if (delay > 0){delay -= 1;}
    // Set the background color
    Gdx.gl.glClearColor(0, 0, 0, 1);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

    spriteBatch.begin();

    background.setPosition(0f, 0f);
    background.draw(spriteBatch);

    instructions.setPosition(900f, 125f);
    instructions.draw(spriteBatch);

    title.setScale(0.5f);
    title.setPosition(0f, 550f);
    title.draw(spriteBatch);

    playButton.render(spriteBatch);
    difficultyButton.render(spriteBatch);
    demoButton.render(spriteBatch);

    spriteBatch.end();
  }
}
