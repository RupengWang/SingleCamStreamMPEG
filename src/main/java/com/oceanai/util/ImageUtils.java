package com.oceanai.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.DataBufferByte;
import java.io.*;

public class ImageUtils {

	/**
	 * Converts an image to byte buffer representing PNG (bytes as they would exist on disk)
	 * @param image
	 * @param encoding the encoding to be used, one of: png, jpeg, bmp, wbmp, gif
	 * @return byte[] representing the image
	 * @throws IOException if the bytes[] could not be written
	 */
	public static byte[] imageToBytes(BufferedImage image, String encoding) throws IOException{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(image, encoding, baos);
		return baos.toByteArray();
	}
	
	/**
	 * Converts the provided byte buffer into an BufferedImage
	 * @param buf byte[] of an image as it would exist on disk
	 * @return
	 * @throws IOException
	 */
	public static BufferedImage bytesToImage(byte[] buf) throws IOException{
		ByteArrayInputStream bais = new ByteArrayInputStream(buf);
		return ImageIO.read(bais);
	}

	/**
	 *
	 * @param bufferedImage
	 * @return
	 * @author WangRupeng
     */
	public static byte[] decodeToPixels(BufferedImage bufferedImage)
	{
		if(bufferedImage == null)
			return null;
		return ((DataBufferByte)bufferedImage.getRaster().getDataBuffer()).getData();
	}

	/**
	 *
	 * @param pixels
	 * @param width
	 * @param height
     * @return
	 *
	 * @author WangRupeng
     */
	public static BufferedImage getImageFromArray(byte[] pixels, int width, int height)
	{
		if(pixels == null || width <= 0 || height <= 0)
			return null;
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
		byte[] array = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
		//int n=  array.length;
		//int m = pixels.length;
		//System.out.println(array.length);
		System.arraycopy(pixels, 0, array, 0, array.length);
		return image;
	}

	/**
	 *
	 * @param bufferedImage
	 * @param formatName
	 * @return
	 * @throws IOException
	 * @author WangRupeng
     */
	public static byte[] encodeToImage(BufferedImage bufferedImage, String formatName) throws IOException {
		if(bufferedImage == null || formatName == null)
			return null;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(bufferedImage, formatName, baos);
		return baos.toByteArray();
	}

	//outputStream转inputStream
	public static ByteArrayInputStream outputStream2inputStream(OutputStream out) throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		baos = (ByteArrayOutputStream)out;
		ByteArrayInputStream swapStream = new ByteArrayInputStream(baos.toByteArray());
		return swapStream;
	}

	public static boolean saveToFile(BufferedImage bufferedImage, String path,String fileName,String imageType) throws IOException {
		if (bufferedImage == null || path == null) {
			return false;
		}
		if (!path.endsWith("/")) {
			path += "/";
		}
		File file = new File(path + fileName + "." + imageType);
		ImageIO.write(bufferedImage, imageType, file);
		return true;
	}

	/**
	 * @param raw
	 * @param formatName
	 * @return
	 * @throws IOException
	 * @author WangRupeng
     */
	public static byte[] encodeToImage(byte[] raw, String formatName) throws IOException
	{
		if(raw == null || formatName == null)
			return null;
		int d = (int)Math.sqrt(raw.length/3);
		BufferedImage bufferedImage = getImageFromArray(raw, d, d);
		return encodeToImage(bufferedImage, formatName);
	}

	/**
	 * Converts a given image into grayscalse
	 * @param src
	 * @return
	 */
	public static BufferedImage convertToGray(BufferedImage src){
        ColorConvertOp grayOp = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
        return grayOp.filter(src,null);
    }

	/**
	 * 绘制矩形框
	 * @param graphics2D
	 * @param box
	 */
	public static void draw(Graphics2D graphics2D, Rectangle box, Color color) {
		graphics2D.setColor(color);
		Point2D point2DA = new Point((int)box.getX(), (int)box.getY());
		Point2D point2DB = new Point((int)(box.getX() + box.getWidth()), (int)box.getY());
		Point2D point2DC = new Point((int)(box.getX() + box.getWidth()), (int)(box.getY() + box.getHeight()));
		Point2D point2DD = new Point((int)box.getX(), (int)(box.getY() + box.getHeight()));

		double width = box.getWidth();
		double height = box.getHeight();

		Line2D lineA_1 = new Line2D.Double(point2DA.getX(),point2DA.getY(),point2DA.getX()+width/4, point2DA.getY());
		Line2D lineA_2 = new Line2D.Double(point2DA.getX(), point2DA.getY(), point2DA.getX(), point2DA.getY() + height/4);
		Line2D lineB_1 = new Line2D.Double(point2DB.getX(), point2DB.getY(), point2DB.getX() - width/4, point2DA.getY());
		Line2D lineB_2 = new Line2D.Double(point2DB.getX(), point2DB.getY(), point2DB.getX(), point2DA.getY() + height/4);
		Line2D lineC_1 = new Line2D.Double(point2DC.getX(), point2DC.getY(), point2DC.getX(), point2DC.getY() - height/4);
		Line2D lineC_2 = new Line2D.Double(point2DC.getX(), point2DC.getY(), point2DC.getX() - width/4, point2DC.getY());
		Line2D lineD_1 = new Line2D.Double(point2DD.getX(), point2DD.getY(), point2DD.getX() + width/4, point2DD.getY());
		Line2D lineD_2 = new Line2D.Double(point2DD.getX(), point2DD.getY(), point2DD.getX(), point2DD.getY() - height/4);

		graphics2D.setStroke(new BasicStroke(1));
		graphics2D.draw(box);
		graphics2D.setStroke(new BasicStroke(4));
		graphics2D.draw(lineA_1);
		graphics2D.draw(lineA_2);
		graphics2D.draw(lineB_1);
		graphics2D.draw(lineB_2);
		graphics2D.draw(lineC_1);
		graphics2D.draw(lineC_2);
		graphics2D.draw(lineD_1);
		graphics2D.draw(lineD_2);
	}
}
