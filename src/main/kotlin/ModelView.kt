import java.awt.Color
import java.awt.Component
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*
import javax.swing.border.Border
import javax.swing.border.CompoundBorder
import javax.swing.border.EmptyBorder
import javax.swing.plaf.basic.BasicBorders.MarginBorder


//retirei private val model
class ModelView (val model: JSONObject) : JPanel() {

    private val observers: MutableList<ModelViewObserver> = mutableListOf()

    fun addObserver(observer: ModelViewObserver) = observers.add(observer)

    fun removeObserver(observer: ModelViewObserver) = observers.remove(observer)

    fun inicial(){
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        alignmentX = Component.LEFT_ALIGNMENT
        alignmentY = Component.TOP_ALIGNMENT

        /*val scrollPane = JScrollPane(testPanel()).apply {
            horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS
            verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_ALWAYS
        }*/

        model.clazz::class.dataClassFields.forEach {
            if (it.call(model.clazz) is Collection<*>) {
                println(it.call(model.clazz))
                add(testWidgetList(it.name, it.call(model.clazz)))
            } else {
                val testw = testWidget(it.name, "${it.call(model.clazz)}", model, this)
                add(testw)
            }
        }
        addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                observers.forEach {
                    it.click(e)
                }

            }
        })
    }

    init {
        inicial()

        // reacts to changes in the model
        model.addObserver(object : JSONObject.JSONObjectObserver {
            override fun propertyAdded(pair: Pair<String, Any>, index: Int?, p:JPanel, undo: Boolean) {
                var counter = 0
                if (!undo) {
                    val widget = testWidget(pair.first, pair.second, model, p)
                    p.layout = BoxLayout(p, BoxLayout.Y_AXIS)
                    if (p.name != null) {
                        val nom = p.name.split(" ")
                        if(nom.size < 2){
                            val blackline = BorderFactory.createLineBorder(Color.black)
                            widget.border = blackline
                        model.valores.forEach {
                            if (it.first.equals(nom[0])) {
                                counter = (it.second as JSONCollection).collect.size - 1
                            }
                        }}
                        else
                            counter = p.name.split(" ")[1].toInt()
                        widget.name = "${nom[0]} $counter"
                        p.add(widget)
                    } else {
                        p.add(widget, index)

                    }

                    p.revalidate()
                    p.repaint()
                    revalidate()
                    repaint()
                }
            }


            override fun propertyRemoved(pair: Pair<String, Any>) {
                revalidate()
                repaint()
            }
            override fun updateObject(pair: Pair<String, Any>) {

                revalidate()
                repaint()
            }
        })


    }


    interface ModelViewObserver {
        fun click(e: MouseEvent) {
        }

        fun key(e: KeyEvent){}

    }

    fun testWidgetList(key: String, value: Any?): JPanel =
        JPanel().apply {
            layout = BoxLayout(this, BoxLayout.X_AXIS)
            alignmentX = Component.LEFT_ALIGNMENT
            alignmentY = Component.TOP_ALIGNMENT

            add(JLabel(key))
            val p = JPanel()
            name = "1st"

            val pane = JPanel().apply {

                layout = BoxLayout(this, BoxLayout.Y_AXIS)
                alignmentX = Component.LEFT_ALIGNMENT
                alignmentY = Component.TOP_ALIGNMENT
                val blackline = BorderFactory.createLineBorder(Color.black)
                border = blackline
                val margin: Border = EmptyBorder(10, 10, 10, 10)
                setBorder(CompoundBorder(border, margin))
                name = "$key"
                var counter = 0



                for (x in (value as Collection<*>)) {
                    val a = JPanel().apply {
                        layout = BoxLayout(this, BoxLayout.Y_AXIS)
                        val blackline = BorderFactory.createLineBorder(Color.black)
                        border = blackline
                        alignmentX = Component.LEFT_ALIGNMENT
                        alignmentY = Component.TOP_ALIGNMENT

                        for (a in x!!::class.dataClassFields) {
                            println("minho "+ a.name + " com counter "+counter)
                            val new = testWidget(a.name, a.call(x)!!, model,this@apply)
                            new.name = "$key $counter"
                            add(new)

                        }
                        counter++
                    }
                    add(a)
                }
                addMouseListener(object : MouseAdapter() {
                    override fun mouseClicked(e: MouseEvent) {
                        println("tou na nova nova")
                        val nome = e.component.name
                        if (SwingUtilities.isRightMouseButton(e)) {
                            var new_value = value
                        println("value "+value)
                        model.valores.forEach {
                            if(it.first.equals(key))
                                new_value = (it.second as JSONCollection).collect
                        }
                        println("new_value "+ new_value)
                            menu(this@apply, model, Pair(key, new_value!!), nome)
                        }
                    }
                })
            }

            p.add(pane)
            add(p)

            p.addMouseListener(object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent) {
                    println("tou na nova nova")
                    val nome = e.component.name
                    if (SwingUtilities.isRightMouseButton(e)){
                       var new_value = value
                        println("value "+value)
                        model.valores.forEach {
                            if(it.first.equals(key))
                                new_value = (it.second as JSONCollection).collect
                        }
                        println("new_value "+ new_value)

                        menu(this@apply, model, Pair(key,new_value!!), nome)
                    }
                }
            })

        }
}