import java.awt.Component
import java.awt.Dimension
import java.awt.GridLayout
import java.awt.event.FocusAdapter
import java.awt.event.FocusEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*
import java.awt.event.*

/**
 * Controller: wires model and view
 */


fun main() {
    val Aluno1 = Aluno(101101, "Dave Farley", true)
    val obj1 = JSONObject(Aluno1)
    val Aluno2 = Aluno(101102, "Martin Fowler", true)
    val Aluno3 = Aluno(26503, "André Santos", false)
    val inscritos = listOf(Aluno1, Aluno2, Aluno3)
    val unidade_curricular = UC("PA",6.0,null,inscritos)
    val obj4 = JSONObject(unidade_curricular)
    val model = obj4


    val frame = JFrame("Josue - JSON Object Editor").apply {
        defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        layout = GridLayout(0, 2)
        size = Dimension(600, 600)
        val pair = Pair("","")
        panel {
            val left = this.panel
            left.layout = GridLayout()
            (+ModelView(model)).addObserver(object : ModelView.ModelViewObserver {
                override fun click(e: MouseEvent) {
                    println("no model em cima e ${e.component}")
                    val nome = e.component.parent.name
                    if (SwingUtilities.isRightMouseButton(e)) {
                        menu(panel, model, pair, nome)
                        println("no model")
                    }

                }
            })


            add(left)
        }
        panel {
            val right = this.panel
            right.layout = GridLayout()
            val srcArea = +TextView(model)
            right.add(srcArea)
            add(right)
        }


    }
    frame.isVisible = true
}

val undoStack = mutableListOf<Command>()

fun menu(p: JPanel
         , model: JSONObject, pair: Pair<String, Any>, nome: String?
)
{
    println("tou no menu")
    // menu
    val menu = JPopupMenu("Message")
    val add = JButton("add")
    add.addActionListener {
        val text = JOptionPane.showInputDialog("Propriedade")
        val command = AddCommand(model, Pair(text, ""), nome,p)
        undoStack.add(command)
        command.run()
        menu.isVisible = false
    }
    val del = JButton("delete")
    del.addActionListener {
        val command = DeleteCommand(model, pair, nome,p)
        undoStack.add(command)
        command.run()
        menu.isVisible = false
        p.revalidate()
        p.repaint()
    }

    val undo = JButton("undo")
    undo.addActionListener {
        if(undoStack.isNotEmpty()){
            val last = undoStack.removeLast()
            last.undo()
        }
    }

    menu.add(add);
    menu.add(del);
    menu.add(undo)
    menu.show(p, 100, 100);

    println("pair")
}



fun testWidget(key: String, value: Any, model: JSONObject, p: JPanel): JPanel =
    JPanel().apply {
        layout = BoxLayout(this, BoxLayout.X_AXIS)
        alignmentX = Component.LEFT_ALIGNMENT
        alignmentY = Component.TOP_ALIGNMENT

        val k = JLabel(key)
        add(k)
        println("this one " + value)
        var new_value: Any = true

        when (value) {
            is String, is Int -> {
                if (value.equals("null")) {
                    val v = JLabel("      N/A")

                    v.addMouseListener(object : MouseAdapter() {
                        override fun mouseClicked(e: MouseEvent) {
                            v.isVisible = false
                            val new_v = JTextField()
                            new_v.addKeyListener(object :KeyAdapter() {
                                override fun keyPressed (e: KeyEvent) {
                                    val k = e.getKeyCode()
                                    if (k == KeyEvent.VK_ENTER) {
                                        println("was enter dentro do null")
                                        val check = JCheckBox()
                                        println("$v.text")
                                        if(new_value != v.text) {
                                            val nome = e.component.parent.name
                                            val com = UpdateCommand(model, Pair(key, new_v.text),nome,  new_v, check)
                                            undoStack.add(com)
                                            com.run()

                                            if(new_v.text.equals("true")|| new_v.text.equals("false")){
                                                new_v.isVisible = false

                                                check.isSelected = new_v.text.toBoolean()
                                                check.addMouseListener(object : MouseAdapter() {
                                                    override fun mouseClicked(e: MouseEvent) {
                                                        val nome = e.component.parent.name
                                                        val com = UpdateCommand(model, Pair(key, check.isSelected),nome,new_v,check)
                                                        undoStack.add(com)
                                                        com.run()

                                                    }
                                                })
                                                add(check)
                                            }
                                            println("added")
                                        }

                                    }


                                }})
                            this@apply.add(new_v)
                        }
                    })
                    add(v)

                } else {
                    val v = JTextField(value.toString())
                    println("dentro do jtextfield")
                    new_value = v.text

                    v.addKeyListener(object :KeyAdapter() {
                        override fun keyPressed (e: KeyEvent) {
                            val k = e.getKeyCode()
                            if (k == KeyEvent.VK_ENTER) {
                                println("was enter")
                                val check = JCheckBox()
                                if(new_value != v.text) {
                                    val nome = e.component.parent.name
                                    println("felix $nome")
                                    val com = UpdateCommand(model, Pair(key, v.text),nome, v, check)
                                    undoStack.add(com)
                                    com.run()
                                    if(v.text.equals("true")|| v.text.equals("false")){
                                        v.isVisible = false
                                        check.isSelected = v.text.toBoolean()
                                        check.addMouseListener(object : MouseAdapter() {
                                            override fun mouseClicked(e: MouseEvent) {
                                                val nome = e.component.parent.name
                                                val com = UpdateCommand(model,Pair(key, check.isSelected),nome, v, check)
                                                undoStack.add(com)
                                                com.run()
                                            }
                                        })
                                        add(check)
                                    }
                                    println("valores aqui "+ model.valores.toString())
                                    println(model.JSONObject_to_String())
                                    println("added")
                                }

                            }

                        }})
                    add(v)
                }
            }
            is Boolean -> {
                val v = JCheckBox()
                v.isSelected = value
                v.addFocusListener(object : FocusAdapter() {
                    override fun focusLost(e: FocusEvent) {
                        new_value = v.isSelected
                    }
                })
                v.addMouseListener(object : MouseAdapter() {
                    override fun mouseClicked(e: MouseEvent) {
                        val nome = e.component.parent.name
                        val com = UpdateCommand(model, Pair(key, v.isSelected),nome, null, v)
                        undoStack.add(com)
                        com.run()
                    }
                })
                add(v)
            }
        }

        this.components.forEach{
            it.addMouseListener(object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent) {
                    println("tou na nova")
                    val nome = e.component.parent.name
                    println("huynjin "+nome)
                    if (SwingUtilities.isRightMouseButton(e)){
                        var new_value: Any? = value
                        println("value "+value)
                        model.valores.forEach {
                            if(it.first.equals(key))
                                new_value = transformValue(it.second)
                        }
                        println("new_value "+ new_value)
                        menu(this@apply, model, Pair(key,value), nome)}
                }
            })
        }

    }


interface Command{
    fun run()
    fun undo()
}


fun getValue(model: JSONObject, key:String, nome: String?): Any?{
    var value :Any? = ""
    println("key $key")

    model.valores.forEach {
        println(it.first)
        println(it.second)
        if(nome == null){
            if(it.first.equals(key)) {
                value = it.second
                println("bitch")
            }}
        else{
            var counter = 0
            if(it.first.equals(nome.split(" ")[0])) {
                (it.second as JSONCollection).collect.forEach {
                    if(counter == nome.split(" ")[1].toInt()){
                        for(a in it!!::class.dataClassFields){
                            if(a.name == key)
                                value = a.call(it)
                        }}
                    counter++

                }
                println("bitch")
            }
        }
    }

    println("here nitch $value")
    return value
}

fun transformValue(value: Any?): Any?{
    var v: Any? = null
    println("antes this is value: $value")
    println("antes this is v: $v")
    var aux : MutableList<Any?> = mutableListOf()
    when(value){
        null -> {
            v = ""
            println("tou dentro do null?")
        }
        is JSONInt -> v = (value).number.toString()
        is JSONBoolean -> v = (value).v.toString()
        is JSONString -> v = (value).nome

        is Int -> v = JSONInt(value)
        is Boolean -> v = JSONBoolean(value)
        is String -> {
            aux.add(value.toIntOrNull())
            aux.add(value.toDoubleOrNull())
            aux.add(value.toBooleanStrictOrNull())
            aux.forEach {
                if(it != null)
                    v = transformValue(it)
            }
            if(v == null)
                v = JSONString(value)
        }

    }
    println("depois this is v: $v")
    return v
}


class AddCommand(val model: JSONObject, val pair:Pair<String, Any>, val nome:String?, val p: JPanel): Command{
    var nome_anterior = ""
    var pair_anterior = ""
    var index_anterior = 0

    //falta para listas como tudo tho

    override fun run(){
        //val value = getValue(model, pair.first, nome)
        //val transf_value = transformValue(value)
        //index_anterior = model.valores.indexOf(Pair(pair.first, value))
        var ind : Int? = null
        if(nome != null){
            println("nome $nome")
            nome_anterior=nome
            if(nome.split(" ").size == 1){
                model.valores.forEach {
                    if(it.first == nome){
                        ind = (it.second as JSONCollection).collect.size
                        nome_anterior = "$nome $ind"
                    }
                }
            }
            if(nome.split(" ").size == 2){
                ind = nome.split(" ")[1].toInt()
            }
        }
        model.add(pair, nome, ind,p)
    }

    override fun undo() {
        val sizing = p.components.size
        var counter = 0
        p.components.forEach {
            if (counter >= sizing - 1 ) {
                p.remove(it)
            }
            counter += 1
        }
        print(pair)
        print(nome)
        println("dream "+p.name)

        model.remove(pair, nome_anterior)
    }
}

class UpdateCommand(val model: JSONObject, val pair:Pair<String, Any>, val nome:String?, val text: JTextField?, val check: JCheckBox?): Command{
    var nome_anterior: String? = null
    var pair_anterior = Pair<String, Any>("", "")
    var checkbox_anterior = false
    var list_removed : Any = ""

    override fun run(){
        var value = getValue(model, pair.first, nome)
        value = transformValue(value)
        println("value $value")
        pair_anterior = Pair(pair.first, value!!)
        nome_anterior = nome
        println("no run() nome_anterior $nome_anterior e pair_anterior $pair_anterior")
        if(pair_anterior.second.toString().equals("true") || pair_anterior.second.toString().equals("false")) {
            checkbox_anterior = true
        }
        model.update(pair, nome)
    }

    override fun undo(){
        println("\n nome_anterior $nome_anterior e pair_anterior $pair_anterior\n")
        var v: Any? = pair_anterior.second
        if(nome != null){
            v = transformValue(v)
        }
        println("here $v")
        if(checkbox_anterior){
            if(check != null) {
                check.isSelected = v.toString().toBoolean()
            }
        }
        else{
            if(text != null) {
                println("estou  here")
                check!!.isVisible = false
                text.isVisible = true
                text.text = v.toString()
                println("text.text ${text.text}")
            }
            else
                if(check != null)
                    check.isSelected = v.toString().toBoolean()
        }

        model.update(Pair(pair_anterior.first, v!!),nome_anterior)
    }
}

class DeleteCommand(val model: JSONObject, val pair:Pair<String, Any>, val nome:String?, val p :JPanel): Command{
    var index_anterior = 0
    var obj_anterior : Any = ""
    var savedComponents = JPanel()
    var panel_anterior = JPanel()
    var value : Any? = ""

    override fun run() {
        if (pair.first == nome) {
            obj_anterior = pair.second
            panel_anterior = p
            p.components.forEach {
                savedComponents.add(it)
                p.remove(it)
            }
            savedComponents.layout = BoxLayout(savedComponents, BoxLayout.Y_AXIS)
            println("skz")
            model.remove(pair, nome)
        } else {
            value = getValue(model, pair.first, nome)
            println("\n nome $nome \n")
            for (i in model.valores) {
                if (i.second is JSONCollection) {
                    (i.second as JSONCollection).collect.forEach {
                        if (it.toString().contains(pair.first) and it.toString().contains(pair.second.toString())) {
                            obj_anterior = it!!
                            print(obj_anterior)
                        }
                    }
                }
            }
            print(Pair(pair.first, value))
            if(nome == null || nome.split(" ").size <2)
                index_anterior = model.valores.indexOf(Pair(pair.first, value))
            else
                index_anterior = nome.split(" ")[1].toInt()
            model.remove(Pair(pair.first, value!!), nome)

            if (nome != null) {
                if (obj_anterior::class.isData and (obj_anterior::class.simpleName != "Pair")) {
                    savedComponents = p.parent as JPanel
                    if (obj_anterior is JSONCollection) {
                        var sizing = 0
                        var parent = p.parent
                        var child = p
                        var string = false
                        if ((obj_anterior as JSONCollection).collect.size > 1) {
                            while (sizing < (obj_anterior as JSONCollection).collect.size - 1) {
                                parent = parent.parent
                                child = child.parent as JPanel
                                if(((obj_anterior as JSONCollection).collect as MutableList<*>)[sizing] == pair){
                                    string = true
                                    break
                                }
                                sizing += 1
                            }
                            if (!string) {
                                parent.remove(child)
                                panel_anterior = parent as JPanel
                                savedComponents = child
                            }else{
                                p.components.forEach {
                                    p.remove(it)
                                }
                            }
                        }else{
                            p.components.forEach {
                                p.remove(it)
                            }
                        }
                    }else{
                        panel_anterior = p.parent.parent as JPanel
                        p.parent.parent.remove(p.parent)
                    }
                }else {
                    p.components.forEach {
                        p.remove(it)
                    }
                }
            } else {
                p.components.forEach {
                    p.remove(it)
                }
            }
        }
    }

    override fun undo(){
        if (nome == null) {
            value = transformValue(value)
            println("index_anterior ${index_anterior}")
            model.add(Pair(pair.first, value!!), nome, index_anterior,p)
        }else{
            println("index_anterior qnd nome n null ${index_anterior}")

            if(nome.split(" ").size == 2){
                (panel_anterior).add(savedComponents,nome.split(" ")[1].toInt())
            }

            (panel_anterior).add(savedComponents,index_anterior)
            panel_anterior.layout = BoxLayout(panel_anterior, BoxLayout.Y_AXIS)
            model.add(pair, nome, index_anterior,p, true, obj_anterior)
        }
    }
}