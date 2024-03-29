package blitz.collections

class Tree<T> {
    var root: Node<T>? = null

    operator fun plusAssign(value: T) {
        val node = Node<T>()
        node.value = value
        root?.children?.add(node)
    }

    fun traverse(traverser: TreeTraverser<T>) =
        root?.traverse(traverser)

    fun traverse(process: (Node<T>) -> Boolean) =
        root?.traverse(process)

    fun updateParents() {
        traverse { node ->
            node.children.forEach { it.parent = node }
            true
        }
    }

    override fun toString(): String {
        return root.toString()
    }
}

class Node<T>(
    var value: T? = null,
    var children: MutableList<Node<T>> = mutableListOf()
) {
    var parent: Node<T>? = null

    fun traverse(traverser: TreeTraverser<T>) =
        traverser.traverse(this)

    fun traverse(process: (Node<T>) -> Boolean) {
        val trav = object : TreeTraverser<T> {
            override fun process(node: Node<T>): Boolean {
                return process(node)
            }
        }
        traverse(trav)
    }

    operator fun plusAssign(value: T) {
        val node = Node<T>()
        node.value = value
        children.add(node)
    }

    fun toLines(): List<Pair<Int, String>> {
        val lines = mutableListOf<Pair<Int, String>>()
        lines += 0 to value.toString()
        children.forEach { child ->
            child.toLines().forEach { (indent, line) ->
                lines += indent + 1 to line
            }
        }
        return lines
    }

    override fun toString(): String {
        val lines = toLines()
        val sb = StringBuilder()
        lines.forEach { (indent, line) ->
            sb.append(" ".repeat(indent * 2))
            sb.append(line)
            sb.append("\n")
        }
        return sb.toString()
    }
}

interface TreeTraverser<T> {
    fun process(node: Node<T>): Boolean /* true if continue, false if stop */

    fun traverse(tree: Node<T>) {
        if (!process(tree)) return
        tree.children.forEach { traverse(it) }
    }
}