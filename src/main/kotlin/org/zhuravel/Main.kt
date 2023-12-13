package org.zhuravel

fun main(args: Array<String>) {
    fun node(init: Node.NodeBuilder.() -> Unit): Node {
        return Node.NodeBuilder().apply(init).build()
    }

    val cluster =  Cluster(listOf())
// declare nodes
    val leader = node {
        this.id = 1
        isLeader = true
        this.cluster = cluster
    }
    val node2 = node {
        this.id = 2
        this.cluster = cluster
    }
    val node3 = node {
        this.id = 3
        this.cluster = cluster
    }
    val node4 = node {
        this.id = 4
        this.cluster = cluster
    }
    val node5 = node {
        this.id = 5
        this.cluster = cluster
    }

    val delayProvideer = NormalDelaySimulator(100 to 600)
    // set up links
    leader linkTo node2 using delayProvideer
    leader linkTo node3 using delayProvideer
    leader linkTo node4 using delayProvideer
    leader linkTo node5 using delayProvideer

    node2 linkTo leader using delayProvideer
    node2 linkTo node3 using delayProvideer
    node2 linkTo node4 using delayProvideer
    node2 linkTo node5 using delayProvideer

    node3 linkTo leader using delayProvideer
    node3 linkTo node2 using delayProvideer
    node3 linkTo node4 using delayProvideer
    node3 linkTo node5 using delayProvideer

    node4 linkTo leader using delayProvideer
    node4 linkTo node2 using delayProvideer
    node4 linkTo node3 using delayProvideer
    node4 linkTo node5 using delayProvideer

    node5 linkTo leader using delayProvideer
    node5 linkTo node2 using delayProvideer
    node5 linkTo node3 using delayProvideer
    node5 linkTo node4 using delayProvideer

    cluster.apply {
        nodes = listOf(leader, node2, node3, node4, node5)
    }

    val reconfigurationFriquency = (1..10).asSequence().map { index ->
        cluster.tick()
        val reconfigurationNumber = cluster.reconfigurationsNumber
        cluster.reconfigurationsNumber = 0
        index to reconfigurationNumber
    }.toMap()

    println(reconfigurationFriquency)

}

