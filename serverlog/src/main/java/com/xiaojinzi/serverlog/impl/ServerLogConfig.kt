package com.xiaojinzi.serverlog.impl

class ServerLogConfig private constructor(builder: Builder) {

    val productName: String
    val debug: Boolean
    val networkLogAllowReadRequestBodyContentTypeSet: Set<String>
    val networkLogAllowReadResponseBodyContentTypeSet: Set<String>

    init {
        productName = builder.productName!!
        debug = builder.debug
        networkLogAllowReadRequestBodyContentTypeSet = builder.networkLogAllowReadRequestBodyContentTypeSet
        networkLogAllowReadResponseBodyContentTypeSet = builder.networkLogAllowReadResponseBodyContentTypeSet
    }

    class Builder constructor() {

        internal var productName: String? = null
        internal var debug = false

        internal val networkLogAllowReadRequestBodyContentTypeSet = mutableSetOf(
             "application/json", "text/plain"
        )
        internal val networkLogAllowReadResponseBodyContentTypeSet = mutableSetOf(
             "application/json", "text/plain"
        )

        fun withProductName(value: String?): Builder {
            productName = value
            return this
        }

        fun withDebug(value: Boolean): Builder {
            debug = value
            return this
        }

        fun addRequestAllowReadBodyContentType(value: String) {
            networkLogAllowReadRequestBodyContentTypeSet.add(value)
        }

        fun addResponseAllowReadBodyContentType(value: String) {
            networkLogAllowReadResponseBodyContentTypeSet.add(value)
        }

        fun build(): ServerLogConfig {
            return ServerLogConfig(this)
        }

    }

    companion object {
        fun newBuilder(): Builder {
            return Builder()
        }
    }

}

