package org.zhuravel.kalasim

import Configuration.LINK_DELAY_DISTRIBUTION
import kotlin.time.Duration.Companion.milliseconds
import org.kalasim.Component
import org.kalasim.ComponentQueue

class Link(val receiver: Node, val incomingMsgsLine: ComponentQueue<Component>) : Component() {
    override fun process() = sequence {
        while (true) {
            if (incomingMsgsLine.isNotEmpty()) {
                val incomingMsg = incomingMsgsLine.poll() as HeartbeatMsg
                hold(LINK_DELAY_DISTRIBUTION.sample().milliseconds, description = "Simulating network delay to ${receiver}")
                receiver.receiveHeartbeat(incomingMsg)
            }
        }
    }
}