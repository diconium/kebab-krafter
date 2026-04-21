package com.diconium.mobile.tools.kebabkrafter.sample.cases.security

import com.diconium.mobile.tools.kebabkrafter.sample.gen.case.security.controllers.GetData
import com.diconium.mobile.tools.kebabkrafter.sample.gen.case.security.models.UserData

class GetDataController : GetData {
    override suspend fun SecureCallScope.execute(): UserData {
        // IMPORTANT REMINDER:
        // This is in absolutely no way a valid implementation of any type of security.
        // That is just a simple example of a controller receiving data from the bearer token
        return UserData("Hello $user!")
    }
}
