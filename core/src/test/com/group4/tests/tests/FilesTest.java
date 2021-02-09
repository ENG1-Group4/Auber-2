package com.group4.tests.tests;

import com.badlogic.gdx.Gdx;
import com.group4.tests.GdxTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

/**
 * Test that all the the files are in the file structure
 *
 * @author Robert Watts
 * @version 1.0
 * @since 2.0
 * */
@RunWith(GdxTestRunner.class)
public class FilesTest {

    static final String[] mapAssets = { "auber.atlas", "auber.png",  "map.tmx",
            "tileset.png", "tileset.tsx"};

    static final String[] audioAssets = {
            "ambience.mp3",
            "footstep.mp3",
            "gameOver.wav",
            "gameWin.wav",
            "infiltratorHurt.mp3",
            "laserCharge.mp3",
            "laserShot.mp3",
            "menuMusic.mp3",
            "menuSelect.ogg",
            "playerHurt.mp3",
            "powerupBoom.wav",
            "powerupPing.mp3",
            "systemDestroyed.wav",
            "systemError.wav",
            "teleporter.wav"
    };

    static final String[] spriteAssets = {
            "alienA.png",
            "alienB.png",
            "alienC.png",
            "arrow.png",
            "arrow2.png",
            "auber_logo.png",
            "demoButton.png",
            "diffButton.png",
            "diffEasyButton.png",
            "diffHardButton.png",
            "diffNormalButton.png",
            "infiltrator.png",
            "instructions.png",
            "loadButton.png",
            "playButton.png",
            "player.png",
            "powerUp.png",
            "powerUpBoom.png",
            "powerUpHeal.png",
            "powerUpInvinc.png",
            "powerUpShield.png",
            "powerUpSpeed.png",
            "projectile.png",
            "stars.png",
            "telesplosion.png",
    };


    @Test
    public void test_all_map_assets_exist(){
        for(int i = 0; i< mapAssets.length; i++){
            AssertAssetExists(mapAssets[i]);
        }
    }

    @Test
    public void test_all_audio_assets_exist(){
        for(int i = 0; i< audioAssets.length; i++){
            AssertAssetExists("audio/" + audioAssets[i]);
        }
    }

    @Test
    public void test_all_sprites_exist(){
        for(int i = 0; i< spriteAssets.length; i++){
            AssertAssetExists("individual_sprites/" + spriteAssets[i]);
        }
    }

    private void AssertAssetExists(String assetRelativePath){
        assertTrue("Could not find asset: " + assetRelativePath, Gdx.files
                .internal(assetRelativePath).exists());
    }

}