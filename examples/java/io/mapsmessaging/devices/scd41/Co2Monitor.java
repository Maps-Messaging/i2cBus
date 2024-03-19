package io.mapsmessaging.devices.scd41;

import io.mapsmessaging.devices.DeviceBusManager;
import io.mapsmessaging.devices.i2c.I2CBusManager;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.I2CDeviceController;
import io.mapsmessaging.devices.i2c.I2CDeviceScheduler;
import io.mapsmessaging.devices.i2c.devices.sensors.scd41.Scd41Sensor;
import io.mapsmessaging.devices.sensorreadings.ComputationResult;
import io.mapsmessaging.devices.sensorreadings.SensorReading;
import lombok.SneakyThrows;

import java.io.IOException;

import static io.mapsmessaging.devices.util.Constants.roundFloatToString;

public class Co2Monitor implements Runnable {

  private final Scd41Sensor device;

  public Co2Monitor(Scd41Sensor device) throws IOException {
    this.device = device;
    Thread t = new Thread(this);
    t.start();
  }

  public static void main(String[] args) throws IOException {
    I2CBusManager[] i2cBusManagers = DeviceBusManager.getInstance().getI2cBusManager();
    int bus = 1;
    if (args.length > 0) {
      bus = Integer.parseInt(args[0]);
    }
    // Configure and mount a device on address 0x5D as a LPS25 pressure & temperature
    I2CDeviceController deviceController = i2cBusManagers[bus].configureDevice(0x62, "SCD-41");
    if (deviceController != null) {
      System.err.println(new String(deviceController.getDeviceConfiguration()));
      I2CDevice sensor = deviceController.getDevice();
      if (sensor instanceof Scd41Sensor) {

        new Co2Monitor((Scd41Sensor) sensor);
      }
    }
  }

  @SneakyThrows
  public void run() {
    SensorReading<?> co2 = null;
    SensorReading<?> temp = null;
    SensorReading<?> humidity = null;

    for (SensorReading<?> val : device.getReadings()) {
      switch (val.getName()) {
        case "CO2":
          co2 = val;
          break;
        case "Humidity":
          humidity = val;
          break;
        case "Temperature":
          temp = val;
          break;
      }
    }
    long stop = System.currentTimeMillis() + 120_000;

    while (co2 != null && humidity != null && temp != null && stop > System.currentTimeMillis()) {
      synchronized (I2CDeviceScheduler.getI2cBusLock()) {
        ComputationResult<Float> tempResult = (ComputationResult<Float>) temp.getValue();
        ComputationResult<Integer> result = (ComputationResult<Integer>) co2.getValue();
        ComputationResult<Float> humResult = (ComputationResult<Float>) humidity.getValue();
        if (!result.hasError()) {
          int ppm = result.getResult();
          float tRes = tempResult.getResult();
           String pre = roundFloatToString(ppm, 2);
          String tmp = roundFloatToString(tRes, 1);
          String dis = roundFloatToString(humResult.getResult(), 1);
          System.err.println(pre + " ppm\t" + tmp + " C\t" + dis + "%");
          Thread.sleep(1000);
        }
      }
    }
  }
}