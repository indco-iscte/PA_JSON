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

   /* val uc = JSONObject(
        "uc" to "PA",
        "ects" to 6.0,
        "data-exame" to null,
        "inscritos" to inscritos
    )

*/


 /*   @Test
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
                "}", uc.JSONObject_to_String())
    }
*/
    /*
    @Test
    fun testJSONStringToString(){
        val jsonS = JSONString("olá")
        assertEquals("\"nome\": \"olá\",\n", jsonS.JSONString_to_String())
    }

    @Test
    fun testJSONBooleanToString(){
        val jsonB = JSONBoolean(true)
        assertEquals("true\n", jsonB.JSONBoolean_to_String())
    }

    @Test
    fun testJSONIntToString(){
        val jsonI = JSONInt(1)
        assertEquals("\"number\": 1,\n", jsonI.JSONInt_to_String())
    }

    @Test
    fun testJSONDateToString(){
        val str = "2017-12-03"
        val date = Date.valueOf(str)
        val jsonD = JSONDate(date)
        assertEquals("\"number\": 2017-12-03,\n", jsonD.JSONDate_to_String())
    }

    @Test
    fun testJSONDoubleToString(){
        val jsonD = JSONDouble(0.45)
        assertEquals("\"number\": 0.45,\n", jsonD.JSONDouble_to_String())
    }

    enum class StudentType {
        Bachelor, Master, Doctoral
    }

    @Test
    fun testJSONEnumToString(){

        val jsonE = JSONEnum(StudentType::class)
        assertEquals("\"enum\": [\"Bachelor\",\"Master\",\"Doctoral\"]\n", jsonE.JSONEnum_to_String())
    }


    @Test
    fun testJSONArrayToString(){
        val jsonA = JSONArray(listOf("olá", "adeus"))
        assertEquals("\"ar\": [\"olá\",\"adeus\"]\n", jsonA.JSONArray_to_String())
    }


    @Test
    fun testJSONClassToString(){
        val jsonC = JSONClass(unidade_curricular)
        assertEquals("\"v\": \"true\",\n", jsonC.JSONClass_to_String())
    }

    @Test
    fun testJSONMapToString(){
        val jsonM = JSONMap(mapOf("numero" to 101101, "nome" to "Dave Farley"))
        assertEquals("\"map\": [\"numero\":101101,\"nome\":\"Dave Farley\"]\n", jsonM.JSONMap_to_String())
    }

/*
    @Test
    fun testJSONCollectionToString(){
        val jsonC = JSONCollection(listOf("olá", "adeus"))
        assertEquals("\"v\": \"true\",\n", jsonC.JSONCollection_to_String())
    }

*/
*/

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