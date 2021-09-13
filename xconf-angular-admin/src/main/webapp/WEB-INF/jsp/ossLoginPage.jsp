<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<!--
* If not stated otherwise in this file or this component's Licenses.txt file the
* following copyright and licenses apply:
*
* Copyright 2018 RDK Management
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
* Author: Igor Kostrov
* Created: 12/10/2018
-->
<html>
<head>
    <title>Xconf Login</title>
    <style type="text/css">
        .login-form {
            width: 400px;
            margin: 200px auto
        }
        .form-group-padding, .container-padding {
            padding: 10px
        }

        .form-group-padding-sm {
            padding: 10px 5px
        }

        .container-padding-sm-v {
            padding: 5px 10px
        }
        .form-group-padding-narrow {
            padding: 10px 0px
        }
        .error-label {
            color: red
        }
    </style>
    <link href="<c:url value="/app/compiled/vendor.css"/>" rel="stylesheet" />
</head>
<body>
<div class="panel panel-default login-form">
    <div class="panel-heading">Please enter your <strong>NT</strong> credentials</div>
    <div class="panel-body">
        <span class="error-label">${error}</span>
        <form method="POST" action="./ossLoginForm">
            <div class="form-group">
                <label for="username">Username:</label>
                <input class="form-control" type="text" name="username" id="username" />
            </div>
            <div class="form-group">
                <label for="password">Password:</label>
                <input class="form-control" type="password" name="password" id="password" />
            </div>

            <button type="submit" class="btn btn-primary pull-right">Login</button>
        </form>
    </div>
</div>

</body>
</html>
