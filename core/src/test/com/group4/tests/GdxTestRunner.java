package com.group4.tests;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.graphics.GL30;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;

/**
 * Run the unit tests as a LibGDX headless application. Add @RunWith(GdxTestRunner.class) just before the declaration of test class.
 *
 * Adjusted from https://github.com/TomGrill/gdx-testing by Robert Watts
 *
 * @see <a href="https://github.com/TomGrill/gdx-testing">https://github.com/TomGrill/gdx-testing</a>
 */
public class GdxTestRunner extends BlockJUnit4ClassRunner implements ApplicationListener {

    private Map<FrameworkMethod, RunNotifier> invokeInRender = new HashMap<FrameworkMethod, RunNotifier>();

    public GdxTestRunner(Class<?> klass) throws InitializationError {
        super(klass);


        HeadlessApplicationConfiguration conf = new HeadlessApplicationConfiguration();
        new HeadlessApplication(this, conf);

        Gdx.gl30 = mock(GL30.class);
        Gdx.gl20 = mock(GL20.class);
        Gdx.gl = mock(GL20.class);

        Gdx.graphics = mock(Graphics.class);
        when(Gdx.graphics.getWidth()).thenReturn(1000);
        when(Gdx.graphics.getHeight()).thenReturn(1000);

    }

    @Override
    public void create() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void render() {
        synchronized (invokeInRender) {
            for (Map.Entry<FrameworkMethod, RunNotifier> each : invokeInRender.entrySet()) {
                super.runChild(each.getKey(), each.getValue());
            }
            invokeInRender.clear();
        }
    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void pause() {
    }

    @Override
    public void dispose() {
    }

    @Override
    protected void runChild(FrameworkMethod method, RunNotifier notifier) {
        synchronized (invokeInRender) {
            // add for invoking in render phase, where gl context is available
            invokeInRender.put(method, notifier);
        }
        // wait until that test was invoked
        waitUntilInvokedInRenderMethod();
    }

    /**
     *
     */
    private void waitUntilInvokedInRenderMethod() {
        try {
            while (true) {
                Thread.sleep(10);
                synchronized (invokeInRender) {
                    if (invokeInRender.isEmpty())
                        break;
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
