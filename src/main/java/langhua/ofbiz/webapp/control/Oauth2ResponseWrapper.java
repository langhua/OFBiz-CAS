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
