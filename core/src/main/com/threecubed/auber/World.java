package com.threecubed.auber;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.maps.tiled.TiledMapTileSet;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;//<changed/>
import com.threecubed.auber.entities.GameEntity;
import com.threecubed.auber.entities.Player;
import com.threecubed.auber.pathfinding.NavigationMesh;
import com.threecubed.auber.screens.GameOverScreen;
import com.threecubed.auber.screens.GameScreen;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
//<changed>
import com.badlogic.gdx.Gdx;
import org.json.*;
import java.math.BigDecimal;
import java.util.Date;
//</changed>
/**
 * The world class stores information related to what is happening within the game world.
 * It should only be used within the GameScreen screen.
 *
 * @author Daniel O'Brien, Adam Wiegand
 * @version 1.0
 * @since 1.0
 * */
public class World {
  private AuberGame game;

  public Player player;
  public int infiltratorCount;

  public boolean demoMode = false;
//<changed>
  public Date saveTime = null;

  private static JSONObject gameData = new JSONObject(Gdx.files.internal("difficulty.json").readString());
  private static JSONObject difficultyData;
  public static void changeDifficulty(String difficulty){
    difficultyData = (JSONObject) gameData.get(difficulty);
    setValues();
  }
  private static float getFloat(JSONObject obj,String key){
    return BigDecimal.valueOf(obj.getDouble(key)).floatValue();
  }
  private static void setValues(){
    AUBER_CHARGE_RATE = getFloat(difficultyData,"AUBER_CHARGE_RATE");
    AUBER_DEBUFF_TIME = getFloat(difficultyData,"AUBER_DEBUFF_TIME");
    AUBER_BUFF_TIME = getFloat(difficultyData,"AUBER_BUFF_TIME");
    AUBER_HEAL_RATE = getFloat(difficultyData,"AUBER_HEAL_RATE");
    SYSTEM_BREAK_TIME = getFloat(difficultyData,"SYSTEM_BREAK_TIME");
    SYSTEM_SABOTAGE_CHANCE = getFloat(difficultyData,"SYSTEM_SABOTAGE_CHANCE");
    INFILTRATOR_SIGHT_RANGE = getFloat(difficultyData,"INFILTRATOR_SIGHT_RANGE");
    INFILTRATOR_PROJECTILE_SPEED = getFloat(difficultyData,"INFILTRATOR_PROJECTILE_SPEED");
    MAX_INFILTRATORS = difficultyData.getInt("MAX_INFILTRATORS");
    INFILTRATOR_FIRING_INTERVAL = getFloat(difficultyData,"INFILTRATOR_FIRING_INTERVAL");
    INFILTRATOR_PROJECTILE_DAMAGE = getFloat(difficultyData,"INFILTRATOR_PROJECTILE_DAMAGE");
    MAX_INFILTRATORS_IN_GAME = difficultyData.getInt("MAX_INFILTRATORS_IN_GAME");
    JSONArray temp = (JSONArray) difficultyData.get("NPC_SPEED_VARIANCE");
    for (int i=0; i < temp.length(); i++) {
      NPC_SPEED_VARIANCE[i] = (float) temp.getDouble(i);
    }
    NPC_FLEE_TIME = getFloat(difficultyData,"NPC_FLEE_TIME");
    NPC_FLEE_MULTIPLIER = getFloat(difficultyData,"NPC_FLEE_MULTIPLIER");
    NPC_MIN_FLEE_DISTANCE = getFloat(difficultyData,"NPC_MIN_FLEE_DISTANCE");
    NPC_EAR_STRENGTH = getFloat(difficultyData,"NPC_EAR_STRENGTH");
    NPC_COUNT = difficultyData.getInt("NPC_COUNT");
    PROJECTILE_SLOW_MULT = getFloat(difficultyData,"PROJECTILE_SLOW_MULT");
    POWERUP_HEALTH_AMOUNT = getFloat(difficultyData,"POWERUP_HEALTH_AMOUNT");
    POWERUP_SHIELD_AMOUNT = difficultyData.getInt("POWERUP_SHIELD_AMOUNT");
    POWERUP_SPEED_MULT = getFloat(difficultyData,"POWERUP_SPEED_MULT");
    POWERUP_BOOM_RANGE = getFloat(difficultyData,"POWERUP_BOOM_RANGE");
  }
//</changed>
  /** Number of infiltrators added, including defeated ones. */
  public int infiltratorsAddedCount = 0;

  private List<GameEntity> entities = new ArrayList<>();
  public List<GameEntity> newEntities = new ArrayList<>();
  public List<GameEntity> oldEntities = new ArrayList<>();

  public OrthographicCamera camera = new OrthographicCamera();

  public static final TiledMap map = new TmxMapLoader().load("map.tmx");
  public static final TiledMapTileSet tileset = map.getTileSets().getTileSet(0);
  public TextureAtlas atlas;

  public OrthogonalTiledMapRenderer renderer = new OrthogonalTiledMapRenderer(map);

  public ArrayList<RectangleMapObject> systems;
  public RectangleMapObject medbay;
  public ArrayList<float[]> spawnLocations;

  public final Random randomNumberGenerator = new Random();

  // ------------------NAVIGATION----------------
  public final NavigationMesh navigationMesh = new NavigationMesh(
      (TiledMapTileLayer) map.getLayers().get("navigation_layer")
      );
  public ArrayList<float[]> fleePoints;

  /** Coordinates for the bottom left and top right tiles of the brig. */
  public static final float[][] BRIG_BOUNDS = {{240f, 608f}, {352f, 640f}};
  /** Coordinates for the medbay teleporter. */
  public static final float[] MEDBAY_COORDINATES = {96f, 640f};

  // --------------------AUBER-------------------
  public float auberTeleporterCharge = 0f;
  /** The rate at which the teleporter ray charges. */
  //<changed removed "final" from difficulty effected values + removed values>
  public static float AUBER_CHARGE_RATE;
  /** The time the ray should visibly render for. */
  public static final float AUBER_RAY_TIME = 0.25f;
  /** The time a debuff should last for (with the exception of blindness). */
  public static float AUBER_DEBUFF_TIME;
  //<changed added>
  /** The time a buff should last for (with the exception of shield and health). */
  public static float AUBER_BUFF_TIME;
  //</changed>
  /** The rate at which auber should heal. */
  public static float AUBER_HEAL_RATE;
  //</changed>
  public static final Color rayColorA = new Color(0.106f, 0.71f, 0.714f, 1f);
  public static final Color rayColorB = new Color(0.212f, 1f, 1f, 0.7f);

  // ------------------RENDERING-----------------
  /** IDs of layers that should be rendered behind entities. */
  public final int[] backgroundLayersIds = {
    map.getLayers().getIndex("background_layer"),
    };

  /** IDs of layers that should be rendered infront of entities. */
  public final int[] foregroundLayersIds = {
    map.getLayers().getIndex("foreground_layer"),
    map.getLayers().getIndex("collision_layer")
    };


  /** An enum containing information about all dynamic/frequently accessed tiles. */
  public static enum Tiles {
    WALL_SYSTEM(38),
    STANDALONE_SYSTEM(62),
    STANDALONE_SYSTEM_LIGHT(50),

    WALL_SYSTEM_ATTACKED(40),
    STANDALONE_SYSTEM_ATTACKED(64),
    STANDALONE_SYSTEM_LIGHT_ATTACKED(52),

    WALL_SYSTEM_DESTROYED(42),
    STANDALONE_SYSTEM_DESTROYED(66),
    STANDALONE_SYSTEM_LIGHT_DESTROYED(54),

    // Pathfinding tiles
    FLEE_POINT(57);



    public final int tileId;

    Tiles(int tileId) {
      this.tileId = tileId;
    }

    /**
     * Return a cell object for a given tile type via its tileId.
     *
     * @return A cell object for the given tile ID
     * */
    public Cell getCell() {
      Cell output = new Cell();
      output.setTile(tileset.getTile(tileId));
      return output;
    }

    /**
     * Return a tile from this enum by a given tile ID.
     *
     * @param id The ID of the {@link Tiles} object to return
     * @return A {@link Tiles} object of the given ID
     * @throws IllegalArgumentException Thrown if no tile could be found with the given ID
     * */
    public static Tiles getTileById(int id) {
      for (Tiles tile : Tiles.values()) {
        if (tile.tileId == id) {
          return tile;
        }
      }
      throw new IllegalArgumentException("Tile of given ID not found.");
    }
  }
//<changed removed "final" from difficulty effected values> + removed values
  /** The amount of time it takes for an infiltrator to sabotage a system. */
  public static float SYSTEM_BREAK_TIME;
  /** The chance an infiltrator will sabotage after pathfinding to a system. */
  public static float SYSTEM_SABOTAGE_CHANCE;
  /** The distance the infiltrator can see. Default: 5 tiles */
  public static float INFILTRATOR_SIGHT_RANGE;
  /** The speed at which infiltrator projectiles should travel. */
  public static float INFILTRATOR_PROJECTILE_SPEED;
  /** Maximum infiltrators in a full game of Auber (including defated ones). */
  public static int MAX_INFILTRATORS;
  /** The interval at which the infiltrator should attack the player when exposed. */
  public static float INFILTRATOR_FIRING_INTERVAL;
  /** The damage a projectile should do. */
  public static float INFILTRATOR_PROJECTILE_DAMAGE;
  /**
   * Max infiltrators alive at a given point, Should always be greater or equal to
   * {@link World#MAX_INFILTRATORS}.
   * */
  public static int MAX_INFILTRATORS_IN_GAME;

  /** The amount of variance there should be between the speeds of different NPCs. */
  public static float[] NPC_SPEED_VARIANCE = {0.8f, 1.2f};//kept to be writen over
  /** The maximum amount of time (in seconds) an NPC should flee for. */
  public static float NPC_FLEE_TIME;
  /** The speed multiplier an NPC should receive when fleeing. */
  public static float NPC_FLEE_MULTIPLIER;
  /** The shortest distance an NPC should move from its current position when fleeing. */
  public static float NPC_MIN_FLEE_DISTANCE;
  /** The distance an NPC can here the teleporter ray shoot from. */
  public static float NPC_EAR_STRENGTH;
  /** The number of NPCs in the game. */
  public static int NPC_COUNT;

  /** the speed multiplier from being slowed */
  public static float PROJECTILE_SLOW_MULT;

  //<changed>
  /** The health gain from a health powerup */
  public static float POWERUP_HEALTH_AMOUNT;
  /** The shield gain from a shield powerup */
  public static int POWERUP_SHIELD_AMOUNT;
  /** The speed multiplier from a speed powerup */
  public static float POWERUP_SPEED_MULT;
  /** */
  public static float POWERUP_BOOM_RANGE;
  //</changed>
  //</changed>
  public static enum SystemStates {
    WORKING,
    ATTACKED,
    DESTROYED
  }

  /**
   * Initialise the game world.
   *
   * @param game The game object.
   * */
  public World(AuberGame game) {
    this.game = game;
    atlas = game.atlas;

    // Configure the camera
    camera.setToOrtho(false, 480, 270);
    camera.update();

    Player player = new Player(64f, 64f, this);
    queueEntityAdd(player);
    this.player = player;

    systems = new ArrayList<>();
    MapObjects objects = map.getLayers().get("object_layer").getObjects();
    for (MapObject object : objects) {
      if (object instanceof RectangleMapObject) {
        RectangleMapObject rectangularObject = (RectangleMapObject) object;
        switch (rectangularObject.getProperties().get("type", String.class)) {
          case "system":
            systems.add(rectangularObject);
            break;
          case "medbay":
            medbay = rectangularObject;
            break;
          default:
            break;
        }
      }
    }
    spawnLocations = new ArrayList<>();
    fleePoints = new ArrayList<>();
    TiledMapTileLayer navigationLayer = (TiledMapTileLayer) map.getLayers().get("navigation_layer");
    for (int y = 0; y < navigationLayer.getHeight(); y++) {
      for (int x = 0; x < navigationLayer.getWidth(); x++) {
        Cell currentCell = navigationLayer.getCell(x, y);
        float[] cellCoordinates = {x * navigationLayer.getTileWidth(),
                                   y * navigationLayer.getTileHeight()};
        if (currentCell != null) {
          spawnLocations.add(cellCoordinates);
          if (currentCell.getTile().getId() == Tiles.FLEE_POINT.tileId) {
            fleePoints.add(cellCoordinates);
          }
        }
      }
    }
  }

  /**
   * Initialise an instance of the world with the given game object.
   * Demo mode locks the player to the center of the screen, makes them invisible and expands the
   * camera to view the whole map.
   *
   * @param game The game object
   * @param demoMode Whether to run the game in demo mode
   * */
  public World(AuberGame game, boolean demoMode) {
    this(game);
    this.demoMode = demoMode;
    if (demoMode) {
      camera.setToOrtho(false, 1920, 1080);
      TiledMapTileLayer layer = ((TiledMapTileLayer) map.getLayers().get(2));
      player.position.x = (layer.getWidth() * layer.getTileWidth()) / 2;
      player.position.y = (layer.getHeight() * layer.getTileHeight()) / 2;
      player.sprite.setColor(1f, 1f, 1f, 0f);
    }
  }

  public void addEntity(GameEntity entity) {
    entities.add(entity);
  }

  public List<GameEntity> getEntities() {
    return entities;
  }

  /**
   * Queue an entity to be added.
   *
   * @param entity The entity to queue
   * */
  public void queueEntityAdd(GameEntity entity) {
    newEntities.add(entity);
  }

  /**
   * Queue an entity to be removed.
   *
   * @param entity The entity to queue
   * */
  public void queueEntityRemove(GameEntity entity) {
    oldEntities.add(entity);
  }

  /**
   * Apply any queued entity removals/additions to the world.
   * */
  public void updateEntities() {
    entities.addAll(newEntities);
    entities.removeAll(oldEntities);
    newEntities.clear();
    oldEntities.clear();
  }

  /**
   * Update the sprite of a system to match a new state.
   *
   * @param x The x coordinate of the system object (not the tile)
   * @param y The y coordinate of the system object (not the tile)
   * @param newState The new state of the system
   **/
  public void updateSystemState(float x, float y, SystemStates newState) {
    TiledMapTileLayer collisionLayer = (TiledMapTileLayer) World.map.getLayers()
        .get("collision_layer");

    int[] systemPosition = {
      (int) x / collisionLayer.getTileWidth(),
      (int) (y / collisionLayer.getTileHeight()) + 1
      };

    Cell attackedSystemCell = collisionLayer.getCell(systemPosition[0], systemPosition[1]);
    int targetTileId = attackedSystemCell.getTile().getId();

    if (targetTileId == World.Tiles.STANDALONE_SYSTEM.tileId
        || targetTileId == World.Tiles.STANDALONE_SYSTEM_ATTACKED.tileId
        || targetTileId == World.Tiles.STANDALONE_SYSTEM_DESTROYED.tileId) {
      Cell newSystem;
      Cell newSystemLight;

      switch (newState) {
        case WORKING:
          newSystem = World.Tiles.STANDALONE_SYSTEM.getCell();
          newSystemLight = World.Tiles.STANDALONE_SYSTEM_LIGHT.getCell();
          break;
        case ATTACKED:
          newSystem = World.Tiles.STANDALONE_SYSTEM_ATTACKED.getCell();
          newSystemLight = World.Tiles.STANDALONE_SYSTEM_LIGHT_ATTACKED.getCell();
          break;
        case DESTROYED:
          newSystem = World.Tiles.STANDALONE_SYSTEM_DESTROYED.getCell();
          newSystemLight = World.Tiles.STANDALONE_SYSTEM_LIGHT_DESTROYED.getCell();
          break;
        default:
          // This line will never fire, However it needs to be here otherwise Java
          // will think that theres a chance newSystem and newSystemLight may not have been
          // initiated even though every enum state is covered
          return;
      }
      collisionLayer.setCell(systemPosition[0], systemPosition[1], newSystem);

      TiledMapTileLayer foregroundLayer = (TiledMapTileLayer) map.getLayers()
          .get("foreground_layer");
      foregroundLayer.setCell(systemPosition[0], systemPosition[1] + 1, newSystemLight);
    } else {
      Cell newSystem;
      switch (newState) {
        case WORKING:
          newSystem = World.Tiles.WALL_SYSTEM.getCell();
          break;
        case ATTACKED:
          newSystem = World.Tiles.WALL_SYSTEM_ATTACKED.getCell();
          break;
        case DESTROYED:
          newSystem = World.Tiles.WALL_SYSTEM_DESTROYED.getCell();
          break;
        default:
          // See above
          return;
      }
      collisionLayer.setCell(systemPosition[0], systemPosition[1], newSystem);
    }

    if (newState == SystemStates.DESTROYED) {
      for (RectangleMapObject system : systems) {
        if (system.getRectangle().getX() == x
            && system.getRectangle().getY() == y) {
          systems.remove(system);
          break;
        }
      }
    }
  }

  /**
   * Return the state of a system, given the coordinates of the system object (not the tile).
   *
   * @param x The x coordinate to check
   * @param y The y coordinate to check
   * @return A {@link SystemStates} representing the state of the system
   * @throws IllegalArgumentException if a standalone system light is at the coordinates provided
   * */
  public SystemStates getSystemState(float x, float y) {
    TiledMapTileLayer collisionLayer = (TiledMapTileLayer) World.map.getLayers()
        .get("collision_layer");

    int[] systemPosition = {
      (int) x / collisionLayer.getTileWidth(),
      (int) (y / collisionLayer.getTileHeight()) + 1
      };

    Cell attackedSystemCell = collisionLayer.getCell(systemPosition[0], systemPosition[1]);
    if (attackedSystemCell == null) {
      throw new IllegalArgumentException("No tile at given coordinates");
    }

    Tiles targetTile = Tiles.getTileById(attackedSystemCell.getTile().getId());

    switch (targetTile) {
      case WALL_SYSTEM:
      case STANDALONE_SYSTEM:
        return SystemStates.WORKING;

      case WALL_SYSTEM_ATTACKED:
      case STANDALONE_SYSTEM_ATTACKED:
        return SystemStates.ATTACKED;

      case WALL_SYSTEM_DESTROYED:
      case STANDALONE_SYSTEM_DESTROYED:
        return SystemStates.DESTROYED;

      default:
        throw new IllegalArgumentException("Use the coordinates of the System object on the"
                                           .concat("tilemap - not the system tile."));
    }
  }

  public SystemStates getSystemState(RectangleMapObject system) {
    return getSystemState(system.getRectangle().x, system.getRectangle().y);
  }

  /**
   * Check to see if any of the end conditions have been met, if so update the screen.
   * */
  public void checkForEndState() {
    if (systems.isEmpty()) {
      if (!demoMode) {
        game.setScreen(new GameOverScreen(game, false));
      } else {
        game.setScreen(new GameScreen(game, true));
      }
    } else if (infiltratorCount <= 0) {
      game.setScreen(new GameOverScreen(game, true));
    }
  }
  //<changed>
  public JSONObject toJSON(){
    JSONObject world = new JSONObject();
    for (String difficulty : gameData.keySet()) {
      if (gameData.get(difficulty).equals(difficultyData)){
        world.put("difficulty",difficulty);
        break;
      }
    }
    world.put("player",player.toJSON(this));
    world.put("auberTeleporterCharge",auberTeleporterCharge);
    world.put("infiltratorCount",infiltratorCount);
    world.put("infiltratorsAddedCount",infiltratorsAddedCount);
    world.put("demoMode",demoMode);
    JSONArray systems = new JSONArray();
    for (RectangleMapObject _system : this.systems) {
      JSONObject system = new JSONObject();
      system.put("x",_system.getRectangle().getX());
      system.put("y",_system.getRectangle().getY());
      system.put("state",getSystemState(_system).name());
      systems.put(system);
    }
    world.put("systems",systems);
    return world;
  }
  /**
   * Creates a world from a given state
   * 
   * @param game    The game object
   * @param world    the JSONObject
   */
  public World(AuberGame game,JSONObject world){
    this.game = game;
    atlas = game.atlas;

    // Configure the camera
    camera.setToOrtho(false, 480, 270);
    camera.update();
    //stored info
    changeDifficulty(world.getString("difficulty"));
    this.player = new Player(world.getJSONObject("player"),this);
    queueEntityAdd(player);
    auberTeleporterCharge = world.getFloat("auberTeleporterCharge");
    infiltratorCount = world.getInt("infiltratorCount");
    infiltratorsAddedCount = world.getInt("infiltratorsAddedCount");
    //systems
    systems = new ArrayList<>();
    MapObjects objects = map.getLayers().get("object_layer").getObjects();
    for (MapObject object : objects) {
      if (object instanceof RectangleMapObject) {
        RectangleMapObject rectangularObject = (RectangleMapObject) object;
        switch (rectangularObject.getProperties().get("type", String.class)) {
          case "system":
            systems.add(rectangularObject);
            break;
          case "medbay":
            medbay = rectangularObject;
            break;
          default:
            break;
        }
      }
    }
    JSONArray jsystems = world.getJSONArray("systems");
    ArrayList<Vector2> newVs = new ArrayList();
    ArrayList<SystemStates> newStates = new ArrayList();
    for (int i = 0; i < jsystems.length(); i++) {
      JSONObject jsystem = jsystems.getJSONObject(i);
      newVs.add(new Vector2(jsystem.getFloat("x"),jsystem.getFloat("y")));
      newStates.add(SystemStates.valueOf(jsystem.getString("state")));
    }
    for (int i = systems.size() -1; i >= 0; i -= 1) {
      Vector2 oldV = new Vector2(systems.get(i).getRectangle().getX(),systems.get(i).getRectangle().getY());
      if (newVs.contains(oldV)){
        updateSystemState(oldV.x, oldV.y, newStates.get(newVs.indexOf(oldV)));
      }else{
        updateSystemState(oldV.x, oldV.y, SystemStates.DESTROYED);
      }
    }
    for (RectangleMapObject system : systems) {
      Vector2 oldV = new Vector2(system.getRectangle().getX(),system.getRectangle().getY());
      if (newVs.contains(oldV)){
        updateSystemState(oldV.x, oldV.y, newStates.get(newVs.indexOf(oldV)));
      }else{
        updateSystemState(oldV.x, oldV.y, SystemStates.DESTROYED);
      }
    }
    //navigation
    spawnLocations = new ArrayList<>();
    fleePoints = new ArrayList<>();
    TiledMapTileLayer navigationLayer = (TiledMapTileLayer) map.getLayers().get("navigation_layer");
    for (int y = 0; y < navigationLayer.getHeight(); y++) {
      for (int x = 0; x < navigationLayer.getWidth(); x++) {
        Cell currentCell = navigationLayer.getCell(x, y);
        float[] cellCoordinates = {x * navigationLayer.getTileWidth(),
                                   y * navigationLayer.getTileHeight()};
        if (currentCell != null) {
          spawnLocations.add(cellCoordinates);
          if (currentCell.getTile().getId() == Tiles.FLEE_POINT.tileId) {
            fleePoints.add(cellCoordinates);
          }
        }
      }
    }
    //demomode
    this.demoMode = world.getBoolean("demoMode");
    if (demoMode) {
      camera.setToOrtho(false, 1920, 1080);
      player.sprite.setColor(1f, 1f, 1f, 0f);
    }
  }
  //</changed>
}