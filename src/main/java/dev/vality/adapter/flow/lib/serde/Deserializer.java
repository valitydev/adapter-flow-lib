package dev.vality.adapter.flow.lib.serde;

public interface Deserializer<T> {

    T read(byte[] data);

    T read(String data);

}
