package org.prismsus.tank.elements

import org.prismsus.tank.event.UpdateEventMask
import org.prismsus.tank.utils.INIT_BULLET_COLBOX
import org.prismsus.tank.utils.INIT_BULLET_SPEED
import org.prismsus.tank.utils.collidable.ColRect
import org.prismsus.tank.utils.errNE

class Bullet(uid: Long, val belongTo : Tank, var speed: Double = INIT_BULLET_SPEED, override val colPoly: ColRect = INIT_BULLET_COLBOX) :
    MovableElement(uid, -1, colPoly) {
    constructor(uid: Long, belongTo: Tank, props: BulletProps) : this(uid, belongTo, props.speed, props.colBox)

    init{
        belongTo.noCollisionElementIds.add(uid)
    }
    // need to convert from m/s to m/ms
    var damage = -1
    override val serialName: String
        get() = "Blt"
    override fun willMove(dt: Long): Boolean {
        return speed errNE .0
    }

    override fun colPolyAfterMove(dt: Long): ColRect {
        val ret = colPoly.copy()
        ret += velocity * dt.toDouble()
        return ret
    }

    override infix fun processCollision(other: GameElement): UpdateEventMask {
        removeStat = RemoveStat.TO_REMOVE
        return super.processCollision(other)
    }
}

class BulletProps(var speed: Double = INIT_BULLET_SPEED, var colBox: ColRect = INIT_BULLET_COLBOX){
//    val colBox : ColRect
    fun copy() : BulletProps {
        return BulletProps(speed, colBox.copy())
    }
}