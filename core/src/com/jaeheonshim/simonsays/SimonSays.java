package com.jaeheonshim.simonsays;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.jaeheonshim.simonsays.screens.CreditScreen;
import com.jaeheonshim.simonsays.screens.PlayScreen;

public class SimonSays extends Game {
	public static final int GAME_WIDTH = 480;
	public static final int GAME_HEIGHT = 900;
	SpriteBatch batch;
	Texture img;
	
	@Override
	public void create () {
		batch = new SpriteBatch();
		setScreen(new CreditScreen(batch, this));
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		super.render();
	}
	
	@Override
	public void dispose () {
		batch.dispose();
	}
}
