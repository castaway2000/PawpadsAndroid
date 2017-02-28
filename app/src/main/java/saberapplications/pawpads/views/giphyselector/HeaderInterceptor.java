package saberapplications.pawpads.views.giphyselector;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Stanislav Volnjanskij on 2/10/17.
 */

public class HeaderInterceptor   implements Interceptor {
    @Override
    public Response intercept(Chain chain)
            throws IOException {
        Request request = chain.request();
        HttpUrl url = request.url().newBuilder().addQueryParameter("api_key","dc6zaTOxFJmzC").build();
        request = request.newBuilder().url(url).build();
        return chain.proceed(request);
    }
}