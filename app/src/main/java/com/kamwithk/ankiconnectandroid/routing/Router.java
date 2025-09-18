package com.kamwithk.ankiconnectandroid.routing;

import android.content.Context;
import androidx.preference.PreferenceManager;
import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.router.RouterNanoHTTPD;

import java.io.IOException;

public class Router extends RouterNanoHTTPD {
    private Context context;
    public static String contentType;

    public Router(Integer port, Context context) throws IOException {
        super(port);
        this.context = context;

        contentType = new ContentType("; charset=UTF-8").getContentTypeHeader();
        addMappings();
        start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
    }

    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    public void addMappings() {
        addRoute("/", RouteHandler.class, this.context);
        addRoute("/localaudio/(.)+", LocalAudioRouteHandler.class, this.context);
    }

    @Override
    public Response serve(IHTTPSession session) {
        if (Method.OPTIONS.equals(session.getMethod())) {
            // This is a pre-flight request for CORS. We must respond successfully
            // for the browser to send the actual POST request.
            Response response = newFixedLengthResponse(Response.Status.OK, MIME_PLAINTEXT, null, 0);

            // We must add the same CORS headers to the pre-flight response
            android.content.SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.context);
            String corsHost = sharedPreferences.getString("cors_host", "");
            if (!corsHost.trim().equals("")) {
                response.addHeader("Access-Control-Allow-Origin", corsHost);
                response.addHeader("Access-Control-Allow-Headers", "*");
                response.addHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
            }
            
            return response;
        }

        // For all other methods (like POST and GET), let the default router do its job.
        return super.serve(session);
    }
}