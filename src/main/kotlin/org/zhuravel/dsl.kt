package org.zhuravel

import org.zhuravel.Cluster.ClusterBuilder
import org.zhuravel.Node.NodeBuilder
import org.zhuravel.Link.LinkBuilder


fun cluster(init: ClusterBuilder.() -> Unit): Cluster {
    return ClusterBuilder().apply(init).build()
}

fun link(init: LinkBuilder.() -> Unit): Link {
    return LinkBuilder().apply(init).build()
}

fun node(init: NodeBuilder.() -> Unit): Node {
    return NodeBuilder().apply(init).build()
}
