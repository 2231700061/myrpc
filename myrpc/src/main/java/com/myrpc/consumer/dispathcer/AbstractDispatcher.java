package com.myrpc.consumer.dispathcer;

import com.myrpc.client.Client;
import com.myrpc.client.ClientFactory;
import com.myrpc.common.RpcConstants;
import com.myrpc.consumer.future.DefaultInvokeFuture;
import com.myrpc.consumer.future.InvokeFuture;
import com.myrpc.consumer.loadbalance.LoadBalance;
import com.myrpc.exception.MyRpcInvokeException;
import com.myrpc.extension.ExtensionLoader;
import com.myrpc.extension.Scope;
import com.myrpc.model.RpcRequest;
import com.myrpc.config.URL;
import com.myrpc.registry.Registry;
import com.myrpc.registry.RegistryListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

abstract class AbstractDispatcher implements Dispatcher {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	protected final Map<String, Client> serverClients = new ConcurrentHashMap<>();
	protected URL url;
	protected LoadBalance loadBalance;
	protected Registry registry;
	protected RegistryListener registryListener;

	protected AbstractDispatcher(URL url) {
		this.url = url;

		this.loadBalance = ExtensionLoader.getExtensionLoader(LoadBalance.class).getExtension(url.getParameter(RpcConstants.LOADBALANCE_KEY));

		this.registry = ExtensionLoader.getExtensionLoader(Registry.class).getExtension(url.getParameter(RpcConstants.REGISTRY_NAME_KEY)).with(url)
				.init();

		this.registryListener = new RegistryListener(url);
		URL subscribeUrl = url.addParameters(RpcConstants.CATEGORY_KEY, RpcConstants.DEFAULT_CATEGORY);
		this.registry.subscribe(subscribeUrl, registryListener);
		URL providerUrl = url.addParameters(RpcConstants.CATEGORY_KEY, RpcConstants.CONSUMERS_CATEGORY);
		this.registry.register(providerUrl);
	}

	protected List<String> getServerNodes(URL url) {
		List<URL> providerUrls = registryListener.getProviderUrls();
		if (providerUrls == null || providerUrls.isEmpty()) {
			throw new MyRpcInvokeException("providerUrl can not be empty");
		}
		Set<String> newAllServerNodeSet = new HashSet<>();

		if (providerUrls != null) {
			if (providerUrls.size() > 0) {
				for (URL providerUrl : providerUrls) {
					String serverNodeAddress = providerUrl.getServerPortStr();
					if (!serverClients.keySet().contains(serverNodeAddress)) {
						providerUrl = providerUrl.addParameters(RpcConstants.CONNECTTIMEOUT_KEY, url.getParameter(RpcConstants.CONNECTTIMEOUT_KEY));
						Client client = ExtensionLoader.getExtensionLoader(ClientFactory.class)
								.getExtension(url.getParameter(RpcConstants.TRANSPORTER_KEY), Scope.SINGLETON).getClient(providerUrl);
						if (!client.isConnected())
							client.doConnect();
						serverClients.put(serverNodeAddress, client);
					}
				}

				for (URL providerUrl : providerUrls) {
					newAllServerNodeSet.add(providerUrl.getServerPortStr());
				}
				for (Map.Entry entry : serverClients.entrySet()) {
					if (!newAllServerNodeSet.contains(entry.getKey())) {
						Client client = serverClients.get(entry.getKey());
						if (client.isConnected())
							client.disConnect();
						serverClients.remove(entry.getKey());
					}
				}
			} else {
				logger.error("No available server node. All server nodes are down !!!");
				for (Map.Entry entry : serverClients.entrySet()) {
					Client client = serverClients.get(entry.getKey());
					if (client.isConnected())
						client.disConnect();
					serverClients.remove(entry.getKey());
				}
				serverClients.clear();
			}
		}
		return new ArrayList<>(newAllServerNodeSet);
	}

	protected Client getServerClient(URL url) {
		List<String> serverNodes = getServerNodes(url);
		String serverNode = loadBalance.select(serverNodes);
		Client client = serverClients.get(serverNode);
		if (!client.isConnected())
			client.doConnect();
		return client;
	}

	protected <T> InvokeFuture<T> write(final RpcRequest request, final Class<T> returnType) {
		final InvokeFuture<T> future = new DefaultInvokeFuture<T>(request).with(returnType);
		getServerClient(url).request(request);
		return future;
	}

}
