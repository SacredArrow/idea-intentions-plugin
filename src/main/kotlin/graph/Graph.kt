package graph

import com.intellij.util.containers.toArray
import graph.Graph.Mappings.indexToPathMapping
import graph.Graph.Mappings.ix
import graph.Graph.Mappings.pathToIndexMapping
import java.io.File
import java.util.*

class Graph {
    object Mappings { // TODO Make it private later
        val pathToIndexMapping = mutableMapOf<List<String>, Int>() // Path -> Index(not hash)
        val indexToPathMapping = mutableMapOf<Int, Pair<List<String>, Int>>() // Index -> Path with length
        var ix = 1
    }
    val nodes = mutableMapOf<Int, Node>()
    private val visited = mutableMapOf<Int, Boolean>()
    var startNode: Int = 0

    fun build(file: File) {
        file.forEachLine {
            val split = it.split("[label=\"") // TODO Change to regexp
            if (split.size == 2) {
                val label = split[1].dropLast(2)
                val splitNumbers = it.split(" ")
                val start = splitNumbers[0].toInt()
                val end = splitNumbers[2].toInt()
                if (startNode == 0) { // TODO change to something better
                    startNode = start
                }
                if (start !in nodes.keys) {
                    nodes[start] = Node(start)
                }
                if (end !in nodes.keys) {
                    nodes[end] = Node(end)
                }
                nodes[start]!!.addVertex(label, end)
            }
        }
        for (hash in nodes.keys) {
            visited[hash] = false
        }
    }

    fun bfs() {
        val q = ArrayDeque<Pair<Int, Vertex>>()
        q.add(Pair(startNode, Vertex("", -1, startNode))) // Start node doesn't have parent vertex
        visited[startNode] = true
        while (!q.isEmpty()) {
            val el = q.removeFirst()
            val node = nodes[el.first]!!
            var path = mutableListOf<String>()
            if (el.second.start != -1) {
                path = indexToPathMapping[nodes[el.second.start]!!.pathIndex]!!.first.toMutableList() // Get path to parent
            }
            path.add(el.second.label) // ...and add last vertex
            if (path !in pathToIndexMapping.keys) {
                node.pathIndex = ix
                pathToIndexMapping[path] = ix
                indexToPathMapping[ix] = Pair(path, path.size)
                ix++
            } else {
                node.pathIndex = pathToIndexMapping[path]!!
            }
            for (vertex in node.verticesOut) {
                if (visited[vertex.end] == false) {
                    q.add(Pair(vertex.end, vertex))
                    visited[vertex.end] = true
                }
            }
        }
//        for ((_,value) in nodes) {
//            println(indexToPathMapping[value.pathIndex])
//        }
    }
}