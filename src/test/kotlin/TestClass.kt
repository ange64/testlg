import org.junit.Test





class ListNode(var `val`: Int) {
    var next: ListNode? = null

    constructor(vararg intArray: Int) : this(intArray[0]) {
        var pointer = this
        for (i in 1..intArray.lastIndex) {
            pointer.next = ListNode(intArray[i])
            pointer = pointer.next!!
        }
    }


    override fun toString(): String {
        val builder = StringBuilder("[")
        var pointer: ListNode? = this
        while (pointer != null) {
            builder.append(pointer.`val`)
            builder.append(',')
            pointer = pointer.next
        }
        return builder.deleteCharAt(builder.lastIndex).append(']').toString()
    }
}

