package org.zhuravel

enum class DelayType {
    NORMAL,
    LOG_NORMAL
}

class DelaySimulatorFactory {
    fun getSimulator(type: DelayType, from: Number, until: Number): DelaySimulator {
        return when (type) {
            DelayType.NORMAL -> NormalDelaySimulator(from.toInt() to until.toInt())
            DelayType.LOG_NORMAL -> LogNormalDelaySimulator(from.toInt() to until.toInt())
        }
    }
}