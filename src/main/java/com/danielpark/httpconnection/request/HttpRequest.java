package com.danielpark.httpconnection.request;

import android.content.Context;

import com.danielpark.httpconnection.model.MultipartFile;
import com.danielpark.httpconnection.model.NameValue;
import com.danielpark.httpconnection.type.ContentType;
import com.danielpark.httpconnection.type.RequestType;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * Copyright (c) 2014-2015 op7773hons@gmail.com
 * Created by Daniel Park on 2015-12-19.
 */
public class HttpRequest extends RequestType {

    private String URL;

    private ArrayList<NameValue> headers;
    private ArrayList<NameValue> parameters;
    private String body;
    private String contentType;
    private ArrayList<MultipartFile> files;

	// Daniel (2016-07-08 16:56:57): cache config
	private boolean isResponseCached = false;
	// Time unit is second
	private int cacheRenewalTime = 60;	// cache age
	private int cacheExpireTime = 0;	// when connectivity is available, use cache instead until request cache is expired.

    public enum Method {
        POST, GET, PUT, DELETE
    }

    private Method httpMethod = Method.GET;  // http type. default value is POST

    public HttpRequest() {
        this.headers = new ArrayList<>();
        this.parameters = new ArrayList<>();
//        this.files = new ArrayList<>();
    }

    /**
     * Set url
     *
     * @param URL
     * @return
     * @throws IllegalArgumentException
     * @throws MalformedURLException
     */
    public HttpRequest setURL(String URL) throws IllegalArgumentException, MalformedURLException {
        if(validate(URL))
            this.URL = URL;
        return this;
    }

    /**
     * Get url
     *
     * @return
     */
    public String getURL() {
        return URL;
    }

    /**
     * Validates the upload request and throws exceptions if one or more parameters are
     * not properly set.
     *
     * @throws IllegalArgumentException if request protocol or URL are not correctly set
     * @throws MalformedURLException    if the provided server URL is not valid
     */
    private boolean validate(String url) throws IllegalArgumentException, MalformedURLException {
        if (url == null || "".equals(url)) {
            throw new IllegalArgumentException("Request URL cannot be either null or empty");
        }

        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            throw new IllegalArgumentException("Specify either http:// or https:// as protocol");
        }

        // Check if the URL is valid
        new URL(url);

        return true;
    }

    /**
     * Sets the HTTP method to use. By default it's set to POST
     *
     * @param method new HTTP method to use
     * @return {@link HttpRequest}
     */
    public HttpRequest setMethod(Method method) {
        if (method != null)
            httpMethod = method;
        return this;
    }

    /**
     * Get the HTTP method
     *
     * @return
     */
    public Method getMethod() {
        return httpMethod;
    }

    /**
     * Adds a header to this request
     *
     * @param headerName  header name
     * @param headerValue header value
     * @return {@link HttpRequest}
     */
    public HttpRequest addHeader(final String headerName, final String headerValue) {
        headers.add(new NameValue(headerName, headerValue));
        return this;
    }

    /**
     * Get headers
     *
     * @return
     */
    public ArrayList<NameValue> getHeaders() {
        return headers;
    }

    /**
     * Remove certain header by Header name
     * @param headerName
     */
    public void removeHeader(final String headerName){
        if(headers == null) return;

        for(NameValue nv : headers){
            if(nv.getName().equals(headerName)){
                headers.remove(nv);
                return;
            }
        }
    }

    /**
     * Remove certain header by Header name and Header value
     * @param headerName
     * @param headerValue
     */
    public void removeHeader(final String headerName, final String headerValue){
        if(headers == null) return;

        for(NameValue nv : headers){
            if(nv.getName().equals(headerName) && nv.getValue().equals(headerValue)){
                headers.remove(nv);
                return;
            }
        }
    }

    /**
     * Adds a parameter to this request (Not body but parameters)
     *
     * @param paramName  parameter name
     * @param paramValue parameter value
     * @return {@link HttpRequest}
     */
    public HttpRequest addParameter(final String paramName, final Object paramValue) {
        try{
            parameters.add(new NameValue(paramName, String.valueOf(paramValue)));
        }catch (Exception ignored){
        }
        return this;
    }

    /**
     * @return Get parameters
     */
    public ArrayList<NameValue> getParameters() {
        return parameters;
    }

    /**
     * Add body String to send
     * @param body
     */
    public void addBody(String body){
        this.body = body;
    }

    /**
     * Get body
     * @return
     */
    public String getBody(){
        return body;
    }

    /**
     * Set contentType;
     * @param contentType
     */
    public void setContentType(String contentType){
        this.contentType = contentType;
    }

    /**
     * Get contentType
     * @return
     */
    public String getContentType(){
        return contentType;
    }

	/**
	 * Add file to send to server
	 * @param file
	 * @param paramName
	 * @param contentType
	 * @return
	 * @throws FileNotFoundException
	 */
	public HttpRequest addFile(File file, String paramName, String contentType) throws FileNotFoundException {
		if (files == null)
			files = new ArrayList<>();

		files.add(new MultipartFile(file.getAbsolutePath(), file.getName(), paramName, contentType));
		return this;
	}

    public ArrayList<MultipartFile> getFiles() {
        if (files == null)
            files = new ArrayList<>();
        return files;
    }

    @Override
    public void setRequestType(Type type) {
        super.setRequestType(type);
    }

    @Override
    public RequestType.Type getRequestType() {
        return super.getRequestType();
    }

	//--------------- This is for response cache -------------------- //
	/**
	 * Response is cached for this call only and HTTP GET method only supports
	 * @param result
	 */
	public void setCacheResponse(boolean result) {
		this.isResponseCached = result;
	}

	/**
	 * Get if this call caches response
	 * @return
	 */
	public boolean isCacheResponse(){
		return isResponseCached;
	}

	/**
	 * It only works when Network connectivity is available. set cache init date. <br>
	 *     How long cache should be remain until cache is updated
	 * @param maxAge
	 * @param timeUnit
	 */
	public void setCacheRenewalTime(int maxAge, TimeUnit timeUnit) {
		if (maxAge < 0) throw new IllegalArgumentException("maxAge < 0: " + maxAge);
		long maxAgeSecondsLong = timeUnit.toSeconds(maxAge);
		this.cacheRenewalTime = maxAgeSecondsLong > Integer.MAX_VALUE
				? Integer.MAX_VALUE
				: (int) maxAgeSecondsLong;
	}

	/**
	 * It only works when Network connectivity is available. set cache expire date. <br>
	 *     How long cache is used instead of network response
	 * @param maxStale
	 * @param timeUnit
	 */
	public void setCacheExpireTime(int maxStale, TimeUnit timeUnit) {
		if (maxStale < 0) throw new IllegalArgumentException("maxStale < 0: " + maxStale);
		long maxStaleSecondsLong = timeUnit.toSeconds(maxStale);
		this.cacheExpireTime = maxStaleSecondsLong > Integer.MAX_VALUE
				? Integer.MAX_VALUE
				: (int) maxStaleSecondsLong;
	}

	public int getCacheRenewalTime() {
		return cacheRenewalTime;
	}

	public int getCacheExpireTime() {
		return cacheExpireTime;
	}
}
