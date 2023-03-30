package cessini.technology.commonui.compression

import java.io.UnsupportedEncodingException
import java.util.zip.DataFormatException
import java.util.zip.Deflater
import java.util.zip.Inflater

class RoomNameEncoder {

    fun encode(room_name:String): String {
        try {
            // Encode a String into bytes
            val input = room_name.toByteArray(charset("UTF-8"))

            // Compress the bytes
            val output = ByteArray(1000)
            val compresser = Deflater()
            compresser.setInput(input)
            compresser.finish()
            val compressedDataLength: Int = compresser.deflate(output)
            compresser.end()

            var encoded=""

            for(i in 0 until compressedDataLength)
            {
                encoded+= (output[i].toInt()).toChar()
            }

            return encoded

        } catch (ex: UnsupportedEncodingException) {
            // handle
        } catch (ex: DataFormatException) {
            // handle
        }

        return "Failed"
    }

    fun decode(str:String): String {

        val compressedDataLength= str.length
        val output = ByteArray(compressedDataLength)

        for(i in 0 until compressedDataLength){
            output[i]= (str[i].toInt()).toByte()
        }


        val decompresser = Inflater()
        decompresser.setInput(output, 0, compressedDataLength)
        val result = ByteArray(100)
        val resultLength: Int = decompresser.inflate(result)
        decompresser.end()

        // Decode the bytes into a String
        return String(result, 0, resultLength, charset("UTF-8"))
    }





}