package org.zhuravel

import kotlin.random.Random

data class NormalDelaySimulator(var settings: Pair<Int, Int>) : DelaySimulator {

    override var from: Int = settings.first
    override var until: Int = settings.second

    override val delay: Int
        get() = Random.nextInt(from, until)

}