/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nu.muntea.pospai.servlet;

import static java.util.Collections.singletonMap;

import java.io.IOException;
import java.io.InputStream;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.apache.jackrabbit.api.JackrabbitSession;
import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Download servlet
 * 
 * <p>This servlet binds all resources of the type <tt>pospai/home</tt>
 * and the zip extension. It generates a simplistic zip archive of all the child
 * pages, based on the <tt>jcr:title</tt> and <tt>jcr:content</tt> properties.</p>
 * 
 * <p>Assuming the sample content is installed, it will serve requests of the form 
 * <tt>GET /content/{contentFolderName}/home.zip</tt></p>
 * 
 * @see <a href="https://sling.apache.org/documentation/the-sling-engine/servlets.html">Servlets and Scripts</a>
 */
@Component(
    service = Servlet.class,
    property = {
        "sling.servlet.resourceTypes=pospai/avatar",
        "sling.servlet.extensions=jpg",
    }
)
@SuppressWarnings("serial")
public class AvatarServlet extends SlingSafeMethodsServlet {

    private static final String DEFAULT_AVATAR_PATH = "/content/pospai/avatar/default.jpg/jcr:content";
    
    private final Logger logger = LoggerFactory.getLogger(getClass());
    
    @Reference
    private ResourceResolverFactory resolverFactory;
    
    @Override
    protected void doGet(SlingHttpServletRequest request,
            SlingHttpServletResponse response) throws ServletException,
            IOException {
        
        try ( ResourceResolver resolver = resolverFactory.getServiceResourceResolver(singletonMap(ResourceResolverFactory.SUBSERVICE, "avatar"))) {
            JackrabbitSession session = (JackrabbitSession) resolver.adaptTo(Session.class);
            Node pictureNode = getUserProfilePicture(request, session);
            if ( pictureNode == null ) {
                if ( session.nodeExists(DEFAULT_AVATAR_PATH)) {
                    pictureNode = session.getNode(DEFAULT_AVATAR_PATH);
                } else {
                    logger.info("No user and no default avatar found, returning 404");
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
            }
            
            response.setContentType(pictureNode.getProperty("jcr:mimeType").getString());
            try ( InputStream in = pictureNode.getProperty("jcr:data").getBinary().getStream()) {
                in.transferTo(response.getOutputStream());
            }
            
        } catch (LoginException | RepositoryException e) {
            throw new RuntimeException(e);
        }
    }

    private Node getUserProfilePicture(SlingHttpServletRequest request, JackrabbitSession session) throws RepositoryException {
        String userId = request.getRequestPathInfo().getSuffix();

        if ( userId == null || userId.isBlank() || userId.equals("/") ) {
            return null;
        }
        
        if ( userId.charAt(0) == '/' )
            userId = userId.substring(1);
        
        Authorizable authorizable = session.getUserManager().getAuthorizable(userId);
        if ( authorizable == null )
            return null;
            
        if ( authorizable.hasProperty("profile/picture/jcr:content/jcr:data") ) {
            return session.getNode(authorizable.getPath()).getNode("profile/picture/jcr:content");
        }
        
        return null;
    }
}

