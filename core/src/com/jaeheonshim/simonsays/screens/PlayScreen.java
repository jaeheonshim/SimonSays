package com.jaeheonshim.simonsays.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.jaeheonshim.simonsays.SimonSays;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import static com.jaeheonshim.simonsays.SimonSays.GAME_HEIGHT;
import static com.jaeheonshim.simonsays.SimonSays.GAME_WIDTH;

public class PlayScreen implements Screen {
    private Viewport viewport;
    private ShapeRenderer renderer;

    private final float BUTTON_MARGIN = 10;
    final float BUTTON_WIDTH = (GAME_WIDTH - 3 * BUTTON_MARGIN) / 2;
    final float BUTTON_HEIGHT = (GAME_HEIGHT - 200) / 2;
    final float PLAY_HIGHLIGHT_LENGTH = 0.7f;
    final float TOUCH_HIGHLIGHT_LENGTH = 0.3f;
    final float SEQUENCE_START_DELAY = 3;
    final float SEQUENCE_BETWEEN_DELAY = 0.1f;

    private Random random;

    private ArrayList<GameColors> sequence;
    private GameColors highlightedColor;
    private float blockPlayTimer = TOUCH_HIGHLIGHT_LENGTH;
    private float highlightTimer = TOUCH_HIGHLIGHT_LENGTH;
    private float sequencePlayDelayTimer = SEQUENCE_START_DELAY;
    private float sequenceBetweenDelayTimer = SEQUENCE_BETWEEN_DELAY;

    private boolean playingSequence = false;
    private boolean checkingSequence = false;
    private boolean isDelayingNextSequence = false;
    private boolean isDelayingBetweenSequence = false;
    private boolean buttonPressed;
    private int sequenceIndex;
    private int checkSequenceIndex;

    private final Color DARKRED = Color.valueOf("6b0902");
    private final Color BRIGHTRED = Color.valueOf("ff1100");
    private final Color DARKBLUE = Color.valueOf("001275");
    private final Color BRIGHTBLUE = Color.valueOf("0027ff");
    private final Color DARKGREEN = Color.valueOf("096e00");
    private final Color BRIGHTGREEN = Color.valueOf("15ff00");
    private final Color DARKYELLOW = Color.valueOf("9da800");
    private final Color BRIGHTYELLOW = Color.valueOf("eeff00");

    enum GameColors {
        YELLOW, BLUE, GREEN, RED
    }

    public PlayScreen() {
        viewport = new FitViewport(SimonSays.GAME_WIDTH, SimonSays.GAME_HEIGHT);
        renderer = new ShapeRenderer();

        sequence = new ArrayList<>(Arrays.asList(GameColors.YELLOW, GameColors.BLUE, GameColors.YELLOW, GameColors.BLUE, GameColors.YELLOW, GameColors.BLUE));

        random = new Random();
    }

    @Override
    public void show() {
        playSequence(0);
    }

    private GameColors playSequence(float delta) {
        if (!playingSequence) {
            playingSequence = true;
            sequenceIndex = 0;
            blockPlayTimer = PLAY_HIGHLIGHT_LENGTH;
        }

        if(isDelayingBetweenSequence) {
            sequenceBetweenDelayTimer -= delta;
            if(sequenceBetweenDelayTimer <= 0) {
                isDelayingBetweenSequence = false;
            } else {
                return null;
            }
        }

        blockPlayTimer -= delta;
        if (blockPlayTimer <= 0) {
            sequenceIndex++;
            if (sequenceIndex == sequence.size()) {
                playingSequence = false;
                return null;
            }
            blockPlayTimer = PLAY_HIGHLIGHT_LENGTH;
            sequenceBetweenDelayTimer = SEQUENCE_BETWEEN_DELAY;
            isDelayingBetweenSequence = true;
            return null;
        }
        return sequence.get(sequenceIndex);
    }

    private boolean checkSequence(GameColors color) {
        if (!checkingSequence) {
            checkSequenceIndex = 0;
            checkingSequence = true;
        }

        return sequence.get(checkSequenceIndex) == color;
    }

    private void addColorToSequence() {
        int item = random.nextInt(GameColors.values().length);
        sequence.add(GameColors.values()[item]);
    }

    private GameColors getTouchedColor(Vector3 worldCoords) {
        Vector2 point = new Vector2(worldCoords.x, worldCoords.y);
        if (point.x > BUTTON_MARGIN && point.x < BUTTON_MARGIN + BUTTON_WIDTH) {
            // point touched is somewhere in the first column of buttons
            if (point.y > BUTTON_MARGIN && point.y < BUTTON_MARGIN + BUTTON_HEIGHT) {
                return GameColors.YELLOW;
            } else if (point.y > BUTTON_MARGIN + BUTTON_HEIGHT && point.y < BUTTON_HEIGHT * 2 + BUTTON_MARGIN * 2) {
                return GameColors.GREEN;
            }
        } else if (point.x > BUTTON_MARGIN * 2 + BUTTON_WIDTH && point.x < BUTTON_WIDTH * 2 + BUTTON_WIDTH * 2) {
            // point touched is somewhere in the second column of buttons
            if (point.y > BUTTON_MARGIN && point.y < BUTTON_MARGIN + BUTTON_HEIGHT) {
                return GameColors.BLUE;
            } else if (point.y > BUTTON_MARGIN + BUTTON_HEIGHT && point.y < BUTTON_HEIGHT * 2 + BUTTON_MARGIN * 2) {
                return GameColors.RED;
            }
        }
        return null;
    }

    public void update(float delta) {
        if (playingSequence) {
            highlightedColor = playSequence(delta);
            return;
        } else {
            checkingSequence = true;
        }

        if(isDelayingNextSequence) {
            sequencePlayDelayTimer -= delta;
            if(sequencePlayDelayTimer <= 0) {
                addColorToSequence();
                playSequence(0);
                isDelayingNextSequence = false;
            } else {
                return;
            }
        }

        if(checkingSequence) {
            if(!Gdx.input.isTouched()) {
                buttonPressed = false;
            }
            if(Gdx.input.isTouched() && !buttonPressed) {
                buttonPressed = true;
                highlightedColor = getTouchedColor(viewport.getCamera().unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0)));
                if(checkSequence(highlightedColor)) {
                    if(checkSequenceIndex + 1 < sequence.size()) {
                        checkSequenceIndex++;
                    } else {
                        checkingSequence = false;
                        isDelayingNextSequence = true;
                        sequencePlayDelayTimer = SEQUENCE_START_DELAY;
                        sequenceIndex = 0;
                        checkSequenceIndex = 0;
                        System.out.println("Going again");
                    }
                } else {
                    System.out.println("incorrect");
                    // TODO handle losing
                }
            } else {
                highlightTimer -= delta;
                if(highlightTimer <= 0) {
                    highlightTimer = TOUCH_HIGHLIGHT_LENGTH;
                    highlightedColor = null;
                }
            }
        }
    }

    @Override
    public void render(float delta) {
        update(delta);
        renderer.setProjectionMatrix(viewport.getCamera().combined);
        renderer.begin(ShapeRenderer.ShapeType.Filled);

        if (highlightedColor == GameColors.YELLOW) {
            renderer.setColor(BRIGHTYELLOW);
        } else {
            renderer.setColor(DARKYELLOW);
        }
        renderer.rect(BUTTON_MARGIN, BUTTON_MARGIN, BUTTON_WIDTH, BUTTON_HEIGHT);
        if (highlightedColor == GameColors.BLUE) {
            renderer.setColor(BRIGHTBLUE);
        } else {
            renderer.setColor(DARKBLUE);
        }
        renderer.rect(2 * BUTTON_MARGIN + BUTTON_WIDTH, BUTTON_MARGIN, BUTTON_WIDTH, BUTTON_HEIGHT);
        if (highlightedColor == GameColors.GREEN) {
            renderer.setColor(BRIGHTGREEN);
        } else {
            renderer.setColor(DARKGREEN);
        }
        renderer.rect(BUTTON_MARGIN, 2 * BUTTON_MARGIN + BUTTON_HEIGHT, BUTTON_WIDTH, BUTTON_HEIGHT);
        if (highlightedColor == GameColors.RED) {
            renderer.setColor(BRIGHTRED);
        } else {
            renderer.setColor(DARKRED);
        }
        renderer.rect(2 * BUTTON_MARGIN + BUTTON_WIDTH, 2 * BUTTON_MARGIN + BUTTON_HEIGHT, BUTTON_WIDTH, BUTTON_HEIGHT);

        renderer.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {

    }
}
