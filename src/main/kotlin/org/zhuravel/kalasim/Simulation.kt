
import Configuration.NODE_RECONFIGURATION_DISTRIBUTION_MEAN
import Configuration.NODE_RECONFIGURATION_DISTRIBUTION_SD
import Configuration.SIMULATION_RUN_FOR
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.currentCoroutineContext
import org.apache.commons.math3.distribution.LogNormalDistribution
import org.kalasim.Environment
import org.kalasim.normal
import org.zhuravel.kalasim.Node
import org.zhuravel.kalasim.NodeState

object Configuration {
    // constants
    val LEADER_SEND_HEARTBEAT_DELAY = 300.milliseconds
    val LINK_DELAY_DISTRIBUTION = LogNormalDistribution(5.76, 0.24)

    const val NODE_RECONFIGURATION_DISTRIBUTION_MEAN = 300
    const val NODE_RECONFIGURATION_DISTRIBUTION_SD = 50

    val SIMULATION_RUN_FOR = 5.seconds
}

class Simulation : Environment() {

    val nodeArray = mutableListOf<Node>()

    fun setUp() {
        for (i in 0 until 5) {
            nodeArray.add(
                Node(
                    i,
                    NodeState.FOLLOWER,
                    mutableListOf(),
                    normal(
                        mean = NODE_RECONFIGURATION_DISTRIBUTION_MEAN,
                        sd = NODE_RECONFIGURATION_DISTRIBUTION_SD,
                        rectify = false
                    ),
                    this.env.now
                )
            )
        }

        for (i in 0 until nodeArray.size) {
            for (j in (i + 1) until nodeArray.size) {
                nodeArray[i] linkWith nodeArray[j]
                nodeArray[j] linkWith nodeArray[i]
            }
        }
    }

}

fun main() {
    val sim = Simulation()
    sim.setUp()
    sim.run(SIMULATION_RUN_FOR)
}