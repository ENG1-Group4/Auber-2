package com.threecubed.auber.screens;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.threecubed.auber.AuberGame;

import org.json.JSONObject;


/**
 * the screen that saves can be loaded from
 * 
 *
 * @author Adam Wiegand
 * @version 1.0
 * @since 2.0
 * */
public class LoadScreen extends ScreenAdapter {
  AuberGame game;

  BitmapFont font = new BitmapFont();
  SpriteBatch batch = new SpriteBatch();
  GlyphLayout layout = new GlyphLayout();
  private Music menuMusic;
  private final Sound menuSelect = Gdx.audio.newSound(Gdx.files.internal("audio/menuSelect.ogg"));
  Sprite background;
  JSONObject saves;
  String savesText;
  ArrayList<String> fileNames;
  int selected_line = 0;

  /**
   * Instantiate the screen with an {@link AuberGame} object.
   *
   * @param game The game object. 
   * @param userWon Whether the user won or lost
   * */
  public LoadScreen(AuberGame game, Music menuMusic) {
    this.game = game;
    this.menuMusic = menuMusic;
    background = game.atlas.createSprite("stars");
    font.getData().setScale(2);
    saves = new JSONObject(Gdx.files.local("saves.json").readString());
    fileNames = new ArrayList();
    for (String name : saves.keySet()) {
      fileNames.add(name);
    } 
    refreshText();
    layout.setText(font, savesText);
  }
  public void refreshText(){
    if (fileNames.size() == 0){
      savesText = "Use f5 to take saves in game\n\n\n\n\n\n\nPress \"Escape\" to return to the menu";
      return;
    }
    savesText = "Use W and S to select save, use Enter to start it, Backspace to remove it\n\n";
    if (selected_line > 1){
      savesText += "   " + fileNames.get(selected_line - 2) + "\n";
    } else {savesText += "\n";}
    if (selected_line > 0){
      savesText += "   " + fileNames.get(selected_line - 1) + "\n";
    } else {savesText += "\n";}
    savesText += "[[[" + fileNames.get(selected_line) + "]]]\n";
    if (fileNames.size() > selected_line + 1){
      savesText += "   " + fileNames.get(selected_line + 1) + "\n";
      if (fileNames.size() > selected_line + 2){
        savesText += "   " + fileNames.get(selected_line + 2) + "\n";
      } else {savesText += "\n";}
    } else {savesText += "\n\n";}
    savesText += "\nPress \"Escape\" to return to the menu";
  }

  @Override
  public void render(float deltaTime) {
    // Set the background color
    Gdx.gl.glClearColor(0, 0, 0, 1);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    
    if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
      menuMusic.stop();
      game.setScreen(new MenuScreen(game));
    }
    boolean changed = false;
    if (Gdx.input.isKeyJustPressed(Input.Keys.W)) {
      selected_line = Math.max(0, selected_line - 1);
      changed = true;
    }
    if (Gdx.input.isKeyJustPressed(Input.Keys.S)) {
      selected_line = Math.min(fileNames.size() - 1, selected_line + 1);
      changed = true;
    }
    if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
      menuMusic.stop();
      menuSelect.play(0.2f);
      game.setScreen(new GameScreen(game, fileNames.get(selected_line)));
    }
    if (fileNames.size() != 0 &&Gdx.input.isKeyJustPressed(Input.Keys.BACKSPACE)) {
      saves.remove(fileNames.get(selected_line));
      fileNames.remove(selected_line);
      if (selected_line == fileNames.size()){selected_line -= 1;}
      Gdx.files.local("saves.json").writeString(saves.toString(), false);
      changed = true;
    }
    if (changed){menuSelect.play(0.2f);refreshText();}
    batch.begin();
    background.setPosition(0f, 0f);
    background.draw(batch);
    font.draw(batch, savesText, (Gdx.graphics.getWidth() - layout.width) / 2,
        300 + (Gdx.graphics.getHeight() - layout.height) / 2);
    batch.end();
  }
  
}
