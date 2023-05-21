package org.prismsus.tank.element

import org.prismsus.tank.utils.INIT_BULLET_COLBOX
import org.prismsus.tank.utils.INIT_BULLET_SPEED
import org.prismsus.tank.utils.collidable.ColBox
import org.prismsus.tank.utils.collidable.RectColBox

class Bullet(uid: Long, var speed: Double = INIT_BULLET_SPEED, override val colBox: RectColBox = INIT_BULLET_COLBOX) :
    MovableElement(uid, -1, colBox) {
    constructor(uid: Long, props: BulletProps) : this(uid, props.speed, props.colBox)

    override val serialName: String
        get() = "Blt"
    var damage: Int = -1

}

data class BulletProps(val speed: Double = INIT_BULLET_SPEED, val colBox: RectColBox = INIT_BULLET_COLBOX)