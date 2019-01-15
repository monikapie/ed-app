package pl.swd.app.utils;

import javafx.collections.ObservableList
import javafx.scene.control.ListView
import javafx.scene.input.MouseEvent
import tornadofx.*
import java.lang.reflect.Type
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

/**
 * Returns empty ObservableList
 */
inline fun <reified T> emptyObservableList(): ObservableList<T> {
    return arrayListOf<T>().observable()
}


/**
 * Any object can be transformed to an optional
 */
fun <T> T?.asOptional() = Optional.ofNullable(this)

fun <T> emptyOptional(): Optional<T> = Optional.empty()


/**
 * ListView extension functions
 *
 * todo: add more extension functions to listview?
 */
fun <T> ListView<T>.onItemClick(action: (T) -> Unit) {
    addEventFilter(MouseEvent.MOUSE_CLICKED) { event ->
        if (selectedItem != null && event.target.isInsideRow()) {
            action(selectedItem!!)
        }
    }
}

fun Type.isInstanceOf(clazz: KClass<*>): Boolean {
    return this.typeName.equals(clazz.qualifiedName)
}

/**
 * Indicates if a class is inherits from or implement interaces directly or indirectly.
 */
fun Class<*>.isSubclassOf(clazz: Class<*>): Boolean {
    return this.kotlin.isSubclassOf(clazz.kotlin)
}
