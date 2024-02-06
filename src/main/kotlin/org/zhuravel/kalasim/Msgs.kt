package org.zhuravel.kalasim

import org.kalasim.Component

class HeartbeatMsg(val term: Int) : Component()
class VoteRequestMsg : Component()
class VoteResponseMsg(val voter: Node, val voted: Boolean) : Component()
class VoteCandidateMsg(val candidate: Node, val voted: Boolean) : Component()
class TermRequestMsg : Component()
class TermResponseMsg(val voter: Node, val voted: Boolean, val term: Int) : Component()