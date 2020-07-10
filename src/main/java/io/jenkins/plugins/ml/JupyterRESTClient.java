/*
 * The MIT License
 *
 * Copyright 2020 Loghi Perinpanayagam.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package io.jenkins.plugins.ml;

import com.google.gson.Gson;
import io.jenkins.plugins.ml.model.Contents;
import io.jenkins.plugins.ml.model.Kernel;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import org.apache.http.HttpException;
import org.apache.http.client.utils.URIBuilder;
import org.glassfish.jersey.SslConfigurator;
import org.glassfish.jersey.client.ClientConfig;

import javax.net.ssl.SSLContext;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

public class JupyterRESTClient  {

    private final static String SSL_ENABLED_PROTOCOL = "https";
    private final static String SSL_DISABLED_PROTOCOL = "http";

    private String host;
    private  int port;
    private Client client;
    private ClientConfig config;
    private boolean isSSLEnabled;
    private URL rootURL;

    protected JupyterRESTClient(String host, int port) {
        this.host = host;
        this.port = port;
        this.isSSLEnabled = false;
    }

    protected JupyterRESTClient(String host, int port, boolean isSSLEnabled) {
        this.host = host;
        this.port = port;
        this.isSSLEnabled = isSSLEnabled;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public Response login(String token) throws URISyntaxException, MalformedURLException {

        // Default configuration
        config = new ClientConfig();
        config.property("token",token);
        if(isSSLEnabled){
            SSLContext sslContext = SslConfigurator.newInstance(true).createSSLContext();
            client = ClientBuilder.newBuilder().sslContext(sslContext)
                    .withConfig(config).build();
        }else {
            client = ClientBuilder.newBuilder().withConfig(config).build();
        }

        // Build the url with host and port
        URIBuilder builder = new URIBuilder();
        builder.setScheme(isSSLEnabled ? SSL_ENABLED_PROTOCOL : SSL_DISABLED_PROTOCOL);
        builder.setHost(host);
        builder.setPort(port);
        this.rootURL = builder.build().toURL();
        return client.target(rootURL.toString())
                                    .path("/login")
                                    .request()
                                    .header(HttpHeaders.AUTHORIZATION, "Token "+token)
                                    .get();
    }

  /**
   * @param path path to the notebook eg: /example.ipynb
   * @return Contents detail of the file
   * @throws HttpException throw exception on failure
   */
  public Contents getContents(String path) throws HttpException {
        Response res = client.target(rootURL.toString())
                .path("/api/contents")
                .path(path)
                .request()
                .header(HttpHeaders.AUTHORIZATION, "Token "+client.getConfiguration().getProperties().get("token"))
                .get();
        if(res.getStatus()== 200){
            Gson gson = new Gson();
            JSONObject jsonObj = (JSONObject) JSONSerializer.toJSON(res.readEntity(String.class));
            return gson.fromJson(jsonObj.toString(),Contents.class);
        }
         throw new HttpException();
    }

  /**
   * @return List of kernels
   * @throws HttpException throw exception on failure
   */
  public List getKernels() throws HttpException {
        Response res = client.target(rootURL.toString())
                .path("/api/kernels")
                .request()
                .header(HttpHeaders.AUTHORIZATION, "Token " + client.getConfiguration().getProperties().get("token"))
                .get();
        // On success return kernel list
        if(res.getStatus()== 200){
            List kernelList = new LinkedList();
            Gson gson = new Gson();
            JSONArray jsonArray = (JSONArray) JSONSerializer.toJSON( res.readEntity(String.class) );
            for (Object o : jsonArray) {
                kernelList.add(gson.fromJson(o.toString(), Kernel.class));
            }
            return kernelList;
        }
        throw  new HttpException();
    }
}
