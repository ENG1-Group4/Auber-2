package com.threecubed.auber.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.threecubed.auber.AuberGame;

public class DesktopLauncher {
  public static void main(String[] arg) {
    LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
    config.title = "Auber";
    //<changed>
    config.width = 1920;
    config.height = 1080;
    config.fullscreen = true;
    config.forceExit = false;
    //</changed>
    new LwjglApplication(new AuberGame(), config);
  }
}
