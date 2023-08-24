package org.yazukov;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

@Path("/posts")
public class PostResource {

    @Inject
    PgPool pgPool;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Multi<Post> getAll() {
        return Post.findAll(pgPool);
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> getById(@PathParam("id") Long id) {
        return Post.findById(pgPool, id).onItem()
                .transform(el -> el != null ? Response.ok(el) : Response.status(Response.Status.NOT_FOUND))
                .onItem().transform(Response.ResponseBuilder::build);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Uni<Response> create(@Context UriInfo uriInfo, Post post) {
        return Post.save(pgPool, post).onItem()
                .transform(el -> el != null ? Response.ok(el) : Response.status(Response.Status.BAD_REQUEST))
                .onItem().transform(Response.ResponseBuilder::build);
    }

    @PUT
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Uni<Response> update(@PathParam("id") Long id, Post post) {
        return Post.update(pgPool, post, id).onItem()
                .transform(el -> el != null ? Response.ok(el) : Response.status(Response.Status.NOT_FOUND))
                .onItem().transform(Response.ResponseBuilder::build);
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> delete(@PathParam("id") Long id) {
        return Post.delete(pgPool, id).onItem()
                .transform(el -> el != null ? Response.status(Response.Status.NO_CONTENT) : Response.status(Response.Status.NOT_FOUND))
                .onItem().transform(Response.ResponseBuilder::build);
    }
}
