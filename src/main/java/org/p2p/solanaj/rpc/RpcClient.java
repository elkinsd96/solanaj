package org.p2p.solanaj.rpc;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;

import org.p2p.solanaj.rpc.types.RpcRequest;
import org.p2p.solanaj.rpc.types.RpcResponse;

import javax.net.ssl.*;

public class RpcClient {
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private String endpoint;
    private OkHttpClient httpClient;
    private RpcApi rpcApi;

    public RpcClient(Cluster endpoint) {
        this(endpoint.getEndpoint());
    }

    public RpcClient(String endpoint) {
        this.endpoint = endpoint;
        this.httpClient = new OkHttpClient.Builder()
                //.addInterceptor(new LoggingInterceptor())
                .build();
        rpcApi = new RpcApi(this);
    }

    public <T> T call(String method, List<Object> params, Class<T> clazz) throws RpcException {
        RpcRequest rpcRequest = new RpcRequest(method, params);

        JsonAdapter<RpcRequest> rpcRequestJsonAdapter = new Moshi.Builder().build().adapter(RpcRequest.class);
        JsonAdapter<RpcResponse<T>> resultAdapter = new Moshi.Builder().build()
                .adapter(Types.newParameterizedType(RpcResponse.class, Type.class.cast(clazz)));

        Request request = new Request.Builder().url(endpoint)
                .post(RequestBody.create(rpcRequestJsonAdapter.toJson(rpcRequest), JSON)).build();

        try {
            Response response = httpClient.newCall(request).execute();
            final String result = response.body().string();
            // System.out.println("Response = " + result);
            RpcResponse<T> rpcResult = resultAdapter.fromJson(result);

            if (rpcResult.getError() != null) {
                throw new RpcException(rpcResult.getError().getMessage());
            }

            return (T) rpcResult.getResult();
        } catch (SSLHandshakeException e) {
            this.httpClient = new OkHttpClient.Builder().build();
            throw new RpcException(e.getMessage());
        } catch (IOException e) {
            throw new RpcException(e.getMessage());
        }
    }

    public RpcApi getApi() {
        return rpcApi;
    }

    public String getEndpoint() {
        return endpoint;
    }

}
