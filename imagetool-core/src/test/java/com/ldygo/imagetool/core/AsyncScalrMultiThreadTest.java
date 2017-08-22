/**   
 * Copyright 2011 The Buzz Media, LLC
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ldygo.imagetool.core;

import com.ldygo.imagetool.core.utils.ImageUtil;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class AsyncScalrMultiThreadTest{
	private static int ITERS = 10;

	@Test
	public void test() throws InterruptedException {
		List<Thread> threadList = new ArrayList<Thread>(ITERS);
		
		for (int i = 0; i < ITERS; i++) {
			if (i % 100 == 0)
				System.out.println("Scale Iteration " + i);

			try {
				Thread t = new ScaleThread();
				t.start();
				threadList.add(t);
			} catch (OutOfMemoryError error) {
				System.out.println("Cannot create any more threads, last created was " + i);
				ITERS = i;
				break;
			}
		}
		
		// Now wait for all the threads to finish
		for (int i = 0; i < ITERS; i++) {
			if (i % 100 == 0)
				System.out.println("Thread Finished " + i);

			threadList.get(i).join();
		}
		

	}

	public class ScaleThread extends Thread {
		@Override
		public void run() {
			try {
				FileInputStream in = new FileInputStream(new File("C:/Users/lenovo/Desktop/新建文件夹/流水1.jpg"));
				FileOutputStream out = new FileOutputStream(new File("C:/Users/lenovo/Desktop/新建文件夹/流水11.jpg"));
				BufferedInputStream bin = new BufferedInputStream(in);
				BufferedOutputStream bout = new BufferedOutputStream(out);
				int degreeOfRotation = ImageUtil.getDegreeOfRotation(new File("C:/Users/lenovo/Desktop/新建文件夹/流水1.jpg"));
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
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}