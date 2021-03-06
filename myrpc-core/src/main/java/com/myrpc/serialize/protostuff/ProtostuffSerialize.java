package com.myrpc.serialize.protostuff;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import com.myrpc.extension.RpcComponent;
import com.myrpc.exception.MyRpcSerializeException;
import com.myrpc.serialize.Serialize;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RpcComponent(name = "protostuff")
public class ProtostuffSerialize implements Serialize {

	private static Map<Class<?>, Schema<?>> cachedSchema = new ConcurrentHashMap<>();

	private static Objenesis objenesis = new ObjenesisStd(true);

	private static <T> Schema<T> getSchema(Class<T> cls) {
		Schema<T> schema = (Schema<T>) cachedSchema.get(cls);
		if (schema == null) {
			schema = RuntimeSchema.createFrom(cls);
			if (schema != null) {
				cachedSchema.put(cls, schema);
			}
		}
		return schema;
	}

	@Override
	public <T> byte[] serialize(T object) {
		Class<T> cls = (Class<T>) object.getClass();
		LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
		try {
			Schema<T> schema = getSchema(cls);
			return ProtostuffIOUtil.toByteArray(object, schema, buffer);
		} catch (Exception e) {
			throw new MyRpcSerializeException(e.getMessage(), e);
		} finally {
			buffer.clear();
		}
	}

	@Override
	public <T> T deserialize(byte[] data, Class<T>... cls) {
		try {
			T message = (T) objenesis.newInstance(cls[0]);
			Schema<T> schema = getSchema(cls[0]);
			ProtostuffIOUtil.mergeFrom(data, message, schema);
			return message;
		} catch (Exception e) {
			throw new MyRpcSerializeException(e.getMessage(), e);
		}
	}
}
