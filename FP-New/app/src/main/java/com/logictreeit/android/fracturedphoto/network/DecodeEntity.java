package com.logictreeit.android.fracturedphoto.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;

public class DecodeEntity {

	/**
	 * It will extract the content from HttpEntity and converts into String
	 * format
	 * 
	 * @param entity
	 * @return content in String format.
	 * @throws IllegalStateException
	 *             Thrown when an action is attempted at a time when the VM is
	 *             not in the correct state.
	 * @throws IOException
	 *             Signals a general, I/O-related error. Error details may be
	 *             specified when calling the constructor, as usual. Note there
	 *             are also several subclasses of this class for more specific
	 *             error situations, such as FileNotFoundException or
	 *             EOFException.
	 */
	protected static String getASCIIContentFromEntity(HttpEntity entity)
			throws IllegalStateException, IOException {
		InputStream in = entity.getContent();
		BufferedReader bufferedReader = new BufferedReader(
				new InputStreamReader(in));
		StringBuffer out = new StringBuffer();
		String line;
		while ((line = bufferedReader.readLine()) != null) {
			out.append(line);
		}
		return out.toString();
	}
}
