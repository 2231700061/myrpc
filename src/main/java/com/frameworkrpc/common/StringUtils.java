package com.frameworkrpc.common;

public class StringUtils {

	public static boolean isEmpty(Object str) {
		return str == null || "".equals(str);
	}
}