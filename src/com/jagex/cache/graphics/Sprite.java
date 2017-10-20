package com.jagex.cache.graphics;

import java.awt.Component;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.image.PixelGrabber;

import com.jagex.cache.Archive;
import com.jagex.draw.Raster;
import com.jagex.io.Buffer;

public final class Sprite extends Raster {

	private int height;
	private int horizontalOffset;
	private int[] raster;
	private int resizeHeight;
	private int resizeWidth;
	private int verticalOffset;
	private int width;

	public Sprite(Archive archive, String name, int id) {
		Buffer sprite = new Buffer(archive.getEntry(name + ".dat"));
		Buffer meta = new Buffer(archive.getEntry("index.dat"));
		meta.setPosition(sprite.readUShort());

		resizeWidth = meta.readUShort();
		resizeHeight = meta.readUShort();

		int colours = meta.readUByte();
		int[] palette = new int[colours];

		for (int index = 0; index < colours - 1; index++) {
			int colour = meta.readUTriByte();
			palette[index + 1] = colour == 0 ? 1 : colour;
		}

		for (int i = 0; i < id; i++) {
			meta.setPosition(meta.getPosition() + 2);
			sprite.setPosition(sprite.getPosition() + meta.readUShort() * meta.readUShort());
			meta.setPosition(meta.getPosition() + 1);
		}

		horizontalOffset = meta.readUByte();
		verticalOffset = meta.readUByte();
		width = meta.readUShort();
		height = meta.readUShort();

		int format = meta.readUByte();
		int pixels = width * height;

		System.out.println(name + " " + id + " " + width + " " + height + " " + format);

		raster = new int[pixels];

		if (format == 0) {
			for (int index = 0; index < pixels; index++) {
				raster[index] = palette[sprite.readUByte()];
			}
		} else if (format == 1) {
			for (int x = 0; x < width; x++) {
				for (int y = 0; y < height; y++) {
					raster[x + y * width] = palette[sprite.readUByte()];
				}
			}
		}
	}

	public Sprite(byte[] data, Component component) {
		try {
			Image image = Toolkit.getDefaultToolkit().createImage(data);
			MediaTracker tracker = new MediaTracker(component);
			tracker.addImage(image, 0);
			tracker.waitForAll();

			width = image.getWidth(component);
			height = image.getHeight(component);
			resizeWidth = width;
			resizeHeight = height;

			horizontalOffset = 0;
			verticalOffset = 0;
			raster = new int[width * height];

			PixelGrabber grabber = new PixelGrabber(image, 0, 0, width, height, raster, 0, width);
			grabber.grabPixels();
		} catch (Exception ex) {
			System.out.println("Error converting jpg");
		}
	}

	public Sprite(int width, int height) {
		raster = new int[width * height];
		this.width = resizeWidth = width;
		this.height = resizeHeight = height;
		horizontalOffset = verticalOffset = 0;
	}

	public void drawBehind(IndexedImage image, int y, int x) {
		x += horizontalOffset;
		y += verticalOffset;
		int k = x + y * Raster.width;
		int l = 0;
		int height = this.height;
		int width = this.width;
		int deltaWidth = Raster.width - width;
		int l1 = 0;

		if (y < Raster.getClipBottom()) {
			int dy = Raster.getClipBottom() - y;
			height -= dy;
			y = Raster.getClipBottom();
			l += dy * width;
			k += dy * Raster.width;
		}

		if (y + height > Raster.getClipTop()) {
			height -= y + height - Raster.getClipTop();
		}

		if (x < Raster.getClipLeft()) {
			int dx = Raster.getClipLeft() - x;
			width -= dx;
			x = Raster.getClipLeft();
			l += dx;
			k += dx;
			l1 += dx;
			deltaWidth += dx;
		}

		if (x + width > Raster.getClipRight()) {
			int dx = x + width - Raster.getClipRight();
			width -= dx;
			l1 += dx;
			deltaWidth += dx;
		}
		if (width > 0 && height > 0) {
			drawBehind(image.getRaster(), raster, width, height, Raster.raster, 0, deltaWidth, k, l1, l);
		}
	}

	public void drawSprite(int x, int y) {
		x += horizontalOffset;
		y += verticalOffset;
		int rasterClip = x + y * Raster.width;
		int imageClip = 0;
		int height = this.height;
		int width = this.width;
		int rasterOffset = Raster.width - width;
		int imageOffset = 0;

		if (y < Raster.getClipBottom()) {
			int dy = Raster.getClipBottom() - y;
			height -= dy;
			y = Raster.getClipBottom();
			imageClip += dy * width;
			rasterClip += dy * Raster.width;
		}

		if (y + height > Raster.getClipTop()) {
			height -= y + height - Raster.getClipTop();
		}

		if (x < Raster.getClipLeft()) {
			int dx = Raster.getClipLeft() - x;
			width -= dx;
			x = Raster.getClipLeft();
			imageClip += dx;
			rasterClip += dx;
			imageOffset += dx;
			rasterOffset += dx;
		}

		if (x + width > Raster.getClipRight()) {
			int dx = x + width - Raster.getClipRight();
			width -= dx;
			imageOffset += dx;
			rasterOffset += dx;
		}

		if (width > 0 && height > 0) {
			draw(Raster.raster, raster, 0, imageClip, rasterClip, width, height, rasterOffset, imageOffset);
		}
	}

	public void drawSprite(int x, int y, int alpha) {
		x += horizontalOffset;
		y += verticalOffset;
		int destIndex = x + y * Raster.width;
		int sourceIndex = 0;
		int height = this.height;
		int width = this.width;
		int destStep = Raster.width - width;
		int sourceStep = 0;

		if (y < Raster.getClipBottom()) {
			int dx = Raster.getClipBottom() - y;
			height -= dx;
			y = Raster.getClipBottom();
			sourceIndex += dx * width;
			destIndex += dx * Raster.width;
		}

		if (y + height > Raster.getClipTop()) {
			height -= y + height - Raster.getClipTop();
		}

		if (x < Raster.getClipLeft()) {
			int dx = Raster.getClipLeft() - x;
			width -= dx;
			x = Raster.getClipLeft();
			sourceIndex += dx;
			destIndex += dx;
			sourceStep += dx;
			destStep += dx;
		}

		if (x + width > Raster.getClipRight()) {
			int dx = x + width - Raster.getClipRight();
			width -= dx;
			sourceStep += dx;
			destStep += dx;
		}

		if (width > 0 && height > 0) {
			draw(sourceIndex, width, Raster.raster, 0, raster, sourceStep, height, destStep, alpha, destIndex);
		}
	}

	public int getHeight() {
		return height;
	}

	public int getHorizontalOffset() {
		return horizontalOffset;
	}

	public int getPixel(int index) {
		return raster[index];
	}

	public int[] getRaster() {
		return raster;
	}

	public int getResizeHeight() {
		return resizeHeight;
	}

	public int getResizeWidth() {
		return resizeWidth;
	}

	public int getVerticalOffset() {
		return verticalOffset;
	}

	public int getWidth() {
		return width;
	}

	public void initRaster() {
		Raster.init(height, width, raster);
	}

	public void method346(int x, int y) {
		x += horizontalOffset;
		y += verticalOffset;

		int destIndex = x + y * Raster.width;
		int sourceIndex = 0;
		int height = this.height;
		int width = this.width;
		int destStep = Raster.width - width;
		int sourceStep = 0;

		if (y < Raster.getClipBottom()) {
			int dy = Raster.getClipBottom() - y;
			height -= dy;
			y = Raster.getClipBottom();
			sourceIndex += dy * width;
			destIndex += dy * Raster.width;
		}

		if (y + height > Raster.getClipTop()) {
			height -= y + height - Raster.getClipTop();
		}

		if (x < Raster.getClipLeft()) {
			int dx = Raster.getClipLeft() - x;
			width -= dx;
			x = Raster.getClipLeft();
			sourceIndex += dx;
			destIndex += dx;
			sourceStep += dx;
			destStep += dx;
		}

		if (x + width > Raster.getClipRight()) {
			int dx = x + width - Raster.getClipRight();
			width -= dx;
			sourceStep += dx;
			destStep += dx;
		}

		if (width > 0 && height > 0) {
			method347(destIndex, width, height, sourceStep, sourceIndex, destStep, raster, Raster.raster);
		}
	}

	public void method352(int height, int theta, int[] ai, int k, int[] destOffsets, int i1, int y, int x, int width, int i2) {
		try {
			int midX = -width / 2;
			int midY = -height / 2;
			int sin = (int) (Math.sin(theta / 326.11D) * 65536);
			int cos = (int) (Math.cos(theta / 326.11D) * 65536);
			sin = sin * k >> 8;
			cos = cos * k >> 8;

			int j3 = (i2 << 16) + midY * sin + midX * cos;
			int k3 = (i1 << 16) + midY * cos - midX * sin;
			int destOffset = x + y * Raster.width;

			for (y = 0; y < height; y++) {
				int offset = destOffsets[y];
				int destIndex = destOffset + offset;
				int k4 = j3 + cos * offset;
				int l4 = k3 - sin * offset;

				for (x = -ai[y]; x < 0; x++) {
					Raster.raster[destIndex++] = raster[(k4 >> 16) + (l4 >> 16) * this.width];
					k4 += cos;
					l4 -= sin;
				}

				j3 += sin;
				k3 += cos;
				destOffset += Raster.width;
			}
		} catch (Exception ex) {
		}
	}

	public void method353(int x, int y, int width, int height, double theta, int j, int l, int j1) {
		try {
			int midX = -width / 2;
			int midY = -height / 2;
			int sin = (int) (Math.sin(theta) * 65536D);
			int cos = (int) (Math.cos(theta) * 65536D);
			sin = sin * j1 >> 8;
			cos = cos * j1 >> 8;
			int i3 = (l << 16) + midY * sin + midX * cos;
			int j3 = (j << 16) + midY * cos - midX * sin;
			int destOffset = x + y * Raster.width;

			for (y = 0; y < height; y++) {
				int destIndex = destOffset;
				int i4 = i3;
				int j4 = j3;

				for (x = -width; x < 0; x++) {
					int colour = raster[(i4 >> 16) + (j4 >> 16) * this.width];
					if (colour != 0) {
						Raster.raster[destIndex++] = colour;
					} else {
						destIndex++;
					}

					i4 += cos;
					j4 -= sin;
				}

				i3 += sin;
				j3 += cos;
				destOffset += Raster.width;
			}
		} catch (Exception ex) {
		}
	}

	public void recolour(int redOffset, int greenOffset, int blueOffset) {
		for (int index = 0; index < raster.length; index++) {
			int rgb = raster[index];

			if (rgb != 0) {
				int red = rgb >> 16 & 0xff;
				red += redOffset;

				if (red < 1) {
					red = 1;
				} else if (red > 255) {
					red = 255;
				}

				int green = rgb >> 8 & 0xff;
				green += greenOffset;

				if (green < 1) {
					green = 1;
				} else if (green > 255) {
					green = 255;
				}

				int blue = rgb & 0xff;
				blue += blueOffset;

				if (blue < 1) {
					blue = 1;
				} else if (blue > 255) {
					blue = 255;
				}

				raster[index] = (red << 16) + (green << 8) + blue;
			}
		}
	}

	public void resize() {
		System.out.println(resizeWidth + " " + resizeHeight + " " + verticalOffset + " " + horizontalOffset + " " + raster.length);

		int[] raster = new int[resizeWidth * resizeHeight];
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				raster[(y + verticalOffset) * resizeWidth + x + horizontalOffset] = this.raster[y * width + x];
			}
		}

		this.raster = raster;
		width = resizeWidth;
		height = resizeHeight;
		horizontalOffset = 0;
		verticalOffset = 0;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public void setHorizontalOffset(int horizontalOffset) {
		this.horizontalOffset = horizontalOffset;
	}

	public void setRaster(int[] raster) {
		this.raster = raster;
	}

	public void setResizeHeight(int resizeHeight) {
		this.resizeHeight = resizeHeight;
	}

	public void setResizeWidth(int resizeWidth) {
		this.resizeWidth = resizeWidth;
	}

	public void setVerticalOffset(int verticalOffset) {
		this.verticalOffset = verticalOffset;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	private void draw(int sourceIndex, int width, int[] dest, int k, int[] source, int sourceStep, int height, int destStep,
			int alpha, int destIndex) {
		int ialpha = 256 - alpha;

		for (int y = -height; y < 0; y++) {
			for (int x = -width; x < 0; x++) {
				k = source[sourceIndex++];
				if (k != 0) {
					int current = dest[destIndex];
					dest[destIndex++] = ((k & 0xff00ff) * alpha + (current & 0xff00ff) * ialpha & 0xff00ff00)
							+ ((k & 0xff00) * alpha + (current & 0xff00) * ialpha & 0xff0000) >> 8;
				} else {
					destIndex++;
				}
			}

			destIndex += destStep;
			sourceIndex += sourceStep;
		}
	}

	private void draw(int raster[], int[] image, int colour, int sourceIndex, int destIndex, int width, int height, int destStep,
			int sourceStep) {
		int minX = -(width >> 2);
		width = -(width & 3);

		for (int y = -height; y < 0; y++) {
			for (int x = minX; x < 0; x++) {
				colour = image[sourceIndex++];
				if (colour != 0) {
					raster[destIndex++] = colour;
				} else {
					destIndex++;
				}
				colour = image[sourceIndex++];

				if (colour != 0) {
					raster[destIndex++] = colour;
				} else {
					destIndex++;
				}
				colour = image[sourceIndex++];

				if (colour != 0) {
					raster[destIndex++] = colour;
				} else {
					destIndex++;
				}
				colour = image[sourceIndex++];

				if (colour != 0) {
					raster[destIndex++] = colour;
				} else {
					destIndex++;
				}
			}

			for (int k2 = width; k2 < 0; k2++) {
				colour = image[sourceIndex++];
				if (colour != 0) {
					raster[destIndex++] = colour;
				} else {
					destIndex++;
				}
			}

			destIndex += destStep;
			sourceIndex += sourceStep;
		}
	}

	private void drawBehind(byte[] image, int[] input, int width, int height, int[] output, int in, int destStep, int destIndex,
			int sourceStep, int sourceIndex) {
		int l1 = -(width >> 2);
		width = -(width & 3);

		for (int y = -height; y < 0; y++) {
			for (int x = l1; x < 0; x++) {

				in = input[sourceIndex++];
				if (in != 0 && image[destIndex] == 0) {
					output[destIndex++] = in;
				} else {
					destIndex++;
				}

				in = input[sourceIndex++];
				if (in != 0 && image[destIndex] == 0) {
					output[destIndex++] = in;
				} else {
					destIndex++;
				}

				in = input[sourceIndex++];
				if (in != 0 && image[destIndex] == 0) {
					output[destIndex++] = in;
				} else {
					destIndex++;
				}

				in = input[sourceIndex++];
				if (in != 0 && image[destIndex] == 0) {
					output[destIndex++] = in;
				} else {
					destIndex++;
				}
			}

			for (int l2 = width; l2 < 0; l2++) {
				in = input[sourceIndex++];
				if (in != 0 && image[destIndex] == 0) {
					output[destIndex++] = in;
				} else {
					destIndex++;
				}
			}

			destIndex += destStep;
			sourceIndex += sourceStep;
		}
	}

	private void method347(int destIndex, int width, int height, int sourceStep, int sourceIndex, int destStep, int[] source,
			int[] raster) {
		int minX = -(width >> 2);
		width = -(width & 3);

		for (int y = -height; y < 0; y++) {
			for (int x = minX; x < 0; x++) {
				raster[destIndex++] = source[sourceIndex++];
				raster[destIndex++] = source[sourceIndex++];
				raster[destIndex++] = source[sourceIndex++];
				raster[destIndex++] = source[sourceIndex++];
			}

			for (int k2 = width; k2 < 0; k2++) {
				raster[destIndex++] = source[sourceIndex++];
			}

			destIndex += destStep;
			sourceIndex += sourceStep;
		}
	}

}