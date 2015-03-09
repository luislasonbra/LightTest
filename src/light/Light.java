package light;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.MultipleGradientPaint.CycleMethod;
import java.awt.RadialGradientPaint;
import java.awt.Transparency;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import utils.GraphicsUtils;
import utils.Vec2D;

public class Light {
	private static final Color NULL_COLOR = new Color(0, 0, 0, 0);
	private static final float[] SIZE_FRACTION = new float[] { 0, 1 };

	public final BufferedImage image;
	private float x;
	private float y;
	private final float radius;
	Color color;

	public Light(final Color c, final Vec2D position, final float radius) {
		super();
		image = GraphicsUtils.createImage((int) radius * 2, (int) radius * 2,
				Transparency.TRANSLUCENT);

		final Graphics2D g = (Graphics2D) image.getGraphics();
		g.setPaint(new RadialGradientPaint(new Rectangle2D.Double(0, 0,
				radius * 2, radius * 2), SIZE_FRACTION, new Color[] { c,
				NULL_COLOR }, CycleMethod.NO_CYCLE));
		g.fillRect(0, 0, (int) radius * 2, (int) (radius * 2));

		color = c;
		this.radius = radius;
		setPosition((float) position.x, (float) position.y);
	}

	public float getX() {
		return x;
	}

	public float getY() {
		return y;
	}

	public void setPosition(final float x, final float y) {
		this.x = x;
		this.y = y;
	}

	public float getRadius() {
		return radius;
	}

	public Color getColor() {
		return color;
	}

	public Vec2D getPosition() {
		return new Vec2D(x, y);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (color == null ? 0 : color.hashCode());
		result = prime * result + Float.floatToIntBits(radius);
		result = prime * result + Float.floatToIntBits(x);
		result = prime * result + Float.floatToIntBits(y);
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Light other = (Light) obj;
		if (color == null) {
			if (other.color != null) {
				return false;
			}
		} else if (!color.equals(other.color)) {
			return false;
		}
		if (Float.floatToIntBits(radius) != Float.floatToIntBits(other.radius)) {
			return false;
		}
		if (Float.floatToIntBits(x) != Float.floatToIntBits(other.x)) {
			return false;
		}
		if (Float.floatToIntBits(y) != Float.floatToIntBits(other.y)) {
			return false;
		}
		return true;
	}

}