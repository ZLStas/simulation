package org.zhuravel.kalasim;

import org.kalasim.State

class IncomingMsg : State<HeartbeatMsg>(HeartbeatMsg(1, false)) {

}