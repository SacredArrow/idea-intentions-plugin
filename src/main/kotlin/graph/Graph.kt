package graph

import IntentionEvent
import graph.Graph.Mappings.indexToPathMapping
import graph.Graph.Mappings.ix
import graph.Graph.Mappings.pathToIndexMapping
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File
import java.util.*
import java.util.Collections.max

class Graph {
    object Mappings { // TODO Make it private later
        var pathToIndexMapping = mutableMapOf<List<String>, Int>() // Path -> Index(not hash)
        var indexToPathMapping = mutableMapOf<Int, Pair<List<String>, Int>>() // Index -> Path with length
        var ix = 1
    }
    val nodes = mutableMapOf<Int, Node>()
    private val visited = mutableMapOf<Int, Boolean>()
    var startNode: Int = 0

    private fun addVertex(start: Int, end: Int, label: String) {
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

    fun build(file: File) {
        file.forEachLine {
            val split = it.split("[label=\"") // TODO Change to regexp
            if (split.size == 2) {
                val label = split[1].dropLast(2)
                val splitNumbers = it.split(" ")
                val start = splitNumbers[0].toInt()
                val end = splitNumbers[2].toInt()
                addVertex(start, end, label)
            }
        }
        for (hash in nodes.keys) {
            visited[hash] = false
        }
    }

    fun build(events: List<IntentionEvent>) {
        for (event in events) {
            addVertex(event.hash_start, event.hash_end, event.name)
        }
        for (hash in nodes.keys) {
            visited[hash] = false
        }
    }

// Here might be a bug with -1 in pathIndex
    fun bfs(loadPathIdsFromFile: Boolean = false) {
        if (loadPathIdsFromFile) {
            // TODO decide about new paths
            indexToPathMapping = Json.decodeFromString(this::class.java.classLoader.getResource("pathIndexToPath.json").readText())
            pathToIndexMapping = mutableMapOf<List<String>, Int>()
            for ((key,value) in indexToPathMapping) {
                pathToIndexMapping[value.first] = key
            }
            ix = max(indexToPathMapping.keys) + 1
        }
        val q = ArrayDeque<Pair<Int, Vertex>>()
        q.add(Pair(startNode, Vertex("", -1, startNode))) // Start node doesn't have parent vertex
        visited[startNode] = true
        while (!q.isEmpty()) {
            val el = q.removeFirst()
            val node = nodes[el.first]!!
            var path = mutableListOf<String>()
            if (el.second.start != -1) {
                if (indexToPathMapping[nodes[el.second.start]!!.pathIndex] != null) { // If there is no such path, we skip this and all subsequent (usually it happens in case of unstoppable recursion)
                    path = indexToPathMapping[nodes[el.second.start]!!.pathIndex]!!.first.toMutableList() // Get path to parent
                } else {
                    continue
                }
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