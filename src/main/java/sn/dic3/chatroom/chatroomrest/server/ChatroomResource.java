package sn.dic3.chatroom.chatroomrest.server;


import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import sn.dic3.chatroom.chatroomrest.Message;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Path("/chatroom")
public class ChatroomResource {

    private final static List<Message> messages = new CopyOnWriteArrayList<>();
    private final static List<String> subscribedUsers = new ArrayList<>();
    private final static List<Instant> instants = new ArrayList<>();
    @GET
    @Path("/hello")
    public Response sayHello() {
        return Response.ok().entity("Hello, world!").build();
    }

    @GET
    @Path("/messages")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMessages() {
        return Response.ok(messages).build();
    }

    @GET
    @Path("/subscribe/{pseudo}")
    @Produces(MediaType.TEXT_PLAIN)
    public Response subscribe(@PathParam("pseudo") String username) {
        if (subscribedUsers.contains(username)) {
            return Response.status(Response.Status.CONFLICT).entity("Username already taken").build();
        }
        subscribedUsers.add(username);
        return Response.ok().entity("Connected").build();
    }

    @GET
    @Path("/unsubscribe/{pseudo}")
    @Produces(MediaType.TEXT_PLAIN)
    public Response unsubscribe(@PathParam("pseudo") String username) {
        if (!subscribedUsers.contains(username)) {
            return Response.status(Response.Status.NOT_FOUND).entity("User not found").build();
        }
        subscribedUsers.remove(username);
        return Response.ok().entity("Disconnected").build();
    }


    @POST
    @Path("/post-message")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response postMessage(Message message) {
        messages.add(message);
        instants.add(Instant.now());
        return Response.status(Response.Status.CREATED).build();
    }

    //get all from a certain time
    @GET
    @Path("/messages/{time}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMessages(@PathParam("time") long time) {
        System.out.println("time: " + time);
        List<Message> messagesToSend = new ArrayList<>();
        for (int i = 0; i < instants.size(); i++) {
            if (instants.get(i).toEpochMilli() > time) {
                messagesToSend.add(messages.get(i));
            }
        }

        System.out.println("messagesToSend: " + messagesToSend);
        return Response.ok(messagesToSend).build();
    }
}
