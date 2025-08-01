

## Design 

- Separate the chip/device logic and its IO where multiple IO options exist
  - Avoids code duplication and provides a clear logical abstraction.


- Don't replicate i2c/spi config parameters. 
  - Instead, let users hand in an i2c/spi instance 
  - This ensures that all parameters can be set to the user's liking, including names and ids.


- No external dependencies other than Pi4j
 

- No interface sharing outside the device package
  - It might be tempting to create e.g. a "temperature sensor interface" or similar, but these easily
    open cans of worms about units, data types etc. -- and might not match the abstraction the
    client app wants to use after all. It seems to be most straightforward to keep close to the
    hardware / vendor documentation and leave the rest to the user.
     

- Extended documentation (pdf etc.) goes into the example package
  - This should help ensuring that large documents don't get bundled into deploy jars by mistake.
