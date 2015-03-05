import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferStrategy;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

/**
 * @author Matt DesLauriers, mattdesl
 */
public class LightingTest {

	public static void main(final String[] args) {
		new LightingTest().run();
	}

	/** The size of the game canvas, initially 640x480. */
	private static int width = 640, height = 480;

	/** Returns the width of the game canvas. */
	public static int getWidth() {
		return Math.max(0, width);
	}

	/** Returns the height of the game canvas. */
	public static int getHeight() {
		return Math.max(0, height);
	}

	/** The buffer strategy used for smooth active rendering. */
	protected BufferStrategy strategy;

	/** True if the game loop is running. */
	protected boolean running;

	/** The current frames per second, used for debugging performance. */
	protected int fps = 60;

	/** Timer used for adding entities every N seconds. */
	private float tickTimer;

	/** A list of entities to render. */
	protected List<Polygon> entities = new ArrayList<>();
	protected List<SmoothLight> lights = new ArrayList<>();

	/** The mouse position */
	protected int mouseX, mouseY;

	/** The frame for our GUI. */
	protected JFrame frame = new JFrame("Shooter Game");

	/**
	 * Constructs a new Game object; run() should be called to start the rendering.
	 */
	public LightingTest() {
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// set up our Canvas which will be used for active rendering (game loop)
		final Canvas canvas = new Canvas();
		canvas.setIgnoreRepaint(true);
		canvas.setPreferredSize(new Dimension(width, height));
		canvas.addComponentListener(new ComponentAdapter() { // on resize...
			@Override
			public void componentResized(final ComponentEvent arg0) {
				width = canvas.getWidth();
				height = canvas.getHeight();
			}
		});

		// set background as per assignment requirement
		canvas.setBackground(Color.darkGray);

		// listen to mouse events
		canvas.addMouseMotionListener(new MouseMoveListener());

		frame.add(canvas, BorderLayout.CENTER);

		// pack to proper size
		frame.pack();

		// center frame on user's screen
		frame.setLocationRelativeTo(null);

		// show the frame
		frame.setVisible(true);

		// set up a buffer strategy used for smooth rendering
		// needs to be called after JFrame is valid and visible
		canvas.createBufferStrategy(2);
		strategy = canvas.getBufferStrategy();
	}

	/** Stops the game loop. */
	public void stop() {
		running = false;
	}

	/** Called to initialize the game loop. */
	public void run() {
		init();

		int frames = 0;
		long lastTime, lastSec;
		lastTime = lastSec = System.nanoTime();
		running = true;
		while (running) {
			final long deltaTime = System.nanoTime() - lastTime;
			lastTime += deltaTime;

			// update the game by a little
			update(deltaTime / 1e9);

			// pretty standard buffer strategy game loop
			do {
				do {
					final Graphics2D g = (Graphics2D) strategy.getDrawGraphics();
					// clear screen
					g.setColor(Color.white);
					g.clearRect(0, 0, width, height);
					render(g);
					g.dispose();
				} while (strategy.contentsRestored());
				strategy.show();
			} while (strategy.contentsLost());

			// count FPS
			frames++;
			if (System.nanoTime() - lastSec >= 1e9) {
				fps = frames;
				frames = 0;
				lastSec += 1e9;
			}

			// sync frame rate to 60 FPS
			do {
				Thread.yield();
			} while (System.nanoTime() - lastTime < 1e9 / 60);
		}
	}

	/** Called on first run to initialize the game and any resources. */
	protected void init() {
		// add the first entity
		entities.add(new Polygon(new int[] { 125, 145, 145, 125 }, new int[] { 225, 225, 245, 245 }, 4));
		entities.add(new Polygon(new int[] { 225, 245, 245, 225 }, new int[] { 245, 245, 275, 285 }, 4));
		lights.add(new SmoothLight(new Light(new Color(255, 200, 0, 200), new Vec2D(0, 0), 200), 5, 5, 5, 50));
		lights.add(new SmoothLight(new Light(new Color(0, 255, 255, 200), new Vec2D(200, 200), 200), 5, 10, 5, 50));
	}

	/** Updates the game's entities. */
	protected void update(final double deltaTime) {
		lights.get(0).setPosition(mouseX, mouseY);
	}

	/** Called to render the frame. */
	protected void render(final Graphics2D g) {
		// we'll use nice quality interpolation
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		g.setColor(Color.WHITE);
		g.drawString("FPS: " + fps, 10, 20);
		// render the shadows first
		for (final SmoothLight l : lights) {
			l.draw(g, entities);
		}

		// render each entity
		for (int i = 0; i < entities.size(); i++) {
			final Shape e = entities.get(i);
			g.setColor(Color.WHITE);
			g.fill(e);
		}
	}

	/** Mouse motion listener for dynamic 2D shadows. */
	private class MouseMoveListener extends MouseMotionAdapter {

		@Override
		public void mouseMoved(final MouseEvent e) {
			mouseX = e.getX();
			mouseY = e.getY();
		}

		@Override
		public void mouseDragged(final MouseEvent e) {
			mouseX = e.getX();
			mouseY = e.getY();
		}
	}

}