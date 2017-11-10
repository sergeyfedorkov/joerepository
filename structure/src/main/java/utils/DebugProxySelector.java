package utils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.net.Proxy.Type;
import java.util.ArrayList;
import java.util.List;

public class DebugProxySelector extends ProxySelector {
	public List<Proxy> select(URI uri) {
		 List<Proxy> proxyList = new ArrayList<Proxy>();
		 proxyList.add(new Proxy(Type.HTTP, new InetSocketAddress("127.0.0.1", 8888)));
		 return proxyList;
	}

	public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {}
}