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

package io.mapsmessaging.devices.spi;

import com.pi4j.io.gpio.digital.DigitalOutput;
import com.pi4j.io.spi.Spi;
import io.mapsmessaging.devices.Device;

public abstract class SpiDevice implements Device {

  protected final Spi spi;
  protected final DigitalOutput chipSelect;

  protected SpiDevice(Spi spi, DigitalOutput chipSelect) {
    this.spi = spi;
    this.chipSelect = chipSelect;
  }

  public void transfer(byte[] request, byte[] response) {
    if (chipSelect != null) {
      chipSelect.high();
      chipSelect.low();
    }
    spi.transfer(request, response);
    if (chipSelect != null) chipSelect.high();
    // Delay.pause(100); // allow for transfer to complete
  }
}
