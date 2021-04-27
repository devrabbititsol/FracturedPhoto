package com.logictreeit.android.fracturedphoto.utils;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class VolleyRequestHandler {
    private  static VolleyRequestHandler instance;
    private RequestQueue requestQueue;
    private Context ctx;

    private VolleyRequestHandler(Context context) {
        ctx = context;
        requestQueue = getRequestQueue();
    }

    public static synchronized VolleyRequestHandler getInstance(Context context) {
        if (instance == null) {
            instance = new VolleyRequestHandler(context);
        }
        return instance;
    }

    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            requestQueue = Volley.newRequestQueue(ctx.getApplicationContext());
        }
        return requestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }
}