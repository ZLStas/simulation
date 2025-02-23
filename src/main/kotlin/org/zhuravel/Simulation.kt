package org.zhuravel

class Simulation {
    companion object {

        fun runWithBrokenLeaderToNodeLinksWhenUpperLimitIncreasesOnly(
            delayType: DelayType,
            from: Int,
            until: Int,
            networkDegradation: Int,
            ticks: Int,
            ab: Pair<Int, Int>,
            Tmax: Int,
            Dme: Float,
            Bc: Int,
            numberOfBrokenLinks: Int
        ): Map<Int, Double> {
            val cluster = buildBasicCluster(delayType, from, until, ab, Tmax, Dme, Bc)

            return (1..networkDegradation).asSequence().map { index ->
                cluster.applyForLeaderToNodeLinks(
                    DelaySimulatorFactory().getSimulator(delayType, from, until + index),
                    numberOfBrokenLinks
                )
                val reconfigurationNumber = cluster.runFor(ticks)
                index to reconfigurationNumber.values.average()
            }.toMap()
        }

        fun runWithBrokenLeaderToNodeLinksWhenLowerLimitIncreasesOnly(
            delayType: DelayType,
            from: Int,
            until: Int,
            networkDegradation: Int,
            ticks: Int,
            ab: Pair<Int, Int>,
            Tmax: Int,
            Dme : Float,
            Bc: Int,
            numberOfBrokenLinks: Int
        ): Map<Int, Double> {
            val cluster = buildBasicCluster(delayType, from, until, ab, Tmax, Dme, Bc)

            return (1..networkDegradation).asSequence().map { index ->
                cluster.applyForLeaderToNodeLinks(
                    DelaySimulatorFactory().getSimulator(
                        delayType,
                        from + index,
                        if (from + index <= until) until else from + index
                    ),
                    numberOfBrokenLinks
                )
                val reconfigurationNumber = cluster.runFor(ticks)
                index to reconfigurationNumber.values.average()
            }.toMap()
        }

        fun runWithBrokenLeaderToNodeLinks(
            delayType: DelayType,
            from: Int,
            until: Int,
            networkDegradation: Int,
            ticks: Int,
            ab: Pair<Int, Int>,
            Tmax: Int,
            Dme: Float,
            Bc: Int,
            numberOfBrokenLinks: Int
        ): Map<Int, Double> {
            val cluster = buildBasicCluster(delayType, from, until, ab, Tmax, Dme, Bc)

            return (1..networkDegradation).asSequence().map { index ->
                cluster.applyForLeaderToNodeLinks(
                    DelaySimulatorFactory().getSimulator(delayType, from + index, until + index),
                    numberOfBrokenLinks
                )
                val reconfigurationNumber = cluster.runFor(ticks)
                index to reconfigurationNumber.values.average()
            }.toMap()
        }


        fun runWithAllNodesDegrading(
            delayType: DelayType,
            from: Int,
            until: Int,
            networkDegradation: Int,
            ticks: Int,
            ab: Pair<Int, Int>,
            Tmax: Int,
            Dme: Float,
            Bc: Int,
        ): Map<Int, Double> {
            val cluster = buildBasicCluster(delayType, from, until, ab, Tmax, Dme,  Bc)
            return (1..networkDegradation).asSequence().map { index ->
                cluster.applyForAllLinksIn(
                    DelaySimulatorFactory().getSimulator(delayType, from + index, until + index)
                )
                val reconfigurationNumber = cluster.runFor(ticks)
                index to reconfigurationNumber.values.average()
            }.toMap()
        }

        fun runFor(
            delayType: DelayType,
            from: Int,
            until: Int,
            times: Int,
            ticks: Int,
            ab: Pair<Int, Int>,
            Tmax: Int,
            Dme: Float,
            Bc: Int,
        ): Map<Int, Double> {
            val cluster = buildBasicCluster(delayType, from, until, ab, Tmax, Dme, Bc)
            return (1..times).asSequence().map { index ->
                val reconfigurationNumber = cluster.runFor(ticks)
                index to reconfigurationNumber.values.average()
            }.toMap()
        }

        fun buildBasicCluster(
            delayType: DelayType,
            from: Int,
            until: Int,
            ab: Pair<Int, Int>,
            Tmax: Int,
            Dme: Float,
            Bc: Int
        ): Cluster {
            val delayProvider = DelaySimulatorFactory().getSimulator(delayType, from, until)

            val cluster = Cluster()
            // declare nodes
            val leader = Cluster.node {
                this.id = 1
                isLeader = true
                this.cluster = cluster
                this.ab = ab
                this.Tmax = Tmax
                this.Dme = Dme
                this.Bc = Bc
            }
            val node2 = Cluster.node {
                this.id = 2
                this.cluster = cluster
                this.ab = ab
                this.Tmax = Tmax
                this.Dme = Dme
                this.Bc = Bc
            }
            val node3 = Cluster.node {
                this.id = 3
                this.cluster = cluster
                this.ab = ab
                this.Tmax = Tmax
                this.Dme = Dme
                this.Bc = Bc
            }
            val node4 = Cluster.node {
                this.id = 4
                this.cluster = cluster
                this.ab = ab
                this.Tmax = Tmax
                this.Dme = Dme
                this.Bc = Bc
            }
            val node5 = Cluster.node {
                this.id = 5
                this.cluster = cluster
                this.ab = ab
                this.Tmax = Tmax
                this.Dme = Dme
                this.Bc = Bc
            }

            val node6 = Cluster.node {
                this.id = 6
                this.cluster = cluster
                this.ab = ab
                this.Tmax = Tmax
                this.Dme = Dme
                this.Bc = Bc
            }

            val node7 = Cluster.node {
                this.id = 7
                this.cluster = cluster
                this.ab = ab
                this.Tmax = Tmax
                this.Dme = Dme
                this.Bc = Bc
            }

            val node8 = Cluster.node {
                this.id = 8
                this.cluster = cluster
                this.ab = ab
                this.Tmax = Tmax
                this.Dme = Dme
                this.Bc = Bc
            }

            // set up links
            leader linkTo node2 using delayProvider
            leader linkTo node3 using delayProvider
            leader linkTo node4 using delayProvider
            leader linkTo node5 using delayProvider
//            leader linkTo node6 using delayProvider
//            leader linkTo node7 using delayProvider
//            leader linkTo node8 using delayProvider

            node2 linkTo leader using delayProvider
            node2 linkTo node3 using delayProvider
            node2 linkTo node4 using delayProvider
            node2 linkTo node5 using delayProvider
//            node2 linkTo node6 using delayProvider
//            node2 linkTo node7 using delayProvider
//            node2 linkTo node8 using delayProvider

            node3 linkTo leader using delayProvider
            node3 linkTo node2 using delayProvider
            node3 linkTo node4 using delayProvider
            node3 linkTo node5 using delayProvider
//            node3 linkTo node6 using delayProvider
//            node3 linkTo node7 using delayProvider
//            node3 linkTo node8 using delayProvider

            node4 linkTo leader using delayProvider
            node4 linkTo node2 using delayProvider
            node4 linkTo node3 using delayProvider
            node4 linkTo node5 using delayProvider
//            node4 linkTo node6 using delayProvider
//            node4 linkTo node7 using delayProvider
//            node4 linkTo node8 using delayProvider

            node5 linkTo leader using delayProvider
            node5 linkTo node2 using delayProvider
            node5 linkTo node3 using delayProvider
            node5 linkTo node4 using delayProvider
//            node5 linkTo node6 using delayProvider
//            node5 linkTo node7 using delayProvider
//            node5 linkTo node8 using delayProvider

//            node6 linkTo leader using delayProvider
//            node6 linkTo node2 using delayProvider
//            node6 linkTo node3 using delayProvider
//            node6 linkTo node4 using delayProvider
//            node6 linkTo node5 using delayProvider
//            node6 linkTo node7 using delayProvider
//            node6 linkTo node8 using delayProvider
//
//            node7 linkTo leader using delayProvider
//            node7 linkTo node2 using delayProvider
//            node7 linkTo node3 using delayProvider
//            node7 linkTo node4 using delayProvider
//            node7 linkTo node5 using delayProvider
//            node7 linkTo node6 using delayProvider
//            node7 linkTo node8 using delayProvider
//
//            node8 linkTo leader using delayProvider
//            node8 linkTo node2 using delayProvider
//            node8 linkTo node3 using delayProvider
//            node8 linkTo node4 using delayProvider
//            node8 linkTo node5 using delayProvider
//            node8 linkTo node6 using delayProvider
//            node8 linkTo node7 using delayProvider

            cluster.apply {
                nodes = listOf(leader, node2, node3, node4, node5)
//                nodes = listOf(leader, node2, node3, node4)
//                nodes = listOf(leader, node2, node3)
            }
            return cluster
        }

    }
}
