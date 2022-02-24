package com.venson.versatile.ubb.demo

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        //"Synopsis":"\"ghbhhbb\"","Title":"游戏哦\(//∇//)\"
        val text = """
            {
            "Synopsis_0":"\"ghbhhbb\"",
            "Title_0":"游戏哦\\(//∇//)\\",
            }
        """
        val json: JSONObject = JSON.parseObject(String(text.toByteArray()))
        json["Synopsis_1"] = "\"ghbhhbb\""
        json["Title_1"] = "游戏哦\\(//∇//)\\"
        print(json.toJSONString())
    }
}