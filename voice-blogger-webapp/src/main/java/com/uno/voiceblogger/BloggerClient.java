package com.uno.voiceblogger;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.blogger.Blogger;
import com.google.api.services.blogger.model.Blog;
import com.google.api.services.blogger.model.BlogList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BloggerClient {
    public static Blogger getBlogger(Credential credential) {
        return new Blogger.Builder(new NetHttpTransport(), new JacksonFactory(), credential)
                .setApplicationName("VoiceBlogger").build();
    }


    public static List<String> listBlogs(Credential credential) {
        try {

            Blogger blogger = getBlogger(credential);
            Blogger.Blogs.ListByUser blogListByUserAction = null;
            blogListByUserAction = blogger.blogs().listByUser("self");

            blogListByUserAction.setFields("items(description,name,posts/totalItems,updated, id)");

            BlogList blogList = blogListByUserAction.execute();

            List<String> result = new ArrayList<String>();

            if (blogList.getItems() != null && !blogList.getItems().isEmpty()) {
                int blogCount = 0;
                for (Blog blog : blogList.getItems()) {
                    result.add(blog.getName() + " #" + blog.getId());
                    System.out.println("Blog #" + ++blogCount);
                    System.out.println("\tName: " + blog.getName());
                    System.out.println("\tDescription: " + blog.getDescription());
                    System.out.println("\tPost Count: " + blog.getPosts().getTotalItems());
                    System.out.println("\tLast Updated: " + blog.getUpdated());
                }
            }
            return result;
        } catch (IOException e) {
            return null;
        }

    }
}
