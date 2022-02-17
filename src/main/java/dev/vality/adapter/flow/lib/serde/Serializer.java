package dev.vality.adapter.flow.lib.serde;

public interface Serializer<T> {

    byte[] writeByte(T obj);

    String writeString(T obj);

}
