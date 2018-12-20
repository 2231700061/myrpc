package com.myrpc.serialize.Jdk;

import com.myrpc.extension.RpcComponent;
import com.myrpc.exception.MyRpcSerializeException;
import com.myrpc.serialize.Serialize;

import java.io.*;

@RpcComponent(name = "jdk")
public class JdkSerialize implements Serialize {

	@Override
	public <T> byte[] serialize(T object) {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			ObjectOutputStream out = null;
			out = new ObjectOutputStream(os);
			out.writeObject(object);
		} catch (IOException e) {
			throw new MyRpcSerializeException(e.getMessage(), e);
		}
		return os.toByteArray();
	}

	@Override
	public <T> T deserialize(byte[] data, Class<T>... cls) {
		ByteArrayInputStream is = new ByteArrayInputStream(data);
		try {
			ObjectInputStream in = new ObjectInputStream(is);
			return (T) in.readObject();
		} catch (ClassNotFoundException e) {
			throw new MyRpcSerializeException(e.getMessage(), e);
		} catch (IOException e) {
			throw new MyRpcSerializeException(e.getMessage(), e);
		}
	}
}
