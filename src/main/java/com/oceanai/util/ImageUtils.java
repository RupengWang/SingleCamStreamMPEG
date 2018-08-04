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
}
