package gateway.controller.modules

class ModuleController() {
    companion object {

    }


    private var moduleProcesses = HashMap<String, Process>()
    private var modulesAcked = HashMap<String, Boolean>()

    fun isAllModuleStarted() : Boolean = modulesAcked.all { it.value }
    fun killModules() = moduleProcesses.forEach { name, process ->  process.destroy().also { println(name+" process destroyed.") }}
    fun setModuleStarted(moduleName : String) {
        modulesAcked[moduleName] = true
    }

    fun addModule(name : String, process : Process){
        moduleProcesses[name] = process
        modulesAcked[name] = false
    }
}
