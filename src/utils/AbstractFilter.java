package utils;

import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

public abstract class AbstractFilter implements BufferedImageOp {
	@Override
	public abstract BufferedImage filter(BufferedImage src, BufferedImage dest);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Rectangle2D getBounds2D(final BufferedImage src) {
		return new Rectangle(0, 0, src.getWidth(), src.getHeight());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BufferedImage createCompatibleDestImage(final BufferedImage src, ColorModel destCM) {
		if (destCM == null) {
			destCM = src.getColorModel();
		}

		return new BufferedImage(destCM, destCM.createCompatibleWritableRaster(src.getWidth(), src.getHeight()), destCM.isAlphaPremultiplied(), null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Point2D getPoint2D(final Point2D srcPt, final Point2D dstPt) {
		return (Point2D) srcPt.clone();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public RenderingHints getRenderingHints() {
		return null;
	}

	/**
	 * <p>
	 * Returns an array of pixels, stored as integers, from a <code>BufferedImage</code>. The pixels are grabbed from a
	 * rectangular area defined by a location and two dimensions. Calling this method on an image of type different from
	 * <code>BufferedImage.TYPE_INT_ARGB</code> and <code>BufferedImage.TYPE_INT_RGB</code> will unmanage the image.
	 * </p>
	 *
	 * @param img
	 *            the source image
	 * @param x
	 *            the x location at which to start grabbing pixels
	 * @param y
	 *            the y location at which to start grabbing pixels
	 * @param w
	 *            the width of the rectangle of pixels to grab
	 * @param h
	 *            the height of the rectangle of pixels to grab
	 * @param pixels
	 *            a pre-allocated array of pixels of size w*h; can be null
	 * @return <code>pixels</code> if non-null, a new array of integers otherwise
	 * @throws IllegalArgumentException
	 *             is <code>pixels</code> is non-null and of length &lt; w*h
	 */
	public static int[] getPixels(final BufferedImage img, final int x, final int y, final int w, final int h, int[] pixels) {
		if (w == 0 || h == 0) {
			return new int[0];
		}

		if (pixels == null) {
			pixels = new int[w * h];
		} else if (pixels.length < w * h) {
			throw new IllegalArgumentException("pixels array must have a length" + " >= w*h");
		}

		final int imageType = img.getType();
		if (imageType == BufferedImage.TYPE_INT_ARGB || imageType == BufferedImage.TYPE_INT_ARGB_PRE || imageType == BufferedImage.TYPE_INT_RGB) {
			final Raster raster = img.getRaster();
			return (int[]) raster.getDataElements(x, y, w, h, pixels);
		}

		// Unmanages the image
		return img.getRGB(x, y, w, h, pixels, 0, w);
	}

	/**
	 * <p>
	 * Writes a rectangular area of pixels in the destination <code>BufferedImage</code>. Calling this method on an
	 * image of type different from <code>BufferedImage.TYPE_INT_ARGB</code> and <code>BufferedImage.TYPE_INT_RGB</code>
	 * will unmanage the image.
	 * </p>
	 *
	 * @param img
	 *            the destination image
	 * @param x
	 *            the x location at which to start storing pixels
	 * @param y
	 *            the y location at which to start storing pixels
	 * @param w
	 *            the width of the rectangle of pixels to store
	 * @param h
	 *            the height of the rectangle of pixels to store
	 * @param pixels
	 *            an array of pixels, stored as integers
	 * @throws IllegalArgumentException
	 *             is <code>pixels</code> is non-null and of length &lt; w*h
	 */
	public static void setPixels(final BufferedImage img, final int x, final int y, final int w, final int h, final int[] pixels) {
		if (pixels == null || w == 0 || h == 0) {
			return;
		} else if (pixels.length < w * h) {
			throw new IllegalArgumentException("pixels array must have a length" + " >= w*h");
		}

		final int imageType = img.getType();
		if (imageType == BufferedImage.TYPE_INT_ARGB || imageType == BufferedImage.TYPE_INT_ARGB_PRE || imageType == BufferedImage.TYPE_INT_RGB) {
			final WritableRaster raster = img.getRaster();
			raster.setDataElements(x, y, w, h, pixels);
		} else {
			// Unmanages the image
			img.setRGB(x, y, w, h, pixels, 0, w);
		}
	}

	/*
	 * Copyright (c) 2007, Romain Guy All rights reserved.
	 *
	 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
	 * following conditions are met:
	 *
	 * * Redistributions of source code must retain the above copyright notice, this list of conditions and the
	 * following disclaimer. * Redistributions in binary form must reproduce the above copyright notice, this list of
	 * conditions and the following disclaimer in the documentation and/or other materials provided with the
	 * distribution. * Neither the name of the TimingFramework project nor the names of its contributors may be used to
	 * endorse or promote products derived from this software without specific prior written permission.
	 *
	 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
	 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
	 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
	 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
	 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
	 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING
	 * IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
	 */

	/**
	 * <p>
	 * A fast blur filter can be used to blur pictures quickly. This filter is an implementation of the box blur
	 * algorithm. The blurs generated by this algorithm might show square artifacts, especially on pictures containing
	 * straight lines (rectangles, text, etc.) On most pictures though, the result will look very good.
	 * </p>
	 * <p>
	 * The force of the blur can be controlled with a radius and the default radius is 3. Since the blur clamps values
	 * on the edges of the source picture, you might need to provide a picture with empty borders to avoid artifacts at
	 * the edges. The performance of this filter are independant from the radius.
	 * </p>
	 *
	 * @author Romain Guy <romain.guy@mac.com>
	 */
	public static class FastBlurFilter extends AbstractFilter {
		private final int radius;

		/**
		 * <p>
		 * Creates a new blur filter with a default radius of 3.
		 * </p>
		 */
		public FastBlurFilter() {
			this(3);
		}

		/**
		 * <p>
		 * Creates a new blur filter with the specified radius. If the radius is lower than 1, a radius of 1 will be
		 * used automatically.
		 * </p>
		 *
		 * @param radius
		 *            the radius, in pixels, of the blur
		 */
		public FastBlurFilter(int radius) {
			if (radius < 1) {
				radius = 1;
			}

			this.radius = radius;
		}

		/**
		 * <p>
		 * Returns the radius used by this filter, in pixels.
		 * </p>
		 *
		 * @return the radius of the blur
		 */
		public int getRadius() {
			return radius;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public BufferedImage filter(final BufferedImage src, BufferedImage dst) {
			final int width = src.getWidth();
			final int height = src.getHeight();

			if (dst == null) {
				dst = createCompatibleDestImage(src, null);
			}

			final int[] srcPixels = new int[width * height];
			final int[] dstPixels = new int[width * height];

			getPixels(src, 0, 0, width, height, srcPixels);
			// horizontal pass
			blur(srcPixels, dstPixels, width, height, radius);
			// vertical pass
			blur(dstPixels, srcPixels, height, width, radius);
			// the result is now stored in srcPixels due to the 2nd pass
			setPixels(dst, 0, 0, width, height, srcPixels);

			return dst;
		}

		/**
		 * <p>
		 * Blurs the source pixels into the destination pixels. The force of the blur is specified by the radius which
		 * must be greater than 0.
		 * </p>
		 * <p>
		 * The source and destination pixels arrays are expected to be in the INT_ARGB format.
		 * </p>
		 *
		 * @param srcPixels
		 *            the source pixels
		 * @param dstPixels
		 *            the destination pixels
		 * @param width
		 *            the width of the source picture
		 * @param height
		 *            the height of the source picture
		 * @param radius
		 *            the radius of the blur effect
		 */
		static void blur(final int[] srcPixels, final int[] dstPixels, final int width, final int height, final int radius) {
			final int windowSize = radius * 2 + 1;
			final int radiusPlusOne = radius + 1;

			int sumAlpha;
			int sumRed;
			int sumGreen;
			int sumBlue;

			int srcIndex = 0;
			int dstIndex;
			int pixel;

			final int[] sumLookupTable = new int[256 * windowSize];
			for (int i = 0; i < sumLookupTable.length; i++) {
				sumLookupTable[i] = i / windowSize;
			}

			final int[] indexLookupTable = new int[radiusPlusOne];
			if (radius < width) {
				for (int i = 0; i < indexLookupTable.length; i++) {
					indexLookupTable[i] = i;
				}
			} else {
				for (int i = 0; i < width; i++) {
					indexLookupTable[i] = i;
				}
				for (int i = width; i < indexLookupTable.length; i++) {
					indexLookupTable[i] = width - 1;
				}
			}

			for (int y = 0; y < height; y++) {
				sumAlpha = sumRed = sumGreen = sumBlue = 0;
				dstIndex = y;

				pixel = srcPixels[srcIndex];
				sumAlpha += radiusPlusOne * (pixel >> 24 & 0xFF);
				sumRed += radiusPlusOne * (pixel >> 16 & 0xFF);
				sumGreen += radiusPlusOne * (pixel >> 8 & 0xFF);
				sumBlue += radiusPlusOne * (pixel & 0xFF);

				for (int i = 1; i <= radius; i++) {
					pixel = srcPixels[srcIndex + indexLookupTable[i]];
					sumAlpha += pixel >> 24 & 0xFF;
				sumRed += pixel >> 16 & 0xFF;
		sumGreen += pixel >> 8 & 0xFF;
		sumBlue += pixel & 0xFF;
				}

				for (int x = 0; x < width; x++) {
					dstPixels[dstIndex] = sumLookupTable[sumAlpha] << 24 | sumLookupTable[sumRed] << 16 | sumLookupTable[sumGreen] << 8 | sumLookupTable[sumBlue];
					dstIndex += height;

					int nextPixelIndex = x + radiusPlusOne;
					if (nextPixelIndex >= width) {
						nextPixelIndex = width - 1;
					}

					int previousPixelIndex = x - radius;
					if (previousPixelIndex < 0) {
						previousPixelIndex = 0;
					}

					final int nextPixel = srcPixels[srcIndex + nextPixelIndex];
					final int previousPixel = srcPixels[srcIndex + previousPixelIndex];

					sumAlpha += nextPixel >> 24 & 0xFF;
					sumAlpha -= previousPixel >> 24 & 0xFF;

					sumRed += nextPixel >> 16 & 0xFF;
					sumRed -= previousPixel >> 16 & 0xFF;

				sumGreen += nextPixel >> 8 & 0xFF;
				sumGreen -= previousPixel >> 8 & 0xFF;

				sumBlue += nextPixel & 0xFF;
				sumBlue -= previousPixel & 0xFF;
				}

				srcIndex += width;
			}
		}
	}
}