package pl.swd

import com.google.gson.Gson
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import org.springframework.test.context.junit4.SpringRunner
import kotlin.test.assertEquals
import org.springframework.context.ApplicationContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.support.beans


@RunWith(SpringRunner::class)
@ContextConfiguration(locations = arrayOf("/test-beans.xml"))
class MyTest {

    @Autowired
    lateinit var applicationContext: ApplicationContext

    @Autowired
    lateinit var gson: Gson

    @Test
    fun foo() {

    }
}