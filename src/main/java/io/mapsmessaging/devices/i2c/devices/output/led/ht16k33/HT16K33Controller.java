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

package io.mapsmessaging.devices.i2c.devices.output.led.ht16k33;

import io.mapsmessaging.devices.util.Delay;
import io.mapsmessaging.devices.i2c.I2CDeviceEntry;
import io.mapsmessaging.schemas.config.SchemaConfig;
import io.mapsmessaging.schemas.config.impl.JsonSchemaConfig;
import org.json.JSONObject;

import java.time.LocalDateTime;

public abstract class HT16K33Controller implements I2CDeviceEntry {

  protected final HT16K33Driver display;

  protected HT16K33Controller() {
    this.display = null;
  }

  protected HT16K33Controller(HT16K33Driver display) {
    this.display = display;
  }

  @Override
  public boolean detect() {
    return display != null && display.isConnected();
  }

  public byte[] getStaticPayload() {
    return "{}".getBytes();
  }

  public byte[] getUpdatePayload() {
    JSONObject jsonObject = new JSONObject();
    if (display != null) {
      jsonObject.put("display", display.getCurrent());
      jsonObject.put("blink", display.isBlinkOn());
      jsonObject.put("blink-fast", display.isFastBlink());
      jsonObject.put("enabled", display.isOn());
      jsonObject.put("brightness", display.getBrightness());
    }
    return jsonObject.toString(2).getBytes();
  }

  @Override
  public void setPayload(byte[] val) {
    if (display == null) return;
    JSONObject jsonObject = new JSONObject(new String(val));
    if (jsonObject.has("brightness")) {
      int brightness = jsonObject.getInt("brightness");
      if (brightness != display.getBrightness()) {
        display.setBrightness((byte) (brightness & 0xf));
      }
    }
    if (jsonObject.has("blink")) {
      boolean blink = jsonObject.optBoolean("blink", display.isBlinkOn());
      if (blink != display.isBlinkOn()) {
        display.enableBlink(blink, display.isFastBlink());
      }
    }
    if (jsonObject.has("blink-fast")) {
      boolean fast = jsonObject.optBoolean("blink", display.isFastBlink());
      if (fast != display.isFastBlink()) {
        display.enableBlink(display.isBlinkOn(), fast);
      }
    }

    if (jsonObject.has("enabled")) {
      boolean isOn = jsonObject.optBoolean("enabled", display.isOn());
      if (isOn != display.isOn()) {
        if (isOn) {
          display.turnOn();
        } else {
          display.turnOff();
        }
      }
    }
    String text = jsonObject.getString("display");
    if (text.length() <= 5) {
      display.write(text);
    }
  }

  public SchemaConfig getSchema() {
    JsonSchemaConfig config = new JsonSchemaConfig();
    config.setSource("I2C bus address configurable from 0x70 to 0x77");
    config.setVersion("1.0");
    config.setResourceType("LED");
    config.setInterfaceDescription("Controls the LED segments");
    return config;
  }

  private void clock(){
    boolean hasColon = false;
    String val;
    LocalDateTime dateTime = LocalDateTime.now();
    int hour = dateTime.getHour();
    int min = dateTime.getMinute();
    if(hour < 10){
      val = "0"+hour;
    }
    else{
      val = ""+hour;
    }
    if(hasColon){
      val += " ";
    }
    else {
      val += ":";
    }
    hasColon = !hasColon;
    if(min < 10){
      val += "0"+min;
    }
    else{
      val += ""+min;
    }
    dateTime.getSecond();
    dateTime.getNano();
    JSONObject payload = new JSONObject();
    payload.put("display", val);
    setPayload(payload.toString(2).getBytes());
    Delay.pause(450);
  }
}