package me.kys0.unifile

import java.io.IOException
import java.io.RandomAccessFile

internal class RawRandomAccessFile(
    private val mFile: RandomAccessFile,
) :
    UniRandomAccessFile {

    @Throws(IOException::class)
    override fun close() = mFile.close()

    @get:Throws(IOException::class)
    override val filePointer: Long
        get() = mFile.filePointer

    @Throws(IOException::class)
    override fun seek(pos: Long) {
        mFile.seek(pos)
    }

    @Throws(IOException::class)
    override fun skipBytes(n: Int): Int = mFile.skipBytes(n)


    @get:Throws(IOException::class)
    override val length: Long
        get() = mFile.length()

    @Throws(IOException::class)
    override fun setLength(newLength: Long) = mFile.setLength(newLength)


    @Throws(IOException::class)
    override fun read(b: ByteArray?) {
        mFile.read(b)
    }

    @Throws(IOException::class)
    override fun read(b: ByteArray?, off: Int, len: Int) {
        mFile.read(b, off, len)
    }

    @Throws(IOException::class)
    override fun write(b: ByteArray?) {
        mFile.write(b)
    }

    @Throws(IOException::class)
    override fun write(b: ByteArray?, off: Int, len: Int) {
        mFile.write(b, off, len)
    }
}
