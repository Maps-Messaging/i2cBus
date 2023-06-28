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

package io.mapsmessaging.devices.spi.devices.mcp3y0x;

import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.DigitalOutput;
import com.pi4j.io.spi.Spi;
import com.pi4j.io.spi.SpiChipSelect;
import com.pi4j.io.spi.SpiMode;
import io.mapsmessaging.devices.spi.SpiDeviceController;
import io.mapsmessaging.schemas.config.SchemaConfig;
import io.mapsmessaging.schemas.config.impl.JsonSchemaConfig;
import org.everit.json.schema.ArraySchema;
import org.everit.json.schema.NumberSchema;
import org.everit.json.schema.ObjectSchema;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;

public class Mcp3y0xController extends SpiDeviceController {

  private final mcp3y0xDevice device;

  public Mcp3y0xController() {
    device = null;
  }

  public Mcp3y0xController(mcp3y0xDevice device) {
    this.device = device;
  }

  @Override
  public SpiDeviceController mount(Context pi4j, Map<String, String> map) {
    int spiBus = Integer.parseInt(map.get("spiBus"));
    int csAddress = Integer.parseInt(map.get("csAddress"));
    int resolution = Integer.parseInt(map.get("resolution"));
    int channels = Integer.parseInt(map.get("channels"));
    DigitalOutput cs = null;//super.createClientSelect(pi4j, getName(), device.getDescription(), csAddress, DigitalState.HIGH, DigitalState.HIGH);

    String description = "Microchip Technology Analog to Digital " + channels + " channel " + resolution + " bit convertor";
    Spi spi = createDevice(pi4j, getName(), description, spiBus, SpiChipSelect.CS_0, SpiMode.MODE_0);
    if (channels == 4) {
      if (resolution == 10) {
        return new Mcp3y0xController(new mcp3004Device(spi, cs));
      }
      return new Mcp3y0xController(new mcp3204Device(spi, cs));
    } else {
      if (resolution == 10) {
        return new Mcp3y0xController(new mcp3008Device(spi, cs));
      }
    }
    return new Mcp3y0xController(new mcp3208Device(spi, cs));
  }

  @Override
  public String getName() {
    if (device == null) return "Mcp3y0x";
    return device.getName();
  }

  public byte[] getStaticPayload() {
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("resolution", device.getBits());
    jsonObject.put("channels", device.getChannels());
    jsonObject.put("dutyCycle", device.getDutyCycle());
    return jsonObject.toString(2).getBytes();
  }

  public byte[] getUpdatePayload() {
    JSONObject jsonObject = new JSONObject();
    JSONArray jsonArray = new JSONArray();
    for (short x = 0; x < device.channels; x++) {
      try {
        jsonArray.put(device.readFromChannel(false, x));
      } catch (IOException e) {
        jsonArray.put(Integer.MIN_VALUE);
      }
    }
    jsonObject.put("current", jsonArray);
    return jsonObject.toString(2).getBytes();
  }

  public SchemaConfig getSchema() {
    JsonSchemaConfig config = new JsonSchemaConfig(buildSchema());
    config.setComments("SPI device Analog to Digital convertor");
    config.setSource("SPI Device");
    config.setVersion("1.0");
    config.setResourceType("sensor");
    config.setInterfaceDescription("Returns JSON object containing the latest readings from all channels");
    return config;
  }

  private String buildSchema() {
    ObjectSchema.Builder staticSchema = ObjectSchema.builder()
        .addPropertySchema("resolution",
            NumberSchema.builder()
                .description("The number of bits of resulution that the result has")
                .build()
        )
        .addPropertySchema("channels",
            NumberSchema.builder()
                .description("Number of ADC channels available")
                .build()
        )
        .addPropertySchema("dutyCycle",
            NumberSchema.builder()
                .description("Number of times per second that the device can be read")
                .build()
        );

    ObjectSchema.Builder updateSchema = ObjectSchema.builder()
        .addPropertySchema("current",
            ArraySchema.builder()
                .minItems(0)
                .maxItems(device.channels)
                .description("Current values of all channels on the ADC")
                .build()
        );

    ObjectSchema.Builder schemaBuilder = ObjectSchema.builder();
    schemaBuilder
        .addPropertySchema("updateSchema", updateSchema.build())
        .addPropertySchema("staticSchema", staticSchema.build())
        .description("Analog to digital convertor")
        .title("Mcp3y0x");
    return schemaToString(schemaBuilder.build());
  }

}
