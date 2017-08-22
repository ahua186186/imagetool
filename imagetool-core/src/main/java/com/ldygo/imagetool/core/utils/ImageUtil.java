package com.ldygo.imagetool.core.utils;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifDirectoryBase;
import com.drew.metadata.exif.ExifIFD0Directory;

import com.ldygo.imagetool.core.AnimatedGifEncoder;
import com.ldygo.imagetool.core.GifDecoder;
import com.ldygo.imagetool.core.Scalr;
import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageEncoder;
import org.apache.commons.io.IOUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.*;

public class ImageUtil {

	public static Font FONT = new Font("微软雅黑", Font.BOLD, 18);
	public static final Color COLOR = Color.WHITE;
	public static final Color FONT_COLOR = new Color(255, 255, 255, 150);
	public static final Color FONT_SHADOW_COLOR = new Color(170, 170, 170, 77);

	public static boolean isJpg(String str) {
		return isEndWid(str, "jpg");
	}

	public static boolean isPng(String str) {
		return isEndWid(str, "png");
	}

	public static boolean isGif(String str) {
		return isEndWid(str, "gif");
	}

	private static boolean isEndWid(String str, String ext) {
		if (str == null || "".equals(str.trim())) {
			return false;
		}

		int position = str.lastIndexOf(".");
		if (position == -1 || (position == str.length() - 1)) {
			return false;
		}
		String suffix = str.substring(position + 1);
		if (ext.equalsIgnoreCase(suffix)) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean isJpg(InputStream in) throws IOException {
		InputStream iis = in;

		if (!in.markSupported()) {
			throw new IllegalArgumentException("Input stream must support mark");
		}

		iis.mark(30);
		// If the first two bytes are a JPEG SOI marker, it's probably
		// a JPEG file. If they aren't, it definitely isn't a JPEG file.
		try {
			int byte1 = iis.read();
			int byte2 = iis.read();
			if ((byte1 == 0xFF) && (byte2 == 0xD8)) {
				return true;
			}
		} finally {
			iis.reset();
		}

		return false;
	}

	public static boolean isPng(InputStream in) throws IOException {
		if (!in.markSupported()) {
			throw new IllegalArgumentException("Input stream must support mark");
		}

		byte[] b = new byte[8];
		try {
			in.mark(30);
			in.read(b);
		} finally {
			in.reset();
		}

		return (b[0] == (byte) 137 && b[1] == (byte) 80 && b[2] == (byte) 78 && b[3] == (byte) 71 && b[4] == (byte) 13
				&& b[5] == (byte) 10 && b[6] == (byte) 26 && b[7] == (byte) 10);
	}

	public static boolean isGif(InputStream in) throws IOException {
		if (!in.markSupported()) {
			throw new IllegalArgumentException("Input stream must support mark");
		}

		byte[] b = new byte[6];

		try {
			in.mark(30);
			in.read(b);
		} finally {
			in.reset();
		}

		return b[0] == 'G' && b[1] == 'I' && b[2] == 'F' && b[3] == '8' && (b[4] == '7' || b[4] == '9') && b[5] == 'a';
	}

	/**
     *压缩图片
	 * @param in   BufferedInputStream
	 * @param out   BufferedOutputStream
	 * @param maxWidth 缩放宽度
	 * @param maxHeight 缩放高度
	 * @param type 传7  ：image format is  jpg or png or gif
	 * @param quality 图片质量
	 * @param watermark 添加水印
	 * @param font 字体
	 * @param fontColor 字体颜色
	 * @param ratio 宽度和高度为0时，等比例缩放
	 * @param rotaton 旋转角度
	 * @throws IOException
	 */
	public static void resize(InputStream in, OutputStream out, int maxWidth, int maxHeight, int type, float quality,
			String[] watermark, Font font, Color fontColor,int ratio,int rotaton) throws IOException {
		if (!(type >= 1 && type <= 7)) {
			throw new IOException("can not support type: " + type + ", type must be in [1-7] ");
		}
		if (type == 1) {
			if (!isJpg(in)) {
				throw new IOException("image format is not jpg ");
			}
			resizeJpg(in, out, maxWidth, maxHeight, quality, watermark, font, fontColor, ratio, rotaton);
			return;
		} else if (type == 2) {
			if (!isPng(in)) {
				throw new IOException("image format is not png ");
			}
			resizePng(in, out, maxWidth, maxHeight, quality, watermark, font, fontColor, ratio, rotaton);
			return;
		} else if (type == 3) {
			if (isJpg(in)) {
				resizeJpg(in, out, maxWidth, maxHeight, quality, watermark, font, fontColor, ratio, rotaton);
				return;
			} else if (isPng(in)) {
				resizePng(in, out, maxWidth, maxHeight, quality, watermark, font, fontColor, ratio, rotaton);
				return;
			}
			throw new IOException("image format is not jpg or png ");
		} else if (type == 4) {
			if (!isGif(in)) {
				throw new IOException("image format is not gif ");
			}
			resizeGif(in, out, maxWidth, maxHeight, quality, watermark, font, fontColor, ratio, rotaton);
			return;
		} else if (type == 5) {
			if (isJpg(in)) {
				resizeJpg(in, out, maxWidth, maxHeight, quality, watermark, font, fontColor, ratio, rotaton);
				return;
			} else if (isGif(in)) {
				resizeGif(in, out, maxWidth, maxHeight, quality, watermark, font, fontColor, ratio, rotaton);
				return;
			}
			throw new IOException("image format is not jpg or gif ");
		} else if (type == 6) {
			if (isPng(in)) {
				resizePng(in, out, maxWidth, maxHeight, quality, watermark, font, fontColor, ratio, rotaton);
				return;
			} else if (isGif(in)) {
				resizeGif(in, out, maxWidth, maxHeight, quality, watermark, font, fontColor, ratio, rotaton);
				return;
			}
			throw new IOException("image format is not png or gif ");
		} else if (type == 7) {
			if (isJpg(in)) {
				resizeJpg(in, out, maxWidth, maxHeight, quality, watermark, font, fontColor, ratio, rotaton);
				return;
			} else if (isPng(in)) {
				resizePng(in, out, maxWidth, maxHeight, quality, watermark, font, fontColor, ratio, rotaton);
				return;
			} else if (isGif(in)) {
				resizeGif(in, out, maxWidth, maxHeight, quality, watermark, font, fontColor, ratio, rotaton);
				return;
			}
			throw new IOException("image format is not jpg or png or gif ");
		}

	}

	public static void resizeJpg(InputStream in, OutputStream out, int maxWidth, int maxHeight, float quality,
			String[] watermark, Font font, Color fontColor,int ratio,int rotaton) throws IOException {
		checkParams(in, out, maxWidth, maxHeight, quality,ratio);
		//
		BufferedImage image = ImageIO.read(in);
		int origWidth =  image.getWidth();
		int origHeight = image.getHeight();
		if(maxWidth==0){
			maxWidth = origWidth/ratio;
		}
		if(maxHeight==0){
			maxHeight = origHeight/ratio;
		}
		image = Scalr.resize(image, Scalr.Method.AUTOMATIC, Scalr.Mode.AUTOMATIC, maxWidth>origWidth?origWidth:maxWidth, maxHeight>origHeight?origHeight:maxHeight);

		if(rotaton==90){
			image = Scalr.rotate(image, Scalr.Rotation.CW_90);
		}else if(rotaton==180){
			image = Scalr.rotate(image, Scalr.Rotation.CW_180);
		}else if(rotaton==270){
			image = Scalr.rotate(image, Scalr.Rotation.CW_270);
		}

		// create new image with right size/format
		BufferedImage bufferedImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
		Graphics2D g = bufferedImage.createGraphics();
		// 因为有的图片背景是透明色，所以用白色填充 FIXED
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 1));
		g.fillRect(0, 0, image.getWidth(), image.getHeight());
		g.drawImage(image, 0, 0, null);
		image = bufferedImage;
		//
		if (watermark != null && watermark.length > 0) {
			makeWatermark(watermark, image, font, fontColor);
		}
		//
		JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
		JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(image);
		param.setQuality(quality, false);
		encoder.setJPEGEncodeParam(param);
		encoder.encode(image);
	}

	public static void resizePng(InputStream in, OutputStream out, int maxWidth, int maxHeight, float quality,
			String[] watermark, Font font, Color fontColor,int ratio,int rotaton) throws IOException {
		checkParams(in, out, maxWidth, maxHeight, quality,ratio);
		//
		BufferedImage image = ImageIO.read(in);
		int origWidth =  image.getWidth();
		int origHeight = image.getHeight();
		if(maxWidth==0){
			maxWidth = origWidth/ratio;
		}
		if(maxHeight==0){
			maxHeight = origHeight/ratio;
		}
		image = Scalr.resize(image, Scalr.Method.AUTOMATIC, Scalr.Mode.AUTOMATIC, maxWidth>origWidth?origWidth:maxWidth, maxHeight>origHeight?origHeight:maxHeight);

		if(rotaton==90){
			image = Scalr.rotate(image, Scalr.Rotation.CW_90);
		}else if(rotaton==180){
			image = Scalr.rotate(image, Scalr.Rotation.CW_180);
		}else if(rotaton==270){
			image = Scalr.rotate(image, Scalr.Rotation.CW_270);
		}

		if (watermark != null && watermark.length > 0) {
			makeWatermark(watermark, image, font, fontColor);
		}
		ImageIO.write(image, "png", out);
	}

	public static void resizeGif(InputStream in, OutputStream out, int maxWidth, int maxHeight, float quality,
			String[] watermark, Font font, Color fontColor,int ratio,int rotaton) throws IOException {
		checkParams(in, out, maxWidth, maxHeight, quality,ratio);
		//
		GifDecoder gd = new GifDecoder();
		int status = gd.read(in);
		if (status != GifDecoder.STATUS_OK) {
			return;
		}
		//
		AnimatedGifEncoder ge = new AnimatedGifEncoder();
		ge.start(out);
		ge.setRepeat(0);

		for (int i = 0; i < gd.getFrameCount(); i++) {
			BufferedImage frame = gd.getFrame(i);
			int origWidth =  frame.getWidth();
			int origHeight = frame.getHeight();
			if(maxWidth==0){
				maxWidth = origWidth/ratio;
			}
			if(maxHeight==0){
				maxHeight = origHeight/ratio;
			}
			BufferedImage rescaled = Scalr.resize(frame, Scalr.Method.AUTOMATIC, Scalr.Mode.AUTOMATIC, maxWidth>origWidth?origWidth:maxWidth, maxHeight>origHeight?origHeight:maxHeight);
			if(rotaton==90){
				rescaled = Scalr.rotate(rescaled, Scalr.Rotation.CW_90);
			}else if(rotaton==180){
				rescaled = Scalr.rotate(rescaled, Scalr.Rotation.CW_180);
			}else if(rotaton==270){
				rescaled = Scalr.rotate(rescaled, Scalr.Rotation.CW_270);
			}
			if (watermark != null && watermark.length > 0) {
				makeWatermark(watermark, rescaled, font, fontColor);
			}
			//
			int delay = gd.getDelay(i);
			ge.setDelay(delay);
			ge.addFrame(rescaled);
		}

		ge.finish();
	}

	private static void makeWatermark(String[] text, BufferedImage image, Font font, Color fontColor) {
		Graphics2D graphics = image.createGraphics();
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		if (font != null) {
			graphics.setFont(font);
		} else {
			graphics.setFont(FONT);
		}
		if (fontColor == null) {
			fontColor = COLOR;
		}
		//
		graphics.setColor(fontColor);
		for (int i = 0; i < text.length; i++) {
			if ("".equals(text[i].trim())) {
				continue;
			}
			FontRenderContext context = graphics.getFontRenderContext();
			Rectangle2D fontRectangle = font.getStringBounds(text[i], context);
			int sw = (int) fontRectangle.getWidth();
			int sh = (int) fontRectangle.getHeight();
			if (text.length - i == 1) {
				graphics.drawString(text[i], image.getWidth() - sw - 6, image.getHeight() - 8);
			} else {
				graphics.drawString(text[i], image.getWidth() - sw - 6, image.getHeight() - sh * (text.length - 1) - 8);
			}
		}
		graphics.dispose();
	}

	private static void checkParams(InputStream in, OutputStream out, int maxWidth, int maxHeight, float quality,int ratio)
			throws IOException {
		if (in == null) {
			throw new IOException("InputStream can not be null ");
		}
		if (out == null) {
			throw new IOException("OutputStream can not be null ");
		}
		if ((maxWidth < 1 || maxHeight < 1) && ratio==0) {
			throw new IOException("maxWidth or maxHeight can not be less than 1 ");
		}
		if (quality < 0f || quality > 1f) {
			throw new IOException("quality must be in [0-1] ");
		}
	}

	public static void main(String[] args) throws IOException {
		FileInputStream in = new FileInputStream(new File("C:/Users/lenovo/Desktop/新建文件夹/流水1.jpg"));
		FileOutputStream out = new FileOutputStream(new File("C:/Users/lenovo/Desktop/新建文件夹/流水11.jpg"));
		BufferedInputStream bin = new BufferedInputStream(in);
		BufferedOutputStream bout = new BufferedOutputStream(out);
		int degreeOfRotation = getDegreeOfRotation(new File("C:/Users/lenovo/Desktop/新建文件夹/流水1.jpg"));
		try {
			long start = System.currentTimeMillis();
			resize(bin, bout, 1024, 1024, 7, 1f, null, FONT, FONT_COLOR, 1, degreeOfRotation);
			long end = System.currentTimeMillis();
			System.out.println("test spend time:" + (end-start));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			IOUtils.closeQuietly(out);
			IOUtils.closeQuietly(in);
		}



	}

	/**
	 * 根据照片拍摄方向获取图片需要旋转的角度，也就是把斜的图片摆正
	 * @throws IOException
	 */
	public static int  getDegreeOfRotation(File soureceFile) throws IOException {
		//获取照片的Exif信息
		Metadata metadata = null;
		int degreeOfRotation=0;
		try {
			metadata = ImageMetadataReader.readMetadata(soureceFile);
		} catch (ImageProcessingException e) {
			e.printStackTrace();
			return 0;
		}
		//Directory exif = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
		ExifIFD0Directory ifd0Dir = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
		//ExifSubIFDDirectory subIfdDir = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
		//ExifThumbnailDirectory thumbDir = metadata.getFirstDirectoryOfType(ExifThumbnailDirectory.class);
		//获取照片拍摄方向
		String type = ifd0Dir ==null ? null : ifd0Dir.getString(ExifDirectoryBase.TAG_ORIENTATION);
		//图片正常情况下，type=1,测试正常图片旋转，请修改case 条件为 1
		if(type!=null && !"".equals(type)){//判断拍摄方向是否为空
			switch (Integer.parseInt(type)) {
				case 3://要进行180度旋转
					degreeOfRotation=180;
					return degreeOfRotation;
				case 6://要进行90度旋转
					degreeOfRotation=90;
					return degreeOfRotation;
				case 8://要进行270度旋转
					degreeOfRotation=270;
					return degreeOfRotation;
				default :
					degreeOfRotation=0;
					return degreeOfRotation;
			}
		}
		return degreeOfRotation;
	}
}
