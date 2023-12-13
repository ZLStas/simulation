package org.zhuravel

import kotlin.random.Random
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.sqrt
import kotlin.math.cos
import kotlin.math.PI

class LogNormalDelaySimulator(var settings: Pair<Int, Int>) : DelaySimulator {

    override var from: Int = settings.first
    override var until: Int = settings.second

    override val delay: Int
        get() = generateLogNormalDelay().coerceIn(from.toDouble()..until.toDouble()).toInt()

    private fun generateLogNormalDelay(): Double {
        val mean = ln(from.toDouble())  // Assuming 'from' as mean of log-normal distribution
        val stdDev = (ln(until.toDouble()) - mean) / 3  // Rough approximation for standard deviation

        val normal = generateGaussian(mean, stdDev)
        return exp(normal)
    }

    private fun generateGaussian(mean: Double, stdDev: Double): Double {
        val u1 = Random.nextDouble()
        val u2 = Random.nextDouble()
        val z0 = sqrt(-2.0 * ln(u1)) * cos(2.0 * PI * u2)
        return z0 * stdDev + mean
    }

}