/*
 *      Copyright [ 2020 - 2023 ] [Matthew Buckton]
 *
 *      Licensed under the Apache License, Version 2.0 (the "License");
 *      you may not use this file except in compliance with the License.
 *      You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *      Unless required by applicable law or agreed to in writing, software
 *      distributed under the License is distributed on an "AS IS" BASIS,
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *      See the License for the specific language governing permissions and
 *      limitations under the License.
 */

package io.mapsmessaging.devices.i2c.devices.drivers.pca9685.registers;

import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.SingleByteRegister;

import java.io.IOException;

public class Mode1Register extends SingleByteRegister {
  private static final int RESET = 0b10000000;
  private static final int EXTCLK = 0b01000000;
  private static final int AI = 0b00100000;
  private static final int SLEEP = 0b00010000;
  private static final int SUB1 = 0b00001000;
  private static final int SUB2 = 0b00000100;
  private static final int SUB3 = 0b00000010;
  private static final int ALLCALL = 0b00000001;

  public Mode1Register(I2CDevice sensor) throws IOException {
    super(sensor, 0x0, "PRE_SCALE");
    reload();
  }

  public void reset() throws IOException {
    setControlRegister(~RESET, RESET);
  }

  public void setExtClk(boolean flag) throws IOException {
    setControlRegister(~EXTCLK, flag ? EXTCLK : 0);
  }

  public boolean isExtClt() {
    return (registerValue & EXTCLK) != 0;
  }

  public void setAutoIncrement(boolean flag) throws IOException {
    setControlRegister(~AI, flag ? AI : 0);
  }

  public boolean isAutoIncrement() {
    return (registerValue & AI) != 0;
  }

  public void setSleep(boolean flag) throws IOException {
    setControlRegister(~AI, flag ? SLEEP : 0);
  }

  public boolean isSleep() {
    return (registerValue & SLEEP) != 0;
  }

  public void setRespondToAddr1(boolean flag) throws IOException {
    setControlRegister(~SUB1, flag ? SUB1 : 0);
  }

  public boolean isRespondToAddr1() {
    return (registerValue & SUB1) != 0;
  }

  public void setRespondToAddr2(boolean flag) throws IOException {
    setControlRegister(~SUB2, flag ? SUB2 : 0);
  }

  public boolean isRespondToAddr2() {
    return (registerValue & SUB2) != 0;
  }

  public void setRespondToAddr3(boolean flag) throws IOException {
    setControlRegister(~SUB3, flag ? SUB3 : 0);
  }

  public boolean isRespondToAddr3() {
    return (registerValue & SUB3) != 0;
  }

  public void enableAllCall(boolean flag) throws IOException {
    setControlRegister(~ALLCALL, flag ? ALLCALL : 0);
  }

  public boolean isEnableAllCall() {
    return (registerValue & ALLCALL) != 0;
  }
}
