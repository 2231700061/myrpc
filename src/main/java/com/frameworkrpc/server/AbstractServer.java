package com.frameworkrpc.server;

import com.frameworkrpc.model.URL;

public class AbstractServer {

	protected volatile boolean isOpened;
	protected volatile boolean isClosed;
	protected URL url;

	public AbstractServer(URL url) {
		this.url = url;
	}

	public URL getUrl() {
		return url;
	}


}
