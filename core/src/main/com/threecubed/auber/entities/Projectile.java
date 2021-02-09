package com.threecubed.auber.entities;

import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Timer.Task;
import com.threecubed.auber.World;
//<changed>
import org.json.JSONObject;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
//</changed>

public class Projectile extends GameEntity {
  CollisionActions collisionAction;
  GameEntity originEntity;

  //<changed>
  private Sound playerHurt = Gdx.audio.newSound(Gdx.files.internal("core/assets/audio/playerHurt.mp3"));
  //</changed>

  public static enum CollisionActions {
    CONFUSE,
    SLOW,
    BLIND;

    public static CollisionActions randomAction() {
      // Int rounds down so no need to sub 1 from length
      return values()[(int) (Math.random() * values().length)];
    }
  }

  /**
   * Initialise a projectile.
   *
   * @param x The x coordinate to initialise at
   * @param y The y coordinate to initialise at
   * @param velocity A {@link Vector2} representing the velocity of the projectile
   * @param originEntity The entity that the projectile originated from
   * @param action The effect the projectile should have on the player
   * @param world The game world
   * */
  public Projectile(float x, float y, Vector2 velocity, GameEntity originEntity,
      CollisionActions action, World world) {
    super(x, y, world.atlas.createSprite("projectile"));
    collisionAction = action;
    this.originEntity = originEntity;
    this.velocity = velocity;
  }

  /**
   * Step the projectile in its target direction, execute the collision handler if it hits the
   * {@link Player}, destroy if it hits anything else.
   *
   * @param world The game world
   * */
  public void update(World world) {
    position.add(velocity);
    for (GameEntity entity : world.getEntities()) {
      if (Intersector.overlaps(entity.sprite.getBoundingRectangle(),
            sprite.getBoundingRectangle())
          && entity != originEntity && entity != this) {
        if (entity instanceof Player) {
          handleCollisionWithPlayer(world);
          //<changed>
          playerHurt.play(0.25f);
          //</changed>
        } 
        world.queueEntityRemove(this);
        return;
      }
    }
    TiledMapTileLayer collisionLayer = (TiledMapTileLayer)
        World.map.getLayers().get("collision_layer");

    int[] cellCoordinates = world.navigationMesh.getTilemapCoordinates(getCenterX(), getCenterY());

    if (collisionLayer.getCell(cellCoordinates[0], cellCoordinates[1]) != null) {
      world.queueEntityRemove(this);
    }
  }

  private void handleCollisionWithPlayer(World world) {
    switch (collisionAction) {
      case CONFUSE:
        confusePlayer(world);
        break;
      case SLOW:
        slowPlayer(world);
        break;
      case BLIND:
        blindPlayer(world);
        break;
      default:
        break;
    }
    //<changed>
    world.player.damage(World.INFILTRATOR_PROJECTILE_DAMAGE);
    //</changed>
  }

  private void confusePlayer(final World world) {
    if (world.player.invinc) {return;}
    world.player.confused = true;
    world.player.addTask(new Task() {
      @Override
      public void run() {
        world.player.confused = false;
      }
    }, World.AUBER_DEBUFF_TIME,"confused");
  }

  private void slowPlayer(final World world) {
    if (world.player.invinc) {return;}
    world.player.slowed = true;
    world.player.addTask(new Task() {
      @Override
      public void run() {
        world.player.slowed = false;
      }
    }, World.AUBER_DEBUFF_TIME,"slow");
  }

  private void blindPlayer(final World world) {
    if (world.player.invinc) {return;}
    world.player.blinded = true;
    world.player.addTask(new Task() {
      @Override
      public void run() {
        world.player.blinded = false;
      }
    }, World.AUBER_DEBUFF_TIME - 3f,"blinded");
  }
  //<changed>
  public JSONObject toJSON(){
    JSONObject projectile = super.toJSON();
    projectile.put("collisionAction",collisionAction.name());
    JSONObject velocity = new JSONObject();
    velocity.put("x",this.velocity.x);
    velocity.put("y",this.velocity.y);
    projectile.put("velocity",velocity);
    projectile.put("originEntity",originEntity.toJSON());
    return projectile;
  }
  public Projectile(JSONObject projectile,World world){
    super(projectile, world.atlas.createSprite("projectile"));
    collisionAction = CollisionActions.valueOf(projectile.getString("collisionAction"));
    JSONObject velocity = projectile.getJSONObject("velocity");
    this.velocity = new Vector2(velocity.getFloat("x"),velocity.getFloat("y"));
    originEntity = Infiltrator.idCheck(projectile.getJSONObject("originEntity").getInt("id"));
  }
  //</changed>
}
