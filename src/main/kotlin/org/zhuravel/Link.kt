package org.zhuravel

class Link(
    val nodes: Pair<Node, Node>,
    delaySimulator: DelaySimulator
) : DelaySimulator by delaySimulator {

    var delaySimulator: DelaySimulator = delaySimulator
        set(value) {
            field = value
        }

    val source: Node
    val destination: Node

    init {
        source = nodes.first
        destination = nodes.second
    }

    class LinkBuilder {
        var source: Node? = null
        var destination: Node? = null
        var delaySimulator: DelaySimulator? = null

        // Function to set the DelaySimulator and build the Link
        infix fun using(delaySimulator: DelaySimulator): Link {
            this.delaySimulator = delaySimulator
            return build().also { link ->
                source!!.links.add(link)
                destination!!.links.add(link)
            }
        }

        fun build(): Link {
            val fromSource = source ?: throw IllegalArgumentException("Source is required.")
            val destination = destination ?: throw IllegalArgumentException("Destination is required")
            val simulator = delaySimulator ?: throw IllegalArgumentException("DelaySimulator is required")
            return Link(fromSource to destination, simulator)
        }

    }

}