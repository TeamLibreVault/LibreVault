package me.kys0.unifile

import java.io.IOException

/**
 * The UniRandomAccessFile is designed to emulate RandomAccessFile interface for UniFile
 */
@Suppress("unused", "KDocUnresolvedReference")
interface UniRandomAccessFile {
    /**
     * Closes this UniRandomAccessFile and releases any resources associated with it.
     * A closed UniRandomAccessFile cannot perform any operations.
     *
     * @throws IOException If an error occurs during processing.
     */
    @Throws(IOException::class)
    fun close()

    @get:Throws(IOException::class)
    val filePointer: Long

    /**
     * Move current location pointer to the new offset.
     *
     * @param pos the offset position, measured in bytes from the beginning
     * @throws IOException If an error occurs during processing.
     */
    @Throws(IOException::class)
    fun seek(pos: Long)

    /**
     * Attempts to skip over n bytes of input discarding the skipped bytes.
     *
     * @param n the number of bytes to be skipped
     * @return the actual number of bytes skipped
     * @throws IOException If an error occurs during processing.
     */
    @Throws(IOException::class)
    fun skipBytes(n: Int): Int

    /**
     * Returns the length of this file.
     *
     * @throws IOException If an error occurs during processing.
     */
    @get:Throws(IOException::class)
    val length: Long

    /**
     * Sets the length of this file.
     *
     * @param newLength the desired length of the file
     * @throws IOException If an error occurs during processing.
     */
    @Throws(IOException::class)
    fun setLength(newLength: Long)

    /**
     * Reads b.length bytes from this file into the byte array,
     * starting at the current file pointer.
     *
     * @param b the buffer into which the data is read
     * @throws EOFException if this file reaches the end before reading all the bytes
     * @throws IOException if an I/O error occurs
     */
    @Throws(IOException::class)
    fun read(b: ByteArray?)

    /**
     * Reads exactly len bytes from this file into the byte array,
     * starting at the current file pointer.
     *
     * @param b the buffer into which the data is read
     * @param off the start offset of the data
     * @param len the number of bytes to read
     * @throws IOException If an error occurs during processing.
     */
    @Throws(IOException::class)
    fun read(b: ByteArray?, off: Int, len: Int)

    /**
     * Writes b.length bytes from the specified byte array to this file,
     * starting at the current file pointer.
     *
     * @param b the data
     * @throws IOException If an error occurs during processing.
     */
    @Throws(IOException::class)
    fun write(b: ByteArray?)

    /**
     * Writes len bytes from the specified byte array starting at offset off to this file.
     *
     * @param b the data
     * @param off the start offset in the data
     * @param len the number of bytes to write
     * @throws IOException If an error occurs during processing.
     */
    @Throws(IOException::class)
    fun write(b: ByteArray?, off: Int, len: Int)
}
