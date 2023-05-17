package org.prismsus.tank.utils
import kotlin.math.*


/**
 * @param startP The starting point of the line.
 * @param endP The ending point of the line.
 * @constructor Create a line with two points.
 * */
class Line(override var pts : Array<DPos2>) : Intersectable {
    init{
        if (pts.size != 2) {
            throw IllegalArgumentException("Line must be initialized with two points")
        }
    }

    override var angleRotated = 0.0
    val origStart = DPos2(startP)
    val origEnd = DPos2(endP)

    override var rotationCenter: DPos2
        get() = ((startP.toVec() + endP.toVec()) / 2.0).toPt()
        set(value) {}

    override val unrotated: Intersectable
        get() = Line(origStart, origEnd)


    constructor (startP : DPos2, endP : DPos2) : this(arrayOf(startP, endP)){

    }
    var slope
        get() = (endP.y - startP.y) / (endP.x - startP.x)
        set(value) {}
    var inter
        get() = startP.y - slope * startP.x
        set(value) {}
    var startP: DPos2
        get() = pts[0]
        set(new) {
            pts[0] = new
        }
    var endP: DPos2
        get() = pts[1]
        set(new) {
            pts[1] = new
        }

    /**
     * Check if two intersectable objects intersect.
     * @param other The other object.
     * @return True if intersects, false otherwise.
     * @see Intersectable.intersect
     * */
    override fun intersect(other: Intersectable): Boolean {
        if (other is DPos2) {
            // calculate y position of the line given x is other.x
            val y = slope * other.x + inter
            return inXrg(other.x) && y == other.y
        }
        if (other !is Line) {
            // in this case, the other object is a colBox
            return other.intersect(this)
        }
        val otherLine = other as Line
        // check if two lines are parallel, in this case, they will never intersect
        if (slope == otherLine.slope) {
            if (inter != otherLine.inter) {
                return false
            }
            // slope and intercept are all the same
            // now check if they overlap within the range
            val inThisRange: Boolean = inXrg(otherLine.startP.x) || inXrg(otherLine.endP.x)
            // either starting point or ending point in the range of this line
            val inOtherRange: Boolean = otherLine.inXrg(startP.x) || otherLine.inXrg(endP.x)
            return inThisRange || inOtherRange
            // using or here considering the case where one line is within the other line
        }

        // check if the slope is infinity
        val infCnt: Int = if (isVertical()) 1 else 0 +
                if (other.isVertical()) 1 else 0

        if (infCnt == 2) {
            // both lines are vertical
            if (startP.x != otherLine.startP.x) {
                return false
            }
            // now check if they overlap within the range
            val inThisRange: Boolean = otherLine.startP.y >= startP.y && otherLine.startP.y <= endP.y
                    || otherLine.endP.y >= startP.y && otherLine.endP.y <= endP.y
            // either starting point or ending point in the range of this line
            val inOtherRange: Boolean = startP.y >= otherLine.startP.y && startP.y <= otherLine.endP.y
                    || endP.y >= otherLine.startP.y && endP.y <= otherLine.endP.y
            return inThisRange || inOtherRange
            // using or here considering the case where one line is within the other line
        }
        if (infCnt == 1) {

            // if only one of two lines are vertical
            // find the y value of the non-vertical line that meet with the vertical line
            val vLine =
                if (isVertical()) this else otherLine
            val nvLine =
                if (isVertical()) otherLine else this
            if (vLine.startP.x < nvLine.startP.x || vLine.startP.x > nvLine.endP.x) {
                return false
            }
            val y = nvLine.slope * vLine.startP.x + nvLine.inter
            val inVrange: Boolean = vLine.inYrg(y)
            val inNVrange: Boolean = nvLine.inYrg(y)
            return inVrange && inNVrange
        }

        val intersectX = (otherLine.inter - inter) / (slope - otherLine.slope)
        // calculate the point where intersection happens
        // then check if this point is in the range of both lines
        val inThisRange: Boolean = inXrg(intersectX)
        val inOtherRange: Boolean = inXrg(intersectX)
        return inThisRange && inOtherRange
    }

    /**
     * Check if two lines are equal, which means they have the same starting point and ending point.
     * Notice that when the difference between two double values is less than [DOUBLE_PRECISION], they are considered equal.
     * @param other the other line to compare with
     * @return true if two lines are equal, false otherwise
     * @see DOUBLE_PRECISION
     * @see DPos2.equals
     * */
    override fun equals(other: Any?): Boolean {
        if (other !is Line) {
            return false
        }
        return startP == other.startP && endP == other.endP
    }

    override fun toString(): String {
        return "$startP -> $endP, slope: $slope, intercept: $inter"
    }


    /**
     * @return the length of the line
     * */
    fun len(): Double {
        return startP.dis(endP)
    }

    /**
     * @return the square of the length of the line
     * @see len
     * */
    fun sqLen(): Double {
        return startP.sqDis(endP)
    }

    /**
     * Return the point when travel a certain distance from the start point of the line
     * @param t the parameter of the line, t = 0 means the start point, t = 1 means the end point
     * @return the point when travel a certain distance from the start point of the line
     * */
    fun at(t : Double) : DPos2 {
        return startP + (endP - startP) * t
    }

    fun atLen(len : Double) : DPos2 {
        return startP + (endP - startP).norm() * len
    }


    /**
     * Calculate the shortest distance from the line to the point
     * @param pt the point to calculate the distance to
     * @return the shortest distance from the line to the point
     * */
    fun disToPt(pt: DPos2): Double {
        // shortest distance from the line to the point
        if (len() < DOUBLE_PRECISION) return startP.dis(pt)
        val toPt = pt - startP // from the start point of the line to the point
        val t = toPt.dot(endP - startP) / sqLen()
        // first divided by Len(), meaning the projection of toPt on the line
        // divide another Len() to get the parameter t
        // clamp this t to make sure that it falls on the line segment
        val clampedT = t.coerceIn(0.0, 1.0)
        return at(clampedT).dis(pt)
    }

    /**
     * Convert the line to a vector points from the starting point to the ending point
     * @return the vector representation of the line
     * */
    fun toVec(): DVec2 {
        return endP - startP
    }

    /**
     * Check if an x value is between the starting point and the ending point of the line, inclusively
     * @param x the x value to check
     * @return true if the x value is between the starting point and the ending point of the line, false otherwise
     * @see inYrg
     * */
    fun inXrg(x: Double): Boolean {
        return x >= startP.x && x <= endP.x
    }

    /**
     * Check if a y value is between the starting point and the ending point of the line, inclusively
     * @param y the y value to check
     * @return true if the y value is between the starting point and the ending point of the line, false otherwise
     * @see inXrg
     * */
    fun inYrg(y: Double): Boolean {
        return y >= startP.y && y <= endP.y
    }

    fun isVertical() : Boolean {
        return startP.x == endP.x
    }


    override fun plus(shift: DVec2): Line{
        return Line(startP + shift, endP + shift)
    }


    override fun minus(shift: DVec2): Line{
        return Line(startP - shift, endP - shift)
    }

    override fun byPts(_pts: Array<DPos2>): Intersectable {
        return Line(_pts[0], _pts[1])
    }
}
