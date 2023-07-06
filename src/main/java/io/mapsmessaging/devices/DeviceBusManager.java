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

package io.mapsmessaging.devices;

import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import com.pi4j.io.i2c.I2CProvider;
import io.mapsmessaging.devices.i2c.I2CBusManager;
import io.mapsmessaging.devices.interrupts.InterruptFactory;
import io.mapsmessaging.devices.logging.DeviceLogMessage;
import io.mapsmessaging.devices.onewire.OneWireBusManager;
import io.mapsmessaging.devices.spi.SpiBusManager;
import io.mapsmessaging.logging.Logger;
import io.mapsmessaging.logging.LoggerFactory;
import lombok.Getter;

import java.io.IOException;
import java.util.Map;

public class DeviceBusManager {

  private static final String[] PROVIDERS = {"pigpio-i2c", "linuxfs-i2c"};

  private static final DeviceBusManager instance = new DeviceBusManager();

  public static DeviceBusManager getInstance() {
    return instance;
  }

  private final Logger logger = LoggerFactory.getLogger(DeviceBusManager.class);

  private final Context pi4j;

  @Getter
  private final I2CBusManager i2cBusManager;
  @Getter
  private final OneWireBusManager oneWireBusManager;
  @Getter
  private final SpiBusManager spiBusManager;
  @Getter
  private final InterruptFactory interruptFactory;

  private DeviceBusManager() {
    logger.log(DeviceLogMessage.BUS_MANAGER_STARTUP);
    pi4j = Pi4J.newAutoContext();
    I2CProvider i2cProvider = pi4j.provider(getProvider());
    i2cBusManager = new I2CBusManager(pi4j, i2cProvider);
    oneWireBusManager = new OneWireBusManager();
    interruptFactory = new InterruptFactory(pi4j);
    spiBusManager = new SpiBusManager(pi4j);
  }

  public void configureDevices(Map<String, Object> config) throws IOException {
    // Note: 1-Wire autoconfigures within the filesystem

    if(config.containsKey("i2c")) {
      Map<String, Object> i2c = (Map) config.get("i2c");
      i2cBusManager.configureDevices(i2c);
    }
    if(config.containsKey("spi")) {
      Map<String, Object> spi = (Map) config.get("spi");
      spiBusManager.configureDevices(spi);
    }
  }

  public void close() {
    pi4j.shutdown();
    logger.log(DeviceLogMessage.BUS_MANAGER_SHUTDOWN);
  }

  /*
  if(addr == 0x72){
            JSONObject config = new JSONObject();
            config.put("enabled", true);
            config.put("task", "clock");
            config.put("brightness", 1);
            physicalDevice.setPayload(config.toString(2).getBytes());
          }
   */


  private void configureSpiDevices(Map<String, Object> configuration){

  }

  private static String getProvider() {
    String provider = System.getProperty("i2C-PROVIDER", PROVIDERS[0]).toLowerCase();
    boolean isValid = false;
    for (String providers : PROVIDERS) {
      if (providers.equals(provider)) {
        isValid = true;
        break;
      }
    }
    if (!isValid) {
      provider = PROVIDERS[0];
    }
    return provider;
  }

}
