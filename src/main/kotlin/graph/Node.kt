package graph

data class Vertex(val label: String, val start: Int, val end: Int)
class Node(val hash: Int) {
    val verticesOut = mutableListOf<Vertex>()
    var pathIndex = -1

    fun addVertex(label: String, end: Int) {
        verticesOut.add(Vertex(label, hash, end))
    }
}