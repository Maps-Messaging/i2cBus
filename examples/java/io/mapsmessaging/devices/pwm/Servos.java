package io.mapsmessaging.devices.pwm;

import io.mapsmessaging.devices.DeviceBusManager;
import io.mapsmessaging.devices.i2c.I2CBusManager;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.I2CDeviceController;
import io.mapsmessaging.devices.i2c.I2CDeviceScheduler;
import io.mapsmessaging.devices.i2c.devices.drivers.pca9685.Pca9685Device;
import io.mapsmessaging.devices.i2c.devices.drivers.pca9685.registers.LedControlRegister;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class Servos {

  public Servos(Pca9685Device device) throws IOException {
    synchronized (I2CDeviceScheduler.getI2cBusLock()) {
      device.setPWMFrequency(60);
      int[] val = {150, 590};
      int counter = 0;
      while (counter < 10) {
        for (LedControlRegister ledControlRegister : device.getLedControlRegisters()) {
          ledControlRegister.setRate(0, val[(counter % 2)]);
        }
        device.delay(5000);
        counter++;
      }
    }
  }

  public static void main(String[] args) throws IOException, InterruptedException {
    I2CBusManager[] i2cBusManagers = DeviceBusManager.getInstance().getI2cBusManager();
    int bus = 1;
    if (args.length > 0) {
      bus = Integer.parseInt(args[0]);
    }

    // Configure and mount a device on address 0x5D as a LPS25 pressure & temperature
    Map<String, Object> map = new LinkedHashMap<>();
    Map<String, Object> config = new LinkedHashMap<>();
    config.put("deviceName", "PCA9685");
    map.put(""+0x40, config);
    I2CDeviceController deviceController = i2cBusManagers[bus].configureDevices(map);
    if (deviceController != null) {
      I2CDevice sensor = deviceController.getDevice();
      if (sensor instanceof Pca9685Device) {
        new Servos((Pca9685Device) sensor);
      }
    }

  }
}