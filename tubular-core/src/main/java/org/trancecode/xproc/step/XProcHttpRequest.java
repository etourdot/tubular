/*
 * Copyright (C) 2011 Emmanuel Tourdot
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * $Id$
 */
package org.trancecode.xproc.step;

import java.util.List;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpRequestBase;

/**
 * User: Emmanuel Tourdot
 * Date: 20 feb. 2011
 * Time: 09:19:56
 */
public class XProcHttpRequest
{
    private List<Header> headers;
    private HttpHost httpHost;
    private HttpRequestBase httpRequest;
    private boolean detailled;
    private boolean statusOnly;
    private HttpEntity entity;
    private CredentialsProvider credentials;

    public List<Header> getHeaders()
    {
        return headers;
    }

    public void setHeaders(final List<Header> headers)
    {
        this.headers = headers;
    }

    public HttpHost getHttpHost()
    {
        return httpHost;
    }

    public void setHttpHost(final HttpHost httpHost)
    {
        this.httpHost = httpHost;
    }

    public HttpRequestBase getHttpRequest()
    {
        return httpRequest;
    }

    public void setHttpRequest(final HttpRequestBase httpRequest)
    {
        this.httpRequest = httpRequest;
    }

    public boolean isDetailled()
    {
        return detailled;
    }

    public void setDetailled(final boolean detailled)
    {
        this.detailled = detailled;
    }

    public boolean isStatusOnly()
    {
        return statusOnly;
    }

    public void setStatusOnly(final boolean statusOnly)
    {
        this.statusOnly = statusOnly;
    }

    public HttpEntity getEntity()
    {
        return entity;
    }

    public void setEntity(final HttpEntity entity)
    {
        this.entity = entity;
    }

    public boolean hasEntity()
    {
        return this.entity != null;
    }

    public CredentialsProvider getCredentials()
    {
        return credentials;
    }

    public void setCredentials(final CredentialsProvider credentials)
    {
        this.credentials = credentials;
    }
}
