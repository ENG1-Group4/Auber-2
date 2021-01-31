package com.threecubed.auber.entities;

import com.badlogic.gdx.utils.Timer.Task;
import com.threecubed.auber.World;
import com.threecubed.auber.pathfinding.NavigationMesh;

import org.json.JSONObject;

/**
 * The Power up which can be picked up by the player.
 *
 * @author Adam Wiegand
 * @version 0.0
 * @since 2.0
 * */
public class PowerUp extends GameEntity {
  PowerUpEffect powerUpEffect;
  public static enum PowerUpEffect {
    SHIELD,
    HEAL,
    SPEED,
    BOOM,
    INVINC,
    DUD;//for it to do nothing
  
    public static PowerUpEffect randomEffect() {
      // Int rounds down so no need to sub 1 from length
        return values()[(int) (Math.random() * (values().length - 1))];
      }
    }
  private int boomDelay = 0;
  /**
   * Initialise a PowerUp.
   *
   * @param x The x coordinate to initialise at
   * @param y The y coordinate to initialise at
   * @param effect The effect the powerUp should have on the player
   * @param world The game world
   * */
  public PowerUp(float x, float y, PowerUpEffect effect, World world) { 
    super(x, y, world.atlas.createSprite(spriteName(effect)));
    powerUpEffect = effect;
  }
  /**
   * the name of that powerUp's sprite.
   * 
   * @param effect The effect the powerUp should have on the player
   * */
  private static String spriteName(PowerUpEffect effect){
    switch (effect) {
      case SHIELD:
        return "powerUpShield";
      case HEAL:
        return "powerUpHeal";
      case SPEED:
        return "powerUpSpeed";
      case BOOM:
        return "powerUpBoom";
      case INVINC:
        return "powerUpInvinc";
      default:
        return "powerUp";
      }
  }
  /**
   * apply this power-up to the player
   * 
   * @param world The world in which the player exists
   * */
  public void handleCollisionWithPlayer(World world) {
    switch (powerUpEffect) {
      case SHIELD:
        shieldPlayer(world,world.POWERUP_SHIELD_AMOUNT);
        world.queueEntityRemove(this);
        break;
      case HEAL:
        healPlayer(world,World.POWERUP_HEALTH_AMOUNT);
        world.queueEntityRemove(this);
        break;
      case SPEED:
        speedPlayer(world);
        world.queueEntityRemove(this);
        break;
      case INVINC:
        invincPlayer(world);
        world.queueEntityRemove(this);
        break;
      case BOOM:
        boom(world);
        break;
      default:
        break;
    }
    //world.queueEntityRemove(this); removed to handle boom
  }
  public void shieldPlayer(final World world,int amount){
    world.player.shield += amount;
  }
  public void healPlayer(final World world,float amount){
    world.player.health = Math.min(1f, world.player.health + amount);
  }
  private void speedPlayer(final World world) {
    if (world.player.fast){return;}
    world.player.fast = true;
    world.player.maxSpeed *= world.POWERUP_SPEED_MULT;
    world.player.addTask(new Task() {
      @Override
      public void run() {
        world.player.maxSpeed /= world.POWERUP_SPEED_MULT;
        world.player.fast = false;
      }
    }, World.AUBER_BUFF_TIME,"fast");
  }
  private void boom(final World world) {
    //sprite positioning stuff
    position.x += sprite.getWidth()/2;
    position.y += sprite.getHeight()/2;
    sprite = world.atlas.createSprite("telesplosion");
    sprite.setScale(World.POWERUP_BOOM_RANGE/sprite.getWidth());//scaleable telesplosion
    position.x -= sprite.getWidth()/2;
    position.y -= sprite.getHeight()/2;
    boomDelay = 6;
    powerUpEffect = PowerUpEffect.DUD;//so no benefit is gained
    for (GameEntity entity : world.getEntities()) {
      float entityDistance = NavigationMesh.getEuclidianDistance(
        new float[] {position.x, position.y},
        new float[] {entity.position.x, entity.position.y}
        );
      if (entityDistance < World.POWERUP_BOOM_RANGE + World.NPC_EAR_STRENGTH && entity instanceof Npc) {
        Npc npc = (Npc) entity;
        if (entityDistance < World.POWERUP_BOOM_RANGE){
          npc.handleTeleporterShot(world);
        }
        if (entity instanceof Infiltrator) {
          Infiltrator infiltrator = (Infiltrator) entity;
          // Exposed infiltrators shouldn't flee
          if (infiltrator.exposed) {continue;}
        }
        npc.navigateToNearestFleepoint(world);
      }
    }
  }
  private void invincPlayer(final World world) {
    world.player.invinc = true;
    world.player.addTask(new Task() {
      @Override
      public void run() {
        world.player.invinc = false;
      }
    }, World.AUBER_BUFF_TIME,"invinc");
  }

  @Override
  public void update(World world) {

    if (boomDelay >= 1){
      boomDelay--;
      if (boomDelay == 0){
        world.queueEntityRemove(this);
      }
    }
  }
  //<changed>
  public JSONObject toJSON(){
    JSONObject powerUp = super.toJSON();
    powerUp.put("powerUpEffect",powerUpEffect.name());
    return powerUp;
  }
  public PowerUp(JSONObject powerUp,World world){
    super(powerUp, world.atlas.createSprite(spriteName(PowerUpEffect.valueOf(powerUp.getString("powerUpEffect")))));
    powerUpEffect = PowerUpEffect.valueOf(powerUp.getString("powerUpEffect"));
  }
  //</changed>
}