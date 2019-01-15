package pl.swd.app.interfaces

import pl.swd.app.utils.emptyOptional
import java.util.*
import kotlin.collections.ArrayList

interface GetResultFragment <T> {
    var cancelFlag: Boolean

    fun getResult(): Optional<T> {
        return emptyOptional()
    }

    fun getResultList(): Optional<List<T>> {
        return Optional.empty()
    }
}