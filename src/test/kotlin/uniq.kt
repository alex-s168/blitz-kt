import blitz.collections.contents
import blitz.collections.easyMappingSequence
import kotlin.test.Test
import kotlin.test.assertEquals

class GeneratorSequences {
    @Test
    fun uniq() {
        val inp = sequenceOf("AAA", "BBB", "AAA", "AAA", "AAA", "BBB")
        val out = inp.easyMappingSequence { i, s, m ->
            if (s(i-1) == m(i)) null
            else m(i)
        }.filterNotNull()
        assertEquals(out.contents, listOf("AAA", "BBB", "AAA", "BBB").contents)
    }
}