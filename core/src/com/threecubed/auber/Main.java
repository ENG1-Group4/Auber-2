package com.threecubed.auber;

import java.util.ArrayList;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.threecubed.auber.entities.GameEntity;
import com.threecubed.auber.entities.Player;

public class Main extends ApplicationAdapter {
  ArrayList<GameEntity> entities;

  TiledMap map;
  OrthogonalTiledMapRenderer renderer;

  OrthographicCamera camera;

  @Override
  public void create () {
    Gdx.graphics.setWindowedMode(960, 540);

    entities = new ArrayList<>();
    entities.add(new Player(0f, 0f));

    // Load the tilemap
    map = new TmxMapLoader().load("map.tmx");
    renderer = new OrthogonalTiledMapRenderer(map, 1/16f);

    camera = new OrthographicCamera();
    camera.setToOrtho(false, 16, 9);
    camera.update();
  }

  @Override
  public void render () {
    Batch batch = renderer.getBatch();
    // Set the background color
    Gdx.gl.glClearColor(0, 0, 0, 1);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

    renderer.setView(camera);
    renderer.render();

    // Iterate over all entities. Perform movement logic and render them.
    // TODO: Make .update() call every 5 frames? better performance
    batch.begin();
    for (GameEntity entity : entities) {
      entity.update();
      entity.render(batch);
    }
    batch.end();
  }

  @Override
  public void dispose () {
  }
}
