/*******************************************************************************
 * Copyright 2018 Langhua Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package langhua.ofbiz.webapp.control;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;

public class Oauth2ResponseWrapper extends HttpServletResponseWrapper {

    private int status = SC_OK;

    public Oauth2ResponseWrapper(HttpServletResponse response) {
        super(response);
    }

    public int getStatus() {
        return status;
    }

    @Override
    public void setStatus(int status) {
        super.setStatus(status);
        this.status = status;
    }

    @Override
    @Deprecated
    public void setStatus(int sc, String sm) {
        super.setStatus(sc, sm);
        this.status = sc;
    }

    @Override
    public void sendError(int sc) throws IOException {
        this.status = sc;
        super.sendError(sc);
    }

    @Override
    public void sendError(int sc, String msg) throws IOException {
        this.status = sc;
        super.sendError(sc, msg);
    }
}
