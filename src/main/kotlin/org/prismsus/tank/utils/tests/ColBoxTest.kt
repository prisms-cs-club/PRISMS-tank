package org.prismsus.tank.utils.tests

import org.junit.jupiter.api.Test
import org.prismsus.tank.utils.*
import org.junit.jupiter.api.Assertions.*
import org.prismsus.tank.utils.intersectables.ColBox
import org.prismsus.tank.utils.intersectables.DPos2
import org.prismsus.tank.utils.intersectables.Line
import org.prismsus.tank.utils.intersectables.RectColBox
import java.awt.Color
import java.awt.Graphics2D
import kotlin.random.Random

typealias Pos = DPos2

class ColBoxTest {

    @Test
    fun rotate() {
        var testNum = 0
        // create a square box, rotate it by 90 degree, it should have the same point set as the original
        println("t${++testNum}")
        var box1 : ColBox = RectColBox.byTopLeft(Pos(0.0, 1.0), DDim2(1.0, 1.0))
        var box2 : ColBox = RectColBox.byTopLeft(Pos.ORIGIN, DDim2(1.0, 1.0)).rotateDeg(90.0, Pos.ORIGIN) as ColBox


        assertTrue(box2 equalPtSet box1)
        println("passed")

        // create a square box, rotate it by 180 degree from the center, it should have the same point set as the original
        println("t${++testNum}")
        box1 = RectColBox.byTopLeft(Pos.ORIGIN, DDim2(1.0, 1.0))
        box2 = RectColBox.byTopLeft(Pos.ORIGIN, DDim2(1.0, 1.0)).rotateDeg(180.0, Pos(.5, -.5)) as ColBox
        assertTrue(box2 == box1)
        println("passed")

        // create a rectangle box with longer side on x axis, rotate it by 90 degree, it should have the same point set
        // with a rectangle box with longer side on y axis
        println("t${++testNum}")
        box1 = RectColBox.byTopLeft(Pos(0.0, 1.0), DDim2(2.0, 1.0)).rotateDeg(90.0, Pos.ORIGIN) as ColBox
        box2 = RectColBox.byTopLeft(Pos(-1.0, 2.0), DDim2(1.0, 2.0))
        assertTrue(box2 == box1)
        println("passed")

        // create a rectangle box with longer side on x axis, rotate it by -90 degree, it should have the same point set
        // with a rectangle box with longer side on y axis
        println("t${++testNum}")
        box1 = RectColBox.byTopLeft(Pos(0.0, 1.0), DDim2(2.0, 1.0)).rotateDeg(-90.0, Pos.ORIGIN) as ColBox
        box2 = RectColBox.byTopLeft(Pos(0.0, 0.0), DDim2(1.0, 2.0))
        assertTrue(box2 == box1)
        println("passed")
    }

    @Test
    fun intersect() {
        var testNum = 0
        // test if two same ColBox.byTopLeftes can intersect
        println("t${++testNum}")
        var box1 : ColBox = RectColBox.byTopLeft(Pos.ORIGIN, DDim2(1.0, 1.0))
        var box2 : ColBox = RectColBox.byTopLeft(Pos.ORIGIN, DDim2(1.0, 1.0))
        assertTrue(box1.intersect(box2) && box2.intersect(box1))
        println("pass")

        // test if two ColBox.byTopLeftes with different size can intersect
        println("t${++testNum}")
        box1 = RectColBox.byTopLeft(Pos.ORIGIN, DDim2(2.0, 2.0))
        box2 = RectColBox.byTopLeft(Pos.ORIGIN, DDim2(1.0, 1.0))
        assertTrue(box1.intersect(box2) && box2.intersect(box1))
        println("pass")

        // test two boxes touching
        println("t${++testNum}")
        box1 = RectColBox.byTopLeft(Pos.ORIGIN, DDim2(1.0, 1.0))
        box2 = RectColBox.byTopLeft(Pos.UP, DDim2(1.0, 1.0))
        assertTrue(box1.intersect(box2) && box2.intersect(box1))
        println("pass")

        // move the second box up, they should not touch this time
        println("t${++testNum}")
        box2 += DVec2.UP
        assertFalse(box1.intersect(box2) && box2.intersect(box1))
        println("pass")

        // test the case when on box enclose the other
        println("t${++testNum}")
        box1 = RectColBox.byTopLeft(Pos.ORIGIN, DDim2(3.0, 3.0))
        box2 = RectColBox.byTopLeft(Pos(0.0, -1.5), DDim2(1.0, 1.0))
        assertTrue(box1.intersect(box2))
        assertTrue(box2.intersect(box1))
        print(box1.intersectPts(box2).contentToString())
        println("pass")

        // test polygon other than rectangle, first test right triangle
        println("t${++testNum}")
        box1 = ColBox(arrayOf(Pos.ORIGIN, Pos.RT, Pos.UP))
        box2 = ColBox(arrayOf(Pos.ORIGIN, Pos.RT, Pos.UP))
        assertTrue(box1.intersect(box2) && box2.intersect(box1))
        println("pass")

        // move the second box a little bit, they should touch this time
        println("t${++testNum}")
        box2 += DVec2.UP * 0.5
        assertTrue(box1.intersect(box2) && box2.intersect(box1))
        println("pass")

        // move it further, they should not touch this time
        println("t${++testNum}")
        box2 += DVec2.UP
        assertFalse(box1.intersect(box2) && box2.intersect(box1))
        println("pass")

        // generate two polygons with PT_CNT points randomly, they share one point, so they should intersect
        println("t${++testNum}")
        val PT_CNT = 200
        val samePt = Pos(1.0, 1.0)
        val pts1 = Array(PT_CNT){(DVec2.randUnitVec() * Random.nextDouble(1.0, 15.0)).toPt()}
        val pts2 = Array(PT_CNT){(DVec2.randUnitVec() * Random.nextDouble(1.0, 15.0)).toPt()}
        pts1[PT_CNT - 1] = samePt
        pts2[PT_CNT - 1] = samePt
        var startTm = System.currentTimeMillis()
        box1 = ColBox.byUnorderedPtSet(pts1)
        box2 = ColBox.byUnorderedPtSet(pts2)
        println("time to construct two boxes: ${System.currentTimeMillis() - startTm} ms")
        startTm = System.currentTimeMillis()
        assertTrue(box1.intersect(box2))
        println("time to test intersection: ${System.currentTimeMillis() - startTm} ms")
        println("pass")
        val panel = CoordPanel(IDim2(1, 1), IDim2(15, 15))
        panel.drawIntersectable(box1)
        panel.graphicsModifier = { g : Graphics2D ->
            g.color = Color.RED
        }
        panel.drawIntersectable(box2)
        panel.showFrame()
        while(true){}
    }


    @Test
    fun testGenRandPolygon(){
        // generate random points, print them in the format of x, y , no parentheses
        val pts = Array(100){(DVec2.randUnitVec() * Random.nextDouble() * 100.0).toPt()}
        val box = ColBox.byUnorderedPtSet(pts)
        for (pt : DPos2 in box.pts) {
            println("${pt.x}, ${pt.y}")
        }
        // print the original point order
        println("original order:")
        for (pt : DPos2 in pts) {
            println("${pt.x}, ${pt.y}")
        }

    }


    @Test
    fun angleRotated(){

        // rotate 100 times by random center
        for (i in 0 until 100) {
            val randObj = Line(DPos2(0.0, .0), DPos2(.0, 1.0))
            val center = DVec2.randUnitVec()
            var rot = Random.nextDouble() * 360.0
            randObj.rotateAssignDeg(rot, center.toPt())

            if (rot < 0)
                rot += 360.0

            assertEquals(rot, randObj.angleRotated.toDeg(), DOUBLE_PRECISION)
        }

    }

}