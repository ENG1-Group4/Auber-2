package com.threecubed.auber.entities;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector2;
import com.threecubed.auber.World;


/**
 * The GameEntity class is the abstract class from which all entities, including the player must
 * inherit from. It contains information regarding the speed and max speed of entities, code to
 * render an entity to the game world and for entities like the {@link Player}, information
 * information regarding friction and velocity and collision handlers.
 *
 * @author Daniel O'Brien
 * @version 1.0
 * @since 1.0
 * */
public abstract class GameEntity {
  public Sprite sprite;

  public float speed = 0.4f;
  public float maxSpeed = 2f;
  public float friction = 0.9f;

  public Vector2 position;
  public Vector2 velocity;
  public float rotation = 0f;

  private float[][] collisionOffsets;

  /**
   * Initialise a game entity at a given x and y coordinates.
   *
   * @param x The x coordinate of the entity
   * @param y The y coordinate of the entity
   * @param sprite The sprite the entity should use
   * */
  public GameEntity(float x, float y, Sprite sprite) {
    this.sprite = sprite; 
    sprite.setOriginCenter();

    position = new Vector2(x, y);
    velocity = new Vector2(0, 0);

    collisionOffsets = new float[][] {
        {2f, 2f},
        {sprite.getWidth() - 2f, 2f},
        {2f, sprite.getHeight() - 2f},
        {sprite.getWidth() - 2f, sprite.getHeight() - 2f}
      };
  }

  /**
   * Render the entity at its current coordinates with its current rotation.
   *
   * @param batch The batch to draw the sprite to
   * @param camera The world's camera
   * */
  public void render(Batch batch, Camera camera) {
    sprite.setRotation(rotation);
    sprite.setPosition(position.x, position.y);
    sprite.draw(batch);
  }

  /**
   * The "brain" of the entity, run any code that should be run at each render cycle that isn't
   * related to rendering the entity.
   *
   * @param world The game world
   * */
  public abstract void update(World world);

  /**
   * Update the entity's position, taking into account any obstacles and their current
   * velocity.
   *
   * @param velocity The entity's current velocity
   * @param map The tilemap to test for collisions on
   * */
  public void move(Vector2 velocity, TiledMap map) {
    TiledMapTileLayer collisionLayer = (TiledMapTileLayer) map.getLayers().get("collision_layer");

    // Store the direction of the velocity. More efficient than calling function multiple times,
    // also helps with the eventuality that the velocity sign might flip when avoiding collisions
    float velocitySignX = Math.signum(velocity.x);
    float velocitySignY = Math.signum(velocity.y);

    for (float[] offset : collisionOffsets) {
      Cell cell = collisionLayer.getCell(
            (int) ((position.x + velocity.x + offset[0]) / collisionLayer.getTileWidth()),
            (int) ((position.y + offset[1]) / collisionLayer.getTileHeight())
      );

      while (cell != null) {
        velocity.x -= velocitySignX * 0.1f;

        cell = collisionLayer.getCell(
            (int) ((position.x + velocity.x + offset[0]) / collisionLayer.getTileWidth()),
            (int) ((position.y + offset[1]) / collisionLayer.getTileHeight())
        );
      }
      cell = collisionLayer.getCell(
            (int) ((position.x + offset[0]) / collisionLayer.getTileWidth()),
            (int) ((position.y + velocity.y + offset[1]) / collisionLayer.getTileHeight())
      );
      while (cell != null) {
        velocity.y -= velocitySignY * 0.1f;

        cell = collisionLayer.getCell(
            (int) ((position.x + offset[0]) / collisionLayer.getTileWidth()),
            (int) ((position.y + velocity.y + offset[1]) / collisionLayer.getTileHeight())
        );
      }
    }

    position.add(velocity);
    velocity.scl(friction);
  }

  /**
   * Return any interactable objects on the entities position.
   *
   * @param map The game world map
   * @return A {@link RectangleMapObject} on the entities position
   * */
  public RectangleMapObject getNearbyObjects(TiledMap map) {
    MapLayer interactionLayer = map.getLayers().get("object_layer");
    MapObjects objects = interactionLayer.getObjects();

    for (MapObject object : objects) {
      if (object instanceof RectangleMapObject) {
        RectangleMapObject rectangularObject = (RectangleMapObject) object;
        if (Intersector.overlaps(sprite.getBoundingRectangle(), rectangularObject.getRectangle())) {
          return rectangularObject;
        }
      }
    }
    return null;
  }

  /**
   * Check if the {@link GameEntity} is currently visible on-screen. This will return true, even
   * if the entity is only partially on the screen.
   *
   * @param world The game world
   * @return A boolean dictating whether the entity is visible by the world's camera
   * */
  public boolean entityOnScreen(World world) {
    float halfWidth = world.camera.viewportWidth / 2;
    float halfHeight = world.camera.viewportHeight / 2;
    return (position.x + sprite.getWidth() > world.camera.position.x - halfWidth
            && position.y + sprite.getHeight() > world.camera.position.y - halfHeight
            && position.x < world.camera.position.x + halfWidth
            && position.y < world.camera.position.y + halfHeight);
  }

  /**
   * Get the x coordinate of the center of an entity.
   *
   * @return The y coordinate at the center of the entity
   * */
  public float getCenterX() {
    return position.x + (sprite.getWidth() / 2);
  }

  /**
   * Get the y coordinate of the center of the entity.
   *
   * @return The y coordinate at the center of the entity
   * */
  public float getCenterY() {
    return position.y + (sprite.getHeight() / 2);
  }

  /**
   * Return the center coordinates of an entity as a {@link Vector2}.
   *
   * @return A {@link Vector2} of the entities central coordinates.
   * */
  public Vector2 getCenter() {
    return new Vector2(getCenterX(), getCenterY());
  }
}
