package org.zhuravel

import kotlin.random.Random

data class Node(val id: Int, var isLeader: Boolean, var cluster: Cluster, var reconfigurationTimout: Int, val ab: Pair<Int, Int>, val Tmax: Int, val Dme: Float, val Bc: Int,  var links: MutableList<Link> = mutableListOf()) {

    var maxLm : Int = 0

    fun linkToUsing(node: Node, delaySimulator: DelaySimulator): Node {
        val link = link {
            source = this@Node
            destination = node
            this.delaySimulator = delaySimulator
        }
        links.add(link)
        node.links.add(link)
        return node
    }

    private fun isBestCandidate(): Boolean {
       return this@Node == cluster.mostEffectiveNode
    }

    // Infix function that starts the link process
    infix fun linkTo(destination: Node): Link.LinkBuilder {
        return Link.LinkBuilder().also {
            it.source = this
            it.destination = destination
        }
    }

    fun biDirectionalLinkUsing(node: Node, delaySimulator: DelaySimulator) {
        this.linkToUsing(node, delaySimulator)
        node.linkToUsing(this, delaySimulator)
    }

    fun biDirectionalLinksUsing(nodes: List<Node>, delaySimulator: DelaySimulator) {
        nodes.forEach { biDirectionalLinkUsing(it, delaySimulator) }
    }

    fun linkToUsing(nodes: List<Node>, delaySimulator: DelaySimulator) {
        nodes.forEach { linkToUsing(it, delaySimulator) }
    }

    fun tick() {
        if (isLeader) {
            return
        } else {
            links.forEach {
                if (!cluster.reconfigurationsTriggeredForCurrentTime && it.source.isLeader && it.destination == this@Node && it.d > reconfigurationTimout) {
                    cluster.reconfigure()
                }
            }
            calculateReconfigurationTime()
        }
    }

    private fun calculateReconfigurationTime() {
        val p1a = ((maxLm.toFloat() / cluster.maxTethaM) * Tmax).toInt()
        val p1b = (
                Tmax * (if (isBestCandidate()) Dme else (1 - Dme))
                ).toInt()
        val p1 = maxOf(p1a, p1b) + Random.nextInt(ab.first, ab.second)
        val p2 = minOf(calculateDelayAdjustmentCoefficient(), Bc)
        reconfigurationTimout = p1 + p2
    }

    private fun calculateDelayAdjustmentCoefficient(): Int {
        // Cluster contains leader as best candidate, and this node is current node,
        // this situation is not real,
        // since model doesn't assess this situation ( take a look above)
        if (isLeader && isBestCandidate()) {
            return 0
        }

        // cluster contains leader as best candidate, and this node is not current node
        if (cluster.links.any { it.destination.isBestCandidate() && it.destination.isLeader }) {
            return 0
        }

        val Tdlbc = cluster.links.first {
            it.source.isLeader && it.destination.isBestCandidate()
        }.d

        val Tbccc = cluster.links.first {
            it.source.isBestCandidate() && it.destination.isLeader
        }.d

        val Tdlcc = cluster.links.first {
            it.source.isLeader && it.destination == this@Node
        }.d

        return Tdlbc + Tbccc - Tdlcc
    }

    fun calculateMaximalLatancyWithinMostEffectiveMagority() {
        val majoritySize = if (cluster.nodes.size % 2 != 0) {
            (cluster.nodes.size + 1) / 2
        } else {
            (cluster.nodes.size / 2) + 1
        }
        maxLm = links.filter { it.source == this@Node }
            .map { it.d }
            .sorted()
            .take(majoritySize -1 )
            .last()
    }

    class NodeBuilder {
        var id: Int? = null
        var isLeader: Boolean = false
        var ab: Pair<Int,Int>? = null
        var Tmax: Int? = null
        var Dme: Float? = null
        var Bc: Int? = null
        var links: MutableList<Link> = mutableListOf()
        var cluster: Cluster? = null

        fun build(): Node {
            return Node(
                id ?: throw IllegalArgumentException("Node id is requred."),
                isLeader,
                cluster ?: throw IllegalArgumentException("cluster is requred."),
                Tmax ?: throw IllegalArgumentException("reconfigurationTimout is requred."),
                ab ?: throw IllegalArgumentException("ab is required."),
                Tmax ?: throw IllegalArgumentException("Tmax is required."),
                Dme ?: throw IllegalArgumentException("Dme is required."),
                Bc ?: throw IllegalArgumentException("Bc is required."),
                links
            )
        }
    }

}
