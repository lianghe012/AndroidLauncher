package com.android.launcher3.util;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

public class FileUtils {

	public static final int WHAT_COPY_START = 1;
	public static final int WHAT_COPY_PROGRESS = 2;
	public static final int WHAT_COPY_FINISH = 3;
	private static final char HEX_DIGITS[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
		 'A', 'B', 'C', 'D', 'E', 'F' };
	
	 public static String toHexString(byte[] b) {
	        StringBuilder sb = new StringBuilder(b.length * 2);
	        for (int i = 0; i < b.length; i++) {
	            sb.append(HEX_DIGITS[(b[i] & 0xf0) >>> 4]);
	            sb.append(HEX_DIGITS[b[i] & 0x0f]);
	        }
	        return sb.toString();
	    }
	
	public static String md5sum(String filename) {
     InputStream fis;
     byte[] buffer = new byte[1024];
     int numRead = 0;
     MessageDigest md5;
     try{
         fis = new FileInputStream(filename);
         md5 = MessageDigest.getInstance("MD5");
         while((numRead=fis.read(buffer)) > 0) {
             md5.update(buffer,0,numRead);
         }
         fis.close();
         return toHexString(md5.digest());  
     } catch (Exception e) {
         System.out.println("error");
         return null;
     }
 }

	public static boolean mkdir(String path) {
		boolean ret = false;
		File myDir = new File(path);
		if (!myDir.exists()) {
			ret = myDir.mkdirs();
		}
		return ret;
	}

	public static void createFile(String path, String text) throws IOException {
		File myFile = new File(path);
		if (!myFile.exists()) {
			myFile.createNewFile();
		}
		if (!text.equals("")) {
			rewriteFile(myFile, text);
		}
	}

	public static void rewriteFile(File file, String text) throws IOException {
		FileWriter myFileWriter = new FileWriter(file);
		myFileWriter.write(text);
		myFileWriter.close();
	}

	public static void rewriteFile(String path, String text) throws IOException {
		File myFile = new File(path);
		rewriteFile(myFile, text);
	}

	public static void appendFile(File file, String text) throws IOException {
		FileWriter myFileWriter = new FileWriter(file);
		myFileWriter.append(text);
		myFileWriter.close();
	}
	
	public static void appendFile(File file, String text ,boolean append) throws IOException {
		FileWriter myFileWriter = new FileWriter(file,append);
		myFileWriter.append(text);
		myFileWriter.close();
	}

	public static void appendFile(String path, String text) throws IOException {
		File myFile = new File(path);
		appendFile(myFile, text);
	}

	public static boolean deleteFile(String path) {
		File myFile = new File(path);
		return myFile.delete();
	}

	public static boolean deleteDir(String path) {
		deleteSubFiles(path);
		File myDir = new File(path);
		return myDir.delete();
	}

	public static void deleteSubFiles(String path) {
		File myFile = new File(path);
		if (!myFile.exists()) {
			return;
		}
		if (!myFile.isDirectory()) {
			return;
		}
		String[] tempList = myFile.list();
		File temp = null;
		for (int i = 0; i < tempList.length; i++) {
			if (path.endsWith(File.separator)) {
				temp = new File(path + tempList[i]);
			} else {
				temp = new File(path + File.separator + tempList[i]);
			}
			if (temp.isFile()) {
				temp.delete();
			}
			if (temp.isDirectory()) {
				deleteSubFiles(path + File.separator + tempList[i]);
				deleteDir(path + File.separator + tempList[i]);
			}
		}
	}

	public static void copyFile(String source, String dest, Handler hProgress)
			throws IOException {

		File oldFile = new File(source);
		if (oldFile.exists()) {
			InputStream is = new FileInputStream(source);
			FileOutputStream fs = new FileOutputStream(dest);

			int size = is.available();
			if (hProgress != null) {
				Message msg = new Message();
				msg.what = WHAT_COPY_START;
				msg.arg1 = 0;
				msg.arg2 = size;
				hProgress.sendMessage(msg);
			}
			int count = 0;
			int n = 0;

			byte[] buffer = new byte[1444];
			while ((n = is.read(buffer)) != -1) {
				fs.write(buffer, 0, n);
				count += n;
				if (hProgress != null) {
					Message msg = new Message();
					msg.what = WHAT_COPY_PROGRESS;
					msg.arg1 = count;
					msg.arg2 = size;
					hProgress.sendMessage(msg);
				}
			}
			is.close();
			fs.close();
			if (hProgress != null) {
				Message msg = new Message();
				msg.what = WHAT_COPY_FINISH;
				msg.arg1 = 0;
				msg.arg2 = 0;
				hProgress.sendMessage(msg);
			}
		}
	}

	public static void copyFolder(String source, String dest)
			throws IOException {
		new File(dest).mkdirs();
		File a = new File(source);
		String[] file = a.list();
		File temp = null;
		for (int i = 0; i < file.length; i++) {
			if (source.endsWith(File.separator)) {
				temp = new File(source + file[i]);
			} else {
				temp = new File(source + File.separator + file[i]);
			}

			if (temp.isFile()) {
				FileInputStream input = new FileInputStream(temp);
				FileOutputStream output = new FileOutputStream(dest
						+ File.separator + (temp.getName()).toString());
				byte[] b = new byte[1024 * 5];
				int len;
				while ((len = input.read(b)) != -1) {
					output.write(b, 0, len);
				}
				output.flush();
				output.close();
				input.close();
			}
			if (temp.isDirectory()) {
				copyFolder(source + File.separator + file[i], dest
						+ File.separator + file[i]);
			}
		}

	}

	public static void moveFile(String source, String dest, Handler hProgress)
			throws IOException {
		copyFile(source, dest, hProgress);
		deleteFile(source);

	}

	public static void moveFolder(String source, String dest)
			throws IOException {
		copyFolder(source, dest);
		deleteDir(source);
	}

	public static List<String> readFile(File file) throws IOException {
		FileReader myFileReader = new FileReader(file);
		BufferedReader myBufferedReader = new BufferedReader(myFileReader);
		String line;
		List<String> fileText = new ArrayList<String>();
		while ((line = myBufferedReader.readLine()) != null) {
			fileText.add(line);
		}
		myBufferedReader.close();
		myFileReader.close();
		return fileText;
	}

	public static String toString(List<String> list) {
		String ret = "";
		for (String s : list) {
			ret += s + "\n";
		}
		return ret;
	}

	public static List<String> readFile(String path) throws IOException {
		File myFile = new File(path);
		return readFile(myFile);
	}

	public static String readAssetFile(Context context, String fileName)
			throws IOException {
		InputStream is = context.getAssets().open(fileName);
		byte[] bytes = new byte[1024];
		ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
		while (is.read(bytes) != -1) {
			arrayOutputStream.write(bytes, 0, bytes.length);
		}
		is.close();
		arrayOutputStream.close();
		String text = new String(arrayOutputStream.toByteArray());
		return text.trim();
	}

	public static List<String> readAssertFileAsList(Context context,
			String fileName) throws IOException {
		InputStream is = context.getAssets().open(fileName);
		InputStreamReader myStreamReader = new InputStreamReader(is);
		BufferedReader myBufferedReader = new BufferedReader(myStreamReader);
		String line;
		List<String> fileText = new ArrayList<String>();
		while ((line = myBufferedReader.readLine()) != null) {
			fileText.add(line);
		}
		myBufferedReader.close();
		myStreamReader.close();
		return fileText;
	}

	public static boolean copyAssetFile(Context context, String fileName,
			String saveDir, Handler hProgress) {
		File fBusybox = new File(saveDir);
		if (!fBusybox.exists()) {
			fBusybox.mkdirs();
		}
		try {
			byte[] buffer = new byte[8192];

			File dest = new File(saveDir + fileName);

			if (dest.exists()) {
				dest.delete();
			}

			InputStream is = context.getAssets().open(fileName);
			OutputStream fos = new BufferedOutputStream(new FileOutputStream(
					dest));

			int count = 0;
			int size = is.available();
			if (hProgress != null) {
				Message msg = new Message();
				msg.what = WHAT_COPY_START;
				msg.arg1 = 0;
				msg.arg2 = size;
				hProgress.sendMessage(msg);
			}
			int n;
			while ((n = is.read(buffer, 0, buffer.length)) != -1) {
				fos.write(buffer, 0, n);
				count += n;
				if (hProgress != null) {
					Message msg = new Message();
					msg.what = WHAT_COPY_PROGRESS;
					msg.arg1 = count;
					msg.arg2 = size;
					hProgress.sendMessage(msg);
				}
			}

			is.close();
			fos.close();
			if (hProgress != null) {
				Message msg = new Message();
				msg.what = WHAT_COPY_FINISH;
				msg.arg1 = 0;
				msg.arg2 = 0;
				hProgress.sendMessage(msg);
			}
			return true;
		} catch (Exception ex) {
			return false;
		}
	}

	public static String readFile(Context context, String path)
			throws IOException {
//		InputStream is = context.openFileInput(path);
		File file = new File(path);
		InputStream is = new FileInputStream(file);
		byte[] bytes = new byte[1024];
		ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
		int length = -1;
		while ((length = is.read(bytes)) != -1) {
			arrayOutputStream.write(bytes, 0, length);
		}
		is.close();
		arrayOutputStream.close();
		String text = new String(arrayOutputStream.toByteArray());
		return text;
	}
	
	public static String readFile(Context context, File file)
			throws IOException {
//		InputStream is = context.openFileInput(path);
		InputStream is = new FileInputStream(file);
		byte[] bytes = new byte[1024];
		ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
		int length = -1;
		while ((length = is.read(bytes)) != -1) {
			arrayOutputStream.write(bytes, 0, length);
		}
		is.close();
		arrayOutputStream.close();
		String text = new String(arrayOutputStream.toByteArray());
		return text;
	}

	public static List<?> loadListFromFile(String path) {
		List<?> list = null;
		try {
			FileInputStream freader = new FileInputStream(path);
			ObjectInputStream objectInputStream = new ObjectInputStream(freader);
			list = (List<?>) objectInputStream.readObject();
			objectInputStream.close();
		} catch (Exception e) {

		}
		return list;
	}

	public static void saveListToFile(List<?> list, String path) {
		try {
			FileOutputStream outStream = new FileOutputStream(path);
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(
					outStream);
			objectOutputStream.writeObject(list);
			outStream.close();
		} catch (Exception e) {

		}
	}

	public static long getDirSize(String path) {
		return getDirSize(new File(path));
	}

	public static long getDirSize(File dir) {
		if (dir == null) {
			return 0;
		}
		if (!dir.isDirectory()) {
			return 0;
		}
		long dirSize = 0;
		File[] files = dir.listFiles();
		for (File file : files) {
			if (file.isFile()) {
				dirSize += file.length();
			} else if (file.isDirectory()) {
				dirSize += file.length();
				dirSize += getDirSize(file);
			}
		}
		return dirSize;
	}

	public static void writeIn(String path , String name,String content,boolean flag){
		File file = new File(path + name);
		byte[] buf = new byte[1024];
		buf =content.getBytes();
		try {
			FileOutputStream op = new FileOutputStream(file,flag);
			op.write(buf);
			op.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void saveToPrefrence(Context context , String fileName,String key,int value){
		SharedPreferences preferences = context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
		Editor editor = preferences.edit();
		editor.putInt(key, value);
		editor.commit();
	}
	
	public static int getChannalFromPrefrence(Context context , String fileName,String key){
		SharedPreferences preferences = context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
		return preferences.getInt(key, 0);
	}
	
	private static final String FILE_NAME_RESERVED = "|\\?*<\":>+[]/'";

	public static String getCanonical(File f) {
		if (f == null)
			return null;

		try {
			return f.getCanonicalPath();
		} catch (IOException e) {
			return f.getAbsolutePath();
		}
	}

	/**
	 * Get the path for the file:/// only
	 * 
	 * @param uri
	 * @return
	 */
	public static String getPath(String uri) {
		if (TextUtils.isEmpty(uri))
			return null;
		if (uri.startsWith("file://") && uri.length() > 7)
			return Uri.decode(uri.substring(7));
		return Uri.decode(uri);
	}

	public static String getName(String uri) {
		String path = getPath(uri);
		if (path != null)
			return new File(path).getName();
		return null;
	}

	public static void deleteDir(File f) {
		if (f.exists() && f.isDirectory()) {
			for (File file : f.listFiles()) {
				if (file.isDirectory())
					deleteDir(file);
				file.delete();
			}
			f.delete();
		}
	}
}
