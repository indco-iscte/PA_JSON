import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test


class Test_Project {

    val Aluno1 = Aluno(101101, "Dave Farley", true)
    val Aluno2 = Aluno(101102, "Martin Fowler", true)
    val Aluno3 = Aluno(26503, "André Santos", false)
    val inscritos = listOf(Aluno1, Aluno2, Aluno3)
    val unidade_curricular = UC("PA",6.0,null,inscritos)


    val obj1 = JSONObject(Aluno1)
    val obj2 = JSONObject(Aluno2)
    val obj3 = JSONObject(Aluno3)
    val obj4 = JSONObject(unidade_curricular)
    val lObjs = listOf(obj1,obj2,obj3)

    @Test
    fun testing_JSONObject(){
        assertEquals("{\n" +
                "\"uc\": \"PA\", \n" +
                "\"ects\": 6.0, \n" +
                "\"data-exame\": null, \n" +
                "\"inscritos\": [ \n" +
                "{ \n" +
                "\"numero\": 101101,\n" +
                "\"nome\": \"Dave Farley\",\n" +
                "\"internacional\": true\n" +
                "}, \n" +
                "{ \n" +
                "\"numero\": 101102,\n" +
                "\"nome\": \"Martin Fowler\",\n" +
                "\"internacional\": true\n" +
                "}, \n" +
                "{ \n" +
                "\"numero\": 26503,\n" +
                "\"nome\": \"André Santos\",\n" +
                "\"internacional\": false\n" +
                "} \n" +
                "] \n" +
                "}", obj4.JSONObject_to_String())
    }

    //efetuar pesquisas, como por exemplo:
    //obter todos os valores guardados em propriedades com identificador “numero”

    @Test
    fun testSavedValues(){
        val property = "internacional"
        val l = listOf(obj1, obj2, obj3, obj4)
        val expected = mutableListOf(JSONBoolean(true), JSONBoolean(true), JSONBoolean(false))
        assertEquals(expected, savedValues(property, l))
    }

    //obter todos os objetos que têm as propriedades numero e nome

    @Test
    fun testAllObjectsWithProperty(){
        val properties = listOf("numero", "nome")
        val l = listOf(obj1, obj2, obj3,obj4)
        val expected = listOf(obj1, obj2, obj3)
        assertEquals(expected, allObjectsWithProperty(properties, l))
    }



    //verificar que o modelo obedece a determinada estrutura, por exemplo:
    //a propriedade numero apenas tem como valores números inteiros

    @Test
    fun testOnlyIntNumbers(){
        val l = listOf(obj1, obj2, obj3, obj4)
        assertTrue(typeEqualsProperty("numero", "JSONInt", l))
    }


    //a propriedade inscritos consiste num array onde todos os objetos têm a mesma estrutura
    // vou assumir q "mesma estrutura" quer dizer q cada aluno tem um numero int, um nome string e um internacional boolean

    @Test
    fun testInscritosSameStructure(){
        val l = listOf(obj1, obj2, obj3, obj4)
        assertTrue(propertySameStructure("inscritos", l))
    }


}