package com.myrpc.server.exporter;

import com.myrpc.common.RpcConstants;
import com.myrpc.extension.ExtensionLoader;
import com.myrpc.extension.Scope;
import com.myrpc.model.URL;
import com.myrpc.registry.Registry;
import com.myrpc.server.Server;
import com.myrpc.server.ServerFactory;

import java.io.Serializable;
import java.util.concurrent.locks.ReentrantLock;

public class Exporter implements Serializable {

	private static final long serialVersionUID = -6374024642709901255L;
	protected static Server server;
	protected static final ReentrantLock lock = new ReentrantLock();
	protected URL url;
	protected Registry registry;

	public Exporter with(URL url) {
		this.url = url;
		return this;
	}

	public Exporter init() {
		if (server == null) {
			try {
				lock.lock();
				if (server == null) {
					server = ExtensionLoader.getExtensionLoader(ServerFactory.class)
							.getExtension(url.getParameter(RpcConstants.TRANSPORTER_KEY), Scope.SINGLETON).getServer(url);
				}
			} finally {
				lock.unlock();
			}
		}

		this.registry = ExtensionLoader.getExtensionLoader(Registry.class).getExtension(url.getParameter(RpcConstants.REGISTRY_NAME_KEY)).with(url)
				.init();
		return this;
	}

	public Exporter start() {
		try {
			lock.lock();
			if (!server.isOpened()) {
				server.doOpen();
			}
		} finally {
			lock.unlock();
		}
		return this;
	}


	public Exporter publish() {
		URL registerUrl = url.addParameters(RpcConstants.CATEGORY_KEY, RpcConstants.DEFAULT_CATEGORY);
		registry.register(registerUrl);
		return this;
	}

	public Exporter unpublish() {
		URL registerUrl = url.addParameters(RpcConstants.CATEGORY_KEY, RpcConstants.DEFAULT_CATEGORY);
		registry.unregister(registerUrl);
		return this;
	}

	public Exporter shutdown() {
		if (!server.isClosed()) {
			server.doClose();
		}
		this.registry.close();
		return this;
	}

}
