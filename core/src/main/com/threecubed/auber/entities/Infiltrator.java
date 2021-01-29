package com.threecubed.auber.entities;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Timer.Task;
import com.threecubed.auber.Utils;
import com.threecubed.auber.World;
//<changed>
import org.json.JSONObject;
import java.util.Hashtable;
//</changed>

/**
 * The infiltrator is the enemy of the game, it will navigate from system to system and sabotage
 * them until caught by the {@link Player}.
 *
 * @author Daniel O'Brien, Adam
 * @version 2.0
 * @since 1.0
 * */
public class Infiltrator extends Npc {
  public boolean exposed = false;
  Sprite unexposedSprite;
  //<changed> adds Id system so infiltrators can be linked to projectiles post saving
  private int id;
  private static int curId = 0;
  private static int nextId(){
    curId ++;
    return curId -1;
  }
  private static Hashtable<Integer,Infiltrator> entities = new Hashtable();
  @Override //so prior equals methods work
  public boolean equals(Object other){
    if (other instanceof Infiltrator){
      Infiltrator infiltrator = (Infiltrator) other;
      return this.id == infiltrator.id;
    } else{
      return false;
    }
  }
  public static Infiltrator idCheck(int id){
    if (entities.keySet().contains(id)){
      return entities.get(id);
    } else {
      throw new IllegalArgumentException("Id not found");
    }
  }
  public Infiltrator(float x, float y, World world) {
    this(x, y, world,nextId());
  }
  /**
   * Initialise an infiltrator at given coordinates.
   *
   * @param x The x position of the infiltrator
   * @param y The y position of the infiltrator
   * @param world The game world
   * */
  public Infiltrator(float x, float y, World world,int id) {
    super(x, y, world);
    navigateToRandomSystem(world);
    this.id = id;
    entities.replace(id, this);
  }
  //</changed>
  /**
   * Initialise the infiltrator at a random position.
   *
   * @param world The game world
   * */
  public Infiltrator(World world) {
    super(world);
    navigateToRandomSystem(world);
    unexposedSprite = new Sprite(sprite);
  }

  @Override
  public void update(World world) {
    super.update(world);
    if (exposed && !entityOnScreen(world)) {
      exposed = false;
      sprite = unexposedSprite;
    }
  }

  @Override
  public void handleDestinationReached(World world) {
    States oldState = state;
    state = States.IDLE;

    // Infiltrator won't try and sabotage a system if it has just been fleeing.
    if (oldState != States.FLEEING) {
      if (!playerNearby(world)
          && Utils.randomFloatInRange(world.randomNumberGenerator, 0, 1)
          < World.SYSTEM_SABOTAGE_CHANCE) {
        attackNearbySystem(world);
      } else {
        idleForGivenTime(world, Utils.randomFloatInRange(world.randomNumberGenerator, 5f, 8f));
      }
    }
  }

  @Override
  public void handleTeleporterShot(final World world) {
    if (!aiEnabled) {return;}//<changed> prevents attacked in brig bugs
    if (state == States.ATTACKING_SYSTEM) {
      RectangleMapObject system = getNearbyObjects(World.map);
      if (system != null) {
        Rectangle boundingBox = system.getRectangle();
        world.updateSystemState(boundingBox.x, boundingBox.y, World.SystemStates.WORKING);
      }
    }

    if (!exposed) {
      exposed = true;
      fireProjectileAtPlayer(world);
      sprite = world.atlas.createSprite("infiltrator");
      state = States.FLEEING;
      navigateToFurthestPointFromPlayer(world);
      npcTimer.scheduleTask(new Task() {
        @Override
        public void run() {
          if (exposed) {
            fireProjectileAtPlayer(world);
          } else {
            cancel();
          }
        }
      }, World.INFILTRATOR_FIRING_INTERVAL, World.INFILTRATOR_FIRING_INTERVAL);
    } else {
      //<changed>
      world.queueEntityAdd(new PowerUp(position.x,position.y,PowerUp.PowerUpEffect.randomEffect(),world));
      //</changed>
      position.x = Utils.randomFloatInRange(world.randomNumberGenerator,
          World.BRIG_BOUNDS[0][0], World.BRIG_BOUNDS[1][0]);
      position.y = Utils.randomFloatInRange(world.randomNumberGenerator,
          World.BRIG_BOUNDS[0][1], World.BRIG_BOUNDS[1][1]);
      aiEnabled = false;    
    }
  }

  /**
   * Attack a system nearby to the infiltrator.
   * */
  private void attackNearbySystem(final World world) {
    state = States.ATTACKING_SYSTEM;

    final RectangleMapObject system = getNearbyObjects(World.map);
    if (system != null) {
      world.updateSystemState(system.getRectangle().getX(), system.getRectangle().getY(),
          World.SystemStates.ATTACKED);

      npcTimer.scheduleTask(new Task() {
        @Override
        public void run() {
          if (aiEnabled) {
            world.updateSystemState(system.getRectangle().getX(), system.getRectangle().getY(),
                World.SystemStates.DESTROYED);
            navigateToRandomSystem(world);
          }
        }
      }, World.SYSTEM_BREAK_TIME);
    }
  }

  @Override
  public void navigateToNearestFleepoint(final World world) {
    if (aiEnabled) {
      if (state == States.ATTACKING_SYSTEM) {
        RectangleMapObject system = getNearbyObjects(World.map);
        if (system != null) {
          Rectangle boundingBox = system.getRectangle();
          world.updateSystemState(boundingBox.x, boundingBox.y, World.SystemStates.WORKING);  
        }
      }
      super.navigateToNearestFleepoint(world);
    }
  }

  private boolean playerNearby(World world) {
    if (world.demoMode) {
      return false;
    }
    Circle infiltratorSight = new Circle(position, World.INFILTRATOR_SIGHT_RANGE);
    if (infiltratorSight.contains(world.player.position)) {
      return true;
    }
    return false;
  }

  private void fireProjectileAtPlayer(World world) {
    Vector2 projectileVelocity = new Vector2(world.player.position.x - position.x,
                                             world.player.position.y - position.y);
    projectileVelocity.setLength(World.INFILTRATOR_PROJECTILE_SPEED);
    Projectile projectile = new Projectile(getCenterX(), getCenterY(), projectileVelocity, this,
        Projectile.CollisionActions.randomAction(), world);
    world.queueEntityAdd(projectile);
  }
  //<changed>
  public JSONObject toJSON(){
    JSONObject infiltrator = super.toJSON();
    infiltrator.put("exposed",exposed);
    infiltrator.put("id",id);
    return infiltrator;
  }
  public Infiltrator(JSONObject infiltrator,World world){
    super(infiltrator, world);
    exposed = infiltrator.getBoolean("exposed");
    unexposedSprite = new Sprite(sprite);
    id = infiltrator.getInt("id");
    entities.replace(id, this);
    curId = Math.max(curId,id + 1);
  }
  //</changed>
}
