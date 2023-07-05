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

package io.mapsmessaging.devices.i2c.devices.sensors.gravity;

import com.pi4j.io.i2c.I2C;
import io.mapsmessaging.devices.i2c.I2CDeviceEntry;
import io.mapsmessaging.schemas.config.SchemaConfig;
import io.mapsmessaging.schemas.config.impl.JsonSchemaConfig;
import org.json.JSONObject;

public class GasSensorController implements I2CDeviceEntry {

  private final int i2cAddr = 0x74;
  private final GasSensor sensor;

  public GasSensorController() {
    sensor = null;
  }

  public GasSensorController(I2C device) {
    sensor = new GasSensor(device);
  }

  @Override
  public boolean detect() {
    return sensor != null && sensor.isConnected();
  }

  public I2CDeviceEntry mount(I2C device) {
    return new GasSensorController(device);
  }

  public byte[] getStaticPayload() {
    JSONObject jsonObject = new JSONObject();
    if (sensor != null) {
      jsonObject.put("sku", sensor.getSensorType().getSku());
      jsonObject.put("name", sensor.getSensorType().name());
      jsonObject.put("resolution", sensor.getSensorType().getResolution());
      jsonObject.put("units", sensor.getSensorType().getUnits());
      jsonObject.put("minimum", sensor.getSensorType().getMinimumRange());
      jsonObject.put("maximum", sensor.getSensorType().getMaximumRange());
      jsonObject.put("responseTime", sensor.getSensorType().getResponseTime());
      jsonObject.put("threshold", sensor.getSensorType().getThreshold());
    }
    return jsonObject.toString(2).getBytes();
  }

  @Override
  public void setPayload(byte[] payload) {
    if(sensor == null)return;
    JSONObject json = new JSONObject(new String(payload));
    if (json.has("functionName") && json.has("parameters")) {
      String functionName = json.getString("functionName");
      JSONObject parameters = json.getJSONObject("parameters");
      String alarmTypeName;
      AlarmType alarmType;
      switch (functionName) {
        case "setThresholdAlarm":
          int threshold = parameters.getInt("threshold");
          alarmTypeName = parameters.getString("alarmType");
          alarmType  = AlarmType.valueOf(alarmTypeName);
          sensor.setThresholdAlarm(threshold, alarmType);
          break;

        case "clearThresholdAlarm":
          alarmTypeName = parameters.getString("alarmType");
          alarmType  = AlarmType.valueOf(alarmTypeName);
          sensor.clearThresholdAlarm(alarmType);
          break;

        case "setI2CAddress":
          byte group = (byte) parameters.getInt("group");
          sensor.setI2CAddress(group);
          break;

        case "changeAcquireMode":
          String acquireMode = parameters.getString("acquireMode");
          AcquireMode mode = AcquireMode.valueOf(acquireMode);
          sensor.changeAcquireMode(mode);
          break;

        default:
          break;
      }
    }
  }

  public String getName(){
    if(sensor == null){
      return "Generic Gas Sensor";
    }
    return sensor.getName();
  }

  public byte[] getUpdatePayload() {
    JSONObject jsonObject = new JSONObject();
    if (sensor != null) {
      sensor.updateAllFields();
      jsonObject.put("temperature", sensor.getTemperature());
      jsonObject.put("concentration", sensor.getConcentration());
      jsonObject.put("compensated", sensor.getTemperatureAdjustedConcentration());
      jsonObject.put("voltage", sensor.readVoltageData());
    }
    return jsonObject.toString(2).getBytes();
  }

  public SchemaConfig getSchema() {
    JsonSchemaConfig config = new JsonSchemaConfig(buildSchema());
    config.setComments(getName());
    config.setSource("I2C bus address : " + i2cAddr);
    config.setVersion("1.0");
    config.setResourceType("sensor");
    config.setInterfaceDescription("Gravity Gas sensor");
    return config;
  }

  @Override
  public int[] getAddressRange() {
    return new int[]{i2cAddr};
  }

  private String buildSchema() {
    return null;
  }
}