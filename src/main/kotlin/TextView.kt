
import java.awt.GridLayout
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTextArea

// TODO 1: make this class react to changes in the model
class TextView(private val model: JSONObject) : JTextArea() {
    init {
        tabSize = 2
        text = model.JSONObject_to_String()
        model.addObserver(object: JSONObject.JSONObjectObserver {
            override fun propertyAdded(pair: Pair<String, Any>, index:Int?,p:JPanel, undo: Boolean) {
                text = model.JSONObject_to_String()
            }
            override fun propertyRemoved(pair: Pair<String, Any>) {
                text = model.JSONObject_to_String()
            }
            override fun updateObject(pair: Pair<String, Any>) {
                text = model.JSONObject_to_String()
            }
        })
    }
}