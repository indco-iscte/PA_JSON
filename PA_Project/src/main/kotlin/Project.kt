import java.sql.Date
import java.util.*
import java.util.stream.IntStream.range
import javax.xml.crypto.Data
import kotlin.collections.ArrayList
import kotlin.reflect.*
import kotlin.reflect.full.*
import javax.swing.JPanel


abstract class JSONElement {
    abstract fun accept(visitor: Visitor)

}

data class JSONString (
    val nome: String
): JSONElement() {
    override fun accept(visitor: Visitor) {
        visitor.visit(this)
    }
    fun JSONString_to_String(): String{
        return '"' + this.nome + '"' + ",\n"
    }
}


data class JSONBoolean (
    val v: Boolean
): JSONElement() {
    override fun accept(visitor: Visitor) {
        visitor.visit(this)
    }
    fun JSONBoolean_to_String(): String{
        return  this.v.toString()  + "\n"
    }
}


data class JSONInt (
    val number: Int
): JSONElement() {
    override fun accept(visitor: Visitor) {
        visitor.visit(this)
    }
    fun JSONInt_to_String(): String{
        return this.number.toString() + ",\n"
    }
}


val <T : Any> KClass<T>.enumConstants: List<T> get() {
    require(isEnum) { "instance must be enum" }
    return java.enumConstants.toList()
}
val KClassifier?.isEnum: Boolean
    get() = this is KClass<*> && this.isSubclassOf(Enum::class)


data class JSONArray (
    val ar: List<*>
): JSONElement() {
    override fun accept(visitor: Visitor) {
        visitor.visit(this)
    }
    fun JSONArray_to_String(): String{
        var string = "["
        var counter = 1
        this.ar.forEach{
            if (counter == this.ar.size){
                if(it is String)
                    string +=  "\"$it\"] \n"
                else
                    string += "$it] \n"
            } else {
                if(it is String)
                    string += "\"$it\","
                else
                    string += "$it,"
                counter += 1
            }
        }
        return string
    }
}

data class JSONClass (
    val clazz: Any,
): JSONElement() {
    override fun accept(visitor: Visitor) {
        visitor.visit(this)
    }
    init {
        this.clazz::class.dataClassFields.forEach {
            if (!it.hasAnnotation<JSON_Excluir>()) {
                if (!it.hasAnnotation<ForceJSONString>()) {
                    when (it.returnType.classifier) {
                        Int::class -> JSONInt(it.call(clazz).toString().toInt())
                        String::class -> JSONString(it.call(clazz).toString())
                        Boolean::class -> JSONBoolean(it.call(clazz).toString().toBoolean())
                        ArrayList::class -> JSONArray(it.call(clazz) as ArrayList<*>)
                        List::class -> JSONCollection(it.call(clazz) as List<*>)
                        Class::class -> JSONClass(it.call(clazz) as Any)
                    }
                } else {
                    JSONString(it.call(clazz).toString())
                }
            }
        }
    }

    fun JSONClass_to_String(bool: Boolean = false, inlist: Boolean = false): String {
        var string = ""
        if (!inlist) {
            string = "{ \n"
        }
        var pass = true
        this.clazz::class.dataClassFields.forEach {
            when (it.returnType.classifier) {
                Int::class -> string += '"' + it.name + '"' + ": " + JSONInt(it.call(clazz).toString().toInt()).JSONInt_to_String()
                String::class -> string += '"' + it.name + '"' + ": " + JSONString(it.call(clazz).toString()).JSONString_to_String()
                Boolean::class -> string += '"' + it.name + '"' + ": " + JSONBoolean(it.call(clazz).toString().toBoolean()).JSONBoolean_to_String()
                ArrayList::class -> string += '"' + it.name + '"' + ": " + JSONArray(it.call(clazz) as ArrayList<*>).JSONArray_to_String()
                Collection::class -> {
                    string += (JSONCollection(it.call(clazz) as Collection<*>).JSONCollection_to_String(true)).replace("\n","").replace(",}","").replace(" {","").replace(",",",\n") + "\n"
                }
                Class::class -> string += '"' + it.name + '"' + ": " + JSONClass(it.call(clazz) as Any).JSONClass_to_String()
                else -> {
                    if(it.toString().contains("Pair")){
                        if(it.name != "second") {
                            string += '"' + it.call(clazz).toString() + '"' + ": "}
                        if(it.name != "first") {
                            string += JSONString(it.call(clazz).toString()).JSONString_to_String()
                        }
                    }
                    else{
                        // come back to this dps de apagar os outros elems
                        string += it.call(clazz).toString()
                    }

                }
            }
        }
        string += if (bool){
            "}, \n"
        } else {
            "} \n"
        }

        return string
    }
}


data class JSONCollection (
    val collect: Collection<*>,
): JSONElement() {

    override fun accept(visitor: Visitor) {
        visitor.visit(this)
    }

    init{

    }

    fun JSONCollection_to_String(inside_list: Boolean = false): String{
        if (!inside_list) {
            var string = "[ \n"
            var counter = 0
            this.collect.forEach {
                if (counter == this.collect.size - 1) {
                    string += "${JSONClass(it!!).JSONClass_to_String()}]\n"
                } else {
                    string += JSONClass(it!!).JSONClass_to_String(true)
                    counter += 1
                }
            }
            return string
        } else {
            var string = ""
            var counter = 0
            this.collect.forEach {
                if (counter == this.collect.size - 1) {
                    string += "${JSONClass(it!!).JSONClass_to_String(false,true)}\n"
                } else {
                    string += JSONClass(it!!).JSONClass_to_String(true,true) + "\n"
                    counter += 1
                }
            }
            return string
        }
    }
}


class JSONObject(val clazz: Any){ //varargs
    var valores : MutableList<Pair<String, JSONElement?>> = mutableListOf()

    interface JSONObjectObserver {
        fun propertyAdded(pair: Pair<String, Any>, index: Int?, p:JPanel, undo: Boolean = false) {
        }
        fun propertyRemoved(pair: Pair<String, Any>) {
        }
        fun updateObject(pair: Pair<String, Any>) { }
    }

    private val observers: MutableList<JSONObjectObserver> = mutableListOf()

    fun addObserver(observer: JSONObjectObserver) = observers.add(observer)

    fun removeObserver(observer: JSONObjectObserver) = observers.remove(observer)

    fun add(pair: Pair<String, Any>, nome: String?, index:Int?, p: JPanel, undo: Boolean = false, obj: Any? = null) {
        if(nome != null) {
            var lista: Any = ""
            if (index == null) {
                for (i in valores) {
                    if (i.first == nome.split(" ")[0]) {
                        lista = (i.second as JSONCollection).collect
                    }
                }

                updateValoresList(pair, (lista as Collection<*>).size, nome, undo, obj)
            }else{

                updateValoresList(pair, index, nome, undo, obj)
            }
        }else{
            if(index == null){
                updateValores(pair,valores.size)
            }
            else
                updateValores(pair,index)
        }
        //updateJSONObject(pair, index, propriedade, null)
        // fires event to registered observers
        observers.forEach {
            it.propertyAdded(pair, index,p, undo)
        }
    }

    fun update(pair: Pair<String, Any>, nome: String?){
        updateJSONObject(pair, nome)
        observers.forEach {
            it.updateObject(pair)
        }
    }

    fun remove(pair: Pair<String, Any>, nome:String?) {
        var ind = 0
        var lista = false
        if(nome == null || nome.split(" ").size <2){
            valores.forEach{
                if(it.first.equals(pair.first) ){
                    var holds : Any = ""
                    when(it.second) {
                        is JSONInt -> holds = (it.second as JSONInt).number.toString()
                        is JSONBoolean -> holds = (it.second as JSONBoolean).v.toString()
                        is JSONString -> holds = (it.second as JSONString).nome
                        is JSONCollection -> {
                            holds = (it.second as JSONCollection).collect.toString()
                            lista = true
                        }
                    }
                    if(!lista){
                    if(pair.second.equals(it.second)){
                        ind = valores.indexOf(Pair(it.first,it.second))
                    }}

                    else
                        if(pair.second.toString().equals(holds.toString())){
                            ind = valores.indexOf(Pair(it.first,it.second))
                        }
                }
            }
            if(!lista)
                valores.remove(valores.get(ind))
            else
                valores[ind] = Pair(pair.first, JSONCollection(mutableListOf<Any?>()))


        }

        else{
            var counter = 0
            valores.forEach {
                if (nome.toString().startsWith(it.first)) {
                    val lista = mutableListOf<Any?>()
                    if(nome.split(" ").size == 2){

                    ((valores[counter].second as JSONCollection).collect as MutableList<*>).forEach {
                        var split = it.toString().split("(", ", ", ")")
                        val juntos = "${pair.first}=${pair.second}"
                        var dentro = 0
                        var b_dentro = false
                        if (it !is JSONCollection){

                            split.forEach {
                                if(it.equals(pair.first) or it.equals(pair.second)){
                                    dentro ++
                                }
                                if(it.equals(juntos)) {
                                    b_dentro = true
                                }
                            }
                            if (dentro != split.size && !b_dentro) {
                                lista.add(it)
                                println("added")
                            }

                        }else {
                            var aux = mutableListOf<Any?>()
                            it.collect.forEach {
                                dentro = 0
                                b_dentro = false
                                split = it.toString().split("(", ", ", ")")
                                split.forEach {

                                    if(it.equals(pair.first) or it.equals(pair.second)){
                                        dentro ++
                                    }
                                    if(it.equals(juntos)) {
                                        b_dentro = true
                                    }
                                }
                                if (dentro != split.size && !b_dentro) {
                                    aux.add(it)
                                    println("added")
                                }
                            }
                            if(aux.size > 0)
                                lista.add(JSONCollection(aux))
                        }
                    }}
                    valores[counter] = Pair(valores[counter].first, JSONCollection(lista))
                }
                counter++
            }
        }
        // fires event to registered observers
        observers.forEach {
            it.propertyRemoved(pair)
        }
        println("valores "+valores.toString())
    }


    fun updateJSONObject(pair: Pair<String, Any>,lista: String?){
        var counter = 0
        var ind = 0
        var old : Any? = ""
        valores.forEach{
            if(lista == null){
                if(it.first.equals(pair.first)) {
                    ind = valores.indexOf(it)
                    println("ind: $ind + $valores")
                    counter++
                }
            }
            else{
                if(it.first.equals(lista.split(" ").get(0))) {
                    ind = valores.indexOf(it)
                    old = it.second
                    counter++
                }
            }
        }
        if(counter == 0){
            updateValores(pair, valores.size-1)
        }
        else{
            if(lista != null){
                var aux : MutableList<Any?> = mutableListOf()
                (old as JSONCollection).collect.forEach{
                    aux.add(it)
                }
                // aux.add(pair)

                val list_ind = lista.split(" ").get(1)

                val old_array = aux.get(list_ind.toInt())
                aux.removeAt(list_ind.toInt())
                var obj : Any? = null
                var aux_contrs : MutableList<Any?> = mutableListOf()

                old_array!!::class.dataClassFields.forEach {
                    //isto é mt à mão mas enfim
                    if(it.name == pair.first)
                        aux_contrs.add(pair.second)
                    else {
                        aux_contrs.add(it.call(old_array))
                    }
                }

                if(old_array::class.toString().contains("Aluno"))
                    obj = Aluno(aux_contrs[0].toString().toInt(), aux_contrs[1].toString(), aux_contrs[2].toString().toBoolean())
                else {
                    var ind = 0
                    val a = (aux_contrs as Collection<*>).elementAt(0)
                    (a as Collection<*>).forEach {
                        if((it as Pair<*,*>).first!!.equals(pair.first)) {
                            ind = a.indexOf(it)
                        }
                    }
                    aux_contrs.clear()
                    for (i in range(0,a.size)){
                        if(i!=ind){
                            aux_contrs.add(a.elementAt(i))
                        }
                        else{
                            aux_contrs.add(pair)
                        }
                    }
                    obj = JSONCollection(aux_contrs)
                }

                aux.add(list_ind.toInt(), obj)

                valores.remove(valores.get(ind))
                updateValores(Pair(lista.split(" ").get(0), aux), ind)
            }
            else {
                valores.remove(valores.get(ind))
                updateValores(pair, ind)
            }
        }

    }



    fun updateValores(pair: Pair<String, Any>, index: Int){
        val s = pair.first
        //println("antes updatevalores "+valores.toString())
        var holds: JSONElement
        when(pair.second) {
            is Int -> holds = JSONInt(pair.second as Int)
            is Boolean -> holds = JSONBoolean(pair.second as Boolean)
            is String -> holds = JSONString(pair.second as String)
            // is ArrayList<*> -> holds = JSONArray(pair.second as ArrayList<*>)
            is List<*> -> holds = JSONCollection(pair.second as List<*>)
            is Class<*> -> holds = JSONClass(pair.second as Class<*>)
            // come back to this dps de apagar os outros elems
            else -> holds = JSONString(pair.second as String)
        }
        // println("holds"+holds)
        valores.add(index, Pair(s, holds))
        println("updatevalores $valores")
    }

    fun updateValoresList(pair: Pair<String, Any>, index: Int, nome: String?, undo: Boolean = false, obj: Any? = null){
        if (!undo) {
            val name_given = pair.first
            var holds: Any = ""
            var counter = 0
            var in_list = false
            var counter_index = 0
            valores.forEach {
                if (nome.toString().startsWith(it.first)) {
                    val valor = ((valores[counter].second as JSONCollection).collect as MutableList<*>)
                    val lista = mutableListOf<Any?>()
                    val aux = mutableListOf<Any?>()
                    valor.forEach {
                        if (counter_index == index){
                            //lista.add(JSONCollection(mutableListOf(it, pair)))
                            in_list = true
                            if(!(it is JSONCollection)) {
                                aux.add(it)
                            }
                            else {
                                it.collect.forEach {
                                    aux.add(it)
                                }
                                aux.add(pair)
                            }
                            in_list = true
                        }else {
                            lista.add(it)
                        }
                        counter_index += 1
                    }
                    if (!in_list) {
                        aux.add(pair)
                    }
                    lista.add(index, JSONCollection(aux))
                    valores[counter] = Pair(it.first, JSONCollection(lista))
                }
                counter += 1
            }
        }else{
            var counter = 0
            var ind = 0
            var added = false
            for (i in valores){
                if (i.second is JSONCollection){
                    val lista = mutableListOf<Any>()
                    if(nome!!.split(" ").size == 2){
                    (i.second as JSONCollection).collect.forEach{
                        if(ind == nome!!.split(" ")[1].toInt()){
                            lista.add(obj!!)
                            added = true
                        }
                        lista.add(it!!)
                        ind += 1
                    }
                    if (!added){
                        lista.add(obj!!)
                    }

                    valores[counter] = Pair(i.first,JSONCollection(lista))
                }
                else {
                        valores[counter] = Pair(i.first, JSONCollection(obj!! as Collection<*>))
                    }
                }
                counter += 1
            }
            print(valores)
        }
    }



    fun classToValues(){
        var holds: JSONElement
        this.clazz::class.dataClassFields.forEach{
            if (!it.hasAnnotation<JSON_Excluir>()) {
                if (!it.hasAnnotation<ForceJSONString>()) {
                    val s = it.name
                    when(it.call(clazz)){
                        is Int -> holds = JSONInt(it.call(clazz) as Int)
                        is Boolean -> holds = JSONBoolean(it.call(clazz) as Boolean)
                        is String -> holds = JSONString(it.call(clazz) as String)
                        is ArrayList<*> -> holds = JSONArray(it.call(clazz) as ArrayList<*>)
                        is List<*> -> holds = JSONCollection(it.call(clazz) as List<*>)
                        is Class<*> -> {
                            holds = JSONClass(it.call(clazz) as Class<*>)
                        }

                        else -> {
                            holds = JSONString(it.call(clazz).toString())
                        }
                    }
                    valores.add(Pair(s,holds))
                }
                else{
                    holds = JSONString(it.call(clazz).toString())
                }
            }
        }
    }


    init{
        classToValues()
    }

    fun JSONObject_to_String(): String {
        var bool = false
        var string = "{\n"
        var counter = 0
        //val map = valores


        this.valores.forEach {
            string += "\"${it.first}\": "
            when (it.second) {
                null -> string += "null"
                is JSONInt -> string += (it.second as JSONInt).JSONInt_to_String()
                is JSONBoolean -> string += (it.second as JSONBoolean).JSONBoolean_to_String()
                is JSONString -> string += (it.second as JSONString).JSONString_to_String()
                is JSONArray -> string += (it.second as JSONArray).JSONArray_to_String()
                is JSONCollection -> string += (it.second as JSONCollection).JSONCollection_to_String()
                is JSONClass -> string += (it.second as JSONClass).JSONClass_to_String()
                // come back to this dps de apagar os outros elems
                else -> {
                    string += (it.second as JSONString).JSONString_to_String()
                }
            }

            counter += 1
            if(counter == this.valores.size) bool = true
            string += if (!bool){
                ""
            } else {
                "}"
            }}
        return string
    }

    fun accept(visitor: Visitor) {
        visitor.visit(this)
    }
}



val KClass<*>.dataClassFields: List<KProperty<*>>
    get() {
        require(isData) { "instance must be data class" }
        return primaryConstructor!!.parameters.map { p ->
            declaredMemberProperties.find { it.name == p.name }!!
        }
    }

interface Visitor {
    fun visit(a : JSONElement)
    fun visit(a : JSONObject)
    fun visit(a: JSONCollection)
    fun visit(a: JSONClass)
}

@Target(AnnotationTarget.CLASS, AnnotationTarget.PROPERTY, AnnotationTarget.FUNCTION )
annotation class JSON_Excluir

@Target(AnnotationTarget.CLASS, AnnotationTarget.PROPERTY, AnnotationTarget.FUNCTION )
annotation class DataClass(val id: String)

@Target(AnnotationTarget.CLASS, AnnotationTarget.PROPERTY, AnnotationTarget.FUNCTION )
annotation class ForceJSONString
// excluir propriedades da instanciação
// utilização de identificadores personalizados (e não os das classes)
// forçar que alguns valores sejam considerados strings JSON (pe. um atributo inteiro ser representado como string)

data class Aluno (
    @DataClass("numero")
   // @ForceJSONString
    val numero: Int,
    val nome: String,
    @DataClass("internacional")
    //@ForceJSONString
    val internacional: Boolean)



data class UC (
    //@JSON_Excluir
    val uc: String,
    @DataClass("ects")
    //@ForceJSONString
    val ects: Double,
    @DataClass("data")
    //@ForceJSONString
    val data_exame : Data? = null,
    val inscritos : List<Aluno> = listOf())


fun allObjectsWithProperty(properties: List<String>, list_objs: List<JSONObject>): List<Any>{

    class ObjectCounter: Visitor {
        var final_list = mutableListOf<JSONObject>()
        override fun visit(a: JSONCollection) {
        }

        override fun visit(a: JSONClass) {
        }


        override fun visit(a: JSONElement) {
        }

        override fun visit(obj: JSONObject) {

            var counter = 0
            var l = 0
            while(l<properties.size){
                obj.valores.forEach {
                    if(it.first.equals(properties[l]))
                        counter += 1
                }
                l+=1
            }
            if(counter == properties.size)
                final_list.add(obj)
        }

    }

    val ob = ObjectCounter()
    list_objs.forEach {
        it.accept(ob)
    }
    return ob.final_list
}






fun savedValues(property: String, list_objs: List<JSONObject>): List<Any?>{

    class Value: Visitor {
        var final_values = mutableListOf<Any?>()
        override fun visit(obj: JSONObject) {
            obj.valores.forEach {
                //obj::class.declaredMemberProperties.forEach {
                if(it.first.equals(property)){
                    //if(it.name.equals(property)) {
                    //final_values.add(it.call(obj).toString())
                    final_values.add(it.second)
                }
            }
        }

        override fun visit(a: JSONCollection) {
        }

        override fun visit(a: JSONClass) {
        }

        override fun visit(a: JSONElement) {
        }
    }
    val v = Value()

    list_objs.forEach {
        it.accept(v)
    }

    return v.final_values
}


fun typeEqualsProperty(property: String, type: String, list_objs: List<JSONObject>): Boolean{

    class typeProperty: Visitor {
        var list_values = mutableListOf<Boolean>()

        override fun visit(obj: JSONObject) {
            obj.valores.forEach {
                // println(it.first)
                //obj::class.declaredMemberProperties.forEach {
                var value = false
                if(it.first.equals(property)){
                    //if(it.name.equals(property)) {
                    if(it.second != null) {
                        // println("${obj}: ${property} should be ${type} but is ${it.second!!::class.toString().removePrefix("class ")}\n")
                        if (it.second!!::class.toString().removePrefix("class ").equals(type)) {
                            value = true
                        }
                        list_values.add(value)
                    }
                }
            }
            // println("na funcao, list_values: ${list_values}")
        }

        override fun visit(a: JSONCollection) {
        }

        override fun visit(a: JSONClass) {
        }

        override fun visit(a: JSONElement) {
        }
    }

    val tp = typeProperty()

    list_objs.forEach {
        it.accept(tp)
    }

    return !tp.list_values.contains(false)
}

fun propertySameStructure(property: String, list_objs: List<JSONObject>): Boolean{

    class SameStructure: Visitor {
        var list_values = mutableListOf<Boolean>()
        override fun visit(a: JSONCollection) {
        }

        override fun visit(a: JSONClass) {
        }

        override fun visit(a: JSONElement) {
        }

        override fun visit(obj: JSONObject) {
            var propTipo = mutableMapOf<String, String>()
            obj.valores.forEach {
                if(it.first.equals(property)){
                    //só funciona para collection
                    (it.second as JSONCollection).collect.forEach{
                        it!!::class.declaredMemberProperties.forEach{
                            if(propTipo.size == 0){
                                propTipo.put(it.name,it.returnType.toString().removePrefix("kotlin."))
                                list_values.add(true)
                            }
                            else
                            {
                                if(propTipo.containsKey(it.name)) {
                                    if (!it.returnType.toString().removePrefix("kotlin.").equals(propTipo.get(it.name)))
                                        list_values.add(false)
                                    else
                                        list_values.add(true)
                                }
                                else
                                {
                                    propTipo.put(it.name,it.returnType.toString().removePrefix("kotlin."))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    val ss = SameStructure()

    list_objs.forEach {
        it.accept(ss)
    }

    return !ss.list_values.contains(false)
}





fun main(){
    val Aluno1 = Aluno(101101, "Dave Farley", true)
    val Aluno2 = Aluno(101102, "Martin Fowler", true)
    val Aluno3 = Aluno(26503, "André Santos", false)
    val inscritos = listOf(Aluno1, Aluno2, Aluno3)
    val unidade_curricular = UC("PA", 6.0, null, inscritos)
    val lala = JSONClass(unidade_curricular)
    val fin = JSONObject(unidade_curricular)
    print(fin.JSONObject_to_String())


    //  val obj1 = JSONObject("numero" to 101101, "nome" to "Dave Farley", "internacional" to true)
    // val obj2 = JSONObject("numero" to 101102, "nome" to "Martin Fowler", "internacional" to true)
    //val obj3 = JSONObject("numero" to 26503, "nome" to "André Santos", "internacional" to false)

}