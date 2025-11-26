package com.revature.controller;

import io.javalin.http.Handler;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.revature.model.Ingredient;
import com.revature.model.Recipe;
import com.revature.service.AuthenticationService;
import com.revature.service.RecipeService;
import com.revature.util.Page;
import com.revature.model.Chef;

/**
 * The RecipeController class provides RESTful endpoints for managing recipes.
 * It interacts with the RecipeService to fetch, create, update, and delete recipes.
 * Handlers in this class are fields assigned to lambdas, which define the behavior for each endpoint.
 */

public class RecipeController 
{

    /** The service used to interact with the recipe data. */
    @SuppressWarnings("unused")
    private RecipeService recipeService;

    /** A service that handles authentication-related operations. */
    @SuppressWarnings("unused")
    private AuthenticationService authService;

    /**
     * TODO: Constructor that initializes the RecipeController with the parameters.
     * 
     * @param recipeService The service that handles the business logic for managing recipes.
     * * @param authService the service used to manage authentication-related operations
     */
    public RecipeController(RecipeService recipeService, AuthenticationService authService)
    {
        this.recipeService=recipeService;
        this.authService=authService;
            
    }

    /**
     * TODO: Handler for fetching all recipes. Supports pagination, sorting, and filtering by recipe name or ingredient.
     * 
     * Responds with a 200 OK status and the list of recipes, or 404 Not Found with a result of "No recipes found".
     */
   public Handler fetchAllRecipes = ctx ->
   {
        String termParam = ctx.queryParam("term");
        String name = ctx.queryParam("name");
        String ingredient = ctx.queryParam("ingredient");

        String term = null;
        if(termParam != null && !termParam.isBlank()) 
        {
            term = termParam;
        } 
        else if(name != null && !name.isBlank())
        {
            term = name;
        } 
        else if(ingredient != null && !ingredient.isBlank()) 
        {
            term = ingredient;
        }

        if(ctx.queryParam("page") != null && ctx.queryParam("pageSize") != null && ctx.queryParam("sortBy") != null && ctx.queryParam("sortDirection") != null) 
        {

            int page = getParamAsClassOrElse(ctx, "page", Integer.class, 1);
            int pageSize = getParamAsClassOrElse(ctx, "pageSize", Integer.class, 10);
            String sortBy = getParamAsClassOrElse(ctx, "sortBy", String.class, "id");
            String sortDirection = getParamAsClassOrElse(ctx, "sortDirection", String.class, "ASC");

            Page<Recipe> pageResult = recipeService.searchRecipes(term, page, pageSize, sortBy, sortDirection);

            if (pageResult == null)
            {
                ctx.status(404);
                ctx.result("No recipes found");
            } 
            else 
            {
                ctx.status(200);
                ctx.json(pageResult);
            }
        } 
        else 
        {
        
            List<Recipe> recipes = recipeService.searchRecipes(term);

            if(recipes == null || recipes.isEmpty()) 
            {
            ctx.status(404);
            ctx.result("No recipes found");
            } 
            else 
            {
            ctx.status(200);
            ctx.json(recipes);
            }
        }
    };




    /**
     * TODO: Handler for fetching a recipe by its ID.
     * 
     * If successful, responds with a 200 status code and the recipe as the response body.
     * 
     * If unsuccessful, responds with a 404 status code and a result of "Recipe not found".
     */
    public Handler fetchRecipeById = ctx -> 
    {

        int id = Integer.parseInt(ctx.pathParam("id"));

        Optional<Recipe> rep=recipeService.findRecipe(id);

        Recipe dbrep = rep.orElse(null);

        if(dbrep!=null)
        {
            ctx.status(200);
            ctx.json(dbrep);
        }
        else
        {
            ctx.status(404);
            ctx.result("Recipe not found");
        }
    };

    /**
     * TODO: Handler for creating a new recipe. Requires authentication via an authorization token taken from the request header.
     * 
     * If successful, responds with a 201 Created status.
     * If unauthorized, responds with a 401 Unauthorized status.
     */

    public Handler createRecipe = ctx -> 
    {
        String token = ctx.header("Authorization");

        
        if (token == null ) 
        {
            ctx.status(401);
            ctx.result("Unauthorized - Invalid token format");
            return;
        }
    
        
        token = token.substring(7);
        if (authService.getChefFromSessionToken(token) == null) 
        {
            ctx.status(401);
            ctx.result("Unauthorized");
            return;
        }

        Recipe recipe = ctx.bodyAsClass(Recipe.class);
        System.out.println(recipe +"in create handler");
        recipeService.saveRecipe(recipe);
        ctx.status(201);
        ctx.json(recipe);
    };

    /**
     * TODO: Handler for deleting a recipe by its id.
     * 
     * If successful, responds with a 200 status and result of "Recipe deleted successfully."
     * 
     * Otherwise, responds with a 404 status and a result of "Recipe not found."
     */
    public Handler deleteRecipe = ctx -> 
    {
 
        int id = Integer.parseInt(ctx.pathParam("id"));

    
        Optional<Recipe> existing = recipeService.findRecipe(id);

        if (existing.isEmpty()) 
        {
            ctx.status(404);
            ctx.result("Recipe not found");
        } 
        else 
        {
        
            recipeService.deleteRecipe(id);
            ctx.status(200);
            ctx.result("Recipe deleted successfully");
        }
    };



    /**
     * TODO: Handler for updating a recipe by its ID.
     * 
     * If successful, responds with a 200 status code and the updated recipe as the response body.
     * 
     * If unsuccessfuly, responds with a 404 status code and a result of "Recipe not found."
     */
    public Handler updateRecipe = ctx -> 
    {
        String json=ctx.body();

        ObjectMapper om=new ObjectMapper();

        try 
        {
            Recipe rep=om.readValue(json,Recipe.class);

            int id = Integer.parseInt(ctx.pathParam("id"));

            Optional<Recipe> existing = recipeService.findRecipe(id);
            
            if(existing.isEmpty()) 
            {
                
                ctx.status(404);
                ctx.result("Recipe not found.");
            }
            else
            {
                recipeService.saveRecipe(rep);
                ctx.status(200);
                ctx.json(rep);
            }


        } 
        catch (JsonMappingException e) 
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } 
        catch (JsonProcessingException e) 
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    };

    /**
     * A helper method to retrieve a query parameter from the context as a specific class type, or return a default value if the query parameter is not present.
     * 
     * @param <T> The type of the query parameter to be returned.
     * @param ctx The context of the request.
     * @param queryParam The query parameter name.
     * @param clazz The class type of the query parameter.
     * @param defaultValue The default value to return if the query parameter is not found.
     * @return The value of the query parameter converted to the specified class type, or the default value.
     */
    private <T> T getParamAsClassOrElse(Context ctx, String queryParam, Class<T> clazz, T defaultValue) {
        String paramValue = ctx.queryParam(queryParam);
        if (paramValue != null) {
            if (clazz == Integer.class) {
                return clazz.cast(Integer.valueOf(paramValue));
            } else if (clazz == Boolean.class) {
                return clazz.cast(Boolean.valueOf(paramValue));
            } else {
                return clazz.cast(paramValue);
            }
        }
        return defaultValue;
    }

    /**
     * Configure the routes for recipe operations.
     *
     * @param app the Javalin application
     */
    public void configureRoutes(Javalin app) {
        app.get("/recipes", fetchAllRecipes);
        app.get("/recipes/{id}", fetchRecipeById);
        app.post("/recipes", createRecipe);
        app.put("/recipes/{id}", updateRecipe);
        app.delete("/recipes/{id}", deleteRecipe);
    }
}


