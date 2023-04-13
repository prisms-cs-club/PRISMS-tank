package org.prismsus.tank.block

class EmptyBlock(x: Int, y: Int): Block(x, y) {
    override fun canPass() = true
    override val bulletAction = BulletAction.PASS
    override fun canSee() = true
}