package com.networknt.saga.orchestration;


import com.networknt.eventuate.common.impl.JSonMapper;

public class SagaDataSerde {

  static <Data> SerializedSagaData serializeSagaData(Data sagaData) {
    return new SerializedSagaData(sagaData.getClass().getName(), JSonMapper.toJson(sagaData));
  }

  static <Data> Data deserializeSagaData(SerializedSagaData serializedSagaData) {
    Class<?> clasz = null;
    try {
      clasz = SagaDataSerde.class.getClassLoader().loadClass(serializedSagaData.getSagaDataType());
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
    Object x = JSonMapper.fromJson(serializedSagaData.getSagaDataJSON(), clasz);
    return (Data)x;
  }
}
