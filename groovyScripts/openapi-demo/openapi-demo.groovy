/*
 * Licensed to the Tianjin Langhua Ltd. under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.ofbiz.base.util.UtilDateTime

// this demo return user roles
Map<String, Object> testOpenApiDemoService() {
    nowTimestamp = UtilDateTime.nowTimestamp()
    result = [:]
    partyId = userLogin.partyId
    viewIndex = context.viewIndex ? context.viewIndex : 1
    viewSize = context.viewSize ? context.viewSize : 10
    username = context.username
    if (username) {
        targetUserLogin = from("UserLogin").where("userLoginId", username)
                                           .cache(false)
                                           .queryOne()
        if (targetUserLogin && targetUserLogin.partyId) {
            partyId = targetUserLogin.partyId
        } else {
            result.result = "error"
            if (!targetUserLogin) {
                result.message = "Cannot find userLoginId[" + username + "] in UserLogin."
            } else {
                result.message = "Cannot find partyId in UserLogin."
            }
            return result
        }
    } else {
        username = userLogin.userLoginId
    }
    rolesResult = []
    securityGroups = from("OpenApiDemoEntity").where("userLoginId", username)
                                              .cache(false)
                                              .filterByDate()
                                              .cursorScrollInsensitive()
                                              .queryPagedList(viewIndex - 1, viewSize)

    if (securityGroups) {
        securityGroups.each { securityGroup ->
            rolesResult << [roleId: securityGroup.securityGroupId, roleName: securityGroup.securityGroupName, roleDescribe: securityGroup.securityGroupDesc]
        }
    }

    result.result = "success"
    result.partyId = partyId
    result.timestamp = System.currentTimeMillis()
    result.totalCount = securityGroups.size()
    result.status = 200
    if (username) {
        result.username = username
    }
    if (rolesResult) {
        result.roles = rolesResult
    }
    return result
}