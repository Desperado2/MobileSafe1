package com.jack.mobilesafe.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class StreamUtils {
	public static String toString(InputStream in) throws IOException{
		
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		
		int len = 0;
		byte[] buffer = new byte[1024];
		if((len = in.read(buffer))!= -1){
			stream.write(buffer, 0, len);
		}
		stream.close();
		in.close();
		String result = stream.toString();
		return result;
		
	}
}
