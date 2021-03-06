import org.python.util.PythonInterpreter
import java.util.function.Predicate

fun main() {
    val pythonInterpreter = PythonInterpreter()
    pythonInterpreter.use { py ->
        py.setOut(System.out)

        val code = py.compile("""
            def doSth(ic):
                print(ic)
                True
            
            #s = S(Predicate(lambda a: doSth(a)))
            
            #s.fn.invoke('Hi')
        """.trimIndent())

        py.set("S", S::class.java)
        py.set("Predicate", p.Predicate::class.java)

        py.exec(code)
        println(py.get("doSth")::class.java)
    }
}

fun PythonInterpreter.print(name: String) {
    val value = get(name)
    println("${value::class.simpleName} $name = $value")
}

class S(val fn: Predicate<String>)

fun echo(it: String) = println(it)

data class IC(val i: Int)

fun interface Tick {
    fun tick()
}