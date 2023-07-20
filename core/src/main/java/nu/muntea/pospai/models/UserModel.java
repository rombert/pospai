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
package nu.muntea.pospai.models;

import java.util.Collections;
import java.util.List;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.annotation.PostConstruct;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;

import org.apache.jackrabbit.api.JackrabbitSession;
import org.apache.jackrabbit.api.security.user.Group;
import org.apache.jackrabbit.api.security.user.User;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;

@Model(adaptables = {Resource.class, SlingHttpServletRequest.class})
public class UserModel {
    
    private static String getName(Group group) {
        try {
            return group.getID();
        } catch (RepositoryException e) {
            throw new RuntimeException(e);
        }
    }
     
    @SlingObject
    private Resource resource;
    
    private Resource currentPage;
    private String userId;
    private String path;
    private String fullName;
    private List<String> groups = Collections.emptyList();

    private String profilePicturePath;
    
    @PostConstruct
    public void loadUser() throws RepositoryException {
        
        currentPage = resource;
        Resource page = resource;
        while ( page != null && !"pospai/page".equals(page.getResourceType() ) ) {
            page = page.getParent();
        }
        
        if ( page != null )
            currentPage = page;
        
        JackrabbitSession session = (JackrabbitSession) resource.getResourceResolver().adaptTo(Session.class);
        
        if ( session.getUserID() == null || "anonymous".equals(session.getUserID()) ) {
            return;
        }
        this.userId = session.getUserID();
        User user = (User) session.getUserManager().getAuthorizable(session.getUserID());
        if ( user == null )
            return;
        
        this.path = user.getPath();
        this.groups = StreamSupport.stream(Spliterators.spliteratorUnknownSize(user.memberOf(), 0), false)
            .map( UserModel::getName )
            .collect(Collectors.toList());
        Value[] familyName = user.getProperty("profile/familyName");
        Value[] givenName = user.getProperty("profile/givenName");
        if ( familyName != null && givenName != null )
            this.fullName = String.format("%s %s", givenName[0].getString(), familyName[0].getString());

        Resource profilePicture = resource.getResourceResolver().getResource(user.getPath()+"/profile/picture");
        if ( profilePicture != null )
            this.profilePicturePath = profilePicture.getPath();
        
    }
    
    public String getUserId()  {
        return userId;
    }
    
    public String getPath() {
        return path;
    }
    
    public List<String> getGroups() {
        return groups;
    }
    
    public String getFullName() {
        return fullName;
    }
    
    public String getProfilePicturePath() {
        return profilePicturePath;
    }
    
    public Resource getCurrentPage() {
        return currentPage;
    }
}
