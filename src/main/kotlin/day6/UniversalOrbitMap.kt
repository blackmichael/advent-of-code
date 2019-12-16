package main.day6

import java.io.File

/**
--- Day 6: Universal Orbit Map ---

You've landed at the Universal Orbit Map facility on Mercury. Because navigation in space often involves transferring between orbits, the orbit maps here are useful for finding efficient routes between, for example, you and Santa. You download a map of the local orbits (your puzzle input).

Except for the universal Center of Mass (COM), every object in space is in orbit around exactly one other object. An orbit looks roughly like this:

\
\
|
|
AAA--> o            o <--BBB
|
|
/
/

In this diagram, the object BBB is in orbit around AAA. The path that BBB takes around AAA (drawn with lines) is only partly shown. In the map data, this orbital relationship is written AAA)BBB, which means "BBB is in orbit around AAA".

Before you use your map data to plot a course, you need to make sure it wasn't corrupted during the download. To verify maps, the Universal Orbit Map facility uses orbit count checksums - the total number of direct orbits (like the one shown above) and indirect orbits.

Whenever A orbits B and B orbits C, then A indirectly orbits C. This chain can be any number of objects long: if A orbits B, B orbits C, and C orbits D, then A indirectly orbits D.

For example, suppose you have the following map:

COM)B
B)C
C)D
D)E
E)F
B)G
G)H
D)I
E)J
J)K
K)L

Visually, the above map of orbits looks like this:

G - H       J - K - L
/           /
COM - B - C - D - E - F
\
I

In this visual representation, when two objects are connected by a line, the one on the right directly orbits the one on the left.

Here, we can count the total number of orbits as follows:

D directly orbits C and indirectly orbits B and COM, a total of 3 orbits.
L directly orbits K and indirectly orbits J, E, D, C, B, and COM, a total of 7 orbits.
COM orbits nothing.

The total number of direct and indirect orbits in this example is 42.

What is the total number of direct and indirect orbits in your map data?

--- Part Two ---

Now, you just need to figure out how many orbital transfers you (YOU) need to take to get to Santa (SAN).

You start at the object YOU are orbiting; your destination is the object SAN is orbiting. An orbital transfer lets you move from any object to an object orbiting or orbited by that object.

For example, suppose you have the following map:

COM)B
B)C
C)D
D)E
E)F
B)G
G)H
D)I
E)J
J)K
K)L
K)YOU
I)SAN

Visually, the above map of orbits looks like this:

YOU
/
G - H       J - K - L
/           /
COM - B - C - D - E - F
\
I - SAN

In this example, YOU are in orbit around K, and SAN is in orbit around I. To move from K to I, a minimum of 4 orbital transfers are required:

K to J
J to E
E to D
D to I

Afterward, the map of orbits looks like this:

G - H       J - K - L
/           /
COM - B - C - D - E - F
\
I - SAN
\
YOU

What is the minimum number of orbital transfers required to move from the object YOU are orbiting to the object SAN is orbiting? (Between the objects they are orbiting - not between YOU and SAN.)

 */
object UniversalOrbitMap {
    fun readFileInput(file: File) = file.readLines()
        .map {
            it.split(")").let {
                it[0] to it[1]
            }
        }

    fun part1(orbits: List<Pair<String, String>>): Int {
        val graph = Graph()
        orbits.forEach { (orbitee, orbiter) ->
            graph.addNode(orbitee, orbiter)
        }
        return graph.size
    }

    fun part2(orbits: List<Pair<String, String>>): Int {
        val graph = Graph()
        orbits.forEach { (orbitee, orbiter) ->
            graph.addNode(orbitee, orbiter)
        }
        return graph.getDistanceBetweenNodes("YOU", "SAN")
    }
}

class Graph {

    private val roots = Node(
        name = "_roots",
        distanceFromRoot = -1
    )

    private data class Node(
        val name: String,
        val distanceFromRoot: Int,
        val children: MutableList<Node> = mutableListOf()
    ) {
        fun copyWithNewDistances(baseDistance: Int): Node =
            copy(
                distanceFromRoot = baseDistance + 1,
                children = children.map { it.copyWithNewDistances(baseDistance + 1) }
                    .toMutableList()
            )

        override fun toString(): String {
            val sb = StringBuilder()
            sb.append("$name\n")
            children.forEach {
                (0..distanceFromRoot).forEach {
                    sb.append("\t")
                }
                sb.append("|-- $it")
            }
            return sb.toString()
        }
    }

    val size: Int
        get() = if (roots.children.size == 1) {
            getSize(roots.children.first())
        } else {
            throw RuntimeException("invalid amount of roots: ${roots.children.map { it.name }}")
        }

    fun getDistanceBetweenNodes(nodeName1: String, nodeName2: String): Int {
        val pathToNode1 = getPathToNode(nodeName1, roots)
        val pathToNode2 = getPathToNode(nodeName2, roots)

        val commonNode = pathToNode1.intersect(pathToNode2).lastOrNull() ?: return -1
        val pathBetweenNodes =
            pathToNode1.subList(pathToNode1.indexOf(commonNode) + 1, pathToNode1.size - 1) +
                commonNode +
                pathToNode2.subList(pathToNode2.indexOf(commonNode) + 1, pathToNode2.size - 1)

        return pathBetweenNodes.size - 1
    }

    fun addNode(parentName: String, childName: String) {
        // find the parent node or create a the parent as a new root
        val parentNode = findNode(parentName, roots) ?: Node(
            name = parentName,
            distanceFromRoot = 0
        ).also { roots.children.add(it) }

        val rootChildNode = roots.children.find { it.name == childName }
        val childNode = if (rootChildNode != null) {
            roots.children.remove(rootChildNode)
            rootChildNode.copyWithNewDistances(parentNode.distanceFromRoot)
        } else {
            Node(
                name = childName,
                distanceFromRoot = parentNode.distanceFromRoot + 1
            )
        }

        parentNode.children.add(childNode)
    }

    private fun getPathToNode(nodeName: String, originNode: Node): List<Node> {
        if (originNode.name == nodeName) {
            return listOf(originNode)
        } else if (originNode.children.size == 0) {
            return listOf()
        }

        val path = originNode.children
            .map { getPathToNode(nodeName, it) }
            .flatten()

        return if (path.isEmpty()) {
            emptyList()
        } else {
            listOf(originNode) + path
        }
    }

    private fun getSize(node: Node): Int =
        node.distanceFromRoot + node.children.sumBy { getSize(it) }

    private fun findNode(name: String, currNode: Node?): Node? {
        if (currNode == null) {
            return null
        } else if (currNode.name == name) {
            return currNode
        }

        return currNode.children
            .mapNotNull { findNode(name, it) }
            .firstOrNull()
    }

    override fun toString(): String {
        return roots.toString()
    }
}