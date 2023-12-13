package org.zhuravel

data class Node(val id: Int, var isLeader: Boolean, var cluster: Cluster, var reconfigurationTimout: Int,  var links: MutableList<Link> = mutableListOf()) {

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
                if (cluster?.reconfigurationsTriggeredForCurrentTime == false && it.source.isLeader && it.destination == this && it.delaySimulator.delay > reconfigurationTimout) {
                    triggerReconfiguration()
                }
            }
        }
    }

    fun triggerReconfiguration(){
        cluster.reconfigure()
    }

    class NodeBuilder {

        var id: Int? = null
        var isLeader: Boolean = false
        var reconfigurationTimout: Int? = null
        var links: MutableList<Link> = mutableListOf()
        var cluster: Cluster? = null

        fun build(): Node {
            return Node(
                id ?: throw IllegalArgumentException("Node id is requred."),
                isLeader,
                cluster ?: throw IllegalArgumentException("cluster is requred."),
                reconfigurationTimout ?: throw IllegalArgumentException("reconfigurationTimout is requred."),
                links
            )
        }
    }

}