package light;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import main.Constants;
import main.LightingTest;
import utils.Vec2D;

public class SmoothLight {
	/** A Polygon object which we will re-use for each shadow geometry. */
	protected final static Polygon POLYGON = new Polygon();

	protected final List<Light> lights = new ArrayList<>();

	/**
	 *
	 * @param center
	 *            the base light
	 * @param circles
	 *            the number of circles per layer
	 * @param oneLayerProjection
	 *            the amount of projection of one layer from the previous
	 * @param layers
	 *            the amount of layers
	 * @param angle
	 *            the angle between each layer
	 */
	public SmoothLight(final Light center, final int circles,
			final int oneLayerProjection, final int layers, final int angle) {

		final int alpha = center.getColor().getAlpha() / circles / layers;
		final Color newColor = new Color(center.getColor().getRed(), center
				.getColor().getGreen(), center.getColor().getBlue(), alpha);

		// creates layers of lights with the angle between each layer
		for (int j = 0; j < layers; j++) {
			// how much to rotate this layer counter-clockwise
			final int radialDifference = angle * j;
			// how much to project this layer
			final int projection = oneLayerProjection * j;
			final int dif = 360 / circles;
			for (int i = radialDifference; i < 360 + radialDifference; i += dif) {
				final double x = Math.cos(Math.toRadians(i)) * projection
						+ center.getX();
				final double y = Math.sin(Math.toRadians(i)) * projection
						+ center.getY();
				lights.add(new Light(newColor, new Vec2D(x, y), center
						.getRadius()));
			}
		}

	}

	/**
	 * @param g
	 *            the graphics to use for rendering
	 * @param entities
	 *            the list of entities to take into account when drawing shadows
	 * @throws Exception
	 */
	public void draw(final Graphics2D g, final List<Polygon> entities) {
		// old Paint object for resetting it later
		final Paint oldPaint = g.getPaint();
		// amount to extrude our shadow polygon by

		for (final Light light : lights) {

			// minimum distance (squared) which will save us some checks
			final float minDistSq = light.getRadius() * light.getRadius();

			// The area for drawing the light in
			Area shadowArea = null;

			for (int i = 0; i < entities.size(); i++) {
				final Polygon e = entities.get(i);

				final Rectangle2D bounds = e.getBounds2D();

				// average to find the entity's radius
				final float radius = (float) (bounds.getWidth() + bounds
						.getHeight()) / 4f;

				// get center of entity
				final Vec2D center = new Vec2D(bounds.getX() + radius,
						bounds.getY() + radius);

				final Vec2D lightToEntity = center.minus(light.getPosition());

				// get euclidean distance from light to center of the entity
				final float distSq = (float) lightToEntity
						.distanceSq(lightToEntity);

				// if the entity is outside of the shadow radius, then ignore
				if (distSq > minDistSq) {
					continue;
				}

				// if A never gets set, it defaults to the center
				Vec2D A = center;
				Vec2D B = center;

				// Find the farthest away vertices for which a line segment
				// between the source and it do not intersect
				// the polygon. Basically, a vertex with a line of sight to the
				// light source. Store these two in A and B.
				float maxAdistSq = 0;
				float maxBdistSq = 0;
				for (int j = 0; j < e.npoints; j++) {
					final int x = e.xpoints[j];
					final int y = e.ypoints[j];

					final float newDistSqred = (float) Line2D.ptLineDistSq(
							light.getX(), light.getY(), center.x, center.y, x,
							y);

					if (maxAdistSq < newDistSqred
							&& isLeft(light.getPosition(), center, new Vec2D(x,
									y))) {
						maxAdistSq = newDistSqred;
						A = new Vec2D(x, y);
					}
					if (maxBdistSq < newDistSqred
							&& !isLeft(light.getPosition(), center, new Vec2D(
									x, y))) {
						maxBdistSq = newDistSqred;
						B = new Vec2D(x, y);
					}
				}
				// project the points by our SHADOW_EXTRUDE amount
				final Vec2D C = project(light.getPosition(), A,
						light.getRadius() * light.getRadius());
				final Vec2D D = project(light.getPosition(), B,
						light.getRadius() * light.getRadius());

				// construct a polygon from our points
				POLYGON.reset();
				POLYGON.addPoint((int) A.x, (int) A.y);
				POLYGON.addPoint((int) B.x, (int) B.y);
				POLYGON.addPoint((int) D.x, (int) D.y);
				POLYGON.addPoint((int) C.x, (int) C.y);
				final Area a = new Area(POLYGON);

				// adds to the existing light area
				if (shadowArea == null) {
					shadowArea = a;
				} else {
					shadowArea.add(a);
				}
				if (Constants.OUTLINE_SHADOWS) {
					g.setColor(Color.PINK);
					g.draw(shadowArea);
				}

			}
			if (shadowArea == null) {
				// fill the polygon with the gradient
				g.drawImage(light.image, null,
						(int) (light.getX() - light.getRadius()),
						(int) (light.getY() - light.getRadius()));
			} else {
				// get the inverse of the lightArea and set that as the clip for
				// shadows
				final Shape s = g.getClip();

				final Area lightArea = new Area(new Rectangle2D.Float(0, 0,
						LightingTest.getWidth(), LightingTest.getHeight()));
				lightArea.subtract(shadowArea);
				g.setClip(lightArea);

				g.drawImage(light.image, null,
						(int) (light.getX() - light.getRadius()),
						(int) (light.getY() - light.getRadius()));

				g.setClip(s);
			}
			if (Constants.OUTLINE_LIGHTS) {
				g.setColor(Color.PINK);
				g.drawOval((int) light.getX() - 2, (int) light.getY() - 2, 4, 4);
			}
		}

		// reset to old Paint object
		g.setPaint(oldPaint);
	}

	/**
	 * Determines whether point c is on the left of the line between a and b
	 *
	 * @param a
	 *            point 1 of line AB
	 * @param b
	 *            point 2 of line AB
	 * @param c
	 *            the point to check
	 * @return whether point C is on the left of line AB. Returns false if the
	 *         point is on the line.
	 */
	public static boolean isLeft(final Vec2D a, final Vec2D b, final Vec2D c) {
		return (b.x - a.x) * (c.y - a.y) - (b.y - a.y) * (c.x - a.x) > 0;
	}

	/**
	 * Projects a point from end along the vector (end - start) by the given
	 * scalar amount.
	 */
	private static Vec2D project(final Vec2D start, final Vec2D end,
			final float scalar) {
		return end.minus(start).unitVector().scalarMult(scalar).plus(end);
	}

	public void setPosition(final float x, final float y) {
		final float differenceX = x - lights.get(0).getX();
		final float differenceY = y - lights.get(0).getY();
		for (final Light l : lights) {
			l.setPosition(l.getX() + differenceX, l.getY() + differenceY);
		}
	}
}