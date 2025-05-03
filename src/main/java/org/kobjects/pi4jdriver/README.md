

## Design 

- Separate the chip/device logic and it's IO where multiple IO options exist


- Don't replicate i2c/spi config parameters. 
  - Instead, let users hand in an i2c/spi instance 
  - This ensures that all parameters can be set.
  

- No external dependencies other than Pi4j
 

- No interface sharing outside the device package
  - It might be tempting to create e.g. a "temperature sensor interface" or similar, but
     


- Extended documentation (pdf etc.) goes into the example package
  - This should help ensuring that large documents don't get bundled into deploy jars by mistake.
