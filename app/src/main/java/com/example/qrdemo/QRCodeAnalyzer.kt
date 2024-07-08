package com.example.qrdemo

import android.graphics.ImageFormat
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer
import java.nio.ByteBuffer

class QRCodeAnalyzer(
    private val QRCodeScanned: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private val supportedImageTypes = listOf(
        ImageFormat.YUV_420_888,
        ImageFormat.YUV_422_888,
        ImageFormat.YUV_444_888,

        )


    override fun analyze(image: ImageProxy) {

        if (image.format in supportedImageTypes){
            val bytes = image.planes.first().buffer.toByteArray() // this returns raw data

            val source = PlanarYUVLuminanceSource(
                bytes,
                image.width,
                image.height,
                0,
                0,
                image.width,
                image.height,
                false
            )
            val binaryBitmap =
                BinaryBitmap(HybridBinarizer(source)) // it has the data about scanned qr code

            try {
                //multi format reader , reads different format of qr codes and bar codes
                val result = MultiFormatReader().apply {
                    //set hints defines what kind of codes should be scan using camera
                    setHints(
                        mapOf(
                            DecodeHintType.POSSIBLE_FORMATS to arrayListOf(BarcodeFormat.QR_CODE)
                        )
                    )
                }.decodeWithState(binaryBitmap)

                QRCodeScanned(result.text)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                image.close()
            }
        }
    }


    private fun ByteBuffer.toByteArray(): ByteArray {
        rewind() // it starts from the very beginning of the byte array
        // it returns the remaining bytes
        return ByteArray(remaining()).also {
            get(it) //This method transfers bytes from this buffer into the given destination array.
            // An invocation of this method of the form src. get(a) behaves in exactly the same way as the invocation
        }


    }
}