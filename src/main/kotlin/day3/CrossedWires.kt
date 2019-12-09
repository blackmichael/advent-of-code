package main.day3

import main.day3.Panel.GridCoordinates
import main.day3.WireDirection.D
import main.day3.WireDirection.L
import main.day3.WireDirection.R
import main.day3.WireDirection.U
import java.io.File
import kotlin.math.abs

/**
Part 1:
The gravity assist was successful, and you're well on your way to the Venus refuelling station. During the rush back on Earth, the fuel management system wasn't completely installed, so that's next on the priority list.

Opening the front panel reveals a jumble of wires. Specifically, two wires are connected to a central port and extend outward on a grid. You trace the path each wire takes as it leaves the central port, one wire per line of text (your puzzle input).

The wires twist and turn, but the two wires occasionally cross paths. To fix the circuit, you need to find the intersection point closest to the central port. Because the wires are on a grid, use the Manhattan distance for this measurement. While the wires do technically cross right at the central port where they both start, this point does not count, nor does a wire count as crossing with itself.

For example, if the first wire's path is R8,U5,L5,D3, then starting from the central port (o), it goes right 8, up 5, left 5, and finally down 3:

...........
...........
...........
....+----+.
....|....|.
....|....|.
....|....|.
.........|.
.o-------+.
...........

Then, if the second wire's path is U7,R6,D4,L4, it goes up 7, right 6, down 4, and left 4:

...........
.+-----+...
.|.....|...
.|..+--X-+.
.|..|..|.|.
.|.-X--+.|.
.|..|....|.
.|.......|.
.o-------+.
...........

These wires cross at two locations (marked X), but the lower-left one is closer to the central port: its distance is 3 + 3 = 6.

Here are a few more examples:

R75,D30,R83,U83,L12,D49,R71,U7,L72
U62,R66,U55,R34,D71,R55,D58,R83 = distance 159
R98,U47,R26,D63,R33,U87,L62,D20,R33,U53,R51
U98,R91,D20,R16,D67,R40,U7,R15,U6,R7 = distance 135

What is the Manhattan distance from the central port to the closest intersection?

---

Part 2:
It turns out that this circuit is very timing-sensitive; you actually need to minimize the signal delay.

To do this, calculate the number of steps each wire takes to reach each intersection; choose the intersection where the sum of both wires' steps is lowest. If a wire visits a position on the grid multiple times, use the steps value from the first time it visits that position when calculating the total value of a specific intersection.

The number of steps a wire takes is the total number of grid squares the wire has entered to get to that location, including the intersection being considered. Again consider the example from above:

...........
.+-----+...
.|.....|...
.|..+--X-+.
.|..|..|.|.
.|.-X--+.|.
.|..|....|.
.|.......|.
.o-------+.
...........

In the above example, the intersection closest to the central port is reached after 8+5+5+2 = 20 steps by the first wire and 7+6+4+3 = 20 steps by the second wire for a total of 20+20 = 40 steps.

However, the top-right intersection is better: the first wire takes only 8+5+2 = 15 and the second wire takes only 7+6+2 = 15, a total of 15+15 = 30 steps.

Here are the best steps for the extra examples from above:

R75,D30,R83,U83,L12,D49,R71,U7,L72
U62,R66,U55,R34,D71,R55,D58,R83 = 610 steps
R98,U47,R26,D63,R33,U87,L62,D20,R33,U53,R51
U98,R91,D20,R16,D67,R40,U7,R15,U6,R7 = 410 steps

What is the fewest combined steps the wires must take to reach an intersection?
 */
object CrossedWires {
    fun readFileInput(file: File): List<List<WirePath>> = file.readLines()
        .map { it.split(",") }
        .map { toWirePaths(it) }

    private fun toWirePaths(paths: List<String>): List<WirePath> = paths.map { path ->
        val (direction, distance) = path.partition { ch ->
            WireDirection.values()
                .map { it.name }
                .contains(ch.toString())
        }

        WirePath(WireDirection.valueOf(direction), distance.toInt())
    }

    fun part1(wires: List<Wire>, centralPortX: Int, centralPortY: Int, width: Int, height: Int): Int {
        val start = GridCoordinates(centralPortX, centralPortY)
        val panel = Panel(wires, start, width, height)

        return panel.getIntersections()
            .map { Panel.calculateDistance(start, it.first) }
            .min() ?: -1
    }

    fun part2(wires: List<Wire>, centralPortX: Int, centralPortY: Int, width: Int, height: Int): Int {
        val start = GridCoordinates(centralPortX, centralPortY)
        val panel = Panel(wires, start, width, height)
        val wireLookup = wires.mapIndexed { index, wire -> (index + 1) to wire }.toMap()

        val minimumCombinedSteps = panel.getIntersections()
            .map { (point, wires) ->
                wires.sumBy { wireNumber ->
                    val wire = wireLookup[wireNumber] ?: throw RuntimeException("no wire found")
                    panel.calculateStepsToPoint(wire, point)
                }
            }
            .min() ?: throw RuntimeException("no intersections found")

        return minimumCombinedSteps
    }
}

typealias Intersection = Pair<GridCoordinates, Set<Int>>

typealias Wire = List<WirePath>

enum class WireDirection { R, U, L, D }

data class WirePath(
    val direction: WireDirection,
    val distance: Int
)

class Panel(
    wires: List<Wire>,
    val centralPort: GridCoordinates,
    val width: Int,
    val height: Int
) {

    data class GridCoordinates(
        val x: Int,
        val y: Int
    )

    // 0 - empty, 1 - wire
    private val panel: Array<Array<Set<Int>>> =
        (0..width).map { x ->
            (0..height).map { y ->
                if (x == centralPort.x && y == centralPort.y) {
                    setOf(0)
                } else {
                    emptySet()
                }
            }.toTypedArray()
        }.toTypedArray()

    companion object {
        fun calculateDistance(a: GridCoordinates, b: GridCoordinates): Int =
            abs(a.x - b.x) + abs(a.y - b.y)
    }

    init {
        wires.forEachIndexed { index, wire ->
            val wireNumber = index + 1
            var nextStart = centralPort
            wire.forEach { path ->
                nextStart = traverse(wireNumber, path, nextStart)
            }
        }
    }

    fun calculateStepsToPoint(wire: Wire, point: GridCoordinates): Int {
        var currX = centralPort.x
        var currY = centralPort.y
        var steps = 0
        wire.forEach { path ->
            // this might be easiest if it iterates point by point
            when (path.direction) {
                R -> {
                    val nextX = currX + path.distance

                    // check to see if we've reached the point or passed it
                    if (currY == point.y && nextX >= point.x) {
                        steps += (point.x - currX)
                        return steps
                    } else {
                        // otherwise continue
                        steps += (nextX - currX)
                        currX = nextX
                    }
                }
                L -> {
                    val nextX = currX - path.distance

                    // check to see if we've reached the point or passed it
                    if (currY == point.y && nextX <= point.x) {
                        steps += (currX - point.x)
                        return steps
                    } else {
                        // otherwise continue
                        steps += (currX - nextX)
                        currX = nextX
                    }
                }
                U -> {
                    val nextY = currY + path.distance

                    // check to see if we've reached the point or passed it
                    if (currX == point.x && nextY >= point.y) {
                        steps += (point.y - currY)
                        return steps
                    } else {
                        // otherwise continue
                        steps += (nextY - currY)
                        currY = nextY
                    }
                }
                D -> {
                    val nextY = currY - path.distance

                    // check to see if we've reached the point or passed it
                    if (currX == point.x && nextY <= point.y) {
                        steps += (currY - point.y)
                        return steps
                    } else {
                        // otherwise continue
                        steps += (currY - nextY)
                        currY = nextY
                    }
                }
            }
        }

        throw RuntimeException("something went wrong")
    }

    fun getIntersections(): List<Intersection> =
        (0..width).map { x ->
            (0..height).mapNotNull { y ->
                if (panel[x][y].size > 1) {
                    GridCoordinates(x, y) to panel[x][y]
                } else {
                    null
                }
            }
        }.flatten()

    private fun traverse(wireNumber: Int, path: WirePath, start: GridCoordinates): GridCoordinates =
        when (path.direction) {
            R -> traverseRight(wireNumber, start, path.distance)
            L -> traverseLeft(wireNumber, start, path.distance)
            U -> traverseUp(wireNumber, start, path.distance)
            D -> traverseDown(wireNumber, start, path.distance)
        }

    private fun traverseRight(wire: Int, start: GridCoordinates, distance: Int): GridCoordinates {
        val startXCoordinate = start.x + 1
        val endXCoordinate = start.x + distance
        if (endXCoordinate > width) {
            throw RuntimeException("cannot traverse right past the panel edge: start=$start, distance=$distance")
        }

        for (x in startXCoordinate..endXCoordinate) {
            panel[x][start.y] = panel[x][start.y] + wire
        }

        return start.copy(x = endXCoordinate)
    }

    private fun traverseLeft(wire: Int, start: GridCoordinates, distance: Int): GridCoordinates {
        val startXCoordinate = start.x - 1
        val endXCoordinate = start.x - distance
        if (endXCoordinate < 0) {
            throw RuntimeException("cannot traverse left past the panel edge: start=$start, distance=$distance")
        }

        for (x in startXCoordinate downTo endXCoordinate) {
            panel[x][start.y] = panel[x][start.y] + wire
        }

        return start.copy(x = endXCoordinate)
    }

    private fun traverseUp(wire: Int, start: GridCoordinates, distance: Int): GridCoordinates {
        val startYCoordinate = start.y + 1
        val endYCoordinate = start.y + distance
        if (endYCoordinate > height) {
            throw RuntimeException("cannot traverse up past the panel edge: start=$start, distance=$distance")
        }

        for (y in startYCoordinate..endYCoordinate) {
            panel[start.x][y] = panel[start.x][y] + wire
        }

        return start.copy(y = endYCoordinate)
    }

    private fun traverseDown(wire: Int, start: GridCoordinates, distance: Int): GridCoordinates {
        val startYCoordinate = start.y - 1
        val endYCoordinate = start.y - distance
        if (endYCoordinate < 0) {
            throw RuntimeException("cannot traverse down past the panel edge: start=$start, distance=$distance")
        }

        for (y in startYCoordinate downTo endYCoordinate) {
            panel[start.x][y] = panel[start.x][y] + wire
        }

        return start.copy(y = endYCoordinate)
    }

    override fun toString(): String {
        val builder = StringBuilder()
        for (y in height downTo 0) {
            for (x in 0..width) {
                val value = panel[x][y].toString().padEnd(10, ' ')
                builder.append("$value ")
            }
            builder.append("\n")
        }

        return builder.toString()
    }

    fun toXString(): String {
        val builder = StringBuilder()
        for (y in height downTo 0) {
            for (x in 0..width) {
                val value = when {
                    panel[x][y].isEmpty() -> " "
                    panel[x][y].contains(0) -> "O"
                    panel[x][y].size == 1 -> panel[x][y].first().toString()
                    else -> "X"
                }
                builder.append("$value ")
            }
            builder.append("\n")
        }

        return builder.toString()
    }
}

