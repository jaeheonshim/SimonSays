package com.jaeheonshim.simonsays.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.jaeheonshim.simonsays.SimonSays;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import static com.jaeheonshim.simonsays.SimonSays.GAME_HEIGHT;
import static com.jaeheonshim.simonsays.SimonSays.GAME_WIDTH;

public class PlayScreen implements Screen {
    public static final int SCOREBOX_WIDTH = 350;
    public static final int SCOREBOX_HEIGHT = 220;
    public static final int SCOREBOX_TITLE_OFFSET = 20;
    public static final int SCOREBOX_SCORE_OFFSET = 75;
    public static final int SCOREBOX_TEXT_OFFSET = 140;
    public static final float STATUS_FONT_SCALE = 0.7f;
    private final int STATUS_TEXT_OFFSET = 95;
    private final int TITLE_TEXT_OFFSET = 32;
    private final float BUTTON_MARGIN = 10;
    private final float BUTTON_WIDTH = (GAME_WIDTH - 3 * BUTTON_MARGIN) / 2;
    private final float BUTTON_HEIGHT = (GAME_HEIGHT - 200) / 2;
    private final float PLAY_HIGHLIGHT_LENGTH = 0.7f;
    private final float TOUCH_HIGHLIGHT_LENGTH = 0.3f;
    private final float SEQUENCE_START_DELAY = 3;
    private final float SEQUENCE_BETWEEN_DELAY = 0.1f;
    private final float START_DELAY = 3;
    private final float GAMEOVER_DELAY = 0.5f; // to prevent user accidentally clicking through score screen

    private Viewport viewport;
    private ShapeRenderer renderer;
    private SpriteBatch batch;
    private SimonSays game;

    private final Sound YELLOW_SOUND = Gdx.audio.newSound(Gdx.files.internal("g_sharp.wav"));
    private final Sound BLUE_SOUND = Gdx.audio.newSound(Gdx.files.internal("c_sharp.wav"));
    private final Sound GREEN_SOUND = Gdx.audio.newSound(Gdx.files.internal("d_sharp.wav"));
    private final Sound RED_SOUND = Gdx.audio.newSound(Gdx.files.internal("f_sharp.wav"));
    private final Sound FAIL_SOUND = Gdx.audio.newSound(Gdx.files.internal("failed.mp3"));

    private BitmapFont font;

    private Random random;

    private ArrayList<GameColors> sequence;
    private GameColors highlightedColor;
    private float blockPlayTimer = TOUCH_HIGHLIGHT_LENGTH;
    private float highlightTimer = TOUCH_HIGHLIGHT_LENGTH;
    private float sequencePlayDelayTimer = SEQUENCE_START_DELAY;
    private float sequenceBetweenDelayTimer = SEQUENCE_BETWEEN_DELAY;
    private float gameoverDelayTimer = GAMEOVER_DELAY;
    private float startDelayTimer = START_DELAY;

    private boolean playingSequence = false;
    private boolean checkingSequence = false;
    private boolean isDelayingNextSequence = false;
    private boolean isDelayingBetweenSequence = false;
    private boolean soundPlayed;
    private boolean failed = false;

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
        YELLOW, BLUE, GREEN, RED;
    }

    public PlayScreen(SpriteBatch batch, SimonSays game) {
        viewport = new FitViewport(SimonSays.GAME_WIDTH, SimonSays.GAME_HEIGHT);
        renderer = new ShapeRenderer();
        this.batch = batch;
        this.game = game;

        font = new BitmapFont(Gdx.files.internal("font.fnt"));

        random = new Random();

        sequence = new ArrayList<>();
        addColorToSequence();
    }

    @Override
    public void show() {
    }

    private GameColors playSequence(float delta) {
        if (!playingSequence) {
            playingSequence = true;
            sequenceIndex = 0;
            blockPlayTimer = PLAY_HIGHLIGHT_LENGTH;
            soundPlayed = false;
        }

        if (isDelayingBetweenSequence) {
            sequenceBetweenDelayTimer -= delta;
            if (sequenceBetweenDelayTimer <= 0) {
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
            soundPlayed = false;
            return null;
        }
        if (!soundPlayed) {
            getSound(sequence.get(sequenceIndex)).play();
            soundPlayed = true;
        }
        return sequence.get(sequenceIndex);
    }

    private Sound getSound(GameColors color) {
        if (color != null) {
            switch (color) {
                case RED:
                    return RED_SOUND;
                case BLUE:
                    return BLUE_SOUND;
                case GREEN:
                    return GREEN_SOUND;
                case YELLOW:
                    return YELLOW_SOUND;
                default:
                    return null;
            }
        } else {
            return null;
        }
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
        if(!failed) {
            if(startDelayTimer > 0) {
                startDelayTimer -= delta;
                return;
            } else if(startDelayTimer != -1 && startDelayTimer < 0) {
                playSequence(0);
                startDelayTimer = -1;
            }

            if (playingSequence) {
                highlightedColor = playSequence(delta);
                return;
            } else {
                checkingSequence = true;
            }

            if (isDelayingNextSequence) {
                highlightedColor = null;
                sequencePlayDelayTimer -= delta;
                if (sequencePlayDelayTimer <= 0) {
                    addColorToSequence();
                    playSequence(0);
                    isDelayingNextSequence = false;
                } else {
                    return;
                }
            }

            if (checkingSequence) {
                if (!Gdx.input.isTouched()) {
                    buttonPressed = false;
                }
                if (Gdx.input.isTouched() && !buttonPressed) {
                    buttonPressed = true;
                    highlightedColor = getTouchedColor(viewport.getCamera().unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0)));

                    Sound buttonSound = getSound(highlightedColor);
                    if (buttonSound != null) {
                        buttonSound.play();
                    }
                    if (checkSequence(highlightedColor)) {
                        if (checkSequenceIndex + 1 < sequence.size()) {
                            checkSequenceIndex++;
                        } else {
                            checkingSequence = false;
                            isDelayingNextSequence = true;
                            sequencePlayDelayTimer = SEQUENCE_START_DELAY;
                            sequenceIndex = 0;
                            checkSequenceIndex = 0;
                        }
                    } else {
                        FAIL_SOUND.play();
                        failed = true;
                    }
                } else {
                    highlightTimer -= delta;
                    if (highlightTimer <= 0) {
                        highlightTimer = TOUCH_HIGHLIGHT_LENGTH;
                        highlightedColor = null;
                    }
                }
            }
        } else {
            if(gameoverDelayTimer > 0) {
                gameoverDelayTimer -= delta;
            } else {
                if(Gdx.input.isTouched()) {
                    this.dispose();
                    game.setScreen(new PlayScreen(batch, game));
                }
            }
        }
    }

    @Override
    public void render(float delta) {
        update(delta);

        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        font.setColor(Color.WHITE);
        font.getData().setScale(1);
        font.draw(batch, "SIMON SAYS", 0, GAME_HEIGHT - TITLE_TEXT_OFFSET, GAME_WIDTH, Align.center, false);
        font.getData().setScale(STATUS_FONT_SCALE);

        if(startDelayTimer != -1) {
            font.draw(batch, "Ready? " + Math.round(startDelayTimer), 0, GAME_HEIGHT - STATUS_TEXT_OFFSET, GAME_WIDTH, Align.center, true);
        } else if(playingSequence) {
            font.draw(batch, "Memorize the sequence", 0, GAME_HEIGHT - STATUS_TEXT_OFFSET, GAME_WIDTH, Align.center, true);
        } else if(isDelayingNextSequence) {
            font.draw(batch, "Nice work! Score: " + (sequence.size()), 0, GAME_HEIGHT - STATUS_TEXT_OFFSET, GAME_WIDTH, Align.center, true);
        } else if(checkingSequence) {
            font.draw(batch, "Repeat the sequence", 0, GAME_HEIGHT - STATUS_TEXT_OFFSET, GAME_WIDTH, Align.center, true);
        }

        batch.end();

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
        if(failed) {
            drawScoreBox(renderer);
        }
    }

    public void drawScoreBox(ShapeRenderer renderer) {
        Vector2 scoreBoxOrigin = new Vector2(GAME_WIDTH / 2 - SCOREBOX_WIDTH / 2, GAME_HEIGHT / 2 - SCOREBOX_HEIGHT / 2);
        renderer.begin(ShapeRenderer.ShapeType.Filled);
        renderer.setColor(Color.WHITE);
        renderer.rect(scoreBoxOrigin.x, scoreBoxOrigin.y, SCOREBOX_WIDTH, SCOREBOX_HEIGHT);
        renderer.end();
        batch.begin();
        font.setColor(Color.BLACK);
        font.draw(batch, "GOOD GAME!", scoreBoxOrigin.x, scoreBoxOrigin.y + SCOREBOX_HEIGHT - SCOREBOX_TITLE_OFFSET, SCOREBOX_WIDTH, Align.center, false);
        font.draw(batch, "SCORE: " + (sequence.size() - 1), scoreBoxOrigin.x + 15, scoreBoxOrigin.y + SCOREBOX_HEIGHT - SCOREBOX_SCORE_OFFSET, SCOREBOX_WIDTH, Align.left, true);
        font.draw(batch, "Tap anywhere to try again", scoreBoxOrigin.x, scoreBoxOrigin.y + SCOREBOX_HEIGHT - SCOREBOX_TEXT_OFFSET, SCOREBOX_WIDTH, Align.center, true);
        batch.end();
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
        YELLOW_SOUND.dispose();
        BLUE_SOUND.dispose();
        RED_SOUND.dispose();
        GREEN_SOUND.dispose();
        FAIL_SOUND.dispose();
    }
}
