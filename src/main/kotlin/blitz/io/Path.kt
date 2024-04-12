package blitz.io

import blitz.modifyException
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.files.SystemPathSeparator

/**
 * Every instance of this class represents an absolute path in a file system.
 */
@JvmInline
value class Path(
    val parts: Array<String>
) {
    val full: String
        get() = parts.joinToString(separator.toString())

    val name: String
        get() = parts.last()

    fun parent(): Path? =
        if (parts.isEmpty()) null
        else Path(parts.dropLast(1).toTypedArray())

    // TODO: remove "." and ".." from the path
    fun child(vararg paths: String): Path =
        modifyException(Exception("children in Path.child() can not contain path separator!")) {
            Path(parts + paths.onEach { assert(separator !in it) })
        }

    fun exists(): Boolean =
         SystemFileSystem.exists(toKotlinxPath())

    fun toKotlinxPath() =
        if (separator == '/') kotlinx.io.files.Path("/", *parts)
        else kotlinx.io.files.Path("", *parts)

    private fun kotlinxMeta() =
        SystemFileSystem.metadataOrNull(toKotlinxPath())

    fun isDir() =
        kotlinxMeta()?.isDirectory ?: false

    fun isFile() =
        kotlinxMeta()?.isRegularFile ?: false

    fun getDir(): Dir {
        if (!exists()) throw Exception("Path does not exist!")
        return Dir(this)
    }

    fun getOrCreateDir(): Dir {
        if (!exists()) SystemFileSystem.createDirectories(toKotlinxPath())
        return Dir(this)
    }

    fun getFile(): File {
        if (!exists()) throw Exception("Path does not exist!")
        return File(this)
    }

    fun getOrCreateFile(): File {
        if (!exists()) SystemFileSystem.sink(toKotlinxPath()).flush()
        return File(this)
    }

    fun deleteFileOrDir() {
        if (!exists()) throw Exception("Path does not exist!")
        SystemFileSystem.delete(toKotlinxPath())
    }

    fun tryDeleteFileOrDir() {
        try {
            if (exists())
                SystemFileSystem.delete(toKotlinxPath())
        } catch (_: Exception) {}
    }

    companion object {
        val separator: Char = SystemPathSeparator

        /**
         * Creates a path from a relative or absolute path string.
         */
        fun of(path: String): Path {
            if (path.isEmpty()) return Path(emptyArray())
            val kx = kotlinx.io.files.Path(path)
            if (kx.isAbsolute)
                return Path(path.split(separator, '/', '\\').filterNot { it.isEmpty() }.toTypedArray())
            return of(SystemFileSystem.resolve(kx).toString())
        }
    }
}