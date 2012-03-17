package com.facebook.zookeeper;

public class VariablePayload implements Encodable {
  private String strData;

  public VariablePayload(String strData) {
    this.strData = strData;
  }

  public void setPayload(String strData) {
    this.strData = strData;
  }

  @Override
  public byte[] encode() {
    return ZkUtil.stringToBytes(strData);
  }

  public static String decode(byte[] byteData) {
    return ZkUtil.bytesToString(byteData);
  }
}
