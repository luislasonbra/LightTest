package utils;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import main.Constants;
import utils.AbstractFilter.FastBlurFilter;

/**
 * The Class GraphicsUtils.
 */
public final class GraphicsUtils {

	/** The Constant RAND. */
	private static final Random RAND = new Random();

	/** The Constant GFX_CONFIG. */
	private static final GraphicsConfiguration GFX_CONFIG = GraphicsEnvironment
			.getLocalGraphicsEnvironment().getDefaultScreenDevice()
			.getDefaultConfiguration();

	// Utility class, cannot instantiate
	private GraphicsUtils() {
	}

	/**
	 * Gets a random similar color to color c.
	 *
	 * @param c
	 *            the c
	 * @param similarity
	 *            the similarity
	 * @return the random similar color
	 */
	public static Color getRandomSimilarColor(final Color c,
			final int similarity) {
		final float[] hsb = Color.RGBtoHSB(c.getRed(), c.getGreen(),
				c.getBlue(), null);
		hsb[0] += RAND.nextFloat() * similarity;
		hsb[1] += RAND.nextFloat() * similarity;
		hsb[2] += RAND.nextFloat() * similarity;
		return Color.getHSBColor(hsb[0], hsb[1], hsb[2]);
	}

	/**
	 * Generate random color, and averages it with the mix.
	 *
	 * @param mix
	 *            the mix
	 * @return the color
	 */
	public static Color generateRandomColor(final Color mix) {
		int red = RAND.nextInt(256);
		int green = RAND.nextInt(256);
		int blue = RAND.nextInt(256);

		// mix the color
		if (mix != null) {
			red = (red + mix.getRed()) / 2;
			green = (green + mix.getGreen()) / 2;
			blue = (blue + mix.getBlue()) / 2;
		}

		return new Color(red, green, blue);
	}

	/**
	 * Takes an image and makes a compatible version.
	 *
	 * @param image
	 *            the image
	 * @return the buffered image
	 */
	public static BufferedImage toCompatibleImage(final BufferedImage image) {
		/*
		 * if image is already compatible and optimized for current system
		 * settings, simply return it
		 */
		if (image.getColorModel().equals(GFX_CONFIG.getColorModel())) {
			return image;
		}

		// image is not optimized, so create a new image that is
		final BufferedImage new_image = GFX_CONFIG.createCompatibleImage(
				image.getWidth(), image.getHeight(), image.getTransparency());

		// get the graphics context of the new image to draw the old image on
		final Graphics2D g2d = (Graphics2D) new_image.getGraphics();

		// actually draw the image and dispose of context no longer needed
		g2d.drawImage(image, 0, 0, null);
		g2d.dispose();

		// return the new optimized image
		return new_image;
	}

	/**
	 * Creates a compatible image given the parameters.
	 *
	 * @param width
	 *            the width
	 * @param height
	 *            the height
	 * @param transparency
	 *            the transparency
	 * @return the buffered image
	 */
	public static BufferedImage createImage(final int width, final int height,
			final int transparency) {
		BufferedImage image = GFX_CONFIG.createCompatibleImage(width, height,
				transparency);
		if (image.getRaster().getDataBuffer().getDataType() != DataBuffer.TYPE_INT) {
			switch (transparency) {
			case Transparency.TRANSLUCENT:
				image = new BufferedImage(width, height,
						BufferedImage.TYPE_INT_ARGB);
				break;
			case Transparency.OPAQUE:
				image = new BufferedImage(width, height,
						BufferedImage.TYPE_INT_RGB);
				break;
			case Transparency.BITMASK:
				image = new BufferedImage(width, height,
						BufferedImage.TYPE_INT_ARGB_PRE);
				break;
			default:
				break;
			}
		}
		return image;
	}

	public static BufferedImage loadImage(final String filename)
			throws IOException {
		final InputStream in = GraphicsUtils.class
				.getResourceAsStream(filename);
		return toCompatibleImage(ImageIO.read(in));
	}

	/**
	 * Copies the image.
	 *
	 * @param bi
	 *            the bi
	 * @return the buffered image
	 */
	public static BufferedImage copyImage(final BufferedImage bi) {
		final ColorModel colorModel = bi.getColorModel();
		final boolean isAlphaPremultiplied = colorModel.isAlphaPremultiplied();
		final WritableRaster raster = bi.copyData(null);
		return new BufferedImage(colorModel, raster, isAlphaPremultiplied, null);
	}

	/**
	 *
	 * @param image
	 * @param width
	 * @param height
	 * @return a new image of width and height
	 */
	public static BufferedImage resize(final Image image, final int width,
			final int height) {
		final BufferedImage bi = new BufferedImage(width, height,
				Transparency.TRANSLUCENT);
		final Graphics2D g2d = bi.createGraphics();
		g2d.addRenderingHints(new RenderingHints(RenderingHints.KEY_RENDERING,
				RenderingHints.VALUE_RENDER_QUALITY));
		g2d.drawImage(image, 0, 0, width, height, null);
		g2d.dispose();
		return bi;
	}

	/**
	 *
	 * @param fileIcon
	 * @param width
	 * @param height
	 * @return a new image icon of size width and height
	 */
	public static ImageIcon resize(final ImageIcon fileIcon, final int width,
			final int height) {
		return new ImageIcon(resize(fileIcon.getImage(), width, height));
	}

	/**
	 * Applies a glow filter on the src image.
	 *
	 * @param src
	 *            the src
	 * @param amount
	 *            the amount
	 */
	public static void glowFilter(final BufferedImage src, final float amount) {
		final int width = src.getWidth();
		final int height = src.getHeight();
		final int[] inPixels = ((DataBufferInt) src.getRaster().getDataBuffer())
				.getData();

		final float a = amount * 8;
		int index = 0;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				final int rgb1 = inPixels[index];
				int r1 = rgb1 >> 16 & 0xff;
				int g1 = rgb1 >> 8 & 0xff;
				int b1 = rgb1 & 0xff;

				r1 = clampPixel((int) (r1 * a));
				g1 = clampPixel((int) (g1 * a));
				b1 = clampPixel((int) (b1 * a));

				inPixels[index] = rgb1 & 0xff000000 | r1 << 16 | g1 << 8 | b1;
				index++;
			}
		}
	}

	/**
	 * Clamps an integer between 0 and 255.
	 *
	 * @param num
	 *            the i
	 * @return the int
	 */
	public static int clampPixel(final int num) {
		if (num < 0) {
			return 0;
		}
		if (num > 255) {
			return 255;
		}
		return num;
	}

	public static final AbstractFilter BLUR_FILTER = new FastBlurFilter(
			Constants.BLUR_AMOUNT);

	/**
	 * Antialiases the graphics.
	 *
	 * @param g
	 *            the graphics
	 */
	public static void prettyGraphics(final Graphics2D g) {
		g.addRenderingHints(new RenderingHints(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON));
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_RENDERING,
				RenderingHints.VALUE_RENDER_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
				RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
	}

}