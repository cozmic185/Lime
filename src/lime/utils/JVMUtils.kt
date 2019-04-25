package lime.utils

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.ArrayList
import java.lang.management.ManagementFactory

object JVMUtils {
    fun restartOnFirstThread(needsOutput: Boolean, vararg args: String): Boolean {
        val stackTrace = Thread.currentThread().stackTrace
        var foundThisMethod = false
        var callingClassName: String? = null
        for (element in stackTrace) {
            val className = element.className
            val methodName = element.methodName
            if (foundThisMethod) {
                callingClassName = className
                break
            } else if (JVMUtils::class.java.name == className && "restartJVMOnFirstThread" == methodName)
                foundThisMethod = true
        }

        requireNotNull(callingClassName) { "Unable to find main class" }

        try {
            val cls = Class.forName(callingClassName, true, Thread.currentThread().contextClassLoader)
            return restartOnFirstThread(needsOutput, cls, *args)
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        }

        return false
    }

    fun restartOnFirstThread(needsOutput: Boolean, customClass: Class<*>?, vararg args: String): Boolean {
        val startOnFirstThread = System.getProperty("XstartOnFirstThread")
        if (startOnFirstThread != null && startOnFirstThread == "true")
            return false

        val osName = System.getProperty("os.name")
        if (!osName.startsWith("mac", true) && !osName.startsWith("darwin", true))
            return false

        val pid = ManagementFactory.getRuntimeMXBean().name.split("@".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
        val env = System.getenv("JAVA_STARTED_ON_FIRST_THREAD_$pid")

        if (env != null && env == "1")
            return false

        val separator = System.getProperty("file.separator")
        val classpath = System.getProperty("java.class.path")
        val mainClass = System.getenv("JAVA_MAIN_CLASS_$pid")
        val jvmPath = System.getProperty("java.home") + separator + "bin" + separator + "java"
        val inputArguments = ManagementFactory.getRuntimeMXBean().inputArguments
        val jvmArgs = ArrayList<String>()

        jvmArgs += jvmPath
        jvmArgs += "-XstartOnFirstThread"
        jvmArgs += inputArguments
        jvmArgs += "-cp"
        jvmArgs += classpath
        jvmArgs += if (customClass == null) mainClass else customClass.name
        jvmArgs += args

        if (!needsOutput) {
            try {
                val processBuilder = ProcessBuilder(jvmArgs)
                processBuilder.start()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } else {
            try {
                val processBuilder = ProcessBuilder(jvmArgs)
                processBuilder.redirectErrorStream(true)
                val process = processBuilder.start()
                val inputStream = process.inputStream
                val inputStreamReader = InputStreamReader(inputStream)
                val bufferedReader = BufferedReader(inputStreamReader)
                var line: String?

                do {
                    line = bufferedReader.readLine()
                    if (line != null)
                        println(line)
                } while (line != null)

                System.exit(0)
                process.waitFor()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return true
    }
}