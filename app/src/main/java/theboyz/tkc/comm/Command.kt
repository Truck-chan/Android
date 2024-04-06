package uni.proj.ec

class Command(cmd: String, args: Array<CommandArgument?>) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Command) return false
        return if (cmd != other.cmd) false else arguments.contentEquals(other.arguments)
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
    }

    override fun hashCode(): Int {
        var result = cmd?.hashCode() ?: 0
        result = 31 * result + arguments.contentHashCode()
        return result
    }

    override fun toString(): String {
        return "Command{" +
                "cmd='" + cmd + '\'' +
                ", arguments=" + arguments.contentToString() +
                '}'
    }

    class CommandArgument(val name: String?, val value: Any?) {
        override fun equals(o: Any?): Boolean {
            if (this === o) return true
            if (o !is CommandArgument) return false
            val that = o
            return if (name != that.name) false else value == that.value
        }

        override fun hashCode(): Int {
            var result = name?.hashCode() ?: 0
            result = 31 * result + (value?.hashCode() ?: 0)
            return result
        }

        override fun toString(): String {
            return "CommandArgument{" +
                    "name='" + name + '\'' +
                    ", value=" + value +
                    '}'
        }
    }

    val cmd: String?
    val arguments: Array<CommandArgument?>

    init {
        this.cmd = cmd.trim { it <= ' ' }
        arguments = args
    }

    companion object {
        private var src: String? = null
        private var pos = 0
        private fun eat(target: Char): Boolean {
            if (pos >= src!!.length) return false
            while (src!![pos] == ' ') pos++
            if (src!![pos] == target) {
                pos++
                return true
            }
            return false
        }

        private fun next(target: Char): Boolean {
            if (pos >= src!!.length) return false
            while (src!![pos] == ' ') pos++
            return src!![pos] == target
        }

        private fun readName(): String {
            val n = StringBuilder()
            while (pos < src!!.length && src!![pos] != '{') {
                n.append(src!![pos])
                pos++
            }
            return n.toString().trim { it <= ' ' }
        }

        private fun readArgName(): String? {
            if (next('}')) return null
            val n = StringBuilder()
            while (src!![pos] != '=') {
                n.append(src!![pos])
                pos++
            }
            return n.toString().trim { it <= ' ' }
        }

        private fun readArgValue(): Any {
            val n = StringBuilder()
            var quotation = false
            while (quotation || src!![pos] != ',' && src!![pos] != '}') {
                if (quotation && src!![pos] == '\"') { //end quotation
                    n.append('\"')
                    pos++
                    break
                }
                if (src!![pos] == '\"') {
                    quotation = true
                }
                n.append(src!![pos])
                pos++
            }
            val `val` = n.toString().trim { it <= ' ' }
            return if (`val`.endsWith("\"") && `val`.startsWith("\"")) {
                `val`.substring(1, `val`.length - 2) //remove the quotation marks
            } else if (`val`.contains(".")) {
                `val`.toFloat()
            } else {
                `val`.toInt()
            }
        }

        private val lock = Any()

        /**
         * format: "command_name{parameter1 = value1 , parameter2 = value2 , string = "string value here"}"
         * ex: hello_world{}
         * say_hi{to = "Abdo"}
         * set_value{val = 15 , val2 = 17}
         * ...
         *
         * @param str the input command as a string format
         * @return the command
         */
        @Throws(Exception::class)
        fun fromString(str: String?): Command {
            synchronized(lock) {
                src = str
                pos = 0
                val name = readName()
                val args =
                    ArrayList<CommandArgument>()
                if (eat('{')) { //begin reading the string
                    while (true) {
                        val a_name = readArgName()
                        if (a_name == null) {
                            eat('}')
                            break
                        }
                        if (!eat('=')) {
                            throw Exception("illegal format at: $pos")
                        }
                        val a_val = readArgValue()
                        args.add(CommandArgument(a_name, a_val))
                        if (eat('}')) break
                        eat(',')
                    }
                }
                val arr =
                    arrayOfNulls<CommandArgument>(args.size)
                for (i in arr.indices) arr[i] = args[i]
                return Command(name, arr)
            }
        }

        /**
         * same as fromString
         * but doesn't throw an Exception
         * return null if input is invalid
         */
        fun s_fromString(str: String?): Command? {
            return try {
                fromString(str)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

        /**
         * formats a string and returns the result of converting the formatted string
         * as a command
         * @param f the format string
         * @param args the objects that replaces the format parameters
         *
         * @return command
         */
        @Throws(Exception::class)
        fun format(f: String?, vararg args: Any?): Command {
            return fromString(String.format(f!!, *args))
        }

        /**
         * same as format , but doesn't throw an Exception
         * return null if input is invalid
         */
        fun s_format(f: String?, vararg args: Any?): Command? {
            return try {
                fromString(String.format(f!!, *args))
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}
val String.command : Command
    get (){
        return Command.fromString(this);
    }

