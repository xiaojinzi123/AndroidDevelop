package com.xiaojinzi.develop

import android.app.Application

class Config private constructor(builder: Builder) {

    val debug: Boolean
    val application: Application
    val developOpenIntentAction: String?
    val developErrorIntentAction: String?
    val mainAppIntentAction: String?
    val isFinishAllActivityBeforeKillProcess: Boolean

    init {
        debug = builder.debug
        application = builder.application!!
        developOpenIntentAction = builder.developOpenIntentAction
        developErrorIntentAction = builder.developErrorIntentAction
        mainAppIntentAction = builder.mainAppIntentAction
        isFinishAllActivityBeforeKillProcess = builder.isFinishAllActivityBeforeKillProcess
    }


    class Builder constructor() {
        internal var debug = false
        internal var application: Application? = null
        internal var developOpenIntentAction: String? = null
        internal var developErrorIntentAction: String? = null
        internal var mainAppIntentAction: String? = null
        internal var isFinishAllActivityBeforeKillProcess = false

        fun withApplication(value: Application?): Builder {
            application = value
            return this
        }

        fun withDebug(value: Boolean): Builder {
            debug = value
            return this
        }

        fun withDevelopOpenIntentAction(value: String?): Builder {
            developOpenIntentAction = value
            return this
        }

        fun withDevelopErrorIntentAction(value: String?): Builder {
            developErrorIntentAction = value
            return this
        }

        fun withMainAppIntentAction(value: String?): Builder {
            mainAppIntentAction = value
            return this
        }

        fun withFinishAllActivityBeforeKillProcess(value: Boolean): Builder {
            isFinishAllActivityBeforeKillProcess = value
            return this
        }

        fun build(): Config {
            return Config(this)
        }
    }

    companion object {
        fun newBuilder(): Builder {
            return Builder()
        }
    }

}

