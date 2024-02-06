package org.zhuravel.kalasim

import Configuration.LEADER_SEND_HEARTBEAT_DELAY
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import org.apache.commons.math3.distribution.NormalDistribution
import org.kalasim.Component
import org.kalasim.ComponentQueue
import org.kalasim.StateRequest

enum class NodeState {
    LEADER, FOLLOWER, CANDIDATE
}
class Node(
    val id: Int,
    var state: NodeState,
    val links: MutableList<Link>,
    reconfigurationDistribution: NormalDistribution,
    var incomingMsg: IncomingMsg
) : Component() {

    val reconfigurationTime = reconfigurationDistribution.sample().milliseconds
    val incomingHeartBeatMsgs = ComponentQueue<HeartbeatMsg>()
    val receivedVotes = AtomicInteger(0)
    val totalNodes = links.size + 1
    var currentTerm = 1


    override fun process() = sequence {
        while (true) {
            when (state) {
                NodeState.FOLLOWER -> doFollowerJob()
                NodeState.LEADER -> doLeaderJob()
                NodeState.CANDIDATE -> doCandidateJob()
            }
        }
    }

    private suspend fun SequenceScope<Component>.doLeaderJob() {
        println("Checking for heartbeat from new leader")


        val newMsgs = incomingHeartBeatMsgs.asSortedList().asSequence()
            .map { it.component }
            .filter { it.term > currentTerm }
            .toList()

        if (newMsgs.isEmpty()) {
            links.forEach { link ->
                sendHeartbeatMsgThrouhg(link)
                println("heartbeat sent to the node: ${link.receiver} by ${this@Node}")
            }
            hold(LEADER_SEND_HEARTBEAT_DELAY)
        } else {
            while (incomingHeartBeatMsgs.isNotEmpty()) {
                val heartbeatMsg = incomingHeartBeatMsgs.poll()
                if (heartbeatMsg.term > currentTerm) {
                    stepDownAsALeader(heartbeatMsg)
                } else {
                    println("Heartbeat with term: ${heartbeatMsg.term} ignored by: ${this@Node} with current term: ${currentTerm}")
                }
            }
        }
    }

    private fun stepDownAsALeader(heartbeatMsg: HeartbeatMsg) {
        currentTerm = heartbeatMsg.term
        println("Node ${this@Node}, stepping down as leader, heartbeat received.")
        state = NodeState.FOLLOWER
    }

    private fun sendHeartbeatMsgThrouhg(link: Link) {
        link.incomingMsgsLine.add(HeartbeatMsg(currentTerm))
    }

    private suspend fun SequenceScope<Component>.doFollowerJob() {
        println("Waiting for heartbeat")
        wait(StateRequest(incomingMsg, predicate = { incomingMsg.value.term >= currentTerm }), failDelay = LEADER_SEND_HEARTBEAT_DELAY)

        if (failed) {
            when (state) {
                NodeState.FOLLOWER -> {
                    println("Heartbeat wasn't received. Reconfiguring the cluster by ${this@Node}")
                    triggerReconfiguration()
                }
                NodeState.LEADER -> {
                    links.forEach { link ->
                        sendHeartbeatMsgThrouhg(link)
                        println("heartbeat sent to the node: ${link.receiver} by ${this@Node}")
                    }
                    hold(LEADER_SEND_HEARTBEAT_DELAY)
                }
                NodeState.CANDIDATE -> {
                    val voteRequests = links.map { it.receiver to VoteRequestMsg() }

                    voteRequests.forEach { (node, msg) ->
                        node.receiveVoteRequest(this@Node, msg)
                    }

                    val votes = receivedVotes.get()
                    if (votes > totalNodes / 2) {
                        this@Node.state = NodeState.LEADER
                        this@Node.currentTerm += 1
                        println("${this@Node} is now the leader of term $currentTerm")
                    } else {
                        this@Node.state = NodeState.FOLLOWER
                        println("Votes ${votes}/${totalNodes / 2} received, ${this@Node} stays as follower")
                    }
                }
            }
        } else {
            when (state) {
                NodeState.FOLLOWER -> {
                    println("Heartbeat: ${incomingMsg.value.term} processed by ${this@Node}")
                    receiveVoteCandidate(this@Node, VoteCandidateMsg(this@Node, true))
                }
                NodeState.LEADER -> {
                    stepDownAsALeader(incomingMsg.value)
                }
                NodeState.CANDIDATE -> {
                    println("Node ${this@Node}, stepping down as candidate, heartbeat received.")
                    state = NodeState.FOLLOWER
                }
            }

        }

//        if (incomingHeartBeatMsgs.isEmpty()) {
//
//        } else {
//            while (incomingHeartBeatMsgs.isNotEmpty()) {
//                val heartbeatMsg = incomingHeartBeatMsgs.poll()
//                if (heartbeatMsg.term >= currentTerm) {
//                    println("Heartbeat: ${heartbeatMsg} processed by ${this@Node}")
//                    receiveVoteCandidate(this@Node, VoteCandidateMsg(this@Node, true))
//                } else {
//                    println("Heartbeat with term: ${heartbeatMsg.term} ignored by node: ${this@Node} with current term: ${currentTerm}")
//                }
//            }
//        }
    }

    private suspend fun SequenceScope<Component>.doCandidateJob() {
        val newMsgs = incomingHeartBeatMsgs.asSortedList().asSequence()
            .map { it.component }
            .filter { it.term >= currentTerm }
            .toList()

        if (newMsgs.isEmpty()) {
            val voteRequests = links.map { it.receiver to VoteRequestMsg() }

            voteRequests.forEach { (node, msg) ->
                node.receiveVoteRequest(this@Node, msg)
            }

            val votes = receivedVotes.get()
            if (votes > totalNodes / 2) {
                this@Node.state = NodeState.LEADER
                this@Node.currentTerm += 1
                println("${this@Node} is now the leader of term $currentTerm")
            } else {
                this@Node.state = NodeState.FOLLOWER
                println("Votes ${votes}/${totalNodes / 2} received, ${this@Node} stays as follower")
            }

        } else {
            while (incomingHeartBeatMsgs.isNotEmpty()) {
                val heartbeatMsg = incomingHeartBeatMsgs.poll()
                if (heartbeatMsg.term >= currentTerm) {
                    println("Node ${this@Node}, stepping down as candidate, heartbeat received.")
                    state = NodeState.FOLLOWER
                } else {
                    println("Heartbeat with term: ${heartbeatMsg.term} ignored by node: ${this@Node} with current term: ${currentTerm}")
                }
            }
        }

    }

    private suspend fun SequenceScope<Component>.waitFor(
        queue: ComponentQueue<HeartbeatMsg>,
        failAt: Duration
    ) {

        for (waitCounter in 0..failAt.inWholeMilliseconds) {
            if (queue.isNotEmpty()) {
                break
            }
//            hold(1.milliseconds)
        }
    }

    fun triggerReconfiguration() {
        this.state = NodeState.CANDIDATE
        this.receivedVotes.set(0)
    }

    fun receiveVoteRequest(candidate: Node, msg: VoteRequestMsg) {
        if (state == NodeState.FOLLOWER || (state == NodeState.CANDIDATE && id < candidate.id && currentTerm <= candidate.currentTerm)) {
            candidate.receiveVoteResult(this, VoteResponseMsg(this, true))
        } else {
            candidate.receiveVoteResult(this, VoteResponseMsg(this, false))
        }
    }

    fun receiveVoteResult(voter: Node, voteResponseMsg: VoteResponseMsg) {
        if (voteResponseMsg.voted && state == NodeState.CANDIDATE) {
            receivedVotes.incrementAndGet()
        }
    }

    fun receiveVoteCandidate(candidate: Node, voteCandidateMsg: VoteCandidateMsg) {
        if (voteCandidateMsg.voted && state == NodeState.FOLLOWER) {
            receivedVotes.incrementAndGet()
        }
    }

    infix fun linkWith(node: Node) {
        val link = Link(node, ComponentQueue())
        links.add(link)
    }

    fun receiveHeartbeat(msg: HeartbeatMsg) {
        // Logic to handle heartbeat
        if (msg.term >= currentTerm) {
            currentTerm = msg.term
            println("Node $id received heartbeat from term ${msg.term}")
            incomingMsg.value = MsgState.HEARTBEAT_RECEIVED
            // Additional logic, if leader step down, if follower update leader if candidate step down etc.
        }
    }
}