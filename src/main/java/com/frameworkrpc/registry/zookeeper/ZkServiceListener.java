package com.frameworkrpc.registry.zookeeper;

import com.frameworkrpc.common.RpcConstant;
import com.frameworkrpc.exception.CommonRpcException;
import com.frameworkrpc.model.URL;
import com.frameworkrpc.registry.ServiceListener;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ZkServiceListener implements ServiceListener {
	@Override
	public void handleNodeChildChange(String parentPath, List<String> currentChilds, ConcurrentHashMap<String, List<URL>> serviceDiscoveUrls) {
		if (currentChilds.size() <= 0) {
			serviceDiscoveUrls.remove(parentPath);
			return;
		}

		Stream<URL> currentChildsStream = currentChilds.stream().map(url -> {
			try {
				return new URL(URLDecoder.decode(url, RpcConstant.CHARSET));
			} catch (UnsupportedEncodingException e) {
				throw new CommonRpcException(e.getMessage(), e);
			}
		});

		serviceDiscoveUrls.put(parentPath, currentChildsStream.collect(Collectors.toList()));
	}

	@Override
	public void handleNodeDataDeleted(String parentPath, String currentNode, ConcurrentHashMap<String, List<URL>> serviceDiscoveUrls) {

	}
}