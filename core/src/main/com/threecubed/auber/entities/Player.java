package com.threecubed.auber.entities;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
//<changed>
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.TimeUtils;

import org.json.JSONArray;
import org.json.JSONObject;
//</changed>
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;
import com.threecubed.auber.Utils;
import com.threecubed.auber.World;
import com.threecubed.auber.pathfinding.NavigationMesh;


/**
 * The player entity that the user controls. Handles keyboard input, and interaction with other
 * entities and tiles in the game world.
 *
 * @author Daniel O'Brien, Adam Wiegand
 * @version 2.0
 * @since 1.0
 * */
public class Player extends GameEntity {
  private Timer playerTimer = new Timer(); //<changed/> made private
  private Vector2 teleporterRayCoordinates = new Vector2();

  /** Health of Auber - varies between 1 and 0. */
  public float health = 1;
  /** <changed> protects from that many hits */
  public int shield = 0;
  //</changed>
  public boolean confused = false;
  public boolean slowed = false;
  public boolean blinded = false;
  //<changed>
  public boolean fast = false;
  public boolean invinc = false;
  //</changed>
  private ShapeRenderer rayRenderer = new ShapeRenderer();
  
  //<changed>
  private Sound step = Gdx.audio.newSound(Gdx.files.internal("audio/footstep.mp3"));
  private Sound teleporter = Gdx.audio.newSound(Gdx.files.internal("audio/teleporter.mp3"));
  private Sound laserCharge = Gdx.audio.newSound(Gdx.files.internal("audio/laserCharge.mp3"));
  private Sound laserShot = Gdx.audio.newSound(Gdx.files.internal("audio/laserShot.mp3"));
  private long audioStartTimer = 0;
  //</changed>

  public Player(float x, float y, World world) {
    super(x, y, world.atlas.createSprite("player"));
  }

  /**
   * Handle player controls such as movement, interaction and firing the teleporing gun.
   *
   * @param world The game world
   * */
  @Override
  public void update(World world) {
    if (!world.demoMode) {
      filterTasks();//<changed/> remove old tasks
      //<changed>check for power-ups
      for (GameEntity entity : world.getEntities()) {
        if (Intersector.overlaps(entity.sprite.getBoundingRectangle(),
              sprite.getBoundingRectangle())
             && entity != this) {
          if (entity instanceof PowerUp) {
            PowerUp pUp = (PowerUp) entity;
            pUp.handleCollisionWithPlayer(world);
          } 
        }
      }
      //</changed>

      //tp to medbay
      if (Gdx.input.isKeyJustPressed(Input.Keys.Q) || health <= 0) {
        position.set(World.MEDBAY_COORDINATES[0], World.MEDBAY_COORDINATES[1]);
        confused = false;
        slowed = false;
        teleporterRayCoordinates.setZero();
      }
      // Increment Auber's health if in medbay
      if (world.medbay.getRectangle().contains(position.x, position.y)) {
        health += World.AUBER_HEAL_RATE;
        health = Math.min(1f, health);
      }
      // Slow down Auber when they charge their weapon. Should be stopped when weapon half charged,
      // hence the * 2
      float speedModifier = Math.min(world.auberTeleporterCharge * speed * 2, speed);

      // Flip the velocity before new velocity calculated if confused. Otherwise, second iteration
      // of flipped velocity will cancel out the first
      if (confused) {
        velocity.set(-velocity.x, -velocity.y);
      }

      if (Gdx.input.isKeyPressed(Input.Keys.W)) {
        velocity.y = Math.min(velocity.y + speed - speedModifier, maxSpeed);
      }
      if (Gdx.input.isKeyPressed(Input.Keys.A)) {
        velocity.x = Math.max(velocity.x - speed + speedModifier, -maxSpeed);
      }
      if (Gdx.input.isKeyPressed(Input.Keys.S)) {
        velocity.y = Math.max(velocity.y - speed + speedModifier, -maxSpeed);
      }
      if (Gdx.input.isKeyPressed(Input.Keys.D)) {
        velocity.x = Math.min(velocity.x + speed - speedModifier, maxSpeed);
      }
      
      //<changed>
      //See if the player has moved
      if (Math.abs(velocity.x) >= 0.7 || Math.abs(velocity.y) >= 0.7) {
        if (slowed) {
          //Sets the footstep sound effect to play at 0.64 sec intervals when the player is moving with slowed modifier
          velocity.scl(world.PROJECTILE_SLOW_MULT);
          if (TimeUtils.timeSinceNanos(audioStartTimer) > 640000000) {
            step.play(0.3f);
            audioStartTimer = TimeUtils.nanoTime();
          }
        } else if (fast) {
          //Sets the footstep sound effect to play at 0.20 sec intervals when the player is moving with speed modifier
          if (TimeUtils.timeSinceNanos(audioStartTimer) > 200000000) {
            step.play(0.3f);
            audioStartTimer = TimeUtils.nanoTime();
          }
        } else {
          //Sets the footstep sound effect to play at 0.32 sec intervals when the player is moving with no modifiers
          if (TimeUtils.timeSinceNanos(audioStartTimer) > 320000000) {
            step.play(0.3f);
            audioStartTimer = TimeUtils.nanoTime();
          }
        }
      }
      //</changed>

      if (Gdx.input.isButtonPressed(Input.Buttons.LEFT) && teleporterRayCoordinates.isZero()) {
        world.auberTeleporterCharge = Math.min(world.auberTeleporterCharge
            + World.AUBER_CHARGE_RATE, 1f);
      } else {
        if (world.auberTeleporterCharge > 0.95f) {
          world.auberTeleporterCharge = 0;

          // Scare entities
          teleporterRayCoordinates = handleRayCollisions(world);
          for (GameEntity entity : world.getEntities()) {
            float entityDistance = NavigationMesh.getEuclidianDistance(
                new float[] {teleporterRayCoordinates.x, teleporterRayCoordinates.y},
                new float[] {entity.position.x, entity.position.y}
                );
            if (entityDistance < World.NPC_EAR_STRENGTH && entity instanceof Npc) {
              if (entity instanceof Infiltrator) {
                Infiltrator infiltrator = (Infiltrator) entity;

                // Exposed infiltrators shouldn't flee
                if (infiltrator.exposed) {
                  continue;
                }
              }
              Npc npc = (Npc) entity;
              npc.navigateToNearestFleepoint(world);
            }
          }

          playerTimer.scheduleTask(new Task() {
            @Override
            public void run() {
              teleporterRayCoordinates.setZero();
            }
          }, World.AUBER_RAY_TIME);
        } else {
          world.auberTeleporterCharge = Math.max(world.auberTeleporterCharge
              - World.AUBER_CHARGE_RATE, 0f);
        }
      }
      if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
        // Interact with an object
        RectangleMapObject nearbyObject = getNearbyObjects(World.map);

        if (nearbyObject != null) {
          MapProperties properties = nearbyObject.getProperties();
          String type = properties.get("type", String.class);

          switch (type) {
            case "teleporter":
              MapObjects objects = World.map.getLayers().get("object_layer").getObjects();

              String linkedTeleporterId = properties.get("linked_teleporter", String.class);
              RectangleMapObject linkedTeleporter = (RectangleMapObject) objects.get(
                  linkedTeleporterId
                  );
              velocity.setZero();
              teleporter.play(0.3f);
              position.x = linkedTeleporter.getRectangle().getX();
              position.y = linkedTeleporter.getRectangle().getY();
              break;

            default:
              break;
          }
        }
      }

      Vector2 mousePosition = Utils.getMouseCoordinates(world.camera);

      // Set the rotation to the angle theta where theta is the angle between the mouse cursor and
      // player position. Correct the player position to be measured from the centre of the sprite.
      rotation = (float) (Math.toDegrees(Math.atan2(
              (mousePosition.y - getCenterY()),
              (mousePosition.x - getCenterX()))
            ) - 90f);

      // Handle the confused debuff
      if (confused) {
        velocity.set(-velocity.x, -velocity.y);
      }

      move(velocity, World.map);  
    }
  }

  /**
   * Overrides the GameEntity render method to render the player's teleporter raygun, as well
   * as the player itself.
   *
   * @param batch The batch to draw to
   * @param camera The world's camera
   * */
  @Override
  public void render(Batch batch, Camera camera) {
    if (!teleporterRayCoordinates.isZero()) {
      batch.end();
      Gdx.gl.glEnable(GL20.GL_BLEND);
      Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
      rayRenderer.setProjectionMatrix(camera.combined);
      rayRenderer.begin(ShapeType.Filled);
      rayRenderer.rectLine(getCenterX(), getCenterY(),
          teleporterRayCoordinates.x, teleporterRayCoordinates.y, 0.5f,
          World.rayColorA, World.rayColorB);
      rayRenderer.end();

      batch.begin();
    }
    super.render(batch, camera);
  }

  /**
   * Handle teleporter ray collisions and return the coordinates of the object it collides with.
   *
   * @param world The game world
   * @return The coordinates the ray hit
   * */
  private Vector2 handleRayCollisions(World world) {
    Vector2 output = new Vector2();

    Vector2 targetCoordinates = new Vector2(Utils.getMouseCoordinates(world.camera));
    float alpha = 0.1f;
    boolean rayIntersected = false;
    // Allow the ray to go 20x the distance between the mouse and player,
    // prevents game from hanging if ray escapes map
    while (!rayIntersected && alpha < 20) {
      output.x = getCenterX();
      output.y = getCenterY();

      output.lerp(targetCoordinates, alpha);

      // Check for entity collisions
      for (GameEntity entity : world.getEntities()) {
        if (!(entity instanceof Player)) {
          if (entity.sprite.getBoundingRectangle().contains(output)) {
            rayIntersected = true;
            if (entity instanceof Npc) {
              Npc npc = (Npc) entity;
              npc.handleTeleporterShot(world);
            }
            break;
          }
        }
      }

      // Check for tile collisions
      TiledMapTileLayer collisionLayer = (TiledMapTileLayer) World.map.getLayers()
          .get("collision_layer");
      Cell targetCell = collisionLayer.getCell(
          (int) output.x / collisionLayer.getTileWidth(),
          (int) output.y / collisionLayer.getTileHeight()
      );
      if (targetCell != null) {
        rayIntersected = true;
      }
      alpha += 0.1f;
    }
    return output;
  }
  /** <changed>
   * attempt to damage the player
   * 
   * @param amount the amount to damage the player by if successful
   * */
  public void damage(float amount){
    if (!invinc){
      if (shield > 0){
        shield -= 1;
      }else{
        health -= amount;
      }
    }
  }
  private ArrayList<Task> tasks = new ArrayList();
  private ArrayList<String> taskEffects = new ArrayList();
  public void addTask(Task task,float delaySeconds,String effect){
    tasks.add(playerTimer.scheduleTask(task, delaySeconds));
    taskEffects.add(effect);
  }
  private void filterTasks(){
    for (int i = tasks.size() - 1; i >= 0; i -= 1) {
      if (!tasks.get(i).isScheduled()){tasks.remove(i);taskEffects.remove(i);}
    }
  }
  public JSONObject toJSON(World world){
    JSONObject player = super.toJSON();
    player.put("health",health);
    player.put("shield",shield);

    player.put("confused",confused);
    player.put("slowed",slowed);
    player.put("blinded",blinded);
    player.put("fast",fast);
    player.put("invinc",invinc);
    filterTasks();
    JSONArray jtasks = new JSONArray();
    for (int i = 0; i < taskEffects.size(); i++) {
      JSONObject task = new JSONObject();
      task.put("effect",taskEffects.get(i));
      task.put("time",tasks.get(i).getExecuteTimeMillis() - TimeUtils.nanosToMillis(TimeUtils.nanoTime()));
      jtasks.put(task);
    }
    player.put("tasks",jtasks);
    return player;
  }
  /**
   * Creates a player from a given state
   * 
   * @param player   the JSONObject of player
   * @param world    the world
   */
  public Player(JSONObject player,World world){
    super(player,world.atlas.createSprite("player"));
    health = player.getFloat("health");
    shield = player.getInt("shield");
    confused = player.getBoolean("confused");
    slowed = player.getBoolean("slowed");
    blinded = player.getBoolean("blinded");
    fast = player.getBoolean("fast");
    invinc = player.getBoolean("invinc");
    for (Object obj : player.getJSONArray("tasks")) {
      JSONObject task = (JSONObject) obj;
      switch (task.getString("effect")) {
        case "confused":
        addTask(new Task() {
          @Override
          public void run() {
            confused = false;
          }
        }, task.getLong("time")/1000f,"confused");
          break;
        case "slowed":
        addTask(new Task() {
          @Override
          public void run() {
            slowed = false;
          }
        }, task.getLong("time")/1000f,"slowed");
            break;
        case "blinded":
        addTask(new Task() {
          @Override
          public void run() {
            blinded = false;
          }
        }, task.getLong("time")/1000f,"blinded");
          break;
        case "fast":
        final float speedMult = world.POWERUP_SPEED_MULT;
        addTask(new Task() {
          @Override
          public void run() {
            maxSpeed /= speedMult;
            fast = false;
          }
        }, task.getLong("time")/1000f,"fast");
          break;
        case "invinc":
        addTask(new Task() {
          @Override
          public void run() {
            invinc = false;
          }
        }, task.getLong("time")/1000f,"invinc");
          break;
        default:
          break;
      }
    }
  }
  //</changed>
}
