package org.kobjects.tablecraft.plugins.pixtend

import com.pi4j.Pi4J
import com.pi4j.io.gpio.digital.DigitalOutputConfig
import com.pi4j.io.spi.Spi
import com.pi4j.io.spi.SpiConfig
import com.pi4j.util.Console


fun crc16(crc: Int, data: Byte): Int {
    var result = crc xor (data.toInt() and 0xff)
    var i = 0
    while (i < 8) {
        result = if (result and 1 != 0) {
            (result shr 1) xor 0xA001
        } else {
            (result shr 1)
        }
        ++i
    }
    return crc
}


fun main() {
    val pi4J = Pi4J.newAutoContext()

    val console: Console = Console()
    println("----------------------------------------------------------")
    println("PI4J PROVIDERS")
    println("----------------------------------------------------------")
    pi4J.providers().describe().print(System.out)
    println("----------------------------------------------------------")

    val pin24dout = pi4J.create(DigitalOutputConfig.newBuilder(pi4J).address(24).build())
    pin24dout.setState(true)

    Thread.sleep(1000)

    val spiConfig: SpiConfig = Spi.newConfigBuilder(pi4J).provider("linuxfs-spi")
       // .mode(SpiMode.MODE_0)
            //   .chipSelect(SpiChipSelect.CS_0)
        .address(0)
        .baud(700_000)
        .build()

    val spi = pi4J.create(spiConfig)

    println(spi)

    spi.open()


    val spiOut = ByteArray(67)
    var crcSumHeader = 0xFFFF

    for (i in 0..7) {
        crcSumHeader = crc16(crcSumHeader, spiOut[i])
    }

    spiOut[7] = crcSumHeader.toByte()  //CRC Low Byte
    spiOut[8] = (crcSumHeader shr 8).toByte() //CRC High Byte

    //Calculate CRC16 Data Transmit Checksum
    var crcSumData = 0xFFFF
    for (i in 0..64) {
        crcSumData = crc16(crcSumData, spiOut[i])
    }
    spiOut[65] = crcSumData.toByte() //CRC Low Byte
    spiOut[66] = (crcSumData shr 8).toByte() //CRC High Byte


    val inputRecord = ByteArray(67)

    println("Spi transfer result: "  + spi.transfer(spiOut, inputRecord, 67))


    println("Received: ")
    for (b in inputRecord) {
        print(" $b")
    }
    println()
}
