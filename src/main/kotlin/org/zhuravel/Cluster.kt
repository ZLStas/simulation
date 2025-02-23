package org.zhuravel

class Cluster(var nodes: List<Node>) {

    constructor() : this(mutableListOf())

    val links : MutableList<Link> = mutableListOf()
    var reconfigurationsNumber: Int = 0
    var reconfigurationsTriggeredForCurrentTime: Boolean = false
    var mostEffectiveNode : Node? = null

    var maxTethaM: Int = 0

    fun setLeader(id: Int) {
        val candidate = nodes.find { it.id == id }
        check(candidate != null) {
            "No such node is present in the cluster"
        }
        val currentLeader = nodes.find { it.isLeader }
        if (currentLeader != null) {
            println("Current leader is stepping down")
            currentLeader.isLeader = false
        }
        candidate.isLeader = true
        println("New leader is ${candidate}")
    }

    fun reconfigure() {
        reconfigurationsNumber++
        reconfigurationsTriggeredForCurrentTime = true
    }

    fun runFor(ticks: Int): Map<Int, Int> {
        return (1..ticks).asSequence().map { index ->
            tick()
            val reconfigurationNumber = reconfigurationsNumber
            reconfigurationsNumber = 0
            index to reconfigurationNumber
        }.toMap()
    }

    fun tick() {
        reconfigurationsTriggeredForCurrentTime = false

        links.forEach {
            it.tick()
        }

        nodes.forEach{
            it.calculateMaximalLatancyWithinMostEffectiveMagority()
        }

        maxTethaM = nodes.map { it.maxLm }.maxOf { it }

        mostEffectiveNode = nodes.minByOrNull { it.maxLm }!!

        nodes.forEach{
            it.tick()
        }
    }

    fun applyForAllLinksIn(delaySimulator: DelaySimulator) {
        nodes.forEach { node ->
            node.links.forEach { link ->
                link.apply {
                    this.delaySimulator = delaySimulator
                }
            }
        }
    }

    fun applyForLeaderToNodeLinks(
        delaySimulator: DelaySimulator,
        numerOfLinksToAssign: Int
    ) {
        val leader = this.nodes.firstOrNull { it.isLeader }
            ?: throw IllegalStateException("No leader is present in cluseter, consider assign a leader first")
        val linksFromLeader = leader.links.filter { it.source == leader }
        linksFromLeader.take(numerOfLinksToAssign).forEach {
            it.delaySimulator = delaySimulator
        }
    }


    companion object {
        fun node(init: Node.NodeBuilder.() -> Unit): Node {
            return Node.NodeBuilder().apply(init).build()
        }
    }

    class ClusterBuilder {
        var nodes: List<Node>? = null

        fun build(): Cluster {
            return Cluster(
                nodes ?: throw IllegalArgumentException("Nodes are required.")
            )
        }

    }

}
