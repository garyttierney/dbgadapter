import ghidra.framework.plugintool.PluginTool

inline fun <reified T> PluginTool.getService(): T = getService(T::class.java)