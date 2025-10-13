package me.kys0.unifile

/**
 * An interface for filtering [UniFile] objects based on their names
 * or the directory they reside in.
 *
 * @see UniFile
 *
 * @see UniFile.listFiles
 */
interface FilenameFilter {
    /**
     * Indicates if a specific filename matches this filter.
     *
     * @param dir
     * the directory in which the `filename` was found.
     * @param filename
     * the name of the file in `dir` to test.
     * @return  `true` if the filename matches the filter
     * and can be included in the list, `false`
     * otherwise.
     */
    fun accept(dir: UniFile?, filename: String?): Boolean
}
