package main;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.JFrame;

import light.Light;
import light.SmoothLight;
import utils.GraphicsUtils;
import utils.Vec2D;

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

	protected BufferedImage lightmap = GraphicsUtils.toCompatibleImage(new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB));

	/** True if the game loop is running. */
	protected boolean running;

	/** The current frames per second, used for debugging performance. */
	protected int fps = 60;

	/** A list of entities to render. */
	protected List<Polygon> entities = new ArrayList<>();
	protected List<SmoothLight> lights = new ArrayList<>();

	/** The mouse position */
	protected int mouseX, mouseY;

	// whether to create a light or create a block
	protected boolean createLight;

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

		final MouseMoveListener m = new MouseMoveListener();
		// listen to mouse events
		canvas.addMouseMotionListener(m);

		canvas.addMouseListener(m);

		canvas.requestFocus();

		canvas.addKeyListener(new KeyListener());

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
		entities.add(new Polygon(new int[] { 225, 245, 245, 225 }, new int[] { 245, 245, 275, 285 }, 4));
		lights.add(new SmoothLight(new Light(new Color(0, 255, 255, 200), new Vec2D(200, 200), 300), 2, 3, 5, 90));
	}

	/** Updates the game's entities. */
	protected void update(final double deltaTime) {
		lights.get(0).setPosition(mouseX, mouseY);
	}

	/** Called to render the frame. */
	protected void render(final Graphics2D g) {
		if (Constants.PRETTY_GRAPHICS) {
			GraphicsUtils.prettyGraphics(g);
		}

		g.setColor(Color.WHITE);
		g.drawString("FPS: " + fps, 10, 20);

		final Graphics2D lightGraphics = lightmap.createGraphics();
		lightGraphics.setBackground(new Color(255, 255, 255, 0));
		lightGraphics.clearRect(0, 0, width, height);

		// render the shadows first
		for (int i = 0; i < lights.size(); i++) {
			final SmoothLight l = lights.get(i);
			l.cut(entities);
			l.draw(lightGraphics);
		}

		lightGraphics.dispose();

		if (Constants.GLOW) {
			GraphicsUtils.glowFilter(lightmap, Constants.GLOW_AMOUNT);
		}

		if (Constants.BLUR) {
			g.drawImage(lightmap, GraphicsUtils.BLUR_FILTER, 0, 0);
		} else {
			g.drawImage(lightmap, null, 0, 0);
		}

		// render each entity
		for (int i = 0; i < entities.size(); i++) {
			final Shape e = entities.get(i);
			g.setColor(Color.WHITE);
			g.fill(e);
		}
	}

	/** Mouse motion listener for dynamic 2D shadows. */
	private class MouseMoveListener extends MouseMotionAdapter implements MouseListener {

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

		@Override
		public void mouseClicked(final MouseEvent e) {
			final Random rand = new Random();
			if (createLight) {
				Color c = null;
				switch (rand.nextInt(10)) {
				case 0:
					c = Color.YELLOW;
					break;
				case 1:
					c = Color.RED;
					break;
				case 2:
					c = Color.BLUE;
					break;
				case 3:
					c = Color.CYAN;
					break;
				case 4:
					c = Color.GREEN;
					break;
				case 5:
					c = Color.MAGENTA;
					break;
				case 6:
					c = Color.ORANGE;
					break;
				case 7:
					c = Color.PINK;
					break;
				case 8:
				case 9:
					c = Color.WHITE;
					break;

				}
				c = new Color(c.getRed(), c.getGreen(), c.getBlue(), 100 + rand.nextInt(100));
				lights.add(new SmoothLight(new Light(c, new Vec2D(e.getX(), e.getY()), rand.nextInt(200) + 100), rand.nextInt(5) + 1, rand.nextInt(5) + 1, rand.nextInt(5) + 1, rand.nextInt(180)));
			} else {
				final int width = rand.nextInt(100);
				final int height = rand.nextInt(100);

				entities.add(new Polygon(new int[] { e.getX(), e.getX() + width, e.getX() + width, e.getX() }, new int[] { e.getY(), e.getY(), e.getY() + height, e.getY() + height }, 4));
			}
		}

		@Override
		public void mousePressed(final MouseEvent e) {
			// TODO Auto-generated method stub

		}

		@Override
		public void mouseReleased(final MouseEvent e) {
			// TODO Auto-generated method stub

		}

		@Override
		public void mouseEntered(final MouseEvent e) {
			// TODO Auto-generated method stub

		}

		@Override
		public void mouseExited(final MouseEvent e) {
			// TODO Auto-generated method stub

		}

	}

	/** Mouse motion listener for dynamic 2D shadows. */
	private class KeyListener implements java.awt.event.KeyListener {

		@Override
		public void keyTyped(final KeyEvent e) {
		}

		@Override
		public void keyPressed(final KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_A) {
				createLight = true;
			} else if (e.getKeyCode() == KeyEvent.VK_D) {
				createLight = false;
			}
		}

		@Override
		public void keyReleased(final KeyEvent e) {
			// TODO Auto-generated method stub

		}
	}

}