package com.ldygo.imagetool.core;

import com.ldygo.imagetool.core.utils.ImageUtil;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 并发测试
 * Jason shen
 */
public class MutiThreadTest {
    public static void main(String[] args) throws InterruptedException {
		final int size = 1; //并发
		final CountDownLatch cdl = new CountDownLatch(size);

		ExecutorService executorServicePool = Executors.newFixedThreadPool(200);
		long begin = System.currentTimeMillis();
		for (int i = 0; i < size; i++) {
			executorServicePool.execute(new Runnable() {
				@Override
				public void run() {
					try {
						FileInputStream in = new FileInputStream(new File("C:/Users/lenovo/Desktop/新建文件夹/征信报告4.jpg"));
						FileOutputStream out = new FileOutputStream(new File("C:/Users/lenovo/Desktop/新建文件夹/征信报告44.jpg"));
						BufferedInputStream bin = new BufferedInputStream(in);
						BufferedOutputStream bout = new BufferedOutputStream(out);
						int degreeOfRotation = ImageUtil.getDegreeOfRotation(new File("C:/Users/lenovo/Desktop/新建文件夹/征信报告4.jpg"));
						try {
							long start = System.currentTimeMillis();
							ImageUtil.resize(bin, bout, 1024, 1024, 7, 1f, null, ImageUtil.FONT, ImageUtil.FONT_COLOR, 1, degreeOfRotation);
							long end = System.currentTimeMillis();
							System.out.println("test spend time:" + (end-start));
						} catch (Exception e) {
							e.printStackTrace();
						} finally {
							IOUtils.closeQuietly(out);
							IOUtils.closeQuietly(in);
							cdl.countDown();
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		}
		//executorServicePool.shutdown();
		//executorService.awaitTermination(10, TimeUnit.MINUTES);
		cdl.await();//等待所有任务处理完
		System.out.println("并发数:" + size );
		long time = System.currentTimeMillis() - begin;
        System.out.println("耗时：" + (double) time / 1000 + " s");
        System.out.println("平均:" + ((double) time) / size +" ms");
        System.out.println("TPS:" + (double) size / ((double) time / 1000));
        
	}

}


