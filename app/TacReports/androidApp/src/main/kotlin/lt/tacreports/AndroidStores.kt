package lt.tacreports

import android.content.Context

/** Loads the shared stores from SharedPreferences; every activity and service calls this first. */
fun initStores(context: Context) = initStores(AndroidKeyValueStore(context))
